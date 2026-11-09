package br.edu.sor.reservas;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.sor.auditoria.AuditoriaService;
import br.edu.sor.catalogo.Recurso;
import br.edu.sor.catalogo.RecursoRepository;
import br.edu.sor.compartilhado.erro.AcessoNegadoException;
import br.edu.sor.compartilhado.erro.EntradaInvalidaException;
import br.edu.sor.compartilhado.erro.NaoEncontradoException;
import br.edu.sor.compartilhado.erro.RegraNegocioException;
import br.edu.sor.compartilhado.erro.TransicaoInvalidaException;
import br.edu.sor.auditoria.EventoAuditoria;
import br.edu.sor.disponibilidade.DisponibilidadeService;
import br.edu.sor.disponibilidade.Ocupacao;
import br.edu.sor.disponibilidade.OcupacaoRepository;
import br.edu.sor.disponibilidade.PeriodoReserva;
import br.edu.sor.disponibilidade.Periodos;
import br.edu.sor.identidade.Perfil;
import br.edu.sor.identidade.UsuarioAutenticado;
import br.edu.sor.identidade.UsuarioRepository;

/**
 * Operações transacionais de reserva. Cada método público é um limite atômico (ADR-004):
 * travar recursos → verificar → gravar reserva, ocupações e auditoria → publicar evento.
 */
@Service
class ReservaTransacoes {

    static final String ENTIDADE = "RESERVA";
    static final int LIMITE_CONSULTA = 500;

    private final ReservaRepository reservas;
    private final OcupacaoRepository ocupacoes;
    private final RecursoRepository recursos;
    private final UsuarioRepository usuarios;
    private final DisponibilidadeService disponibilidade;
    private final AuditoriaService auditoria;
    private final ApplicationEventPublisher eventos;
    private final List<PendenciasDeDevolucao> pendencias;
    private final Clock relogio;

    ReservaTransacoes(ReservaRepository reservas, OcupacaoRepository ocupacoes, RecursoRepository recursos,
            UsuarioRepository usuarios, DisponibilidadeService disponibilidade, AuditoriaService auditoria,
            ApplicationEventPublisher eventos, List<PendenciasDeDevolucao> pendencias, Clock relogio) {
        this.reservas = reservas;
        this.ocupacoes = ocupacoes;
        this.recursos = recursos;
        this.usuarios = usuarios;
        this.disponibilidade = disponibilidade;
        this.auditoria = auditoria;
        this.eventos = eventos;
        this.pendencias = pendencias;
        this.relogio = relogio;
    }

    LocalDateTime agora() {
        return LocalDateTime.now(relogio);
    }

    @Transactional
    ReservaVisao criar(NovaReserva pedido, UsuarioAutenticado ator) {
        PeriodoReserva periodo = Periodos.futuro(pedido.inicio(), pedido.fim(), agora());
        List<Recurso> travados = travar(pedido.recursoIds());
        boolean exigeAprovacao = travados.stream().anyMatch(Recurso::isRestrito);
        exigirJustificativaSeRestrito(exigeAprovacao, pedido.justificativa());
        disponibilidade.garantirLivre(travados, periodo, null, false);

        EstadoReserva estado = exigeAprovacao ? EstadoReserva.SOLICITADA : EstadoReserva.APROVADA;
        Reserva reserva = reservas.save(new Reserva(usuarios.getReferenceById(ator.id()), periodo, estado,
                pedido.finalidade().trim(), aparar(pedido.justificativa()), agora()));
        travados.forEach(r -> ocupacoes.save(Ocupacao.deReserva(r, reserva.getId(), periodo, estado.ocupaHorario())));

        auditoria.registrar(ator.username(), "RESERVA_CRIADA", ENTIDADE, reserva.getId(), null, estado,
                "recursos=" + codigos(travados) + (exigeAprovacao ? "; aguarda aprovação"
                        : "; aprovação automática (nenhum recurso restrito)"));
        eventos.publishEvent(new ReservaEvento(reserva.getId(), ReservaEvento.Tipo.CRIADA, ator.id(), estado, null));
        return ReservaVisao.de(reserva, travados);
    }

    // ---------------------------------------------------------------- transições (story 1.6)

    /** RF-016: somente Responsável (garantido em SecurityConfig); revalida que os recursos seguem ativos. */
    @Transactional
    ReservaVisao aprovar(Long id, String motivo, UsuarioAutenticado ator) {
        return transicionar(id, ator, EstadoReserva.APROVADA, "RESERVA_APROVADA", ReservaEvento.Tipo.APROVADA, motivo,
                reserva -> {
                    exigirEstado(reserva, EstadoReserva.SOLICITADA, "Só solicitações pendentes podem ser aprovadas.");
                    if (recursosDa(reserva.getId()).stream().anyMatch(r -> !r.isAtivo())) {
                        throw new RegraNegocioException("RECURSO_INDISPONIVEL",
                                "Um dos recursos foi desativado depois da solicitação. Rejeite-a com o motivo.");
                    }
                    reserva.registrarDecisao(usuarios.getReferenceById(ator.id()), aparar(motivo));
                });
    }

    /** RF-017, RN-014: rejeição exige motivo e libera o horário. */
    @Transactional
    ReservaVisao rejeitar(Long id, String motivo, UsuarioAutenticado ator) {
        if (motivo == null || motivo.isBlank()) {
            throw new EntradaInvalidaException("Informe o motivo da rejeição.");
        }
        return transicionar(id, ator, EstadoReserva.REJEITADA, "RESERVA_REJEITADA", ReservaEvento.Tipo.REJEITADA,
                motivo, reserva -> {
                    exigirEstado(reserva, EstadoReserva.SOLICITADA, "Só solicitações pendentes podem ser rejeitadas.");
                    reserva.registrarDecisao(usuarios.getReferenceById(ator.id()), motivo.trim());
                });
    }

    /** RF-014, RN-029: dono (ou Administrador) cancela antes do início; reserva iniciada nunca é cancelada/apagada. */
    @Transactional
    ReservaVisao cancelar(Long id, String motivo, UsuarioAutenticado ator) {
        return transicionar(id, ator, EstadoReserva.CANCELADA, "RESERVA_CANCELADA", ReservaEvento.Tipo.CANCELADA, motivo,
                reserva -> {
                    if (!ator.tem(Perfil.ADMINISTRADOR) && !reserva.pertenceA(ator.id())) {
                        throw new AcessoNegadoException("Você só pode cancelar as suas próprias reservas.");
                    }
                    exigirNaoIniciada(reserva, "Reservas já iniciadas ou encerradas não podem ser canceladas nem apagadas.");
                });
    }

    @Transactional
    ReservaVisao iniciarUso(Long id, UsuarioAutenticado ator) {
        return transicionar(id, ator, EstadoReserva.EM_USO, "RESERVA_EM_USO", null, null,
                reserva -> exigirEstado(reserva, EstadoReserva.APROVADA, "Só reservas aprovadas podem entrar em uso."));
    }

    /** RN-030: não conclui com material retirado e não devolvido. */
    @Transactional
    ReservaVisao concluir(Long id, UsuarioAutenticado ator) {
        return transicionar(id, ator, EstadoReserva.CONCLUIDA, "RESERVA_CONCLUIDA", null, null, reserva -> {
            exigirEstado(reserva, EstadoReserva.EM_USO, "Só reservas em uso podem ser concluídas.");
            if (pendencias.stream().anyMatch(p -> p.possuiRetiradaAberta(reserva.getId()))) {
                throw new RegraNegocioException("MATERIAL_PENDENTE",
                        "Há material ou equipamento retirado e ainda não devolvido nesta reserva.");
            }
        });
    }

    /** RN-025: não comparecimento só depois do horário de início. */
    @Transactional
    ReservaVisao registrarNaoComparecimento(Long id, UsuarioAutenticado ator) {
        return transicionar(id, ator, EstadoReserva.NAO_COMPARECEU, "RESERVA_NAO_COMPARECEU",
                ReservaEvento.Tipo.NAO_COMPARECEU, null, reserva -> {
                    exigirEstado(reserva, EstadoReserva.APROVADA, "Só reservas aprovadas podem ser marcadas como não comparecimento.");
                    if (!reserva.iniciada(agora())) {
                        throw new TransicaoInvalidaException("Só é possível registrar não comparecimento após o horário de início.");
                    }
                });
    }

    /** RF-013, RN-029: dono altera período/dados antes do início; revalida conflitos no mesmo ponto atômico. */
    @Transactional
    ReservaVisao alterar(Long id, AlteracaoReserva dados, UsuarioAutenticado ator) {
        Reserva reserva = reservas.travar(id).orElseThrow(() -> new NaoEncontradoException("Reserva não encontrada."));
        if (!reserva.pertenceA(ator.id())) {
            throw new AcessoNegadoException("Você só pode alterar as suas próprias reservas.");
        }
        exigirNaoIniciada(reserva, "Só é possível alterar reservas solicitadas ou aprovadas que ainda não começaram.");
        PeriodoReserva novo = Periodos.futuro(dados.inicio(), dados.fim(), agora());
        List<Ocupacao> ocupacoesDaReserva = ocupacoes.daReserva(id);
        List<Recurso> travados = travar(ocupacoesDaReserva.stream().map(o -> o.getRecurso().getId()).toList());
        boolean exigeAprovacao = travados.stream().anyMatch(Recurso::isRestrito);
        exigirJustificativaSeRestrito(exigeAprovacao, dados.justificativa());
        disponibilidade.garantirLivre(travados, novo, id, false);

        EstadoReserva anterior = reserva.getEstado();
        EstadoReserva novoEstado = exigeAprovacao ? EstadoReserva.SOLICITADA : anterior;
        String detalhe = "período: " + reserva.getInicio() + "–" + reserva.getFim() + " → " + novo.inicio() + "–"
                + novo.termino();
        reserva.reagendar(novo, dados.finalidade().trim(), aparar(dados.justificativa()), novoEstado, agora());
        ocupacoesDaReserva.forEach(o -> o.reagendar(novo));
        ocupacoes.flush();

        auditoria.registrar(ator.username(), "RESERVA_ALTERADA", ENTIDADE, id, anterior, novoEstado, detalhe);
        eventos.publishEvent(new ReservaEvento(id, ReservaEvento.Tipo.ALTERADA, reserva.getSolicitante().getId(),
                novoEstado, detalhe));
        return ReservaVisao.de(reserva, travados);
    }

    @Transactional(readOnly = true)
    List<ReservaVisao> listar(FiltroReservas filtro) {
        Specification<Reserva> especificacao = (raiz, consulta, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (filtro.estado() != null) {
                condicoes.add(cb.equal(raiz.get("estado"), filtro.estado()));
            }
            if (filtro.de() != null) {
                condicoes.add(cb.greaterThan(raiz.get("fim"), filtro.de()));
            }
            if (filtro.ate() != null) {
                condicoes.add(cb.lessThan(raiz.get("inicio"), filtro.ate()));
            }
            if (filtro.recursoId() != null) {
                Subquery<Long> sub = consulta.subquery(Long.class);
                var ocupacao = sub.from(Ocupacao.class);
                sub.select(ocupacao.get("reservaId")).where(cb.equal(ocupacao.get("recurso").get("id"), filtro.recursoId()));
                condicoes.add(raiz.get("id").in(sub));
            }
            return cb.and(condicoes.toArray(Predicate[]::new));
        };
        return visoes(reservas.findAll(especificacao,
                PageRequest.of(0, LIMITE_CONSULTA, Sort.by("inicio").ascending())).getContent());
    }

    @Transactional(readOnly = true)
    List<EventoAuditoria> historico(Long id, UsuarioAutenticado ator) {
        buscarVisivel(id, ator);
        return auditoria.historico(ENTIDADE, id);
    }

    /**
     * Fluxo único de transição (refatoração da story 1.6): trava a reserva, valida, muda o estado,
     * libera o horário se o novo estado não ocupa, audita e publica o evento — tudo na mesma transação.
     */
    private ReservaVisao transicionar(Long id, UsuarioAutenticado ator, EstadoReserva destino, String acao,
            ReservaEvento.Tipo evento, String detalhe, Consumer<Reserva> validacoes) {
        Reserva reserva = reservas.travar(id).orElseThrow(() -> new NaoEncontradoException("Reserva não encontrada."));
        EstadoReserva anterior = reserva.getEstado();
        validacoes.accept(reserva);
        reserva.mudarPara(destino, agora());
        if (!destino.ocupaHorario()) {
            ocupacoes.daReserva(id).forEach(Ocupacao::liberar);
        }
        auditoria.registrar(ator.username(), acao, ENTIDADE, id, anterior, destino, aparar(detalhe));
        if (evento != null) {
            eventos.publishEvent(new ReservaEvento(id, evento, reserva.getSolicitante().getId(), destino, aparar(detalhe)));
        }
        return ReservaVisao.de(reserva, recursosDa(id));
    }

    private static void exigirEstado(Reserva reserva, EstadoReserva esperado, String mensagem) {
        if (reserva.getEstado() != esperado) {
            throw new TransicaoInvalidaException(mensagem + " Estado atual: " + reserva.getEstado() + ".");
        }
    }

    private void exigirNaoIniciada(Reserva reserva, String mensagem) {
        if (!reserva.getEstado().editavel() || reserva.iniciada(agora())) {
            throw new TransicaoInvalidaException(mensagem);
        }
    }

    // ---------------------------------------------------------------- movimentação (story 1.7)

    @Transactional
    ReservaService.RetiradaAutorizada autorizarRetirada(Long reservaId, Long recursoId, UsuarioAutenticado ator) {
        Reserva reserva = reservas.travar(reservaId)
                .orElseThrow(() -> new NaoEncontradoException("Reserva não encontrada."));
        EstadoReserva estado = reserva.getEstado();
        if (estado != EstadoReserva.APROVADA && estado != EstadoReserva.EM_USO) {
            throw new TransicaoInvalidaException(
                    "Retirada só é permitida para reservas aprovadas ou em uso. Estado atual: " + estado + ".");
        }
        Recurso recurso = recursosDa(reservaId).stream().filter(r -> r.getId().equals(recursoId)).findFirst()
                .orElseThrow(() -> new EntradaInvalidaException("O recurso informado não faz parte da reserva."));
        if (!recurso.getTipo().movimentavel()) {
            throw new EntradaInvalidaException("Só materiais e equipamentos são retirados e devolvidos.");
        }
        if (estado == EstadoReserva.APROVADA) {
            reserva.mudarPara(EstadoReserva.EM_USO, agora());
            auditoria.registrar(ator.username(), "RESERVA_EM_USO", ENTIDADE, reservaId, estado, EstadoReserva.EM_USO,
                    "retirada de " + recurso.getCodigo());
        }
        return new ReservaService.RetiradaAutorizada(recurso, reserva.getFim());
    }

    @Transactional
    void concluirAposDevolucao(Long reservaId, UsuarioAutenticado ator) {
        Reserva reserva = reservas.travar(reservaId)
                .orElseThrow(() -> new NaoEncontradoException("Reserva não encontrada."));
        boolean somenteMateriais = recursosDa(reservaId).stream().allMatch(r -> r.getTipo().movimentavel());
        boolean nadaPendente = pendencias.stream().noneMatch(p -> p.possuiRetiradaAberta(reservaId));
        if (reserva.getEstado() == EstadoReserva.EM_USO && somenteMateriais && nadaPendente) {
            reserva.mudarPara(EstadoReserva.CONCLUIDA, agora());
            ocupacoes.daReserva(reservaId).forEach(Ocupacao::liberar);
            auditoria.registrar(ator.username(), "RESERVA_CONCLUIDA", ENTIDADE, reservaId, EstadoReserva.EM_USO,
                    EstadoReserva.CONCLUIDA, "devolução do último material");
        }
    }

    // ---------------------------------------------------------------- consultas e apoio

    @Transactional(readOnly = true)
    ReservaVisao detalhar(Long id, UsuarioAutenticado ator) {
        Reserva reserva = buscarVisivel(id, ator);
        return ReservaVisao.de(reserva, recursosDa(reserva.getId()));
    }

    @Transactional(readOnly = true)
    List<ReservaVisao> minhas(UsuarioAutenticado ator, EstadoReserva estado) {
        return visoes(reservas.doSolicitante(ator.id(), estado));
    }

    Reserva buscarVisivel(Long id, UsuarioAutenticado ator) {
        Reserva reserva = reservas.findById(id).orElseThrow(() -> new NaoEncontradoException("Reserva não encontrada."));
        if (ator.tem(Perfil.SOLICITANTE) && !reserva.pertenceA(ator.id())) {
            throw new AcessoNegadoException("Você só pode consultar as suas próprias reservas.");
        }
        return reserva;
    }

    List<ReservaVisao> visoes(List<Reserva> lista) {
        if (lista.isEmpty()) {
            return List.of();
        }
        Map<Long, List<Recurso>> recursosPorReserva = ocupacoes.dasReservas(lista.stream().map(Reserva::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(Ocupacao::getReservaId,
                        Collectors.mapping(Ocupacao::getRecurso, Collectors.toList())));
        return lista.stream().map(r -> ReservaVisao.de(r, recursosPorReserva.getOrDefault(r.getId(), List.of()))).toList();
    }

    List<Recurso> recursosDa(Long reservaId) {
        return ocupacoes.daReserva(reservaId).stream().map(Ocupacao::getRecurso).toList();
    }

    /** Trava os recursos em ordem crescente de id (evita deadlock) e confirma que todos existem. */
    List<Recurso> travar(List<Long> ids) {
        Set<Long> ordenados = new TreeSet<>(ids);
        List<Recurso> travados = recursos.travarEmOrdem(ordenados);
        if (travados.size() != ordenados.size()) {
            throw new NaoEncontradoException("Um ou mais recursos informados não existem.");
        }
        return travados;
    }

    static void exigirJustificativaSeRestrito(boolean restrito, String justificativa) {
        if (restrito && (justificativa == null || justificativa.isBlank())) {
            throw new EntradaInvalidaException("Justificativa é obrigatória para reservar recursos restritos.");
        }
    }

    static String codigos(List<Recurso> lista) {
        return lista.stream().map(Recurso::getCodigo).collect(Collectors.joining(","));
    }

    static String aparar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}

package br.edu.sor.reservas;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.sor.auditoria.AuditoriaService;
import br.edu.sor.catalogo.Recurso;
import br.edu.sor.catalogo.RecursoRepository;
import br.edu.sor.compartilhado.erro.AcessoNegadoException;
import br.edu.sor.compartilhado.erro.EntradaInvalidaException;
import br.edu.sor.compartilhado.erro.NaoEncontradoException;
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

    private final ReservaRepository reservas;
    private final OcupacaoRepository ocupacoes;
    private final RecursoRepository recursos;
    private final UsuarioRepository usuarios;
    private final DisponibilidadeService disponibilidade;
    private final AuditoriaService auditoria;
    private final ApplicationEventPublisher eventos;
    private final Clock relogio;

    ReservaTransacoes(ReservaRepository reservas, OcupacaoRepository ocupacoes, RecursoRepository recursos,
            UsuarioRepository usuarios, DisponibilidadeService disponibilidade, AuditoriaService auditoria,
            ApplicationEventPublisher eventos, Clock relogio) {
        this.reservas = reservas;
        this.ocupacoes = ocupacoes;
        this.recursos = recursos;
        this.usuarios = usuarios;
        this.disponibilidade = disponibilidade;
        this.auditoria = auditoria;
        this.eventos = eventos;
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

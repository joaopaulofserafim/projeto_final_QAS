package br.edu.sor.disponibilidade;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.edu.sor.catalogo.OcupacaoDeRecursos;
import br.edu.sor.catalogo.Recurso;
import br.edu.sor.catalogo.RecursoRepository;
import br.edu.sor.catalogo.TipoRecurso;
import br.edu.sor.compartilhado.erro.ConflitoDetalhe;
import br.edu.sor.compartilhado.erro.ConflitoHorarioException;
import br.edu.sor.compartilhado.erro.EntradaInvalidaException;
import br.edu.sor.compartilhado.erro.NaoEncontradoException;

/**
 * Disponibilidade e integridade (componente C4 nível 3). Consulta não reserva nada (RN-006);
 * a garantia real acontece em {@link #garantirLivre}, chamada com os recursos já travados.
 */
@Service
public class DisponibilidadeService implements OcupacaoDeRecursos {

    static final String RECURSO_INATIVO = "RECURSO_INATIVO";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final long NENHUMA_RESERVA = -1L;

    private final OcupacaoRepository ocupacoes;
    private final RecursoRepository recursos;

    public DisponibilidadeService(OcupacaoRepository ocupacoes, RecursoRepository recursos) {
        this.ocupacoes = ocupacoes;
        this.recursos = recursos;
    }

    public record SituacaoRecurso(Long recursoId, String codigo, String nome, TipoRecurso tipo, boolean disponivel,
            List<ConflitoDetalhe> motivos) {
    }

    @Override
    @Transactional(readOnly = true)
    public boolean possuiReservaAtivaApos(Long recursoId, LocalDateTime instante) {
        return ocupacoes.existeAtivaApos(recursoId, TipoOcupacao.RESERVA, instante);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> ocupadosNoPeriodo(Collection<Long> recursoIds, LocalDateTime inicio, LocalDateTime fim) {
        return ocupacoes.conflitantes(recursoIds, inicio, fim, NENHUMA_RESERVA).stream()
                .map(o -> o.getRecurso().getId())
                .collect(Collectors.toSet());
    }

    /** Consulta de disponibilidade (RF-007). Resultado informativo: não garante o aceite posterior. */
    @Transactional(readOnly = true)
    public List<SituacaoRecurso> consultar(Collection<Long> recursoIds, LocalDateTime inicio, LocalDateTime fim) {
        PeriodoReserva periodo = Periodos.de(inicio, fim);
        if (recursoIds == null || recursoIds.isEmpty()) {
            throw new EntradaInvalidaException("Informe ao menos um recurso.");
        }
        Set<Long> ids = new TreeSet<>(recursoIds);
        Map<Long, Recurso> encontrados = recursos.findAllById(ids).stream()
                .collect(Collectors.toMap(Recurso::getId, Function.identity()));
        if (encontrados.size() != ids.size()) {
            throw new NaoEncontradoException("Um ou mais recursos informados não existem.");
        }
        List<ConflitoDetalhe> conflitos = detalhar(encontrados.values(), periodo, NENHUMA_RESERVA, false);
        return ids.stream().map(encontrados::get).map(r -> {
            List<ConflitoDetalhe> motivos = conflitos.stream().filter(c -> c.recursoId().equals(r.getId())).toList();
            return new SituacaoRecurso(r.getId(), r.getCodigo(), r.getNome(), r.getTipo(), motivos.isEmpty(), motivos);
        }).toList();
    }

    /**
     * Ponto de verificação dentro do limite atômico (ADR-004): deve ser chamado na mesma transação
     * que travou os recursos e que vai gravar a ocupação.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void garantirLivre(Collection<Recurso> recursosTravados, PeriodoReserva periodo, Long reservaIgnorada,
            boolean exporReservas) {
        List<ConflitoDetalhe> conflitos = detalhar(recursosTravados, periodo,
                reservaIgnorada == null ? NENHUMA_RESERVA : reservaIgnorada, exporReservas);
        if (!conflitos.isEmpty()) {
            throw new ConflitoHorarioException(mensagem(conflitos), conflitos);
        }
    }

    @Transactional(readOnly = true)
    public List<Ocupacao> agenda(Long recursoId, LocalDateTime inicio, LocalDateTime fim) {
        PeriodoReserva periodo = Periodos.de(inicio, fim);
        if (!recursos.existsById(recursoId)) {
            throw new NaoEncontradoException("Recurso não encontrado.");
        }
        return ocupacoes.agenda(recursoId, periodo.inicio(), periodo.termino());
    }

    private List<ConflitoDetalhe> detalhar(Collection<Recurso> alvo, PeriodoReserva periodo, Long reservaIgnorada,
            boolean exporReservas) {
        List<ConflitoDetalhe> conflitos = new ArrayList<>();
        alvo.stream().filter(r -> !r.isAtivo())
                .forEach(r -> conflitos.add(new ConflitoDetalhe(r.getId(), r.getCodigo(), RECURSO_INATIVO, null, null, null)));
        List<Long> ids = alvo.stream().map(Recurso::getId).toList();
        for (Ocupacao o : ocupacoes.conflitantes(ids, periodo.inicio(), periodo.termino(), reservaIgnorada)) {
            conflitos.add(new ConflitoDetalhe(o.getRecurso().getId(), o.getRecurso().getCodigo(), o.getTipo().name(),
                    o.getInicio(), o.getFim(), exporReservas ? o.getReservaId() : null));
        }
        return conflitos;
    }

    static String mensagem(List<ConflitoDetalhe> conflitos) {
        return "Horário indisponível: " + conflitos.stream().map(c -> switch (c.motivo()) {
            case RECURSO_INATIVO -> c.recursoCodigo() + " está desativado";
            case "MANUTENCAO" -> c.recursoCodigo() + " estará em manutenção de " + FORMATO.format(c.inicio())
                    + " a " + FORMATO.format(c.fim());
            default -> c.recursoCodigo() + " já está reservado de " + FORMATO.format(c.inicio())
                    + " a " + FORMATO.format(c.fim());
        }).collect(Collectors.joining("; ")) + ".";
    }
}

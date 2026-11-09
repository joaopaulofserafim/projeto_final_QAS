package br.edu.sor.relatorios;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.sor.catalogo.Recurso;
import br.edu.sor.catalogo.TipoRecurso;
import br.edu.sor.disponibilidade.ConflitoEvitado;
import br.edu.sor.disponibilidade.PeriodoReserva;
import br.edu.sor.disponibilidade.Periodos;
import br.edu.sor.disponibilidade.TipoOcupacao;
import br.edu.sor.reservas.EstadoReserva;

/**
 * Relatórios com critério explícito e fonte verificável (RN-024, DP-010). Só leitura.
 * Reservas consideradas: APROVADA, EM_USO e CONCLUIDA, recortadas pela janela pedida.
 */
@Service
public class RelatorioService {

    static final List<EstadoReserva> CONFIRMADAS = List.of(EstadoReserva.APROVADA, EstadoReserva.EM_USO,
            EstadoReserva.CONCLUIDA);
    static final String CRITERIO_HORAS = "Soma das horas de reservas nos estados APROVADA, EM_USO e CONCLUIDA, "
            + "contando só a parte dentro da janela [início, fim). Solicitações pendentes, rejeitadas, canceladas e "
            + "não comparecimentos não contam. Fonte: tabelas reserva e ocupacao.";
    static final String CRITERIO_CONFLITOS = "Tentativas de reserva ou alteração recusadas por conflito de horário, "
            + "contadas pela data da tentativa dentro da janela [início, fim). Fonte: tabela conflito_evitado.";

    private final EntityManager em;
    private final Clock relogio;

    public RelatorioService(EntityManager em, Clock relogio) {
        this.em = em;
        this.relogio = relogio;
    }

    public record LinhaHoras(Long recursoId, String codigo, String nome, TipoRecurso tipo, int reservas, double horas,
            double percentualOcupacao) {
    }

    public record RelatorioHoras(LocalDateTime inicio, LocalDateTime fim, String unidade, String criterio,
            List<LinhaHoras> itens) {
    }

    public record LinhaConflito(Long recursoId, String codigo, long quantidade) {
    }

    public record RelatorioConflitos(LocalDateTime inicio, LocalDateTime fim, String criterio, long total,
            Map<String, Long> porMotivo, List<LinhaConflito> porRecurso) {
    }

    public record Resumo(LocalDateTime geradoEm, Map<EstadoReserva, Long> reservasPorEstado, long solicitacoesPendentes,
            long movimentacoesAbertas, long movimentacoesAtrasadas, long manutencoesAtivas, long conflitosEvitados) {
    }

    /** RF-034: utilização por recurso. */
    @Transactional(readOnly = true)
    public RelatorioHoras utilizacao(LocalDateTime inicio, LocalDateTime fim, TipoRecurso tipo) {
        PeriodoReserva janela = Periodos.de(inicio, fim);
        return horasPorRecurso(janela, tipo);
    }

    /** RF-035: carga horária alocada por professor. */
    @Transactional(readOnly = true)
    public RelatorioHoras cargaHoraria(LocalDateTime inicio, LocalDateTime fim) {
        return horasPorRecurso(Periodos.de(inicio, fim), TipoRecurso.PROFESSOR);
    }

    /** RF-036: conflitos evitados, por motivo e por recurso. */
    @Transactional(readOnly = true)
    public RelatorioConflitos conflitos(LocalDateTime inicio, LocalDateTime fim) {
        PeriodoReserva janela = Periodos.de(inicio, fim);
        List<ConflitoEvitado> registros = em.createQuery("""
                select c from ConflitoEvitado c where c.ocorridoEm >= :inicio and c.ocorridoEm < :fim
                """, ConflitoEvitado.class)
                .setParameter("inicio", janela.inicio()).setParameter("fim", janela.termino()).getResultList();
        Map<String, Long> porMotivo = new TreeMap<>();
        Map<Long, Long> porRecurso = new LinkedHashMap<>();
        for (ConflitoEvitado c : registros) {
            porMotivo.merge(c.getMotivo(), 1L, Long::sum);
            porRecurso.merge(c.getRecursoId(), 1L, Long::sum);
        }
        Map<Long, String> codigos = new LinkedHashMap<>();
        if (!porRecurso.isEmpty()) {
            em.createQuery("select r from Recurso r where r.id in :ids", Recurso.class)
                    .setParameter("ids", porRecurso.keySet()).getResultList()
                    .forEach(r -> codigos.put(r.getId(), r.getCodigo()));
        }
        List<LinhaConflito> linhas = porRecurso.entrySet().stream()
                .map(e -> new LinhaConflito(e.getKey(), codigos.get(e.getKey()), e.getValue()))
                .sorted((a, b) -> Long.compare(b.quantidade(), a.quantidade()))
                .toList();
        return new RelatorioConflitos(janela.inicio(), janela.termino(), CRITERIO_CONFLITOS, registros.size(), porMotivo,
                linhas);
    }

    /** RF-028: indicadores do painel administrativo. */
    @Transactional(readOnly = true)
    public Resumo resumo() {
        LocalDateTime agora = LocalDateTime.now(relogio);
        Map<EstadoReserva, Long> porEstado = new EnumMap<>(EstadoReserva.class);
        for (EstadoReserva e : EstadoReserva.values()) {
            porEstado.put(e, 0L);
        }
        em.createQuery("select r.estado, count(r) from Reserva r group by r.estado", Object[].class).getResultList()
                .forEach(linha -> porEstado.put((EstadoReserva) linha[0], (Long) linha[1]));
        long abertas = contar("select count(m) from Movimentacao m where m.devolvidaEm is null", null);
        long atrasadas = contar("select count(m) from Movimentacao m where m.devolvidaEm is null and m.prazoDevolucao < :agora",
                agora);
        long manutencoes = contar("select count(m) from Manutencao m where m.ativa = true and m.fim > :agora", agora);
        long conflitos = contar("select count(c) from ConflitoEvitado c", null);
        return new Resumo(agora, porEstado, porEstado.get(EstadoReserva.SOLICITADA), abertas, atrasadas, manutencoes,
                conflitos);
    }

    private RelatorioHoras horasPorRecurso(PeriodoReserva janela, TipoRecurso tipo) {
        List<Recurso> recursos = em.createQuery("""
                select r from Recurso r where r.ativo = true and (:tipo is null or r.tipo = :tipo) order by r.tipo, r.codigo
                """, Recurso.class).setParameter("tipo", tipo).getResultList();
        List<Object[]> ocupacoes = em.createQuery("""
                select o.recurso.id, o.inicio, o.fim from Ocupacao o, Reserva r
                where o.reservaId = r.id and o.tipo = :tipoOcupacao and r.estado in :estados
                  and o.inicio < :fim and o.fim > :inicio
                """, Object[].class)
                .setParameter("tipoOcupacao", TipoOcupacao.RESERVA)
                .setParameter("estados", CONFIRMADAS)
                .setParameter("inicio", janela.inicio())
                .setParameter("fim", janela.termino())
                .getResultList();
        Map<Long, double[]> totais = new LinkedHashMap<>();
        for (Object[] o : ocupacoes) {
            double horas = CalculadoraHoras.horasDentro((LocalDateTime) o[1], (LocalDateTime) o[2], janela.inicio(),
                    janela.termino());
            double[] acumulado = totais.computeIfAbsent((Long) o[0], id -> new double[2]);
            acumulado[0] += 1;
            acumulado[1] += horas;
        }
        double horasJanela = CalculadoraHoras.horasDaJanela(janela.inicio(), janela.termino());
        List<LinhaHoras> linhas = recursos.stream().map(r -> {
            double[] t = totais.getOrDefault(r.getId(), new double[2]);
            double horas = CalculadoraHoras.arredondar(t[1], 2);
            return new LinhaHoras(r.getId(), r.getCodigo(), r.getNome(), r.getTipo(), (int) t[0], horas,
                    CalculadoraHoras.percentual(horas, horasJanela));
        }).toList();
        return new RelatorioHoras(janela.inicio(), janela.termino(), "horas", CRITERIO_HORAS, linhas);
    }

    private long contar(String jpql, LocalDateTime agora) {
        var consulta = em.createQuery(jpql, Long.class);
        if (agora != null) {
            consulta.setParameter("agora", agora);
        }
        return consulta.getSingleResult();
    }
}

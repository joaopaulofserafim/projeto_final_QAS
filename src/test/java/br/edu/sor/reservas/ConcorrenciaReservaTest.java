package br.edu.sor.reservas;

import static br.edu.sor.suporte.Api.pedidoReserva;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.edu.sor.suporte.IntegracaoBase;

/**
 * Story 1.5 — RF-008, RN-005, RNF-004 (limitador de nota: possibilidade de dupla reserva).
 * Requisições HTTP reais e simultâneas contra a aplicação e um PostgreSQL real; nenhum mock.
 */
class ConcorrenciaReservaTest extends IntegracaoBase {

    private static final int REQUISICOES = 20;

    private ExecutorService executor;

    @BeforeEach
    void iniciarExecutor() {
        executor = Executors.newFixedThreadPool(REQUISICOES);
    }

    @AfterEach
    void encerrarExecutor() {
        executor.shutdownNow();
    }

    /** Dispara todas as tarefas ao mesmo tempo (largada única) e devolve os códigos HTTP. */
    private List<Integer> dispararJuntos(List<Callable<Integer>> tarefas) throws Exception {
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Integer>> futuros = new ArrayList<>();
        for (Callable<Integer> tarefa : tarefas) {
            futuros.add(executor.submit(() -> {
                largada.await();
                return tarefa.call();
            }));
        }
        largada.countDown();
        List<Integer> status = new ArrayList<>();
        for (Future<Integer> futuro : futuros) {
            status.add(futuro.get(60, TimeUnit.SECONDS));
        }
        return status;
    }

    private Callable<Integer> pedido(String login, Map<String, Object> corpo) {
        return () -> api.como(login).post("/api/v1/reservas", corpo).getStatusCode().value();
    }

    private static Map<Integer, Long> contagem(List<Integer> status) {
        return status.stream().collect(Collectors.groupingBy(s -> s, Collectors.counting()));
    }

    @Test
    void vinteSolicitacoesSimultaneasParaOMesmoHorarioProduzemUmaUnicaReserva() throws Exception {
        long sala = api.criarRecurso("SALA-DISPUTADA", "SALA", false);
        Map<String, Object> corpo = pedidoReserva("2026-11-25T14:00:00", "2026-11-25T16:00:00", sala);
        List<Callable<Integer>> tarefas = new ArrayList<>();
        for (int i = 0; i < REQUISICOES; i++) {
            tarefas.add(pedido(i % 2 == 0 ? "solicitante" : "solicitante2", corpo));
        }

        Map<Integer, Long> resultado = contagem(dispararJuntos(tarefas));

        assertThat(resultado).containsEntry(201, 1L).containsEntry(409, (long) REQUISICOES - 1).hasSize(2);
        assertThat(contar("select count(*) from ocupacao where recurso_id = ? and ativa", sala)).isEqualTo(1);
        assertThat(contar("select count(*) from conflito_evitado where recurso_id = ?", sala))
                .isEqualTo(REQUISICOES - 1);
    }

    @Test
    void pedidosSimultaneosComHorariosSobrepostosDiferentesNuncaGeramSobreposicaoNoBanco() throws Exception {
        long sala = api.criarRecurso("SALA-ESCALONADA", "SALA", false);
        List<Callable<Integer>> tarefas = new ArrayList<>();
        for (int i = 0; i < REQUISICOES; i++) {
            String inicio = String.format("2026-11-26T%02d:%02d:00", 8 + (i * 15) / 60, (i * 15) % 60);
            String fim = String.format("2026-11-26T%02d:%02d:00", 9 + (i * 15) / 60, (i * 15) % 60);
            tarefas.add(pedido("solicitante", pedidoReserva(inicio, fim, sala)));
        }

        List<Integer> status = dispararJuntos(tarefas);

        assertThat(status).containsOnly(201, 409).contains(201);
        assertThat(contar("""
                select count(*) from ocupacao a join ocupacao b
                  on a.recurso_id = b.recurso_id and a.id < b.id and a.periodo && b.periodo
                where a.ativa and b.ativa and a.recurso_id = ?
                """, sala)).as("pares de ocupações ativas sobrepostas").isZero();
    }

    @Test
    void mesmoProfessorDisputadoPorSalasDiferentesFicaComUmaUnicaAlocacao() throws Exception {
        long professor = api.criarRecurso("PROF-DISPUTADO", "PROFESSOR", false);
        List<Callable<Integer>> tarefas = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long sala = api.criarRecurso("SALA-P" + i, "SALA", false);
            tarefas.add(pedido("solicitante", pedidoReserva("2026-11-27T10:00:00", "2026-11-27T12:00:00", sala, professor)));
        }

        Map<Integer, Long> resultado = contagem(dispararJuntos(tarefas));

        assertThat(resultado).containsEntry(201, 1L).containsEntry(409, 9L);
        assertThat(contar("select count(*) from ocupacao where recurso_id = ? and ativa", professor)).isEqualTo(1);
        assertThat(contar("select count(*) from ocupacao where ativa")).as("sala da reserva perdedora não fica presa")
                .isEqualTo(2);
    }

    @Test
    void pedidosCruzadosDeVariosRecursosNaoCausamDeadlock() throws Exception {
        long a = api.criarRecurso("SALA-A", "SALA", false);
        long b = api.criarRecurso("PROJ-B", "EQUIPAMENTO", false);
        List<Callable<Integer>> tarefas = new ArrayList<>();
        for (int i = 0; i < REQUISICOES; i++) {
            long[] ordem = i % 2 == 0 ? new long[] {a, b} : new long[] {b, a};
            tarefas.add(pedido("solicitante", pedidoReserva("2026-11-28T10:00:00", "2026-11-28T11:00:00", ordem)));
        }

        Map<Integer, Long> resultado = contagem(dispararJuntos(tarefas));

        assertThat(resultado).as("nenhum erro 500 por deadlock").containsOnlyKeys(201, 409).containsEntry(201, 1L);
    }

    @Test
    void recursosIndependentesSaoReservadosEmParaleloSemConflitoFalso() throws Exception {
        List<Callable<Integer>> tarefas = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long sala = api.criarRecurso("SALA-IND" + i, "SALA", false);
            tarefas.add(pedido("solicitante2", pedidoReserva("2026-11-29T10:00:00", "2026-11-29T11:00:00", sala)));
        }

        assertThat(dispararJuntos(tarefas)).containsOnly(201);
    }
}

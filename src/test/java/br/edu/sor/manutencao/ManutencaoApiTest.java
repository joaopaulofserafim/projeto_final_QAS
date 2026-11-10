package br.edu.sor.manutencao;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.sor.suporte.IntegracaoBase;

/** Story 1.7 — RF-026, RN-007, RN-028 (DP-004): recurso em manutenção não pode ser reservado. */
class ManutencaoApiTest extends IntegracaoBase {

    private long sala;

    @BeforeEach
    void recurso() {
        sala = api.criarRecurso("SALA-MNT", "SALA", false);
    }

    private ResponseEntity<JsonNode> manutencao(String inicio, String fim) {
        return api.como("admin").post("/api/v1/manutencoes",
                Map.of("recursoId", sala, "inicio", inicio, "fim", fim, "motivo", "Troca do projetor"));
    }

    @Test
    void recursoEmManutencaoNaoPodeSerReservadoMasHorarioAdjacenteSim() {
        assertThat(manutencao("2026-11-20T08:00:00", "2026-11-20T18:00:00").getStatusCode().value()).isEqualTo(201);

        ResponseEntity<JsonNode> durante = api.como("solicitante").post("/api/v1/reservas",
                pedidoReserva("2026-11-20T10:00:00", "2026-11-20T12:00:00", sala));
        ResponseEntity<JsonNode> depois = api.como("solicitante").post("/api/v1/reservas",
                pedidoReserva("2026-11-20T18:00:00", "2026-11-20T19:00:00", sala));

        assertThat(durante.getStatusCode().value()).isEqualTo(409);
        assertThat(durante.getBody().get("conflitos").get(0).get("motivo").asText()).isEqualTo("MANUTENCAO");
        assertThat(durante.getBody().get("detail").asText()).contains("em manutenção");
        assertThat(depois.getStatusCode().value()).isEqualTo(201);
        assertThat(contar("select count(*) from evento_auditoria where acao = 'MANUTENCAO_REGISTRADA'")).isEqualTo(1);
    }

    @Test
    void manutencaoSobreReservaAtivaEhRecusadaListandoAsReservasParaOAdministradorResolver() {
        long reserva = api.como("solicitante").post("/api/v1/reservas",
                pedidoReserva("2026-11-20T10:00:00", "2026-11-20T12:00:00", sala)).getBody().get("id").asLong();

        ResponseEntity<JsonNode> recusada = manutencao("2026-11-20T08:00:00", "2026-11-20T18:00:00");
        api.como("admin").post("/api/v1/reservas/" + reserva + "/cancelar", Map.of("motivo", "Manutenção urgente"));
        ResponseEntity<JsonNode> aceita = manutencao("2026-11-20T08:00:00", "2026-11-20T18:00:00");

        assertThat(recusada.getStatusCode().value()).isEqualTo(409);
        assertThat(recusada.getBody().get("detail").asText()).contains("Manutenção recusada");
        assertThat(recusada.getBody().get("conflitos").get(0).get("reservaId").asLong()).isEqualTo(reserva);
        assertThat(aceita.getStatusCode().value()).isEqualTo(201);
    }

    @Test
    void cancelarManutencaoLiberaORecursoEApareceNaConsulta() {
        long id = manutencao("2026-11-20T08:00:00", "2026-11-20T18:00:00").getBody().get("id").asLong();
        JsonNode disponibilidade = api.como("solicitante").get("/api/v1/disponibilidade?recursoIds=" + sala
                + "&inicio=2026-11-20T09:00:00&fim=2026-11-20T10:00:00").getBody();

        ResponseEntity<JsonNode> cancelada = api.como("admin").post("/api/v1/manutencoes/" + id + "/cancelar", null);
        ResponseEntity<JsonNode> repetida = api.como("admin").post("/api/v1/manutencoes/" + id + "/cancelar", null);

        assertThat(disponibilidade.get(0).get("motivos").get(0).get("motivo").asText()).isEqualTo("MANUTENCAO");
        assertThat(cancelada.getBody().get("ativa").asBoolean()).isFalse();
        assertThat(repetida.getStatusCode().value()).isEqualTo(409);
        assertThat(api.como("solicitante").post("/api/v1/reservas",
                pedidoReserva("2026-11-20T10:00:00", "2026-11-20T12:00:00", sala)).getStatusCode().value()).isEqualTo(201);
    }

    @Test
    void responsavelConsultaManutencoesAtivasDoRecurso() {
        manutencao("2026-11-20T08:00:00", "2026-11-20T09:00:00");
        long cancelada = manutencao("2026-11-21T08:00:00", "2026-11-21T09:00:00").getBody().get("id").asLong();
        api.como("admin").post("/api/v1/manutencoes/" + cancelada + "/cancelar", null);

        assertThat(api.como("responsavel").get("/api/v1/manutencoes?recursoId=" + sala).getBody()).hasSize(2);
        assertThat(api.como("responsavel").get("/api/v1/manutencoes?recursoId=" + sala + "&somenteAtivas=true")
                .getBody()).hasSize(1);
    }

    @Test
    void validacoesDaManutencao() {
        assertThat(manutencao("2026-11-20T18:00:00", "2026-11-20T08:00:00").getStatusCode().value()).isEqualTo(400);
        assertThat(manutencao("2026-11-01T08:00:00", "2026-11-02T08:00:00").getBody().get("detail").asText())
                .contains("término da manutenção");
        assertThat(api.como("admin").post("/api/v1/manutencoes", Map.of("recursoId", 999, "inicio",
                "2026-11-20T08:00:00", "fim", "2026-11-20T09:00:00", "motivo", "x")).getStatusCode().value()).isEqualTo(404);
        assertThat(api.como("admin").post("/api/v1/manutencoes/999/cancelar", null).getStatusCode().value()).isEqualTo(404);
        assertThat(api.como("admin").post("/api/v1/manutencoes", Map.of("recursoId", sala)).getStatusCode().value())
                .isEqualTo(400);
    }

    @Test
    void manutencaoEReservasSimultaneasNuncaDeixamOcupacoesSobrepostas() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(11);
        CountDownLatch largada = new CountDownLatch(1);
        List<Callable<Integer>> tarefas = new ArrayList<>();
        tarefas.add(() -> manutencao("2026-11-22T08:00:00", "2026-11-22T18:00:00").getStatusCode().value());
        for (int i = 0; i < 10; i++) {
            tarefas.add(() -> api.como("solicitante").post("/api/v1/reservas",
                    pedidoReserva("2026-11-22T10:00:00", "2026-11-22T11:00:00", sala)).getStatusCode().value());
        }
        List<Future<Integer>> futuros = new ArrayList<>();
        for (Callable<Integer> t : tarefas) {
            futuros.add(executor.submit(() -> {
                largada.await();
                return t.call();
            }));
        }
        largada.countDown();
        List<Integer> status = new ArrayList<>();
        for (Future<Integer> f : futuros) {
            status.add(f.get(60, TimeUnit.SECONDS));
        }
        executor.shutdown();

        assertThat(status).containsOnly(201, 409).filteredOn(s -> s == 201).hasSize(1);
        assertThat(contar("select count(*) from ocupacao where recurso_id = ? and ativa", sala)).isEqualTo(1);
    }
}

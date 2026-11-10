package br.edu.sor.notificacao;

import static br.edu.sor.suporte.Api.pedidoReserva;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.sor.suporte.IntegracaoBase;

/** Story 1.8 — RF-031, RF-032 (modo simulado): notificações registradas e consultáveis por cada usuário. */
class NotificacaoApiTest extends IntegracaoBase {

    @Test
    void solicitacaoRestritaNotificaSolicitanteEResponsavelEDecisaoNotificaSolicitante() {
        long lab = api.criarRecurso("LAB-N", "SALA", true);
        long id = api.como("solicitante").post("/api/v1/reservas",
                pedidoReserva("2026-11-20T10:00:00", "2026-11-20T12:00:00", lab)).getBody().get("id").asLong();

        JsonNode doResponsavel = api.como("responsavel").get("/api/v1/notificacoes").getBody();
        api.como("responsavel").post("/api/v1/reservas/" + id + "/rejeitar", Map.of("motivo", "Sala em prova"));
        JsonNode doSolicitante = api.como("solicitante").get("/api/v1/notificacoes").getBody();

        assertThat(doResponsavel).hasSize(1);
        assertThat(doResponsavel.get(0).get("mensagem").asText()).contains("aguarda sua decisão");
        assertThat(doSolicitante).hasSize(2);
        assertThat(doSolicitante.get(0).get("mensagem").asText()).contains("rejeitada", "Sala em prova");
        assertThat(doSolicitante.get(0).get("status").asText()).isEqualTo("SIMULADA");
        assertThat(api.como("solicitante2").get("/api/v1/notificacoes").getBody()).isEmpty();
    }

    @Test
    void alteracaoCancelamentoENaoComparecimentoGeramNotificacao() {
        long sala = api.criarRecurso("SALA-N", "SALA", false);
        long id = api.como("solicitante").post("/api/v1/reservas",
                pedidoReserva("2026-11-20T10:00:00", "2026-11-20T12:00:00", sala)).getBody().get("id").asLong();
        api.como("solicitante").put("/api/v1/reservas/" + id, Map.of("inicio", "2026-11-21T10:00:00",
                "fim", "2026-11-21T11:00:00", "finalidade", "Remarcada"));
        api.como("admin").post("/api/v1/reservas/" + id + "/cancelar", Map.of("motivo", "Feriado"));

        JsonNode mensagens = api.como("solicitante").get("/api/v1/notificacoes").getBody();

        assertThat(mensagens).hasSize(3);
        assertThat(mensagens.toString()).contains("confirmada", "alterada", "cancelada", "Feriado");
    }
}

package br.edu.sor.reservas;

import static br.edu.sor.suporte.Api.pedidoReserva;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.sor.suporte.IntegracaoBase;

/**
 * Story 1.5 — RF-007, RF-008, RF-009, RF-011, RF-018: criação de reservas com detecção de sobreposição
 * (inclusive na agenda do professor), aprovação obrigatória para recurso restrito e conflitos evitados.
 */
class ReservaApiTest extends IntegracaoBase {

    private static final String DIA = "2026-11-20T";

    private long sala;
    private long sala2;
    private long laboratorio;
    private long professora;
    private long projetor;

    @BeforeEach
    void recursos() {
        sala = api.criarRecurso("SALA-101", "SALA", false);
        sala2 = api.criarRecurso("SALA-102", "SALA", false);
        laboratorio = api.criarRecurso("LAB-1", "SALA", true);
        professora = api.criarRecurso("PROF-ANA", "PROFESSOR", false);
        projetor = api.criarRecurso("PROJ-1", "EQUIPAMENTO", false);
    }

    private ResponseEntity<JsonNode> reservar(String login, String inicio, String fim, long... recursos) {
        return api.como(login).post("/api/v1/reservas", pedidoReserva(DIA + inicio, DIA + fim, recursos));
    }

    @Test
    void reservaDeRecursosComunsNasceAprovadaComSalaEProfessorEAuditada() {
        ResponseEntity<JsonNode> resposta = reservar("solicitante", "10:00:00", "12:00:00", sala, professora);

        assertThat(resposta.getStatusCode().value()).isEqualTo(201);
        JsonNode reserva = resposta.getBody();
        assertThat(reserva.get("estado").asText()).isEqualTo("APROVADA");
        assertThat(reserva.get("inicio").asText()).isEqualTo("2026-11-20T10:00:00");
        assertThat(reserva.get("recursos")).hasSize(2);
        assertThat(reserva.get("solicitante").get("nome").asText()).isEqualTo("Marina Costa");
        assertThat(contar("select count(*) from evento_auditoria where acao = 'RESERVA_CRIADA' and entidade_id = ?",
                reserva.get("id").asLong())).isEqualTo(1);
        assertThat(api.como("solicitante").get("/api/v1/reservas/minhas").getBody()).hasSize(1);
    }

    @Test
    void recursoRestritoExigeJustificativaENasceSolicitada() {
        Map<String, Object> semJustificativa = new HashMap<>(pedidoReserva(DIA + "10:00:00", DIA + "12:00:00", laboratorio));
        semJustificativa.remove("justificativa");

        ResponseEntity<JsonNode> recusada = api.como("solicitante").post("/api/v1/reservas", semJustificativa);
        ResponseEntity<JsonNode> aceita = reservar("solicitante", "10:00:00", "12:00:00", laboratorio);

        assertThat(recusada.getStatusCode().value()).isEqualTo(400);
        assertThat(recusada.getBody().get("detail").asText()).contains("Justificativa");
        assertThat(aceita.getStatusCode().value()).isEqualTo(201);
        assertThat(aceita.getBody().get("estado").asText()).isEqualTo("SOLICITADA");
    }

    @ParameterizedTest(name = "existente 10:00–12:00 x pedido {0}–{1} -> {2}")
    @CsvSource({
        "11:00:00, 13:00:00, 409",
        "09:00:00, 11:00:00, 409",
        "10:00:00, 12:00:00, 409",
        "10:30:00, 11:30:00, 409",
        "09:00:00, 13:00:00, 409",
        "12:00:00, 14:00:00, 201",
        "08:00:00, 10:00:00, 201"
    })
    void sobreposicaoNoMesmoRecursoEhRecusadaEAdjacenciaEhAceita(String inicio, String fim, int esperado) {
        reservar("solicitante", "10:00:00", "12:00:00", sala);

        ResponseEntity<JsonNode> resposta = reservar("solicitante2", inicio, fim, sala);

        assertThat(resposta.getStatusCode().value()).isEqualTo(esperado);
        if (esperado == 409) {
            assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CONFLITO_HORARIO");
            assertThat(resposta.getBody().get("detail").asText()).contains("SALA-101 já está reservado");
            JsonNode conflito = resposta.getBody().get("conflitos").get(0);
            assertThat(conflito.get("motivo").asText()).isEqualTo("RESERVA");
            assertThat(conflito.has("reservaId")).as("não expõe a reserva de outra pessoa").isFalse();
        }
    }

    @Test
    void professorNaoPodeSerAlocadoEmDuasSalasNoMesmoHorario() {
        reservar("solicitante", "10:00:00", "12:00:00", sala, professora);

        ResponseEntity<JsonNode> resposta = reservar("solicitante2", "11:00:00", "12:30:00", sala2, professora);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("conflitos").get(0).get("recursoCodigo").asText()).isEqualTo("PROF-ANA");
    }

    @Test
    void pedidoComVariosRecursosEhTudoOuNada() {
        reservar("solicitante", "10:00:00", "12:00:00", projetor);

        ResponseEntity<JsonNode> resposta = reservar("solicitante2", "10:00:00", "12:00:00", sala2, projetor);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(contar("select count(*) from ocupacao where recurso_id = ?", sala2)).isZero();
        assertThat(contar("select count(*) from reserva")).isEqualTo(1);
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
        "término antes do início | 2026-11-20T12:00:00 | 2026-11-20T10:00:00 | 400 | O término deve ser posterior",
        "término igual ao início | 2026-11-20T10:00:00 | 2026-11-20T10:00:00 | 400 | O término deve ser posterior",
        "início no passado       | 2026-11-08T10:00:00 | 2026-11-08T12:00:00 | 400 | data e hora futura"
    })
    void periodoInvalidoEhRecusadoComMensagemClara(String caso, String inicio, String fim, int status, String mensagem) {
        ResponseEntity<JsonNode> resposta = api.como("solicitante").post("/api/v1/reservas",
                pedidoReserva(inicio, fim, sala));

        assertThat(resposta.getStatusCode().value()).isEqualTo(status);
        assertThat(resposta.getBody().get("detail").asText()).contains(mensagem);
        assertThat(contar("select count(*) from reserva")).isZero();
    }

    @Test
    void camposObrigatoriosAusentesRetornam400ComCampos() {
        ResponseEntity<JsonNode> resposta = api.como("solicitante").post("/api/v1/reservas",
                Map.of("inicio", DIA + "10:00:00", "recursoIds", List.of()));

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().get("erros").toString()).contains("fim", "finalidade", "recursoIds");
    }

    @Test
    void recursoInexistenteRetorna404() {
        assertThat(reservar("solicitante", "10:00:00", "12:00:00", 9999).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void recursoDesativadoNaoPodeSerReservado() {
        api.como("admin").post("/api/v1/recursos/" + sala2 + "/desativar", null);

        ResponseEntity<JsonNode> resposta = reservar("solicitante", "10:00:00", "12:00:00", sala2);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("conflitos").get(0).get("motivo").asText()).isEqualTo("RECURSO_INATIVO");
    }

    @Test
    void cadaTentativaRecusadaPorConflitoEhRegistradaComoConflitoEvitado() {
        reservar("solicitante", "10:00:00", "12:00:00", sala);
        reservar("solicitante2", "11:00:00", "13:00:00", sala);
        reservar("solicitante2", "10:30:00", "10:45:00", sala);

        assertThat(contar("select count(*) from conflito_evitado where recurso_id = ?", sala)).isEqualTo(2);
    }

    @Test
    void consultaDeDisponibilidadeAgendaEPesquisaRefletemAReserva() {
        reservar("solicitante", "10:00:00", "12:00:00", sala, professora);
        String periodo = "&inicio=" + DIA + "11:00:00&fim=" + DIA + "13:00:00";

        JsonNode situacao = api.como("solicitante2")
                .get("/api/v1/disponibilidade?recursoIds=" + sala + "," + sala2 + periodo).getBody();
        JsonNode agenda = api.como("solicitante2").get("/api/v1/disponibilidade/recursos/" + professora + "/agenda"
                + periodo.replaceFirst("&", "?")).getBody();
        JsonNode salasLivres = api.como("solicitante2").get("/api/v1/recursos?tipo=SALA&disponivelInicio=" + DIA
                + "11:00:00&disponivelFim=" + DIA + "13:00:00").getBody();

        assertThat(situacao.get(0).get("disponivel").asBoolean()).isFalse();
        assertThat(situacao.get(0).get("motivos").get(0).get("motivo").asText()).isEqualTo("RESERVA");
        assertThat(situacao.get(1).get("disponivel").asBoolean()).isTrue();
        assertThat(agenda).hasSize(1);
        assertThat(agenda.get(0).get("inicio").asText()).isEqualTo("2026-11-20T10:00:00");
        assertThat(salasLivres.toString()).contains("SALA-102").doesNotContain("SALA-101");
    }

    @Test
    void solicitanteNaoVeReservaDeOutraPessoaMasResponsavelEAdministradorVeem() {
        long id = reservar("solicitante", "10:00:00", "12:00:00", sala).getBody().get("id").asLong();

        assertThat(api.como("solicitante2").get("/api/v1/reservas/" + id).getStatusCode().value()).isEqualTo(403);
        assertThat(api.como("responsavel").get("/api/v1/reservas/" + id).getStatusCode().value()).isEqualTo(200);
        assertThat(api.como("admin").get("/api/v1/reservas/" + id).getStatusCode().value()).isEqualTo(200);
        assertThat(api.como("solicitante2").get("/api/v1/reservas/minhas").getBody()).isEmpty();
        assertThat(api.como("solicitante").get("/api/v1/reservas/999").getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void minhasReservasFiltraPorEstado() {
        reservar("solicitante", "10:00:00", "12:00:00", sala);
        reservar("solicitante", "10:00:00", "12:00:00", laboratorio);

        JsonNode solicitadas = api.como("solicitante").get("/api/v1/reservas/minhas?estado=SOLICITADA").getBody();

        assertThat(solicitadas).hasSize(1);
        assertThat(solicitadas.get(0).get("recursos").get(0).get("codigo").asText()).isEqualTo("LAB-1");
    }

    @Test
    void recursoComReservaFuturaNaoPodeSerDesativado() {
        reservar("solicitante", "10:00:00", "12:00:00", sala);

        ResponseEntity<JsonNode> resposta = api.como("admin").post("/api/v1/recursos/" + sala + "/desativar", null);

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("RECURSO_COM_RESERVAS");
    }
}

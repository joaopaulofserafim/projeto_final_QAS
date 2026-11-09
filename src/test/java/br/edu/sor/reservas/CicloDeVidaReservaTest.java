package br.edu.sor.reservas;

import static br.edu.sor.suporte.Api.pedidoReserva;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 * Story 1.6 — RF-010, RF-012..RF-017, RF-027, RN-011..RN-014, RN-025, RN-029:
 * aprovação somente por Responsável, rejeição com motivo, cancelamento, alteração, uso, conclusão e não comparecimento.
 */
class CicloDeVidaReservaTest extends IntegracaoBase {

    private static final String INICIO = "2026-11-20T10:00:00";
    private static final String FIM = "2026-11-20T12:00:00";

    private long sala;
    private long laboratorio;

    @BeforeEach
    void recursos() {
        sala = api.criarRecurso("SALA-101", "SALA", false);
        laboratorio = api.criarRecurso("LAB-1", "SALA", true);
    }

    private long reservar(String login, long recurso) {
        return reservar(login, recurso, INICIO, FIM);
    }

    private long reservar(String login, long recurso, String inicio, String fim) {
        ResponseEntity<JsonNode> r = api.como(login).post("/api/v1/reservas", pedidoReserva(inicio, fim, recurso));
        assertThat(r.getStatusCode().value()).as(r.getBody().toString()).isEqualTo(201);
        return r.getBody().get("id").asLong();
    }

    private ResponseEntity<JsonNode> acao(String login, long id, String acao, Map<String, Object> corpo) {
        return api.como(login).post("/api/v1/reservas/" + id + "/" + acao, corpo);
    }

    private String estado(long id) {
        return api.como("admin").get("/api/v1/reservas/" + id).getBody().get("estado").asText();
    }

    @Test
    void responsavelVeFilaEAprovaSolicitacaoRestritaComRegistroDoDecisor() {
        long id = reservar("solicitante", laboratorio);

        JsonNode fila = api.como("responsavel").get("/api/v1/reservas?estado=SOLICITADA").getBody();
        ResponseEntity<JsonNode> aprovada = acao("responsavel", id, "aprovar", Map.of("motivo", "Ok para a aula"));

        assertThat(fila).hasSize(1);
        assertThat(aprovada.getStatusCode().value()).isEqualTo(200);
        assertThat(aprovada.getBody().get("estado").asText()).isEqualTo("APROVADA");
        assertThat(aprovada.getBody().get("decididoPor").get("nome").asText()).isEqualTo("Rita Responsável");
        JsonNode historico = api.como("solicitante").get("/api/v1/reservas/" + id + "/historico").getBody();
        assertThat(historico).hasSize(2);
        assertThat(historico.get(1).get("estadoAnterior").asText()).isEqualTo("SOLICITADA");
        assertThat(historico.get(1).get("estadoNovo").asText()).isEqualTo("APROVADA");
        assertThat(historico.get(1).get("ator").asText()).isEqualTo("responsavel");
    }

    @Test
    void aprovarDuasVezesRetornaTransicaoInvalida() {
        long id = reservar("solicitante", laboratorio);
        acao("responsavel", id, "aprovar", null);

        ResponseEntity<JsonNode> segunda = acao("responsavel", id, "aprovar", null);

        assertThat(segunda.getStatusCode().value()).isEqualTo(409);
        assertThat(segunda.getBody().get("codigo").asText()).isEqualTo("TRANSICAO_INVALIDA");
    }

    @Test
    void aprovacoesSimultaneasDaMesmaSolicitacaoResultamEmUmaUnicaDecisao() throws Exception {
        long id = reservar("solicitante", laboratorio);
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch largada = new CountDownLatch(1);
        List<Future<Integer>> resultados = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            resultados.add(executor.submit(() -> {
                largada.await();
                return acao("responsavel", id, "aprovar", null).getStatusCode().value();
            }));
        }
        largada.countDown();
        List<Integer> status = new ArrayList<>();
        for (Future<Integer> f : resultados) {
            status.add(f.get(60, TimeUnit.SECONDS));
        }
        executor.shutdown();

        assertThat(status).containsOnly(200, 409).filteredOn(s -> s == 200).hasSize(1);
        assertThat(contar("select count(*) from evento_auditoria where acao = 'RESERVA_APROVADA'")).isEqualTo(1);
    }

    @Test
    void rejeicaoExigeMotivoELiberaOHorario() {
        long id = reservar("solicitante", laboratorio);

        ResponseEntity<JsonNode> semMotivo = acao("responsavel", id, "rejeitar", Map.of());
        ResponseEntity<JsonNode> rejeitada = acao("responsavel", id, "rejeitar", Map.of("motivo", "Laboratório reservado para prova"));

        assertThat(semMotivo.getStatusCode().value()).isEqualTo(400);
        assertThat(semMotivo.getBody().get("detail").asText()).contains("motivo");
        assertThat(rejeitada.getBody().get("estado").asText()).isEqualTo("REJEITADA");
        assertThat(rejeitada.getBody().get("motivoDecisao").asText()).contains("prova");
        reservar("solicitante2", laboratorio);
    }

    @Test
    void solicitanteCancelaAPropriaReservaAntesDoInicioSemApagarOHistorico() {
        long id = reservar("solicitante", sala);

        ResponseEntity<JsonNode> cancelada = acao("solicitante", id, "cancelar", Map.of("motivo", "Aula remarcada"));

        assertThat(cancelada.getBody().get("estado").asText()).isEqualTo("CANCELADA");
        assertThat(estado(id)).isEqualTo("CANCELADA");
        reservar("solicitante2", sala);
        assertThat(contar("select count(*) from reserva")).isEqualTo(2);
    }

    @Test
    void solicitanteNaoCancelaReservaDeOutraPessoaMasAdministradorPode() {
        long id = reservar("solicitante", sala);

        assertThat(acao("solicitante2", id, "cancelar", null).getStatusCode().value()).isEqualTo(403);
        assertThat(acao("admin", id, "cancelar", Map.of("motivo", "Conflito com manutenção")).getBody().get("estado")
                .asText()).isEqualTo("CANCELADA");
    }

    @Test
    void reservaIniciadaNaoPodeSerCanceladaNemApagada() {
        long id = reservar("solicitante", sala);
        assertThat(acao("responsavel", id, "iniciar", null).getBody().get("estado").asText()).isEqualTo("EM_USO");

        ResponseEntity<JsonNode> cancelar = acao("solicitante", id, "cancelar", null);
        ResponseEntity<JsonNode> apagar = api.como("admin").delete("/api/v1/reservas/" + id);

        assertThat(cancelar.getStatusCode().value()).isEqualTo(409);
        assertThat(cancelar.getBody().get("detail").asText()).contains("não podem ser canceladas");
        assertThat(apagar.getStatusCode().value()).isEqualTo(405);
        assertThat(estado(id)).isEqualTo("EM_USO");
    }

    @Test
    void reservaAprovadaCujoHorarioJaComecouNaoPodeSerCancelada() {
        long id = reservar("solicitante", sala);
        relogio.definir(LocalDateTime.parse("2026-11-20T10:05:00"));

        assertThat(acao("solicitante", id, "cancelar", null).getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void alteracaoRevalidaConflitoELiberaOHorarioAntigo() {
        long minha = reservar("solicitante", sala);
        reservar("solicitante2", sala, "2026-11-20T14:00:00", "2026-11-20T16:00:00");

        ResponseEntity<JsonNode> conflito = api.como("solicitante").put("/api/v1/reservas/" + minha,
                alteracao("2026-11-20T15:00:00", "2026-11-20T17:00:00"));
        ResponseEntity<JsonNode> alterada = api.como("solicitante").put("/api/v1/reservas/" + minha,
                alteracao("2026-11-20T08:00:00", "2026-11-20T09:30:00"));

        assertThat(conflito.getStatusCode().value()).isEqualTo(409);
        assertThat(contar("select count(*) from conflito_evitado")).isEqualTo(1);
        assertThat(alterada.getStatusCode().value()).isEqualTo(200);
        assertThat(alterada.getBody().get("inicio").asText()).isEqualTo("2026-11-20T08:00:00");
        assertThat(alterada.getBody().get("estado").asText()).isEqualTo("APROVADA");
        reservar("solicitante2", sala);
    }

    @Test
    void alterarReservaAprovadaDeRecursoRestritoExigeNovaAprovacao() {
        long id = reservar("solicitante", laboratorio);
        acao("responsavel", id, "aprovar", null);

        ResponseEntity<JsonNode> alterada = api.como("solicitante").put("/api/v1/reservas/" + id,
                alteracao("2026-11-21T10:00:00", "2026-11-21T12:00:00"));

        assertThat(alterada.getBody().get("estado").asText()).isEqualTo("SOLICITADA");
        assertThat(alterada.getBody().has("decididoPor")).isFalse();
    }

    @Test
    void alteracaoSoPeloDonoEAntesDoUso() {
        long id = reservar("solicitante", sala);

        assertThat(api.como("solicitante2").put("/api/v1/reservas/" + id, alteracao("2026-11-21T10:00:00",
                "2026-11-21T11:00:00")).getStatusCode().value()).isEqualTo(403);
        acao("responsavel", id, "iniciar", null);
        assertThat(api.como("solicitante").put("/api/v1/reservas/" + id, alteracao("2026-11-21T10:00:00",
                "2026-11-21T11:00:00")).getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void naoComparecimentoSoDepoisDoInicioELiberaOHorario() {
        long id = reservar("solicitante", sala);

        ResponseEntity<JsonNode> cedo = acao("responsavel", id, "nao-comparecimento", null);
        relogio.definir(LocalDateTime.parse("2026-11-20T10:20:00"));
        ResponseEntity<JsonNode> registrado = acao("responsavel", id, "nao-comparecimento", null);

        assertThat(cedo.getStatusCode().value()).isEqualTo(409);
        assertThat(registrado.getBody().get("estado").asText()).isEqualTo("NAO_COMPARECEU");
        assertThat(contar("select count(*) from ocupacao where reserva_id = ? and ativa", id)).isZero();
    }

    @Test
    void concluirExigeReservaEmUso() {
        long id = reservar("solicitante", sala);

        ResponseEntity<JsonNode> antes = acao("responsavel", id, "concluir", null);
        acao("responsavel", id, "iniciar", null);
        ResponseEntity<JsonNode> concluida = acao("responsavel", id, "concluir", null);

        assertThat(antes.getStatusCode().value()).isEqualTo(409);
        assertThat(concluida.getBody().get("estado").asText()).isEqualTo("CONCLUIDA");
        assertThat(contar("select count(*) from ocupacao where reserva_id = ? and ativa", id)).isZero();
        assertThat(contar("select count(*) from evento_auditoria where entidade = 'RESERVA' and entidade_id = ?", id))
                .isEqualTo(3);
    }

    @Test
    void consultaAdministrativaFiltraPorEstadoRecursoEPeriodo() {
        reservar("solicitante", sala);
        reservar("solicitante2", laboratorio);
        reservar("solicitante", sala, "2026-12-05T10:00:00", "2026-12-05T11:00:00");

        assertThat(api.como("admin").get("/api/v1/reservas?recursoId=" + sala).getBody()).hasSize(2);
        assertThat(api.como("admin").get("/api/v1/reservas?de=2026-12-01T00:00:00&ate=2026-12-31T00:00:00").getBody())
                .hasSize(1);
        assertThat(api.como("responsavel").get("/api/v1/reservas?estado=APROVADA").getBody()).hasSize(2);
    }

    @Test
    void historicoDaReservaNaoEhVisivelParaOutroSolicitante() {
        long id = reservar("solicitante", sala);

        assertThat(api.como("solicitante2").get("/api/v1/reservas/" + id + "/historico").getStatusCode().value())
                .isEqualTo(403);
        assertThat(api.como("responsavel").get("/api/v1/reservas/" + id + "/historico").getBody()).hasSize(1);
    }

    @Test
    void acaoSobreReservaInexistenteRetorna404() {
        assertThat(acao("responsavel", 999, "aprovar", null).getStatusCode().value()).isEqualTo(404);
    }

    private static Map<String, Object> alteracao(String inicio, String fim) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("inicio", inicio);
        corpo.put("fim", fim);
        corpo.put("finalidade", "Aula remarcada");
        corpo.put("justificativa", "Mudança no calendário");
        return corpo;
    }
}

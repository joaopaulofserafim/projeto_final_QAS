package br.edu.sor.relatorios;

import static br.edu.sor.suporte.Api.pedidoReserva;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.sor.suporte.IntegracaoBase;

/** Story 1.8 — RF-028, RF-034, RF-035, RF-036, RN-024 (DP-010): relatórios com critério explícito e fonte verificável. */
class RelatorioApiTest extends IntegracaoBase {

    private static final String JANELA = "?inicio=2026-11-20T00:00:00&fim=2026-11-21T00:00:00";

    private long salaA;
    private long salaB;
    private long professora;

    private long reservar(String login, String inicio, String fim, long... recursos) {
        return api.como(login).post("/api/v1/reservas", pedidoReserva("2026-11-20T" + inicio, "2026-11-20T" + fim,
                recursos)).getBody().get("id").asLong();
    }

    @BeforeEach
    void cenario() {
        salaA = api.criarRecurso("SALA-A", "SALA", false);
        salaB = api.criarRecurso("SALA-B", "SALA", true);
        professora = api.criarRecurso("PROF-ANA", "PROFESSOR", false);

        reservar("solicitante", "10:00:00", "12:00:00", salaA, professora);           // APROVADA: 2h + 2h docente
        long cancelada = reservar("solicitante", "14:00:00", "15:00:00", salaA);       // CANCELADA: não conta
        api.como("solicitante").post("/api/v1/reservas/" + cancelada + "/cancelar", null);
        reservar("solicitante2", "10:00:00", "11:00:00", salaB);                       // SOLICITADA: não conta
        long ausente = reservar("solicitante2", "16:00:00", "17:00:00", salaA);        // NAO_COMPARECEU: não conta
        relogio.definir(LocalDateTime.parse("2026-11-20T16:30:00"));
        api.como("responsavel").post("/api/v1/reservas/" + ausente + "/nao-comparecimento", null);
        relogio.definir(LocalDateTime.parse("2026-11-09T08:00:00"));
        api.como("solicitante2").post("/api/v1/reservas", pedidoReserva("2026-11-20T11:00:00", "2026-11-20T13:00:00", salaA));
        api.como("solicitante2").post("/api/v1/reservas", pedidoReserva("2026-11-20T11:30:00", "2026-11-20T12:30:00", professora));
    }

    @Test
    void utilizacaoPorRecursoContaSoReservasConfirmadasDentroDaJanela() {
        JsonNode relatorio = api.como("admin").get("/api/v1/relatorios/utilizacao" + JANELA).getBody();

        assertThat(relatorio.get("criterio").asText()).contains("APROVADA", "EM_USO", "CONCLUIDA");
        assertThat(relatorio.get("unidade").asText()).isEqualTo("horas");
        JsonNode a = item(relatorio, "SALA-A");
        assertThat(a.get("reservas").asInt()).isEqualTo(1);
        assertThat(a.get("horas").asDouble()).isEqualTo(2.0);
        assertThat(a.get("percentualOcupacao").asDouble()).isEqualTo(8.3);
        assertThat(item(relatorio, "SALA-B").get("horas").asDouble()).isZero();
    }

    @Test
    void utilizacaoRecortaAJanelaEFiltraPorTipo() {
        JsonNode relatorio = api.como("admin")
                .get("/api/v1/relatorios/utilizacao?inicio=2026-11-20T11:00:00&fim=2026-11-20T23:00:00&tipo=SALA").getBody();

        assertThat(relatorio.get("itens")).hasSize(2);
        assertThat(item(relatorio, "SALA-A").get("horas").asDouble()).isEqualTo(1.0);
    }

    @Test
    void cargaHorariaPorProfessor() {
        JsonNode relatorio = api.como("admin").get("/api/v1/relatorios/carga-horaria" + JANELA).getBody();

        assertThat(relatorio.get("itens")).hasSize(1);
        assertThat(item(relatorio, "PROF-ANA").get("horas").asDouble()).isEqualTo(2.0);
    }

    @Test
    void conflitosEvitadosVemDasTentativasRegistradas() {
        JsonNode relatorio = api.como("admin").get("/api/v1/relatorios/conflitos?inicio=2026-11-09T00:00:00&fim=2026-11-10T00:00:00")
                .getBody();
        JsonNode outroDia = api.como("admin").get("/api/v1/relatorios/conflitos?inicio=2026-12-01T00:00:00&fim=2026-12-02T00:00:00")
                .getBody();

        assertThat(relatorio.get("total").asInt()).isEqualTo(2);
        assertThat(relatorio.get("porMotivo").get("RESERVA").asInt()).isEqualTo(2);
        assertThat(relatorio.get("porRecurso")).hasSize(2);
        assertThat(outroDia.get("total").asInt()).isZero();
    }

    @Test
    void resumoOperacionalDoPainel() {
        JsonNode resumo = api.como("admin").get("/api/v1/relatorios/resumo").getBody();

        assertThat(resumo.get("reservasPorEstado").get("APROVADA").asInt()).isEqualTo(1);
        assertThat(resumo.get("reservasPorEstado").get("CANCELADA").asInt()).isEqualTo(1);
        assertThat(resumo.get("reservasPorEstado").get("NAO_COMPARECEU").asInt()).isEqualTo(1);
        assertThat(resumo.get("reservasPorEstado").get("EM_USO").asInt()).isZero();
        assertThat(resumo.get("solicitacoesPendentes").asInt()).isEqualTo(1);
        assertThat(resumo.get("conflitosEvitados").asInt()).isEqualTo(2);
        assertThat(resumo.get("movimentacoesAbertas").asInt()).isZero();
        assertThat(resumo.get("manutencoesAtivas").asInt()).isZero();
    }

    @Test
    void janelaInvalidaRetorna400() {
        assertThat(api.como("admin").get("/api/v1/relatorios/utilizacao?inicio=2026-11-21T00:00:00&fim=2026-11-20T00:00:00")
                .getStatusCode().value()).isEqualTo(400);
        assertThat(api.como("admin").get("/api/v1/relatorios/carga-horaria").getStatusCode().value()).isEqualTo(400);
    }

    private static JsonNode item(JsonNode relatorio, String codigo) {
        for (JsonNode item : relatorio.get("itens")) {
            if (item.get("codigo").asText().equals(codigo)) {
                return item;
            }
        }
        throw new AssertionError("item " + codigo + " ausente em " + relatorio);
    }
}

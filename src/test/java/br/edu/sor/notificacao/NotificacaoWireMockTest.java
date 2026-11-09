package br.edu.sor.notificacao;

import static br.edu.sor.suporte.Api.pedidoReserva;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import br.edu.sor.suporte.IntegracaoBase;

/**
 * Story 1.8 — RF-031..RF-033, RN-023, ADR-007: integração HTTP com a API externa de notificações simulada pelo
 * WireMock. Falha ou lentidão do provedor nunca desfaz a reserva.
 */
class NotificacaoWireMockTest extends IntegracaoBase {

    @RegisterExtension
    static WireMockExtension provedor = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    @DynamicPropertySource
    static void notificacaoHttp(DynamicPropertyRegistry registro) {
        registro.add("sor.notificacao.modo", () -> "http");
        registro.add("sor.notificacao.url", provedor::baseUrl);
        registro.add("sor.notificacao.timeout", () -> "1s");
    }

    private ResponseEntity<JsonNode> reservar() {
        long sala = api.criarRecurso("SALA-W", "SALA", false);
        return api.como("solicitante").post("/api/v1/reservas",
                pedidoReserva("2026-11-20T10:00:00", "2026-11-20T12:00:00", sala));
    }

    private String statusDaNotificacao() {
        return jdbc.queryForObject("select status from notificacao order by id desc limit 1", String.class);
    }

    @Test
    void notificacaoEnviadaComDadosMinimosQuandoOProvedorAceita() {
        provedor.stubFor(post(urlEqualTo("/notificacoes")).willReturn(aResponse().withStatus(202)));

        ResponseEntity<JsonNode> reserva = reservar();

        assertThat(reserva.getStatusCode().value()).isEqualTo(201);
        assertThat(statusDaNotificacao()).isEqualTo("ENVIADA");
        provedor.verify(postRequestedFor(urlEqualTo("/notificacoes"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(matchingJsonPath("$.destinatario", containing("solicitante")))
                .withRequestBody(matchingJsonPath("$.referencia", containing("reserva-")))
                .withRequestBody(matchingJsonPath("$.mensagem", containing("confirmada"))));
    }

    @Test
    void erroDoProvedorMarcaFalhaSemDesfazerAReserva() {
        provedor.stubFor(post(urlEqualTo("/notificacoes")).willReturn(aResponse().withStatus(500)));

        ResponseEntity<JsonNode> reserva = reservar();

        assertThat(reserva.getStatusCode().value()).isEqualTo(201);
        assertThat(contar("select count(*) from reserva where estado = 'APROVADA'")).isEqualTo(1);
        assertThat(statusDaNotificacao()).isEqualTo("FALHA");
        assertThat(jdbc.queryForObject("select erro from notificacao order by id desc limit 1", String.class))
                .contains("500");
    }

    @Test
    void provedorLentoAlemDoTimeoutMarcaFalhaSemDesfazerAReserva() {
        provedor.stubFor(post(urlEqualTo("/notificacoes")).willReturn(aResponse().withStatus(200).withFixedDelay(3000)));

        long inicio = System.nanoTime();
        ResponseEntity<JsonNode> reserva = reservar();
        long duracaoMs = (System.nanoTime() - inicio) / 1_000_000;

        assertThat(reserva.getStatusCode().value()).isEqualTo(201);
        assertThat(statusDaNotificacao()).isEqualTo("FALHA");
        assertThat(duracaoMs).as("timeout de 1s respeitado").isLessThan(2900);
    }
}

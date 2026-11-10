package br.edu.sor.compartilhado;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.sor.suporte.IntegracaoBase;

/** RNF-003: erros com mensagem compreensível, status correto e sem detalhes internos. */
class ErrosApiTest extends IntegracaoBase {

    private ResponseEntity<JsonNode> enviarBruto(String corpo, MediaType tipo) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setBasicAuth("admin", "Senha@123");
        cabecalhos.setContentType(tipo);
        return http.exchange("/api/v1/recursos", HttpMethod.POST, new HttpEntity<>(corpo, cabecalhos), JsonNode.class);
    }

    @Test
    void jsonMalFormadoRetorna400SemStackTrace() {
        ResponseEntity<JsonNode> resposta = enviarBruto("{\"codigo\": ", MediaType.APPLICATION_JSON);

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().get("title").asText()).isEqualTo("Requisição mal formatada");
        assertThat(resposta.getBody().toString()).doesNotContain("Exception", "at br.edu", "jackson");
    }

    @Test
    void tipoDeConteudoNaoSuportadoRetorna415() {
        assertThat(enviarBruto("codigo=1", MediaType.TEXT_PLAIN).getStatusCode().value()).isEqualTo(415);
    }

    @Test
    void enumInvalidoNoFiltroRetorna400() {
        ResponseEntity<JsonNode> resposta = api.como("solicitante").get("/api/v1/recursos?tipo=NAVE");

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().get("detail").asText()).contains("tipo");
    }

    @Test
    void parametroObrigatorioAusenteRetorna400() {
        ResponseEntity<JsonNode> resposta = api.como("solicitante").get("/api/v1/disponibilidade?recursoIds=1");

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().get("detail").asText()).contains("inicio");
    }

    @Test
    void rotaInexistenteRetorna404() {
        assertThat(api.como("solicitante").get("/api/v1/nao-existe").getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void metodoNaoSuportadoRetorna405() {
        ResponseEntity<JsonNode> resposta = api.como("admin").delete("/api/v1/recursos/1");

        assertThat(resposta.getStatusCode().value()).isEqualTo(405);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("METODO_NAO_PERMITIDO");
    }
}

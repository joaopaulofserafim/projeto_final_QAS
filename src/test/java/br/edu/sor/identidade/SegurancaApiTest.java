package br.edu.sor.identidade;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.sor.suporte.IntegracaoBase;

/** RF-001..RF-003, RF-022, RNF-001, RN-015: autenticação, autorização por perfil e proteção de sessão. */
class SegurancaApiTest extends IntegracaoBase {

    @Test
    void anonimoRecebe401ComMensagemEmPortuguesESemJanelaDeLoginDoNavegador() {
        ResponseEntity<JsonNode> resposta = api.anonimo().get("/api/v1/auth/me");

        assertThat(resposta.getStatusCode().value()).isEqualTo(401);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("NAO_AUTENTICADO");
        assertThat(resposta.getBody().get("detail").asText()).contains("Faça login");
        assertThat(resposta.getHeaders().containsKey(HttpHeaders.WWW_AUTHENTICATE)).isFalse();
    }

    @ParameterizedTest(name = "{0} autentica com perfil {1}")
    @CsvSource({"admin, ADMINISTRADOR", "responsavel, RESPONSAVEL", "solicitante, SOLICITANTE"})
    void cadaPerfilAutenticaERecebeSeuPerfil(String login, String perfil) {
        ResponseEntity<JsonNode> resposta = api.como(login).get("/api/v1/auth/me");

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody().get("perfil").asText()).isEqualTo(perfil);
        assertThat(resposta.getBody().has("senhaHash")).isFalse();
    }

    @Test
    void senhaErradaEhRecusada() {
        assertThat(api.como("solicitante", "errada").get("/api/v1/auth/me").getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void usuarioDesativadoNaoAutentica() {
        jdbc.update("UPDATE usuario SET ativo = FALSE WHERE username = 'solicitante2'");

        assertThat(api.como("solicitante2").get("/api/v1/auth/me").getStatusCode().value()).isEqualTo(401);
    }

    /** Matriz de autorização: cada linha é uma combinação perfil x operação que DEVE ser negada. */
    @ParameterizedTest(name = "{0} {1} {2} -> 403")
    @CsvSource({
        "solicitante, GET,  /api/v1/usuarios",
        "responsavel, GET,  /api/v1/usuarios",
        "solicitante, GET,  /api/v1/auditoria",
        "responsavel, GET,  /api/v1/auditoria",
        "solicitante, GET,  /api/v1/relatorios/resumo",
        "responsavel, GET,  /api/v1/relatorios/utilizacao",
        "solicitante, POST, /api/v1/recursos",
        "responsavel, POST, /api/v1/recursos",
        "responsavel, POST, /api/v1/recursos/1/desativar",
        "solicitante, GET,  /api/v1/manutencoes",
        "solicitante, POST, /api/v1/manutencoes",
        "responsavel, POST, /api/v1/manutencoes",
        "solicitante, POST, /api/v1/reservas/1/aprovar",
        "admin,       POST, /api/v1/reservas/1/aprovar",
        "solicitante, POST, /api/v1/reservas/1/rejeitar",
        "admin,       POST, /api/v1/reservas/1/rejeitar",
        "solicitante, POST, /api/v1/reservas/1/iniciar",
        "solicitante, POST, /api/v1/reservas/1/concluir",
        "solicitante, POST, /api/v1/reservas/1/nao-comparecimento",
        "admin,       POST, /api/v1/reservas",
        "responsavel, POST, /api/v1/reservas",
        "responsavel, POST, /api/v1/reservas/1/cancelar",
        "solicitante, GET,  /api/v1/reservas",
        "solicitante, POST, /api/v1/movimentacoes/retiradas",
        "admin,       POST, /api/v1/movimentacoes/retiradas",
        "solicitante, GET,  /api/v1/movimentacoes"
    })
    void perfilSemPermissaoRecebe403(String login, String metodo, String caminho) {
        Object corpo = "POST".equals(metodo) ? Map.of() : null;
        ResponseEntity<JsonNode> resposta = api.como(login).trocar(HttpMethod.valueOf(metodo), caminho, corpo);

        assertThat(resposta.getStatusCode().value()).isEqualTo(403);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("ACESSO_NEGADO");
    }

    /**
     * Varre o contrato OpenAPI publicado e confirma que nenhuma operação de /api aceita acesso anônimo
     * (exceto o próprio login). Se alguém criar um endpoint novo sem regra, este teste falha.
     * Escritas anônimas podem ser barradas antes pelo filtro CSRF (403); leituras recebem 401.
     */
    @Test
    void nenhumEndpointDaApiAceitaAcessoAnonimo() {
        JsonNode contrato = api.anonimo().get("/v3/api-docs").getBody();
        List<String> liberados = new ArrayList<>();
        int verificados = 0;
        for (Iterator<Map.Entry<String, JsonNode>> it = contrato.get("paths").fields(); it.hasNext();) {
            Map.Entry<String, JsonNode> caminho = it.next();
            String url = caminho.getKey().replaceAll("\\{[^}]+}", "1");
            for (Iterator<String> metodos = caminho.getValue().fieldNames(); metodos.hasNext();) {
                String metodo = metodos.next().toUpperCase();
                Object corpo = metodo.equals("GET") ? null : Map.of();
                int status = api.anonimo().trocar(HttpMethod.valueOf(metodo), url, corpo).getStatusCode().value();
                verificados++;
                if (status != 401 && status != 403) {
                    liberados.add(metodo + " " + url + " -> " + status);
                }
            }
        }
        assertThat(verificados).isPositive();
        assertThat(liberados).as("endpoints acessíveis sem login").isEmpty();
    }

    @Test
    void respostasTemCabecalhosDeSeguranca() {
        ResponseEntity<JsonNode> resposta = api.como("solicitante").get("/api/v1/auth/me");

        assertThat(resposta.getHeaders().getFirst("Content-Security-Policy")).contains("default-src 'self'");
        assertThat(resposta.getHeaders().getFirst("X-Frame-Options")).isEqualTo("DENY");
        assertThat(resposta.getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
    }

    /** Fluxo da interface web: login por formulário + sessão + token CSRF obrigatório em escrita. */
    @Test
    void sessaoWebExigeTokenCsrfParaEscreverEEncerraNoLogout() {
        String sessao = login("admin");
        Map<String, Object> recurso = Map.of("codigo", "SALA-CSRF", "nome", "Sala", "tipo", "SALA", "capacidade", 10);

        ResponseEntity<JsonNode> semToken = comSessao(HttpMethod.POST, "/api/v1/recursos", recurso, sessao, null);
        assertThat(semToken.getStatusCode().value()).isEqualTo(403);
        assertThat(semToken.getBody().get("codigo").asText()).isEqualTo("CSRF_INVALIDO");

        String token = tokenCsrf(sessao);
        ResponseEntity<JsonNode> comToken = comSessao(HttpMethod.POST, "/api/v1/recursos", recurso, sessao, token);
        assertThat(comToken.getStatusCode().value()).isEqualTo(201);

        ResponseEntity<JsonNode> logout = comSessao(HttpMethod.POST, "/api/v1/auth/logout", null, sessao, token);
        assertThat(logout.getStatusCode().value()).isEqualTo(204);
        assertThat(comSessao(HttpMethod.GET, "/api/v1/auth/me", null, sessao, null).getStatusCode().value())
                .isEqualTo(401);
    }

    @Test
    void loginComSenhaErradaRetornaMensagemCompreensivel() {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", "admin");
        form.add("password", "errada");

        ResponseEntity<JsonNode> resposta = http.postForEntity("/api/v1/auth/login", new HttpEntity<>(form, cabecalhos),
                JsonNode.class);

        assertThat(resposta.getStatusCode().value()).isEqualTo(401);
        assertThat(resposta.getBody().get("detail").asText()).contains("Usuário ou senha inválidos");
    }

    private String login(String usuario) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", usuario);
        form.add("password", "Senha@123");
        ResponseEntity<JsonNode> resposta = http.postForEntity("/api/v1/auth/login", new HttpEntity<>(form, cabecalhos),
                JsonNode.class);
        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(resposta.getBody().get("perfil").asText()).isEqualTo("ADMINISTRADOR");
        return cookie(resposta, "JSESSIONID");
    }

    private String tokenCsrf(String sessao) {
        ResponseEntity<JsonNode> resposta = comSessao(HttpMethod.GET, "/api/v1/auth/me", null, sessao, null);
        return cookie(resposta, "XSRF-TOKEN");
    }

    private ResponseEntity<JsonNode> comSessao(HttpMethod metodo, String caminho, Object corpo, String sessao,
            String token) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setContentType(MediaType.APPLICATION_JSON);
        String cookies = "JSESSIONID=" + sessao + (token == null ? "" : "; XSRF-TOKEN=" + token);
        cabecalhos.add(HttpHeaders.COOKIE, cookies);
        if (token != null) {
            cabecalhos.add("X-XSRF-TOKEN", token);
        }
        return http.exchange(caminho, metodo, new HttpEntity<>(corpo, cabecalhos), JsonNode.class);
    }

    private static String cookie(ResponseEntity<?> resposta, String nome) {
        return resposta.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE).stream()
                .filter(c -> c.startsWith(nome + "=") && !c.startsWith(nome + "=;"))
                .map(c -> c.substring(nome.length() + 1, c.indexOf(';')))
                .findFirst()
                .orElseThrow(() -> new AssertionError("cookie " + nome + " ausente em " + resposta.getHeaders()));
    }
}

package br.edu.sor.identidade;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.sor.suporte.IntegracaoBase;

/** RF-022: gestão de usuários e permissões pelo Administrador, com auditoria. */
class UsuarioApiTest extends IntegracaoBase {

    private static Map<String, Object> novo(String login, String senha, String perfil) {
        return Map.of("username", login, "nome", "Pessoa " + login, "email", login + "@sor.local",
                "senha", senha, "perfil", perfil);
    }

    @Test
    void administradorCriaUsuarioQueConsegueAutenticarESemExporSenha() {
        ResponseEntity<JsonNode> criado = api.como("admin").post("/api/v1/usuarios",
                novo("coord.ana", "SenhaForte1", "SOLICITANTE"));

        assertThat(criado.getStatusCode().value()).isEqualTo(201);
        assertThat(criado.getBody().has("senha")).isFalse();
        assertThat(criado.getBody().has("senhaHash")).isFalse();
        assertThat(api.como("coord.ana", "SenhaForte1").get("/api/v1/auth/me").getBody().get("perfil").asText())
                .isEqualTo("SOLICITANTE");
        assertThat(contar("select count(*) from evento_auditoria where acao = 'USUARIO_CRIADO'")).isEqualTo(1);
        assertThat(jdbc.queryForObject("select senha_hash from usuario where username = 'coord.ana'", String.class))
                .startsWith("{bcrypt}").doesNotContain("SenhaForte1");
    }

    @Test
    void loginDuplicadoEhRecusadoComConflito() {
        ResponseEntity<JsonNode> resposta = api.como("admin").post("/api/v1/usuarios",
                novo("solicitante", "SenhaForte1", "SOLICITANTE"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("USUARIO_DUPLICADO");
    }

    @Test
    void dadosInvalidosRetornamErrosPorCampo() {
        ResponseEntity<JsonNode> resposta = api.como("admin").post("/api/v1/usuarios",
                Map.of("username", "x", "nome", "", "senha", "curta"));

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().get("erros").toString()).contains("username", "nome", "senha", "perfil");
    }

    @Test
    void administradorAlteraPerfilEDesativaUsuarioSemApagarHistorico() {
        long id = api.como("admin").post("/api/v1/usuarios", novo("prof.bia", "SenhaForte1", "SOLICITANTE"))
                .getBody().get("id").asLong();

        ResponseEntity<JsonNode> alterado = api.como("admin").put("/api/v1/usuarios/" + id,
                Map.of("nome", "Bia", "perfil", "RESPONSAVEL", "ativo", false));

        assertThat(alterado.getStatusCode().value()).isEqualTo(200);
        assertThat(alterado.getBody().get("ativo").asBoolean()).isFalse();
        assertThat(api.como("prof.bia", "SenhaForte1").get("/api/v1/auth/me").getStatusCode().value()).isEqualTo(401);
        assertThat(contar("select count(*) from usuario where id = ?", id)).isEqualTo(1);
        assertThat(api.como("admin").get("/api/v1/usuarios").getBody().size()).isEqualTo(5);
    }

    @Test
    void administradorNaoPodeDesativarAPropriaConta() {
        long idAdmin = contar("select id from usuario where username = 'admin'");

        ResponseEntity<JsonNode> resposta = api.como("admin").put("/api/v1/usuarios/" + idAdmin,
                Map.of("nome", "Admin", "perfil", "ADMINISTRADOR", "ativo", false));

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("AUTO_ALTERACAO");
    }

    @Test
    void alterarUsuarioInexistenteRetorna404() {
        ResponseEntity<JsonNode> resposta = api.como("admin").put("/api/v1/usuarios/999",
                Map.of("nome", "X", "perfil", "SOLICITANTE", "ativo", true));

        assertThat(resposta.getStatusCode().value()).isEqualTo(404);
    }
}

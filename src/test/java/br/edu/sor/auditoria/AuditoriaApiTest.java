package br.edu.sor.auditoria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.UncategorizedSQLException;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.sor.suporte.IntegracaoBase;

/** RF-029, RF-030, RN-016..RN-018: trilha de auditoria consultável e imutável. */
class AuditoriaApiTest extends IntegracaoBase {

    private void criarUsuario(String login) {
        api.como("admin").post("/api/v1/usuarios", Map.of("username", login, "nome", "Pessoa", "senha", "SenhaForte1",
                "perfil", "SOLICITANTE"));
    }

    @Test
    void administradorConsultaEventosComFiltrosMaisRecentesPrimeiro() {
        criarUsuario("pessoa.um");
        criarUsuario("pessoa.dois");
        api.como("admin").post("/api/v1/recursos", Map.of("codigo", "SALA-AUD", "nome", "Sala", "tipo", "SALA",
                "capacidade", 10));

        JsonNode todos = api.como("admin").get("/api/v1/auditoria").getBody();
        JsonNode usuarios = api.como("admin").get("/api/v1/auditoria?entidade=usuario&acao=usuario_criado&ator=admin")
                .getBody();

        assertThat(todos.size()).isEqualTo(3);
        assertThat(todos.get(0).get("acao").asText()).isEqualTo("RECURSO_CRIADO");
        assertThat(usuarios.size()).isEqualTo(2);
        assertThat(usuarios.get(0).get("ator").asText()).isEqualTo("admin");
    }

    @Test
    void filtraPorEntidadeEJanelaDeTempo() {
        criarUsuario("pessoa.tres");
        long id = contar("select id from usuario where username = 'pessoa.tres'");

        JsonNode porId = api.como("admin").get("/api/v1/auditoria?entidade=USUARIO&entidadeId=" + id).getBody();
        JsonNode foraDaJanela = api.como("admin")
                .get("/api/v1/auditoria?de=2020-01-01T00:00:00&ate=2020-01-02T00:00:00").getBody();
        JsonNode dentroDaJanela = api.como("admin")
                .get("/api/v1/auditoria?de=2026-11-09T00:00:00&ate=2026-11-10T00:00:00").getBody();

        assertThat(porId.size()).isEqualTo(1);
        assertThat(foraDaJanela.size()).isZero();
        assertThat(dentroDaJanela.size()).isEqualTo(1);
    }

    @Test
    void bancoRecusaAlterarOuApagarEventoDeAuditoria() {
        criarUsuario("pessoa.quatro");

        assertThatThrownBy(() -> jdbc.update("UPDATE evento_auditoria SET ator = 'hacker'"))
                .isInstanceOf(UncategorizedSQLException.class)
                .hasMessageContaining("imutáveis");
        assertThatThrownBy(() -> jdbc.update("DELETE FROM evento_auditoria"))
                .hasMessageContaining("imutáveis");
        assertThat(contar("select count(*) from evento_auditoria where ator = 'admin'")).isEqualTo(1);
    }

    @Test
    void apiNaoOfereceAlteracaoNemExclusaoDeEventos() {
        ResponseEntity<JsonNode> exclusao = api.como("admin").delete("/api/v1/auditoria");
        ResponseEntity<JsonNode> alteracao = api.como("admin").put("/api/v1/auditoria", Map.of("ator", "x"));

        assertThat(exclusao.getStatusCode().value()).isEqualTo(405);
        assertThat(alteracao.getStatusCode().value()).isEqualTo(405);
    }
}

package br.edu.sor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.edu.sor.suporte.IntegracaoBase;

class SistemaOrganizacaoRecursosApplicationTest extends IntegracaoBase {

    @Test
    void iniciaContextoComBancoMigradoESaudavel() {
        assertThat(contar("select count(*) from flyway_schema_history where success")).isGreaterThanOrEqualTo(2);
        assertThat(api.anonimo().get("/actuator/health").getBody().get("status").asText()).isEqualTo("UP");
    }
}

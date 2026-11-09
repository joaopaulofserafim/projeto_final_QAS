package br.edu.sor.compartilhado.erro;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;


class ViolacaoExclusaoTest {

    @Test
    void reconheceSqlState23P01EmQualquerNivelDaCadeia() {
        var raiz = new SQLException("exclusion", "23P01");
        assertThat(ViolacaoExclusao.causou(new RuntimeException(new RuntimeException(raiz)))).isTrue();
    }

    @Test
    void ignoraOutrosErros() {
        assertThat(ViolacaoExclusao.causou(new RuntimeException(new SQLException("unique", "23505")))).isFalse();
        assertThat(ViolacaoExclusao.causou(new RuntimeException("sem causa"))).isFalse();
        assertThat(ViolacaoExclusao.causou(null)).isFalse();
    }
}

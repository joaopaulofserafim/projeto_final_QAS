package br.edu.sor.compartilhado.erro;

import java.sql.SQLException;

/** Reconhece a violação da constraint de exclusão do PostgreSQL (SQLSTATE 23P01). */
public final class ViolacaoExclusao {

    static final String SQLSTATE_EXCLUSAO = "23P01";

    private ViolacaoExclusao() {
    }

    public static boolean causou(Throwable erro) {
        for (Throwable atual = erro; atual != null; atual = atual.getCause()) {
            if (atual instanceof SQLException sql && SQLSTATE_EXCLUSAO.equals(sql.getSQLState())) {
                return true;
            }
            if (atual.getCause() == atual) {
                return false;
            }
        }
        return false;
    }
}

package br.edu.sor.reservas;

import jakarta.validation.constraints.Size;

/** Corpo opcional das ações sobre a reserva (motivo da decisão, do cancelamento etc.). */
public record Decisao(@Size(max = 500, message = "no máximo 500 caracteres") String motivo) {

    static String motivoDe(Decisao decisao) {
        return decisao == null ? null : decisao.motivo();
    }
}

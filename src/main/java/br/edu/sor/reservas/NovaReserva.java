package br.edu.sor.reservas;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Pedido de reserva: um ou mais recursos (sala, professor, materiais) no mesmo período, tudo-ou-nada (DP-013). */
public record NovaReserva(
        @NotNull(message = "informe o início") LocalDateTime inicio,
        @NotNull(message = "informe o término") LocalDateTime fim,
        @NotBlank(message = "informe a finalidade") @Size(max = 300) String finalidade,
        @Size(max = 500) String justificativa,
        @NotEmpty(message = "informe ao menos um recurso") @Size(max = 10, message = "no máximo 10 recursos por reserva")
        List<@NotNull Long> recursoIds) {
}

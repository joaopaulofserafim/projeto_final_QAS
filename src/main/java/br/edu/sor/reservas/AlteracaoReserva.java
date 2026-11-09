package br.edu.sor.reservas;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Alteração de período e dados de uma reserva pelo dono (RN-029). Os recursos não mudam: para isso, cancele e peça outra. */
public record AlteracaoReserva(
        @NotNull(message = "informe o início") LocalDateTime inicio,
        @NotNull(message = "informe o término") LocalDateTime fim,
        @NotBlank(message = "informe a finalidade") @Size(max = 300) String finalidade,
        @Size(max = 500) String justificativa) {
}

package br.edu.sor.catalogo;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Dados de cadastro/alteração de um recurso. O código e o tipo não mudam depois de criados. */
public record DadosRecurso(
        @NotBlank @Size(max = 40)
        @Pattern(regexp = "[A-Za-z0-9._-]+", message = "use apenas letras, números, ponto, hífen ou sublinhado")
        String codigo,
        @NotBlank @Size(max = 120) String nome,
        @NotNull TipoRecurso tipo,
        @Positive(message = "a capacidade deve ser maior que zero") Integer capacidade,
        @Size(max = 120) String localizacao,
        @Size(max = 500) String descricao,
        boolean restrito,
        Set<@NotBlank @Size(max = 80) String> competencias) {
}

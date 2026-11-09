package br.edu.sor.compartilhado.erro;

import java.time.LocalDateTime;

/**
 * Explica um conflito sem expor dados pessoais de terceiros: qual recurso, o tipo de ocupação e o intervalo.
 * O id da reserva só é preenchido quando quem consulta pode vê-lo (por exemplo, o Administrador ao bloquear manutenção).
 */
public record ConflitoDetalhe(
        Long recursoId,
        String recursoCodigo,
        String motivo,
        LocalDateTime inicio,
        LocalDateTime fim,
        Long reservaId) {
}

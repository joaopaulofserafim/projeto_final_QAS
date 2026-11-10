package br.edu.sor.reservas;

/** Fato publicado após mudanças relevantes; consumido pelas notificações depois do commit (ADR-007). */
public record ReservaEvento(Long reservaId, Tipo tipo, Long solicitanteId, EstadoReserva estado, String detalhe) {

    public enum Tipo {
        CRIADA,
        APROVADA,
        REJEITADA,
        CANCELADA,
        ALTERADA,
        NAO_COMPARECEU
    }
}

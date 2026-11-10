package br.edu.sor.reservas;

import java.util.EnumSet;
import java.util.Set;

/**
 * Estados da reserva e transições permitidas (PRD/arquitetura/uml/ESTADOS_RESERVA.md).
 * Ocupam horário: SOLICITADA, APROVADA e EM_USO (DP-001, RN-027).
 */
public enum EstadoReserva {
    SOLICITADA(true),
    APROVADA(true),
    EM_USO(true),
    CONCLUIDA(false),
    REJEITADA(false),
    CANCELADA(false),
    NAO_COMPARECEU(false);

    private final boolean ocupaHorario;

    EstadoReserva(boolean ocupaHorario) {
        this.ocupaHorario = ocupaHorario;
    }

    public boolean ocupaHorario() {
        return ocupaHorario;
    }

    /** Alteração e cancelamento só antes do uso começar (RN-029). */
    public boolean editavel() {
        return this == SOLICITADA || this == APROVADA;
    }

    public boolean podeIrPara(EstadoReserva destino) {
        return destinos().contains(destino);
    }

    Set<EstadoReserva> destinos() {
        return switch (this) {
            case SOLICITADA -> EnumSet.of(APROVADA, REJEITADA, CANCELADA);
            case APROVADA -> EnumSet.of(EM_USO, CANCELADA, NAO_COMPARECEU);
            case EM_USO -> EnumSet.of(CONCLUIDA);
            case CONCLUIDA, REJEITADA, CANCELADA, NAO_COMPARECEU -> EnumSet.noneOf(EstadoReserva.class);
        };
    }
}

package br.edu.sor.disponibilidade;

import java.time.LocalDateTime;
import java.util.Objects;

public record PeriodoReserva(LocalDateTime inicio, LocalDateTime termino) {

    public PeriodoReserva {
        Objects.requireNonNull(inicio, "inicio must not be null");
        Objects.requireNonNull(termino, "termino must not be null");
        if (!termino.isAfter(inicio)) {
            throw new IllegalArgumentException("termino must be after inicio");
        }
    }

    public boolean sobrepoe(PeriodoReserva outro) {
        Objects.requireNonNull(outro, "outro must not be null");
        return inicio.isBefore(outro.termino) && outro.inicio.isBefore(termino);
    }
}


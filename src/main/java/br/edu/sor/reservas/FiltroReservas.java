package br.edu.sor.reservas;

import java.time.LocalDateTime;

/** Filtros da fila do Responsável e da consulta administrativa (RF-015, RF-027). */
public record FiltroReservas(EstadoReserva estado, Long recursoId, LocalDateTime de, LocalDateTime ate) {
}

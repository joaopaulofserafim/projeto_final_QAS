package br.edu.sor.relatorios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

/** Regras puras dos relatórios (DP-010): horas dentro da janela e percentual com uma casa decimal. */
final class CalculadoraHoras {

    private CalculadoraHoras() {
    }

    static double horasDentro(LocalDateTime inicio, LocalDateTime fim, LocalDateTime janelaInicio,
            LocalDateTime janelaFim) {
        LocalDateTime de = inicio.isAfter(janelaInicio) ? inicio : janelaInicio;
        LocalDateTime ate = fim.isBefore(janelaFim) ? fim : janelaFim;
        if (!ate.isAfter(de)) {
            return 0.0;
        }
        return arredondar(minutos(de, ate) / 60.0, 2);
    }

    static double horasDaJanela(LocalDateTime janelaInicio, LocalDateTime janelaFim) {
        return minutos(janelaInicio, janelaFim) / 60.0;
    }

    /** Duração em minutos no relógio local único da instituição (DP-015): ambos os extremos usam o mesmo deslocamento. */
    private static long minutos(LocalDateTime de, LocalDateTime ate) {
        Instant inicio = de.toInstant(ZoneOffset.UTC);
        Instant fim = ate.toInstant(ZoneOffset.UTC);
        return Duration.between(inicio, fim).toMinutes();
    }

    static double percentual(double horas, double total) {
        if (total <= 0) {
            return 0.0;
        }
        return arredondar(100.0 * horas / total, 1);
    }

    static double arredondar(double valor, int casas) {
        return BigDecimal.valueOf(valor).setScale(casas, RoundingMode.HALF_UP).doubleValue();
    }
}

package br.edu.sor.relatorios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

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
        return arredondar(ChronoUnit.MINUTES.between(de, ate) / 60.0, 2);
    }

    static double horasDaJanela(LocalDateTime janelaInicio, LocalDateTime janelaFim) {
        return ChronoUnit.MINUTES.between(janelaInicio, janelaFim) / 60.0;
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

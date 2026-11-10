package br.edu.sor.relatorios;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** DP-010: horas contadas apenas dentro da janela do relatório (início inclusivo, término exclusivo). */
class CalculadoraHorasTest {

    private static final LocalDateTime JANELA_INICIO = LocalDateTime.parse("2026-11-20T08:00:00");
    private static final LocalDateTime JANELA_FIM = LocalDateTime.parse("2026-11-20T18:00:00");

    @ParameterizedTest(name = "{0}: {1}–{2} -> {3}h")
    @CsvSource({
        "totalmente dentro,      2026-11-20T10:00:00, 2026-11-20T12:00:00, 2.0",
        "começa antes,           2026-11-20T07:00:00, 2026-11-20T09:30:00, 1.5",
        "termina depois,         2026-11-20T17:00:00, 2026-11-20T20:00:00, 1.0",
        "cobre a janela,         2026-11-20T00:00:00, 2026-11-21T00:00:00, 10.0",
        "antes da janela,        2026-11-20T05:00:00, 2026-11-20T07:00:00, 0.0",
        "adjacente ao início,    2026-11-20T06:00:00, 2026-11-20T08:00:00, 0.0",
        "adjacente ao fim,       2026-11-20T18:00:00, 2026-11-20T19:00:00, 0.0",
        "quinze minutos,         2026-11-20T10:00:00, 2026-11-20T10:15:00, 0.25"
    })
    void contaSomenteAParteDentroDaJanela(String caso, LocalDateTime inicio, LocalDateTime fim, double horas) {
        assertThat(CalculadoraHoras.horasDentro(inicio, fim, JANELA_INICIO, JANELA_FIM)).isEqualTo(horas);
    }

    @ParameterizedTest(name = "{0}h de {1}h -> {2}%")
    @CsvSource({"2.0, 24.0, 8.3", "0.0, 10.0, 0.0", "10.0, 10.0, 100.0", "1.0, 0.0, 0.0"})
    void percentualArredondadoComUmaCasa(double horas, double total, double percentual) {
        assertThat(CalculadoraHoras.percentual(horas, total)).isEqualTo(percentual);
    }
}

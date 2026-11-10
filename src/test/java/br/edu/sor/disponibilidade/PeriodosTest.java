package br.edu.sor.disponibilidade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import br.edu.sor.compartilhado.erro.EntradaInvalidaException;

/** RN-001 traduzida para mensagens ao usuário; complementa PeriodoReservaTest (Story 1.1). */
class PeriodosTest {

    private static final LocalDateTime AGORA = LocalDateTime.parse("2026-11-09T08:00:00");

    @ParameterizedTest(name = "{0} -> {3}")
    @CsvSource(delimiter = '|', nullValues = "null", value = {
        "início ausente         | null                | 2026-11-10T10:00:00 | Informe o início e o término",
        "término ausente        | 2026-11-10T10:00:00 | null                | Informe o início e o término",
        "término igual          | 2026-11-10T10:00:00 | 2026-11-10T10:00:00 | O término deve ser posterior",
        "término anterior       | 2026-11-10T12:00:00 | 2026-11-10T10:00:00 | O término deve ser posterior",
        "início no passado      | 2026-11-08T10:00:00 | 2026-11-08T12:00:00 | O início deve ser uma data e hora futura",
        "início exatamente agora| 2026-11-09T08:00:00 | 2026-11-09T09:00:00 | O início deve ser uma data e hora futura"
    })
    void recusaPeriodoInvalidoComMensagemClara(String caso, LocalDateTime inicio, LocalDateTime fim, String mensagem) {
        assertThatThrownBy(() -> Periodos.futuro(inicio, fim, AGORA))
                .isInstanceOf(EntradaInvalidaException.class)
                .hasMessageContaining(mensagem);
    }

    @Test
    void aceitaPeriodoFuturoValido() {
        PeriodoReserva periodo = Periodos.futuro(LocalDateTime.parse("2026-11-10T10:00:00"),
                LocalDateTime.parse("2026-11-10T12:00:00"), AGORA);

        assertThat(periodo.termino()).isEqualTo(LocalDateTime.parse("2026-11-10T12:00:00"));
    }
}

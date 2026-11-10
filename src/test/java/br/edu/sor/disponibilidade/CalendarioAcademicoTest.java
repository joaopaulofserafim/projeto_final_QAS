package br.edu.sor.disponibilidade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import br.edu.sor.compartilhado.erro.EntradaInvalidaException;

/** RN-031: reservas não acontecem em feriados nacionais nem nos recessos configurados pela instituição. */
class CalendarioAcademicoTest {

    private final CalendarioAcademico calendario = new CalendarioAcademico(true, "2026-07-15, 2026-07-16");

    @ParameterizedTest(name = "Páscoa de {0} = {1}")
    @CsvSource({ "2024, 2024-03-31", "2025, 2025-04-20", "2026, 2026-04-05", "2027, 2027-03-28", "2038, 2038-04-25" })
    void calculaADomingoDePascoa(int ano, LocalDate esperado) {
        assertThat(CalendarioAcademico.pascoa(ano)).isEqualTo(esperado);
    }

    @ParameterizedTest(name = "{0} é feriado ({1})")
    @CsvSource({
        "2026-01-01, Confraternização", "2026-02-16, segunda de Carnaval", "2026-02-17, terça de Carnaval",
        "2026-04-03, Sexta-feira Santa", "2026-04-21, Tiradentes", "2026-05-01, Trabalho",
        "2026-06-04, Corpus Christi", "2026-09-07, Independência", "2026-10-12, Aparecida", "2026-11-02, Finados",
        "2026-11-15, República", "2026-11-20, Consciência Negra", "2026-12-25, Natal",
        "2027-02-09, Carnaval 2027", "2027-03-26, Sexta-feira Santa 2027", "2027-05-27, Corpus Christi 2027",
        "2026-07-15, recesso da instituição"
    })
    void reconheceFeriados(LocalDate dia, String nome) {
        assertThat(calendario.ehFeriado(dia)).isTrue();
    }

    @ParameterizedTest(name = "{0} é dia letivo")
    @CsvSource({ "2026-11-19", "2026-11-23", "2026-02-18", "2026-04-04", "2026-07-17" })
    void diasComunsNaoSaoFeriados(LocalDate dia) {
        assertThat(calendario.ehFeriado(dia)).isFalse();
    }

    @Test
    void periodoQueTocaUmFeriadoEhRecusadoComMensagemClara() {
        PeriodoReserva cruzaNatal = new PeriodoReserva(LocalDateTime.parse("2026-12-24T18:00:00"),
                LocalDateTime.parse("2026-12-26T09:00:00"));

        assertThatThrownBy(() -> calendario.garantirDiasLetivos(cruzaNatal))
                .isInstanceOf(EntradaInvalidaException.class)
                .hasMessageContaining("25/12/2026 é feriado");
    }

    @Test
    void terminoAMeiaNoiteNaoContaODiaSeguinte() {
        PeriodoReserva vesperaDeFeriado = new PeriodoReserva(LocalDateTime.parse("2026-11-19T20:00:00"),
                LocalDateTime.parse("2026-11-20T00:00:00"));

        assertThatCode(() -> calendario.garantirDiasLetivos(vesperaDeFeriado)).doesNotThrowAnyException();
    }

    @Test
    void listaOsFeriadosDoAnoEmOrdem() {
        assertThat(calendario.feriadosDoAno(2026)).hasSize(15)
                .startsWith(LocalDate.parse("2026-01-01"))
                .endsWith(LocalDate.parse("2026-12-25"))
                .contains(LocalDate.parse("2026-07-15"));
    }

    @Test
    void semFeriadosNacionaisValemSoOsConfigurados() {
        CalendarioAcademico soRecesso = new CalendarioAcademico(false, "2026-12-25");

        assertThat(soRecesso.ehFeriado(LocalDate.parse("2026-11-20"))).isFalse();
        assertThat(soRecesso.ehFeriado(LocalDate.parse("2026-12-25"))).isTrue();
    }
}

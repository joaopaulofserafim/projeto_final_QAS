package br.edu.sor.disponibilidade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PeriodoReservaTest {

    @Test
    void aceitaPeriodoValidoInclusiveAtravessandoMeiaNoite() {
        var periodo = new PeriodoReserva(
                LocalDateTime.parse("2026-10-28T23:00:00"),
                LocalDateTime.parse("2026-10-29T01:00:00"));

        assertEquals(LocalDateTime.parse("2026-10-28T23:00:00"), periodo.inicio());
        assertEquals(LocalDateTime.parse("2026-10-29T01:00:00"), periodo.termino());
    }

    @Test
    void aceitaLimitesTemporaisExtremosSemCalculoDeDuracao() {
        var periodo = new PeriodoReserva(LocalDateTime.MIN, LocalDateTime.MAX);

        assertEquals(LocalDateTime.MIN, periodo.inicio());
        assertEquals(LocalDateTime.MAX, periodo.termino());
    }

    @Test
    void rejeitaInicioAusente() {
        assertThrows(NullPointerException.class,
                () -> new PeriodoReserva(null, LocalDateTime.parse("2026-10-28T12:00:00")));
    }

    @Test
    void rejeitaTerminoAusente() {
        assertThrows(NullPointerException.class,
                () -> new PeriodoReserva(LocalDateTime.parse("2026-10-28T10:00:00"), null));
    }

    @ParameterizedTest
    @MethodSource("periodosInvalidos")
    void rejeitaPeriodoNuloOuInvertido(LocalDateTime inicio, LocalDateTime termino) {
        assertThrows(IllegalArgumentException.class, () -> new PeriodoReserva(inicio, termino));
    }

    static Stream<Arguments> periodosInvalidos() {
        var limite = LocalDateTime.parse("2026-10-28T12:00:00");
        return Stream.of(
                Arguments.of(limite, limite),
                Arguments.of(limite.plusHours(1), limite));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("periodosSobrepostos")
    void identificaSobreposicaoEmQualquerOrdem(String caso, PeriodoReserva a, PeriodoReserva b) {
        assertTrue(a.sobrepoe(b), caso);
        assertTrue(b.sobrepoe(a), caso);
    }

    static Stream<Arguments> periodosSobrepostos() {
        return Stream.of(
                Arguments.of("sobreposição parcial",
                        periodo("2026-10-28T10:00:00", "2026-10-28T12:00:00"),
                        periodo("2026-10-28T11:00:00", "2026-10-28T13:00:00")),
                Arguments.of("períodos idênticos",
                        periodo("2026-10-28T10:00:00", "2026-10-28T12:00:00"),
                        periodo("2026-10-28T10:00:00", "2026-10-28T12:00:00")),
                Arguments.of("B contido em A",
                        periodo("2026-10-28T10:00:00", "2026-10-28T14:00:00"),
                        periodo("2026-10-28T11:00:00", "2026-10-28T12:00:00")),
                Arguments.of("A contido em B",
                        periodo("2026-10-28T11:00:00", "2026-10-28T12:00:00"),
                        periodo("2026-10-28T10:00:00", "2026-10-28T14:00:00")),
                Arguments.of("mesmo início",
                        periodo("2026-10-28T10:00:00", "2026-10-28T12:00:00"),
                        periodo("2026-10-28T10:00:00", "2026-10-28T13:00:00")),
                Arguments.of("mesmo término",
                        periodo("2026-10-28T10:00:00", "2026-10-28T12:00:00"),
                        periodo("2026-10-28T11:00:00", "2026-10-28T12:00:00")),
                Arguments.of("sobreposição de um nanossegundo",
                        periodo("2026-10-28T10:00:00.000000001", "2026-10-28T10:00:00.000000003"),
                        periodo("2026-10-28T10:00:00.000000002", "2026-10-28T10:00:00.000000004")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("periodosNaoSobrepostos")
    void naoIdentificaAdjacenciaOuSeparacaoEmQualquerOrdem(String caso, PeriodoReserva a, PeriodoReserva b) {
        assertFalse(a.sobrepoe(b), caso);
        assertFalse(b.sobrepoe(a), caso);
    }

    static Stream<Arguments> periodosNaoSobrepostos() {
        return Stream.of(
                Arguments.of("adjacentes na ordem normal",
                        periodo("2026-10-28T10:00:00", "2026-10-28T12:00:00"),
                        periodo("2026-10-28T12:00:00", "2026-10-28T14:00:00")),
                Arguments.of("adjacentes na ordem inversa",
                        periodo("2026-10-28T12:00:00", "2026-10-28T14:00:00"),
                        periodo("2026-10-28T10:00:00", "2026-10-28T12:00:00")),
                Arguments.of("períodos separados",
                        periodo("2026-10-28T10:00:00", "2026-10-28T12:00:00"),
                        periodo("2026-10-28T13:00:00", "2026-10-28T15:00:00")),
                Arguments.of("adjacência com precisão de nanossegundos",
                        periodo("2026-10-28T10:00:00.000000001", "2026-10-28T10:00:00.000000002"),
                        periodo("2026-10-28T10:00:00.000000002", "2026-10-28T10:00:00.000000003")));
    }

    @Test
    void rejeitaOutroPeriodoAusenteAoComparar() {
        var periodo = periodo("2026-10-28T10:00:00", "2026-10-28T12:00:00");

        assertThrows(NullPointerException.class, () -> periodo.sobrepoe(null));
    }

    private static PeriodoReserva periodo(String inicio, String termino) {
        return new PeriodoReserva(LocalDateTime.parse(inicio), LocalDateTime.parse(termino));
    }
}

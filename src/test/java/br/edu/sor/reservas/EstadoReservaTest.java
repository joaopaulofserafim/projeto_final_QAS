package br.edu.sor.reservas;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import static br.edu.sor.reservas.EstadoReserva.*;

/** Tabela completa do modelo de estados (7 x 7 = 49 combinações) — PRD/arquitetura/uml/ESTADOS_RESERVA.md. */
class EstadoReservaTest {

    private static final Map<EstadoReserva, Set<EstadoReserva>> PERMITIDAS = Map.of(
            SOLICITADA, Set.of(APROVADA, REJEITADA, CANCELADA),
            APROVADA, Set.of(EM_USO, CANCELADA, NAO_COMPARECEU),
            EM_USO, Set.of(CONCLUIDA),
            CONCLUIDA, Set.of(),
            REJEITADA, Set.of(),
            CANCELADA, Set.of(),
            NAO_COMPARECEU, Set.of());

    static Stream<Arguments> todosOsPares() {
        return Stream.of(EstadoReserva.values())
                .flatMap(origem -> Stream.of(EstadoReserva.values()).map(destino -> Arguments.of(origem, destino)));
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @MethodSource("todosOsPares")
    void somenteTransicoesDoModeloSaoPermitidas(EstadoReserva origem, EstadoReserva destino) {
        assertThat(origem.podeIrPara(destino)).isEqualTo(PERMITIDAS.get(origem).contains(destino));
    }

    @ParameterizedTest
    @EnumSource(value = EstadoReserva.class, names = {"SOLICITADA", "APROVADA", "EM_USO"})
    void estadosAtivosOcupamHorario(EstadoReserva estado) {
        assertThat(estado.ocupaHorario()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = EstadoReserva.class, names = {"CONCLUIDA", "REJEITADA", "CANCELADA", "NAO_COMPARECEU"})
    void estadosEncerradosLiberamHorarioESaoTerminais(EstadoReserva estado) {
        assertThat(estado.ocupaHorario()).isFalse();
        assertThat(estado.destinos()).isEmpty();
        assertThat(estado.editavel()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = EstadoReserva.class, names = {"SOLICITADA", "APROVADA"})
    void somenteReservasNaoIniciadasSaoEditaveis(EstadoReserva estado) {
        assertThat(estado.editavel()).isTrue();
        assertThat(EM_USO.editavel()).isFalse();
    }
}

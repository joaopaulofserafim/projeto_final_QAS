package br.edu.sor.suporte;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** Relógio dos testes: permite simular "agora" (ex.: reserva já iniciada, não comparecimento, atraso). */
public class RelogioControlavel extends Clock {

    public static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    public static final LocalDateTime AGORA_PADRAO = LocalDateTime.of(2026, 11, 9, 8, 0);

    private volatile Instant instante = AGORA_PADRAO.atZone(FUSO).toInstant();

    public void definir(LocalDateTime agora) {
        this.instante = agora.atZone(FUSO).toInstant();
    }

    public void avancar(Duration duracao) {
        this.instante = instante.plus(duracao);
    }

    @Override
    public ZoneId getZone() {
        return FUSO;
    }

    @Override
    public Clock withZone(ZoneId zona) {
        return Clock.fixed(instante, zona);
    }

    @Override
    public Instant instant() {
        return instante;
    }
}

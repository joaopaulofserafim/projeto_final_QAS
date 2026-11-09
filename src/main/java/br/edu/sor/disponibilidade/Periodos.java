package br.edu.sor.disponibilidade;

import java.time.LocalDateTime;

import br.edu.sor.compartilhado.erro.EntradaInvalidaException;

/** Valida período vindo do usuário e traduz a regra RN-001 para uma mensagem compreensível. */
public final class Periodos {

    private Periodos() {
    }

    public static PeriodoReserva de(LocalDateTime inicio, LocalDateTime fim) {
        if (inicio == null || fim == null) {
            throw new EntradaInvalidaException("Informe o início e o término do período.");
        }
        if (!fim.isAfter(inicio)) {
            throw new EntradaInvalidaException("O término deve ser posterior ao início.");
        }
        return new PeriodoReserva(inicio, fim);
    }

    /** Novas reservas e alterações só podem começar no futuro (DP-015). */
    public static PeriodoReserva futuro(LocalDateTime inicio, LocalDateTime fim, LocalDateTime agora) {
        PeriodoReserva periodo = de(inicio, fim);
        if (!periodo.inicio().isAfter(agora)) {
            throw new EntradaInvalidaException("O início deve ser uma data e hora futura.");
        }
        return periodo;
    }
}

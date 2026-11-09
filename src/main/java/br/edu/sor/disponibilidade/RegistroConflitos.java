package br.edu.sor.disponibilidade;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra tentativas recusadas em transação própria: a transação da reserva foi desfeita,
 * mas o fato "um conflito foi evitado" precisa permanecer (DP-010).
 */
@Service
public class RegistroConflitos {

    private final ConflitoEvitadoRepository conflitos;
    private final Clock relogio;

    public RegistroConflitos(ConflitoEvitadoRepository conflitos, Clock relogio) {
        this.conflitos = conflitos;
        this.relogio = relogio;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long solicitanteId, Collection<Long> recursoIds, LocalDateTime inicio, LocalDateTime fim,
            String motivo) {
        LocalDateTime agora = LocalDateTime.now(relogio);
        for (Long recursoId : recursoIds) {
            conflitos.save(new ConflitoEvitado(recursoId, solicitanteId, inicio, fim, motivo, agora));
        }
    }
}

package br.edu.sor.disponibilidade;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import br.edu.sor.compartilhado.erro.ConflitoDetalhe;

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

    /** Um registro por recurso em conflito (o primeiro motivo encontrado para o recurso). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(Long solicitanteId, List<ConflitoDetalhe> detalhes, LocalDateTime inicio, LocalDateTime fim) {
        LocalDateTime agora = LocalDateTime.now(relogio);
        Map<Long, String> porRecurso = new LinkedHashMap<>();
        detalhes.forEach(d -> porRecurso.putIfAbsent(d.recursoId(), d.motivo()));
        porRecurso.forEach((recursoId, motivo) ->
                conflitos.save(new ConflitoEvitado(recursoId, solicitanteId, inicio, fim, motivo, agora)));
    }
}

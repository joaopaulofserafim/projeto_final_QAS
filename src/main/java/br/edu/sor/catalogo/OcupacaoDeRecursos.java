package br.edu.sor.catalogo;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

/**
 * Porta consultada pelo catálogo e implementada pelo módulo de disponibilidade.
 * Mantém a direção de dependência catalogo &lt;- disponibilidade (ADR-002).
 */
public interface OcupacaoDeRecursos {

    boolean possuiReservaAtivaApos(Long recursoId, LocalDateTime instante);

    Set<Long> ocupadosNoPeriodo(Collection<Long> recursoIds, LocalDateTime inicio, LocalDateTime fim);
}

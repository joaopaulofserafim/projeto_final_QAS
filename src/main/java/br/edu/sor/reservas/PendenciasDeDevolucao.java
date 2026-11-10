package br.edu.sor.reservas;

/**
 * Porta consultada antes de concluir uma reserva; implementada pelo módulo de movimentação
 * (mantém a direção reservas &lt;- movimentacao, ADR-002).
 */
public interface PendenciasDeDevolucao {

    boolean possuiRetiradaAberta(Long reservaId);
}

package br.edu.sor.movimentacao;

import org.springframework.stereotype.Component;

import br.edu.sor.reservas.PendenciasDeDevolucao;

/** Implementa a porta do módulo de reservas sem depender dele em tempo de construção (evita ciclo de beans). */
@Component
class PendenciasDeDevolucaoBanco implements PendenciasDeDevolucao {

    private final MovimentacaoRepository movimentacoes;

    PendenciasDeDevolucaoBanco(MovimentacaoRepository movimentacoes) {
        this.movimentacoes = movimentacoes;
    }

    @Override
    public boolean possuiRetiradaAberta(Long reservaId) {
        return movimentacoes.existsByReservaIdAndDevolvidaEmIsNull(reservaId);
    }
}

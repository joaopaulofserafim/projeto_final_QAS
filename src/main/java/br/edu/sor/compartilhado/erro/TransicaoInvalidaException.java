package br.edu.sor.compartilhado.erro;

/** A reserva não está em um estado que permita a ação pedida. */
public class TransicaoInvalidaException extends RegraNegocioException {

    public TransicaoInvalidaException(String mensagem) {
        super("TRANSICAO_INVALIDA", mensagem);
    }
}

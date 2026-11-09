package br.edu.sor.compartilhado.erro;

/** Dados enviados pelo usuário violam uma regra de validação (HTTP 400). */
public class EntradaInvalidaException extends RuntimeException {

    public EntradaInvalidaException(String mensagem) {
        super(mensagem);
    }
}

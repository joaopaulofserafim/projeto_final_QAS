package br.edu.sor.compartilhado.erro;

/** Registro solicitado não existe (HTTP 404). */
public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}

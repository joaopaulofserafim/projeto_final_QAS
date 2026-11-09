package br.edu.sor.compartilhado.erro;

/** Usuário autenticado tenta agir sobre algo que não lhe pertence ou fora do seu perfil (HTTP 403). */
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}

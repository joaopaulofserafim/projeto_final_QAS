package br.edu.sor.compartilhado.erro;

import java.util.List;

/** O horário pedido se sobrepõe a uma ocupação ativa de algum recurso. */
public class ConflitoHorarioException extends RegraNegocioException {

    private final transient List<ConflitoDetalhe> conflitos;

    public ConflitoHorarioException(String mensagem, List<ConflitoDetalhe> conflitos) {
        super("CONFLITO_HORARIO", mensagem);
        this.conflitos = List.copyOf(conflitos);
    }

    public List<ConflitoDetalhe> getConflitos() {
        return conflitos;
    }
}

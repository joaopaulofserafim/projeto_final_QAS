package br.edu.sor.notificacao;

/** Fronteira com o provedor externo de notificações (ADR-007). Lança exceção em qualquer falha de entrega. */
public interface NotificacaoGateway {

    void enviar(MensagemExterna mensagem);
}

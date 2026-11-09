package br.edu.sor.notificacao;

/** Corpo enviado à API externa: só o necessário para a entrega (minimização de dados, RNF-002). */
public record MensagemExterna(String destinatario, String email, String assunto, String mensagem, String referencia) {
}

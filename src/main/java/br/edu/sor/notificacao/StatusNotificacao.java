package br.edu.sor.notificacao;

public enum StatusNotificacao {
    /** Modo simulado: registrada e consultável, sem chamada externa. */
    SIMULADA,
    /** Aceita pela API externa. */
    ENVIADA,
    /** A API externa falhou ou demorou além do limite; a operação de negócio não foi afetada. */
    FALHA
}

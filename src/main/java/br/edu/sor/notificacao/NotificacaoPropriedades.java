package br.edu.sor.notificacao;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuração em application.yml (sor.notificacao.*). */
@ConfigurationProperties("sor.notificacao")
public record NotificacaoPropriedades(Modo modo, URI url, Duration timeout) {

    public enum Modo {
        SIMULADO,
        HTTP
    }

    public NotificacaoPropriedades {
        modo = modo == null ? Modo.SIMULADO : modo;
        timeout = timeout == null ? Duration.ofSeconds(2) : timeout;
    }
}

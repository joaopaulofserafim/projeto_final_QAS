package br.edu.sor.notificacao;

import java.net.http.HttpClient;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP da API de notificações, com timeout de conexão e de leitura.
 * Usa HTTP/1.1: a tentativa de HTTP/2 em texto puro (h2c) do cliente do JDK foi cancelada pelo servidor
 * (RST_STREAM) no teste com WireMock — defeito DEF-009.
 */
@Component
class HttpNotificacaoGateway implements NotificacaoGateway {

    private final RestClient cliente;

    HttpNotificacaoGateway(NotificacaoPropriedades propriedades, RestClient.Builder construtor) {
        HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(propriedades.timeout()).build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(http);
        fabrica.setReadTimeout(propriedades.timeout());
        this.cliente = construtor.baseUrl(propriedades.url().toString()).requestFactory(fabrica).build();
    }

    @Override
    public void enviar(MensagemExterna mensagem) {
        cliente.post().uri("/notificacoes").contentType(MediaType.APPLICATION_JSON).body(mensagem)
                .retrieve().toBodilessEntity();
    }
}

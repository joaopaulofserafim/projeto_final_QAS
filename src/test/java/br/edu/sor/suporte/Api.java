package br.edu.sor.suporte;

import java.util.List;
import java.util.Map;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Cliente HTTP caixa-preta: fala com a aplicação real (porta aleatória) só por JSON, como um cliente externo.
 * Não usa classes internas da aplicação.
 */
public class Api {

    public static final String SENHA = "Senha@123";

    private final TestRestTemplate http;
    private final String usuario;
    private final String senha;

    public Api(TestRestTemplate http) {
        this(http, null, null);
    }

    private Api(TestRestTemplate http, String usuario, String senha) {
        this.http = http;
        this.usuario = usuario;
        this.senha = senha;
    }

    public Api como(String login) {
        return new Api(http, login, SENHA);
    }

    public Api como(String login, String outraSenha) {
        return new Api(http, login, outraSenha);
    }

    public Api anonimo() {
        return new Api(http);
    }

    public ResponseEntity<JsonNode> get(String caminho) {
        return trocar(HttpMethod.GET, caminho, null);
    }

    public ResponseEntity<JsonNode> post(String caminho, Object corpo) {
        return trocar(HttpMethod.POST, caminho, corpo);
    }

    public ResponseEntity<JsonNode> put(String caminho, Object corpo) {
        return trocar(HttpMethod.PUT, caminho, corpo);
    }

    public ResponseEntity<JsonNode> delete(String caminho) {
        return trocar(HttpMethod.DELETE, caminho, null);
    }

    public ResponseEntity<JsonNode> trocar(HttpMethod metodo, String caminho, Object corpo) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.APPLICATION_PROBLEM_JSON));
        if (corpo != null) {
            cabecalhos.setContentType(MediaType.APPLICATION_JSON);
        }
        if (usuario != null) {
            cabecalhos.setBasicAuth(usuario, senha);
        }
        return http.exchange(caminho, metodo, new HttpEntity<>(corpo, cabecalhos), JsonNode.class);
    }

    /** Cria um recurso como Administrador e devolve o id. */
    public long criarRecurso(String codigo, String tipo, boolean restrito) {
        Integer capacidade = "SALA".equals(tipo) ? 30 : null;
        Map<String, Object> corpo = new java.util.HashMap<>();
        corpo.put("codigo", codigo);
        corpo.put("nome", "Recurso " + codigo);
        corpo.put("tipo", tipo);
        corpo.put("capacidade", capacidade);
        corpo.put("localizacao", "Bloco Teste");
        corpo.put("restrito", restrito);
        ResponseEntity<JsonNode> resposta = como("admin").post("/api/v1/recursos", corpo);
        if (resposta.getStatusCode().value() != 201) {
            throw new IllegalStateException("Falha ao criar recurso: " + resposta.getBody());
        }
        return resposta.getBody().get("id").asLong();
    }

    public static Map<String, Object> pedidoReserva(String inicio, String fim, long... recursoIds) {
        return Map.of(
                "inicio", inicio,
                "fim", fim,
                "finalidade", "Aula de teste",
                "justificativa", "Atividade prevista no plano de ensino",
                "recursoIds", java.util.Arrays.stream(recursoIds).boxed().toList());
    }
}

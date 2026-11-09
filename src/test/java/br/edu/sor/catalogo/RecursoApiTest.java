package br.edu.sor.catalogo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.sor.suporte.IntegracaoBase;

/** RF-004, RF-005, RF-023..RF-025: cadastro e pesquisa de salas, professores, materiais e equipamentos. */
class RecursoApiTest extends IntegracaoBase {

    private static Map<String, Object> recurso(String codigo, String nome, String tipo, Integer capacidade,
            String localizacao, boolean restrito, Set<String> competencias) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("codigo", codigo);
        corpo.put("nome", nome);
        corpo.put("tipo", tipo);
        corpo.put("capacidade", capacidade);
        corpo.put("localizacao", localizacao);
        corpo.put("restrito", restrito);
        corpo.put("competencias", competencias);
        return corpo;
    }

    @BeforeEach
    void catalogo() {
        api.como("admin").post("/api/v1/recursos", recurso("SALA-101", "Sala 101", "SALA", 40, "Bloco A", false, null));
        api.como("admin").post("/api/v1/recursos", recurso("SALA-202", "Sala 202", "SALA", 20, "Bloco B", false, null));
        api.como("admin").post("/api/v1/recursos", recurso("LAB-1", "Laboratório", "SALA", 30, "Bloco B", true, null));
        api.como("admin").post("/api/v1/recursos",
                recurso("PROF-ANA", "Profa. Ana", "PROFESSOR", null, "Computação", false, Set.of("Java", "Testes")));
        api.como("admin").post("/api/v1/recursos",
                recurso("PROF-RUI", "Prof. Rui", "PROFESSOR", null, "Computação", false, Set.of("Redes")));
        api.como("admin").post("/api/v1/recursos", recurso("PROJ-1", "Projetor", "EQUIPAMENTO", null, "Almox", false, null));
        api.como("admin").post("/api/v1/recursos", recurso("KIT-1", "Kit Arduino", "MATERIAL", null, "Almox", true, null));
    }

    @ParameterizedTest(name = "filtro {0} encontra {1}")
    @CsvSource(delimiter = '|', value = {
        "tipo=SALA                       | SALA-101,SALA-202,LAB-1",
        "tipo=SALA&capacidadeMinima=30   | SALA-101,LAB-1",
        "localizacao=bloco b             | SALA-202,LAB-1",
        "competencia=java                | PROF-ANA",
        "tipo=PROFESSOR&competencia=Redes| PROF-RUI",
        "texto=projet                    | PROJ-1",
        "restrito=true                   | LAB-1,KIT-1",
        "tipo=MATERIAL                   | KIT-1"
    })
    void pesquisaCombinaFiltros(String filtro, String esperados) {
        ResponseEntity<JsonNode> resposta = api.como("solicitante").get("/api/v1/recursos?" + filtro.trim());

        assertThat(resposta.getStatusCode().value()).isEqualTo(200);
        assertThat(codigos(resposta.getBody())).containsExactlyInAnyOrder(esperados.trim().split(","));
    }

    @Test
    void detalhaRecursoComCompetencias() {
        long id = idDe("PROF-ANA");

        JsonNode detalhe = api.como("responsavel").get("/api/v1/recursos/" + id).getBody();

        assertThat(detalhe.get("tipo").asText()).isEqualTo("PROFESSOR");
        assertThat(detalhe.get("competencias").toString()).contains("Java", "Testes");
    }

    @Test
    void recursoInexistenteRetorna404() {
        assertThat(api.como("solicitante").get("/api/v1/recursos/9999").getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void codigoDuplicadoEhRecusadoMesmoComCaixaDiferente() {
        ResponseEntity<JsonNode> resposta = api.como("admin").post("/api/v1/recursos",
                recurso("sala-101", "Outra", "SALA", 10, null, false, null));

        assertThat(resposta.getStatusCode().value()).isEqualTo(409);
        assertThat(resposta.getBody().get("codigo").asText()).isEqualTo("CODIGO_DUPLICADO");
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
        "sala sem capacidade          | SALA      |      | Informe a capacidade",
        "competência em equipamento   | EQUIPAMENTO|     | Competências só se aplicam",
        "capacidade negativa          | SALA      | -5   | Corrija os campos"
    })
    void validacoesDeCadastroGeramMensagensCompreensiveis(String caso, String tipo, Integer capacidade, String mensagem) {
        Set<String> competencias = caso.startsWith("competência") ? Set.of("Java") : null;

        ResponseEntity<JsonNode> resposta = api.como("admin").post("/api/v1/recursos",
                recurso("NOVO-1", "Novo", tipo.trim(), capacidade, null, false, competencias));

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
        assertThat(resposta.getBody().get("detail").asText()).contains(mensagem.trim());
    }

    @Test
    void alteraDadosMasNaoOTipo() {
        long id = idDe("SALA-101");

        ResponseEntity<JsonNode> alterado = api.como("admin").put("/api/v1/recursos/" + id,
                recurso("SALA-101", "Sala 101 reformada", "SALA", 45, "Bloco A", true, null));
        ResponseEntity<JsonNode> trocaTipo = api.como("admin").put("/api/v1/recursos/" + id,
                recurso("SALA-101", "Sala", "MATERIAL", null, null, false, null));

        assertThat(alterado.getStatusCode().value()).isEqualTo(200);
        assertThat(alterado.getBody().get("restrito").asBoolean()).isTrue();
        assertThat(alterado.getBody().get("capacidade").asInt()).isEqualTo(45);
        assertThat(trocaTipo.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void desativarRetiraDaPesquisaSemApagarEPodeSerRevertido() {
        long id = idDe("SALA-202");

        assertThat(api.como("admin").post("/api/v1/recursos/" + id + "/desativar", null).getBody().get("ativo")
                .asBoolean()).isFalse();
        assertThat(codigos(api.como("solicitante").get("/api/v1/recursos?tipo=SALA").getBody()))
                .doesNotContain("SALA-202");
        assertThat(codigos(api.como("admin").get("/api/v1/recursos?tipo=SALA&incluirInativos=true").getBody()))
                .contains("SALA-202");
        assertThat(codigos(api.como("solicitante").get("/api/v1/recursos?tipo=SALA&incluirInativos=true").getBody()))
                .doesNotContain("SALA-202");

        api.como("admin").post("/api/v1/recursos/" + id + "/desativar", null);
        assertThat(api.como("admin").post("/api/v1/recursos/" + id + "/reativar", null).getBody().get("ativo")
                .asBoolean()).isTrue();
        api.como("admin").post("/api/v1/recursos/" + id + "/reativar", null);
        assertThat(contar("select count(*) from evento_auditoria where entidade = 'RECURSO' and entidade_id = ?", id))
                .isEqualTo(3);
    }

    @Test
    void filtroDeDisponibilidadeExigePeriodoCompleto() {
        ResponseEntity<JsonNode> resposta = api.como("solicitante")
                .get("/api/v1/recursos?disponivelInicio=2026-11-20T10:00:00");

        assertThat(resposta.getStatusCode().value()).isEqualTo(400);
    }

    private long idDe(String codigo) {
        return contar("select id from recurso where codigo = ?", codigo);
    }

    static List<String> codigos(JsonNode lista) {
        return java.util.stream.StreamSupport.stream(lista.spliterator(), false)
                .map(n -> n.get("codigo").asText())
                .toList();
    }
}

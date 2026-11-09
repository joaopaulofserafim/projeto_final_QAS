package br.edu.sor.movimentacao;

import static br.edu.sor.suporte.Api.pedidoReserva;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;

import br.edu.sor.suporte.IntegracaoBase;

/** Story 1.7 — RF-019, RF-020, RF-021, RN-020, RN-021, RN-030: retirada e devolução de materiais e equipamentos. */
class MovimentacaoApiTest extends IntegracaoBase {

    private long projetor;
    private long kit;
    private long sala;

    @BeforeEach
    void recursos() {
        projetor = api.criarRecurso("PROJ-1", "EQUIPAMENTO", false);
        kit = api.criarRecurso("KIT-1", "MATERIAL", false);
        sala = api.criarRecurso("SALA-1", "SALA", false);
    }

    private long reservar(long... recursos) {
        ResponseEntity<JsonNode> r = api.como("solicitante").post("/api/v1/reservas",
                pedidoReserva("2026-11-20T10:00:00", "2026-11-20T12:00:00", recursos));
        assertThat(r.getStatusCode().value()).isEqualTo(201);
        return r.getBody().get("id").asLong();
    }

    private ResponseEntity<JsonNode> retirar(long reserva, long recurso) {
        return api.como("responsavel").post("/api/v1/movimentacoes/retiradas",
                Map.of("reservaId", reserva, "recursoId", recurso, "observacao", "Retirado no balcão"));
    }

    private ResponseEntity<JsonNode> devolver(long movimentacao, boolean avaria) {
        return api.como("responsavel").post("/api/v1/movimentacoes/" + movimentacao + "/devolucao",
                Map.of("observacao", avaria ? "Lente riscada" : "Ok", "avaria", avaria));
    }

    private String estado(long reserva) {
        return api.como("admin").get("/api/v1/reservas/" + reserva).getBody().get("estado").asText();
    }

    @Test
    void retiradaIniciaOUsoEDevolucaoDoUltimoMaterialConcluiAReserva() {
        long reserva = reservar(projetor, kit);

        ResponseEntity<JsonNode> retirada = retirar(reserva, projetor);
        long mov2 = retirar(reserva, kit).getBody().get("id").asLong();
        JsonNode abertas = api.como("responsavel").get("/api/v1/movimentacoes?situacao=ABERTAS").getBody();

        assertThat(retirada.getStatusCode().value()).isEqualTo(201);
        assertThat(retirada.getBody().get("prazoDevolucao").asText()).isEqualTo("2026-11-20T12:00:00");
        assertThat(estado(reserva)).isEqualTo("EM_USO");
        assertThat(abertas).hasSize(2);

        devolver(retirada.getBody().get("id").asLong(), false);
        assertThat(estado(reserva)).isEqualTo("EM_USO");
        devolver(mov2, false);
        assertThat(estado(reserva)).isEqualTo("CONCLUIDA");
        assertThat(api.como("responsavel").get("/api/v1/movimentacoes?situacao=ABERTAS").getBody()).isEmpty();
        assertThat(api.como("responsavel").get("/api/v1/movimentacoes?reservaId=" + reserva).getBody()).hasSize(2);
        assertThat(contar("select count(*) from evento_auditoria where entidade = 'MOVIMENTACAO'")).isEqualTo(4);
    }

    @Test
    void reservaComMaterialPendenteNaoPodeSerConcluida() {
        long reserva = reservar(sala, projetor);
        long mov = retirar(reserva, projetor).getBody().get("id").asLong();

        ResponseEntity<JsonNode> pendente = api.como("responsavel").post("/api/v1/reservas/" + reserva + "/concluir", null);
        devolver(mov, false);

        assertThat(pendente.getStatusCode().value()).isEqualTo(409);
        assertThat(pendente.getBody().get("codigo").asText()).isEqualTo("MATERIAL_PENDENTE");
        assertThat(estado(reserva)).as("reserva com sala é concluída manualmente").isEqualTo("EM_USO");
        assertThat(api.como("responsavel").post("/api/v1/reservas/" + reserva + "/concluir", null).getBody()
                .get("estado").asText()).isEqualTo("CONCLUIDA");
    }

    @Test
    void retiradaEDevolucaoDuplicadasSaoRecusadas() {
        long reserva = reservar(projetor);
        long mov = retirar(reserva, projetor).getBody().get("id").asLong();

        assertThat(retirar(reserva, projetor).getStatusCode().value()).isEqualTo(409);
        devolver(mov, false);
        ResponseEntity<JsonNode> segunda = devolver(mov, false);
        assertThat(segunda.getStatusCode().value()).isEqualTo(409);
        assertThat(segunda.getBody().get("detail").asText()).contains("já foi registrada");
    }

    @Test
    void soMateriaisEEquipamentosDaPropriaReservaPodemSerRetirados() {
        long reserva = reservar(sala, kit);

        ResponseEntity<JsonNode> salaRetirada = retirar(reserva, sala);
        ResponseEntity<JsonNode> foraDaReserva = retirar(reserva, projetor);

        assertThat(salaRetirada.getStatusCode().value()).isEqualTo(400);
        assertThat(salaRetirada.getBody().get("detail").asText()).contains("materiais e equipamentos");
        assertThat(foraDaReserva.getStatusCode().value()).isEqualTo(400);
        assertThat(foraDaReserva.getBody().get("detail").asText()).contains("não faz parte da reserva");
    }

    @Test
    void retiradaExigeReservaAprovadaOuEmUso() {
        long restrito = api.criarRecurso("NOTE-1", "EQUIPAMENTO", true);
        long solicitada = reservar(restrito);
        long cancelada = reservar(kit);
        api.como("solicitante").post("/api/v1/reservas/" + cancelada + "/cancelar", null);

        assertThat(retirar(solicitada, restrito).getStatusCode().value()).isEqualTo(409);
        assertThat(retirar(cancelada, kit).getStatusCode().value()).isEqualTo(409);
        assertThat(retirar(9999, kit).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void devolucaoAposOPrazoEhMarcadaComoAtrasadaERegistraAvaria() {
        long reserva = reservar(projetor);
        long mov = retirar(reserva, projetor).getBody().get("id").asLong();
        relogio.definir(LocalDateTime.parse("2026-11-20T13:00:00"));

        JsonNode pendentes = api.como("responsavel").get("/api/v1/movimentacoes?situacao=ABERTAS").getBody();
        JsonNode devolvida = devolver(mov, true).getBody();

        assertThat(pendentes.get(0).get("atrasada").asBoolean()).isTrue();
        assertThat(devolvida.get("atrasada").asBoolean()).isTrue();
        assertThat(devolvida.get("avaria").asBoolean()).isTrue();
        assertThat(devolvida.get("recebidoPor").asText()).isEqualTo("Rita Responsável");
        assertThat(api.como("responsavel").post("/api/v1/movimentacoes/999/devolucao", Map.of()).getStatusCode().value())
                .isEqualTo(404);
    }
}

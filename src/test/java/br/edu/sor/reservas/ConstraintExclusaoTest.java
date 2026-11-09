package br.edu.sor.reservas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import br.edu.sor.compartilhado.erro.ViolacaoExclusao;
import br.edu.sor.suporte.IntegracaoBase;

/**
 * Defesa em profundidade (ADR-004): mesmo escrevendo direto no banco, sem passar pelo lock da aplicação,
 * o PostgreSQL recusa duas ocupações ativas sobrepostas do mesmo recurso.
 */
class ConstraintExclusaoTest extends IntegracaoBase {

    private long recurso;
    private long reserva;

    @BeforeEach
    void dados() {
        recurso = api.criarRecurso("SALA-DB", "SALA", false);
        long usuario = contar("select id from usuario where username = 'solicitante'");
        reserva = jdbc.queryForObject("""
                insert into reserva (solicitante_id, inicio, fim, estado, finalidade, criada_em, atualizada_em)
                values (?, '2026-12-01 10:00', '2026-12-01 12:00', 'APROVADA', 'teste', now(), now()) returning id
                """, Long.class, usuario);
    }

    private void ocupar(String inicio, String fim, boolean ativa) {
        jdbc.update("insert into ocupacao (recurso_id, reserva_id, tipo, inicio, fim, ativa) values (?, ?, 'RESERVA', "
                + "cast(? as timestamp), cast(? as timestamp), ?)", recurso, reserva, inicio, fim, ativa);
    }

    @Test
    void bancoRecusaOcupacoesAtivasSobrepostas() {
        ocupar("2026-12-01 10:00", "2026-12-01 12:00", true);

        assertThatThrownBy(() -> ocupar("2026-12-01 11:00", "2026-12-01 13:00", true))
                .isInstanceOf(DataIntegrityViolationException.class)
                .satisfies(e -> assertThat(ViolacaoExclusao.causou(e)).isTrue());
    }

    @Test
    void bancoAceitaOcupacoesAdjacentesEOcupacoesInativasSobrepostas() {
        ocupar("2026-12-01 10:00", "2026-12-01 12:00", true);
        ocupar("2026-12-01 12:00", "2026-12-01 14:00", true);
        ocupar("2026-12-01 10:30", "2026-12-01 11:30", false);

        assertThat(contar("select count(*) from ocupacao where recurso_id = ?", recurso)).isEqualTo(3);
    }

    @Test
    void bancoRecusaPeriodoInvertido() {
        assertThatThrownBy(() -> ocupar("2026-12-01 12:00", "2026-12-01 10:00", true))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

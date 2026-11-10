package br.edu.sor.compartilhado.erro;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;

/** Teste unitário dos caminhos de erro difíceis de provocar pela API. */
class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void erroInesperadoNaoVazaDetalhesEInformaCodigoDeRastreio() {
        ProblemDetail p = handler.erroInesperado(new IllegalStateException("senha do banco: segredo"));

        assertThat(p.getStatus()).isEqualTo(500);
        assertThat(p.getDetail()).doesNotContain("segredo");
        assertThat(p.getProperties()).containsKey("rastreio");
    }

    @Test
    void violacaoDaConstraintDeExclusaoViraConflitoDeHorario() {
        var causa = new SQLException("conflicting key value violates exclusion constraint", "23P01");
        ProblemDetail p = handler.integridade(new DataIntegrityViolationException("x", causa));

        assertThat(p.getStatus()).isEqualTo(409);
        assertThat(p.getProperties()).containsEntry("codigo", "CONFLITO_HORARIO");
    }

    @Test
    void outraViolacaoDeIntegridadeViraConflitoGenerico() {
        ProblemDetail p = handler.integridade(new DataIntegrityViolationException("x", new SQLException("dup", "23505")));

        assertThat(p.getProperties()).containsEntry("codigo", "INTEGRIDADE");
    }

    @Test
    void edicaoConcorrenteViraConflito() {
        ProblemDetail p = handler.edicaoConcorrente(new ObjectOptimisticLockingFailureException(Object.class, 1L));

        assertThat(p.getStatus()).isEqualTo(409);
        assertThat(p.getProperties()).containsEntry("codigo", "EDICAO_CONCORRENTE");
    }

    @Test
    void acessoNegadoUsaMensagemDoDominioOuGenerica() {
        assertThat(handler.acessoNegado(new AcessoNegadoException("Reserva de outra pessoa.")).getDetail())
                .isEqualTo("Reserva de outra pessoa.");
        assertThat(handler.acessoNegado(new AccessDeniedException("x")).getDetail())
                .isEqualTo("Seu perfil não permite esta operação.");
    }
}

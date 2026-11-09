package br.edu.sor.reservas;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import br.edu.sor.compartilhado.erro.ConflitoDetalhe;
import br.edu.sor.compartilhado.erro.ConflitoHorarioException;
import br.edu.sor.compartilhado.erro.ViolacaoExclusao;
import br.edu.sor.auditoria.EventoAuditoria;
import br.edu.sor.catalogo.Recurso;
import br.edu.sor.disponibilidade.RegistroConflitos;
import br.edu.sor.identidade.UsuarioAutenticado;

/**
 * Fachada do módulo de reservas. Fica fora da transação para poder registrar o conflito evitado
 * depois que a transação recusada foi desfeita.
 */
@Service
public class ReservaService {

    static final String CONCORRENCIA = "CONCORRENCIA";

    private final ReservaTransacoes transacoes;
    private final RegistroConflitos conflitos;

    ReservaService(ReservaTransacoes transacoes, RegistroConflitos conflitos) {
        this.transacoes = transacoes;
        this.conflitos = conflitos;
    }

    public ReservaVisao criar(NovaReserva pedido, UsuarioAutenticado ator) {
        try {
            return transacoes.criar(pedido, ator);
        } catch (ConflitoHorarioException e) {
            conflitos.registrar(ator.id(), e.getConflitos(), pedido.inicio(), pedido.fim());
            throw e;
        } catch (DataIntegrityViolationException e) {
            throw conflitoConcorrente(e, ator, pedido.recursoIds(), pedido.inicio(), pedido.fim());
        }
    }

    public ReservaVisao alterar(Long id, AlteracaoReserva dados, UsuarioAutenticado ator) {
        try {
            return transacoes.alterar(id, dados, ator);
        } catch (ConflitoHorarioException e) {
            conflitos.registrar(ator.id(), e.getConflitos(), dados.inicio(), dados.fim());
            throw e;
        } catch (DataIntegrityViolationException e) {
            throw conflitoConcorrente(e, ator, List.of(), dados.inicio(), dados.fim());
        }
    }

    public ReservaVisao aprovar(Long id, String motivo, UsuarioAutenticado ator) {
        return transacoes.aprovar(id, motivo, ator);
    }

    public ReservaVisao rejeitar(Long id, String motivo, UsuarioAutenticado ator) {
        return transacoes.rejeitar(id, motivo, ator);
    }

    public ReservaVisao cancelar(Long id, String motivo, UsuarioAutenticado ator) {
        return transacoes.cancelar(id, motivo, ator);
    }

    public ReservaVisao iniciarUso(Long id, UsuarioAutenticado ator) {
        return transacoes.iniciarUso(id, ator);
    }

    public ReservaVisao concluir(Long id, UsuarioAutenticado ator) {
        return transacoes.concluir(id, ator);
    }

    public ReservaVisao registrarNaoComparecimento(Long id, UsuarioAutenticado ator) {
        return transacoes.registrarNaoComparecimento(id, ator);
    }

    public List<ReservaVisao> listar(FiltroReservas filtro) {
        return transacoes.listar(filtro);
    }

    public List<EventoAuditoria> historico(Long id, UsuarioAutenticado ator) {
        return transacoes.historico(id, ator);
    }

    /** Item liberado para retirada e o prazo de devolução (término da reserva). */
    public record RetiradaAutorizada(Recurso recurso, LocalDateTime prazoDevolucao) {
    }

    /** Usado dentro da transação da movimentação: valida a reserva travada e, se APROVADA, passa a EM_USO. */
    public RetiradaAutorizada autorizarRetirada(Long reservaId, Long recursoId, UsuarioAutenticado ator) {
        return transacoes.autorizarRetirada(reservaId, recursoId, ator);
    }

    /** Usado dentro da transação da devolução: conclui a reserva só de materiais quando nada mais está pendente. */
    public void concluirAposDevolucao(Long reservaId, UsuarioAutenticado ator) {
        transacoes.concluirAposDevolucao(reservaId, ator);
    }

    public ReservaVisao detalhar(Long id, UsuarioAutenticado ator) {
        return transacoes.detalhar(id, ator);
    }

    public List<ReservaVisao> minhas(UsuarioAutenticado ator, EstadoReserva estado) {
        return transacoes.minhas(ator, estado);
    }

    /**
     * Defesa final: se a constraint do banco recusou a gravação (SQLSTATE 23P01), outra transação ocupou o
     * horário. A resposta vira conflito de negócio, nunca erro interno.
     */
    private RuntimeException conflitoConcorrente(DataIntegrityViolationException e, UsuarioAutenticado ator,
            List<Long> recursoIds, LocalDateTime inicio, LocalDateTime fim) {
        if (!ViolacaoExclusao.causou(e)) {
            return e;
        }
        List<ConflitoDetalhe> detalhes = recursoIds.stream()
                .map(id -> new ConflitoDetalhe(id, null, CONCORRENCIA, inicio, fim, null))
                .toList();
        conflitos.registrar(ator.id(), detalhes, inicio, fim);
        return new ConflitoHorarioException(
                "Outra solicitação ocupou este horário ao mesmo tempo. Consulte a disponibilidade e tente novamente.",
                detalhes);
    }
}

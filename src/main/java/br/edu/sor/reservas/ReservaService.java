package br.edu.sor.reservas;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import br.edu.sor.compartilhado.erro.ConflitoDetalhe;
import br.edu.sor.compartilhado.erro.ConflitoHorarioException;
import br.edu.sor.compartilhado.erro.ViolacaoExclusao;
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
            throw conflitoConcorrente(e, ator, pedido);
        }
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
            NovaReserva pedido) {
        if (!ViolacaoExclusao.causou(e)) {
            return e;
        }
        List<ConflitoDetalhe> detalhes = pedido.recursoIds().stream()
                .map(id -> new ConflitoDetalhe(id, null, CONCORRENCIA, pedido.inicio(), pedido.fim(), null))
                .toList();
        conflitos.registrar(ator.id(), detalhes, pedido.inicio(), pedido.fim());
        return new ConflitoHorarioException(
                "Outra solicitação ocupou este horário ao mesmo tempo. Consulte a disponibilidade e tente novamente.",
                detalhes);
    }
}

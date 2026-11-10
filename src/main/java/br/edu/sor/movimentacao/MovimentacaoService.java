package br.edu.sor.movimentacao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.sor.auditoria.AuditoriaService;
import br.edu.sor.compartilhado.erro.NaoEncontradoException;
import br.edu.sor.compartilhado.erro.RegraNegocioException;
import br.edu.sor.identidade.UsuarioAutenticado;
import br.edu.sor.identidade.UsuarioRepository;
import br.edu.sor.reservas.ReservaService;
import br.edu.sor.reservas.ReservaService.RetiradaAutorizada;

/** Retirada e devolução pelo Responsável (RF-019..RF-021, DP-008, RN-030). */
@Service
public class MovimentacaoService {

    static final String ENTIDADE = "MOVIMENTACAO";

    private final MovimentacaoRepository movimentacoes;
    private final ReservaService reservas;
    private final UsuarioRepository usuarios;
    private final AuditoriaService auditoria;
    private final Clock relogio;

    public MovimentacaoService(MovimentacaoRepository movimentacoes, ReservaService reservas, UsuarioRepository usuarios,
            AuditoriaService auditoria, Clock relogio) {
        this.movimentacoes = movimentacoes;
        this.reservas = reservas;
        this.usuarios = usuarios;
        this.auditoria = auditoria;
        this.relogio = relogio;
    }

    LocalDateTime agora() {
        return LocalDateTime.now(relogio);
    }

    /** Valida reserva e item (com a reserva travada), registra a retirada e, se preciso, coloca a reserva EM_USO. */
    @Transactional
    public Movimentacao retirar(Long reservaId, Long recursoId, String observacao, UsuarioAutenticado ator) {
        RetiradaAutorizada autorizada = reservas.autorizarRetirada(reservaId, recursoId, ator);
        if (movimentacoes.existsByReservaIdAndRecursoId(reservaId, recursoId)) {
            throw new RegraNegocioException("RETIRADA_DUPLICADA",
                    "A retirada deste item já foi registrada nesta reserva.");
        }
        Movimentacao movimentacao = movimentacoes.save(new Movimentacao(reservaId, autorizada.recurso(),
                usuarios.getReferenceById(ator.id()), agora(), aparar(observacao), autorizada.prazoDevolucao()));
        auditoria.registrar(ator.username(), "MATERIAL_RETIRADO", ENTIDADE, movimentacao.getId(), null, "ABERTA",
                "reserva=" + reservaId + "; recurso=" + autorizada.recurso().getCodigo());
        return movimentacao;
    }

    @Transactional
    public Movimentacao devolver(Long id, String observacao, boolean avaria, UsuarioAutenticado ator) {
        Movimentacao movimentacao = movimentacoes.travar(id)
                .orElseThrow(() -> new NaoEncontradoException("Movimentação não encontrada."));
        if (!movimentacao.aberta()) {
            throw new RegraNegocioException("DEVOLUCAO_DUPLICADA", "A devolução deste item já foi registrada.");
        }
        movimentacao.registrarDevolucao(usuarios.getReferenceById(ator.id()), agora(), aparar(observacao), avaria);
        movimentacoes.flush();
        auditoria.registrar(ator.username(), "MATERIAL_DEVOLVIDO", ENTIDADE, id, "ABERTA", "DEVOLVIDA",
                (movimentacao.atrasada(agora()) ? "devolução com atraso" : "no prazo") + (avaria ? "; com avaria" : ""));
        reservas.concluirAposDevolucao(movimentacao.getReservaId(), ator);
        return movimentacao;
    }

    @Transactional(readOnly = true)
    public List<Movimentacao> listar(boolean somenteAbertas, Long reservaId) {
        return movimentacoes.filtrar(somenteAbertas, reservaId);
    }

    private static String aparar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}

package br.edu.sor.manutencao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.sor.auditoria.AuditoriaService;
import br.edu.sor.catalogo.Recurso;
import br.edu.sor.catalogo.RecursoRepository;
import br.edu.sor.compartilhado.erro.ConflitoHorarioException;
import br.edu.sor.compartilhado.erro.EntradaInvalidaException;
import br.edu.sor.compartilhado.erro.NaoEncontradoException;
import br.edu.sor.compartilhado.erro.RegraNegocioException;
import br.edu.sor.disponibilidade.DisponibilidadeService;
import br.edu.sor.disponibilidade.Ocupacao;
import br.edu.sor.disponibilidade.OcupacaoRepository;
import br.edu.sor.disponibilidade.PeriodoReserva;
import br.edu.sor.disponibilidade.Periodos;
import br.edu.sor.identidade.UsuarioAutenticado;
import br.edu.sor.identidade.UsuarioRepository;

/**
 * Bloqueio de recursos para manutenção (RF-026). Usa o mesmo ponto atômico das reservas (ADR-004) e
 * nunca cancela reservas automaticamente: se houver reserva ativa no intervalo, recusa e lista as reservas (DP-004).
 */
@Service
public class ManutencaoService {

    static final String ENTIDADE = "MANUTENCAO";

    private final ManutencaoRepository manutencoes;
    private final RecursoRepository recursos;
    private final OcupacaoRepository ocupacoes;
    private final UsuarioRepository usuarios;
    private final DisponibilidadeService disponibilidade;
    private final AuditoriaService auditoria;
    private final Clock relogio;

    public ManutencaoService(ManutencaoRepository manutencoes, RecursoRepository recursos, OcupacaoRepository ocupacoes,
            UsuarioRepository usuarios, DisponibilidadeService disponibilidade, AuditoriaService auditoria, Clock relogio) {
        this.manutencoes = manutencoes;
        this.recursos = recursos;
        this.ocupacoes = ocupacoes;
        this.usuarios = usuarios;
        this.disponibilidade = disponibilidade;
        this.auditoria = auditoria;
        this.relogio = relogio;
    }

    @Transactional
    public Manutencao registrar(Long recursoId, LocalDateTime inicio, LocalDateTime fim, String motivo,
            UsuarioAutenticado ator) {
        PeriodoReserva periodo = Periodos.de(inicio, fim);
        if (!periodo.termino().isAfter(LocalDateTime.now(relogio))) {
            throw new EntradaInvalidaException("O término da manutenção deve ser uma data e hora futura.");
        }
        List<Recurso> travado = recursos.travarEmOrdem(List.of(recursoId));
        if (travado.isEmpty()) {
            throw new NaoEncontradoException("Recurso não encontrado.");
        }
        try {
            disponibilidade.garantirLivre(travado, periodo, null, true);
        } catch (ConflitoHorarioException e) {
            throw new ConflitoHorarioException("Manutenção recusada. " + e.getMessage()
                    + " Cancele ou realoque as reservas listadas antes de bloquear o recurso.", e.getConflitos());
        }
        Recurso recurso = travado.get(0);
        Manutencao manutencao = manutencoes.save(new Manutencao(recurso, periodo, motivo.trim(),
                usuarios.getReferenceById(ator.id()), LocalDateTime.now(relogio)));
        ocupacoes.save(Ocupacao.deManutencao(recurso, manutencao.getId(), periodo));
        auditoria.registrar(ator.username(), "MANUTENCAO_REGISTRADA", ENTIDADE, manutencao.getId(), null, "ATIVA",
                "recurso=" + recurso.getCodigo() + "; período: " + periodo.inicio() + "–" + periodo.termino());
        return manutencao;
    }

    @Transactional
    public Manutencao cancelar(Long id, UsuarioAutenticado ator) {
        Manutencao manutencao = manutencoes.travar(id)
                .orElseThrow(() -> new NaoEncontradoException("Manutenção não encontrada."));
        if (!manutencao.isAtiva()) {
            throw new RegraNegocioException("MANUTENCAO_JA_CANCELADA", "Esta manutenção já foi cancelada.");
        }
        manutencao.cancelar();
        ocupacoes.findByManutencaoId(id).forEach(Ocupacao::liberar);
        auditoria.registrar(ator.username(), "MANUTENCAO_CANCELADA", ENTIDADE, id, "ATIVA", "CANCELADA", null);
        return manutencao;
    }

    @Transactional(readOnly = true)
    public List<Manutencao> listar(Long recursoId, boolean somenteAtivas) {
        return manutencoes.filtrar(recursoId, somenteAtivas);
    }
}

package br.edu.sor.auditoria;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grava eventos de auditoria <b>na mesma transação</b> da mudança (ADR-006): se a auditoria falhar,
 * a mudança também é desfeita. Propagation.MANDATORY garante que ninguém audite fora de uma transação.
 */
@Service
public class AuditoriaService {

    static final int LIMITE_CONSULTA = 500;

    private final EventoAuditoriaRepository eventos;
    private final Clock relogio;

    public AuditoriaService(EventoAuditoriaRepository eventos, Clock relogio) {
        this.eventos = eventos;
        this.relogio = relogio;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(String ator, String acao, String entidade, Long entidadeId,
            Object estadoAnterior, Object estadoNovo, String detalhes) {
        eventos.save(new EventoAuditoria(LocalDateTime.now(relogio), ator, acao, entidade, entidadeId,
                texto(estadoAnterior), texto(estadoNovo), detalhes));
    }

    @Transactional(readOnly = true)
    public List<EventoAuditoria> historico(String entidade, Long entidadeId) {
        return eventos.findByEntidadeAndEntidadeIdOrderByIdAsc(entidade, entidadeId);
    }

    @Transactional(readOnly = true)
    public List<EventoAuditoria> consultar(String entidade, Long entidadeId, String ator, String acao,
            LocalDateTime de, LocalDateTime ate) {
        Specification<EventoAuditoria> filtro = (raiz, consulta, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (entidade != null && !entidade.isBlank()) {
                condicoes.add(cb.equal(raiz.get("entidade"), entidade.toUpperCase()));
            }
            if (entidadeId != null) {
                condicoes.add(cb.equal(raiz.get("entidadeId"), entidadeId));
            }
            if (ator != null && !ator.isBlank()) {
                condicoes.add(cb.equal(raiz.get("ator"), ator));
            }
            if (acao != null && !acao.isBlank()) {
                condicoes.add(cb.equal(raiz.get("acao"), acao.toUpperCase()));
            }
            if (de != null) {
                condicoes.add(cb.greaterThanOrEqualTo(raiz.get("ocorridoEm"), de));
            }
            if (ate != null) {
                condicoes.add(cb.lessThan(raiz.get("ocorridoEm"), ate));
            }
            return cb.and(condicoes.toArray(Predicate[]::new));
        };
        return eventos.findAll(filtro, PageRequest.of(0, LIMITE_CONSULTA, Sort.by(Sort.Direction.DESC, "id")))
                .getContent();
    }

    private static String texto(Object valor) {
        return valor == null ? null : valor.toString();
    }
}

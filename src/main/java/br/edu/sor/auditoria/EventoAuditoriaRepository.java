package br.edu.sor.auditoria;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EventoAuditoriaRepository
        extends JpaRepository<EventoAuditoria, Long>, JpaSpecificationExecutor<EventoAuditoria> {

    List<EventoAuditoria> findByEntidadeAndEntidadeIdOrderByIdAsc(String entidade, Long entidadeId);
}

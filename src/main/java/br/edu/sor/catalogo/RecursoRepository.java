package br.edu.sor.catalogo;

import java.util.Collection;
import java.util.List;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecursoRepository extends JpaRepository<Recurso, Long>, JpaSpecificationExecutor<Recurso> {

    boolean existsByCodigo(String codigo);

    /**
     * Trava as linhas dos recursos (SELECT ... FOR UPDATE) em ordem crescente de id.
     * Toda operação que muda a ocupação de um recurso passa por aqui (ADR-004): as operações concorrentes sobre o
     * mesmo recurso são serializadas e a ordem fixa evita deadlock quando há vários recursos.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Recurso r where r.id in :ids order by r.id")
    List<Recurso> travarEmOrdem(@Param("ids") Collection<Long> ids);
}

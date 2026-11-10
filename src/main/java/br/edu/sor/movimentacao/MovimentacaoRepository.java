package br.edu.sor.movimentacao;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    boolean existsByReservaIdAndDevolvidaEmIsNull(Long reservaId);

    boolean existsByReservaIdAndRecursoId(Long reservaId, Long recursoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Movimentacao m where m.id = :id")
    Optional<Movimentacao> travar(@Param("id") Long id);

    @Query("""
            select m from Movimentacao m join fetch m.recurso join fetch m.retiradoPor left join fetch m.recebidoPor
            where (:somenteAbertas = false or m.devolvidaEm is null) and (:reservaId is null or m.reservaId = :reservaId)
            order by m.prazoDevolucao
            """)
    List<Movimentacao> filtrar(@Param("somenteAbertas") boolean somenteAbertas, @Param("reservaId") Long reservaId);
}

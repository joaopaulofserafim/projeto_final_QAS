package br.edu.sor.manutencao;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Manutencao m where m.id = :id")
    Optional<Manutencao> travar(@Param("id") Long id);

    @Query("""
            select m from Manutencao m join fetch m.recurso join fetch m.criadaPor
            where (:recursoId is null or m.recurso.id = :recursoId) and (:somenteAtivas = false or m.ativa = true)
            order by m.inicio
            """)
    List<Manutencao> filtrar(@Param("recursoId") Long recursoId, @Param("somenteAtivas") boolean somenteAtivas);

    long countByAtivaTrue();
}

package br.edu.sor.disponibilidade;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OcupacaoRepository extends JpaRepository<Ocupacao, Long> {

    /** Ocupações ativas que se sobrepõem a [inicio, fim) segundo RN-002 (início inclusivo, término exclusivo). */
    @Query("""
            select o from Ocupacao o join fetch o.recurso r
            where o.ativa = true and r.id in :recursoIds
              and o.inicio < :fim and o.fim > :inicio
              and (o.reservaId is null or o.reservaId <> :reservaIgnorada)
            order by r.id, o.inicio
            """)
    List<Ocupacao> conflitantes(@Param("recursoIds") Collection<Long> recursoIds,
            @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim,
            @Param("reservaIgnorada") Long reservaIgnorada);

    @Query("select o from Ocupacao o join fetch o.recurso where o.reservaId = :reservaId order by o.recurso.id")
    List<Ocupacao> daReserva(@Param("reservaId") Long reservaId);

    @Query("select o from Ocupacao o join fetch o.recurso where o.reservaId in :reservaIds order by o.recurso.id")
    List<Ocupacao> dasReservas(@Param("reservaIds") Collection<Long> reservaIds);

    List<Ocupacao> findByManutencaoId(Long manutencaoId);

    @Query("""
            select count(o) > 0 from Ocupacao o
            where o.recurso.id = :recursoId and o.ativa = true and o.tipo = :tipo and o.fim > :instante
            """)
    boolean existeAtivaApos(@Param("recursoId") Long recursoId, @Param("tipo") TipoOcupacao tipo,
            @Param("instante") LocalDateTime instante);

    @Query("""
            select o from Ocupacao o
            where o.recurso.id = :recursoId and o.ativa = true and o.inicio < :fim and o.fim > :inicio
            order by o.inicio
            """)
    List<Ocupacao> agenda(@Param("recursoId") Long recursoId, @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);
}

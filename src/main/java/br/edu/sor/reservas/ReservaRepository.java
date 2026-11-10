package br.edu.sor.reservas;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaRepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {

    /** Serializa transições concorrentes da mesma reserva (ex.: duas aprovações ao mesmo tempo). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reserva r where r.id = :id")
    Optional<Reserva> travar(@Param("id") Long id);

    @Query("""
            select r from Reserva r join fetch r.solicitante
            where r.solicitante.id = :solicitanteId and (:estado is null or r.estado = :estado)
            order by r.inicio desc
            """)
    List<Reserva> doSolicitante(@Param("solicitanteId") Long solicitanteId, @Param("estado") EstadoReserva estado);
}

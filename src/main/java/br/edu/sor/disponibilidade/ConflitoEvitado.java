package br.edu.sor.disponibilidade;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Tentativa de reserva recusada por conflito: fonte verificável do indicador "conflitos evitados" (RN-024). */
@Entity
@Table(name = "conflito_evitado")
public class ConflitoEvitado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recurso_id", nullable = false)
    private Long recursoId;

    @Column(name = "solicitante_id", nullable = false)
    private Long solicitanteId;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fim;

    @Column(nullable = false, length = 40)
    private String motivo;

    @Column(name = "ocorrido_em", nullable = false)
    private LocalDateTime ocorridoEm;

    protected ConflitoEvitado() {
    }

    ConflitoEvitado(Long recursoId, Long solicitanteId, LocalDateTime inicio, LocalDateTime fim, String motivo,
            LocalDateTime ocorridoEm) {
        this.recursoId = recursoId;
        this.solicitanteId = solicitanteId;
        this.inicio = inicio;
        this.fim = fim;
        this.motivo = motivo;
        this.ocorridoEm = ocorridoEm;
    }

    public Long getRecursoId() {
        return recursoId;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getOcorridoEm() {
        return ocorridoEm;
    }
}

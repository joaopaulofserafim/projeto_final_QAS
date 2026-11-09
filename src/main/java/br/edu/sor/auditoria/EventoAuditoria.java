package br.edu.sor.auditoria;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;

/** Registro imutável de uma mudança relevante (RN-016..018). O banco também impede UPDATE/DELETE. */
@Entity
@Immutable
@Table(name = "evento_auditoria")
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ocorrido_em", nullable = false)
    private LocalDateTime ocorridoEm;

    @Column(nullable = false, length = 60)
    private String ator;

    @Column(nullable = false, length = 60)
    private String acao;

    @Column(nullable = false, length = 30)
    private String entidade;

    @Column(name = "entidade_id", nullable = false)
    private Long entidadeId;

    @Column(name = "estado_anterior", length = 30)
    private String estadoAnterior;

    @Column(name = "estado_novo", length = 30)
    private String estadoNovo;

    @Column(length = 1000)
    private String detalhes;

    protected EventoAuditoria() {
    }

    EventoAuditoria(LocalDateTime ocorridoEm, String ator, String acao, String entidade, Long entidadeId,
            String estadoAnterior, String estadoNovo, String detalhes) {
        this.ocorridoEm = ocorridoEm;
        this.ator = ator;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNovo = estadoNovo;
        this.detalhes = detalhes;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getOcorridoEm() {
        return ocorridoEm;
    }

    public String getAtor() {
        return ator;
    }

    public String getAcao() {
        return acao;
    }

    public String getEntidade() {
        return entidade;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public String getEstadoNovo() {
        return estadoNovo;
    }

    public String getDetalhes() {
        return detalhes;
    }
}

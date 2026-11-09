package br.edu.sor.manutencao;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import br.edu.sor.catalogo.Recurso;
import br.edu.sor.disponibilidade.PeriodoReserva;
import br.edu.sor.identidade.Usuario;

@Entity
@Table(name = "manutencao")
public class Manutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recurso_id", nullable = false)
    private Recurso recurso;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fim;

    @Column(nullable = false, length = 300)
    private String motivo;

    @Column(nullable = false)
    private boolean ativa = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criada_por_id", nullable = false)
    private Usuario criadaPor;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    protected Manutencao() {
    }

    Manutencao(Recurso recurso, PeriodoReserva periodo, String motivo, Usuario criadaPor, LocalDateTime criadaEm) {
        this.recurso = recurso;
        this.inicio = periodo.inicio();
        this.fim = periodo.termino();
        this.motivo = motivo;
        this.criadaPor = criadaPor;
        this.criadaEm = criadaEm;
    }

    void cancelar() {
        this.ativa = false;
    }

    public Long getId() {
        return id;
    }

    public Recurso getRecurso() {
        return recurso;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public String getMotivo() {
        return motivo;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public Usuario getCriadaPor() {
        return criadaPor;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }
}

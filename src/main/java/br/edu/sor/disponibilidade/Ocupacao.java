package br.edu.sor.disponibilidade;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import br.edu.sor.catalogo.Recurso;

/**
 * Intervalo em que um recurso está ocupado por uma reserva ou manutenção.
 * Só ocupações ativas bloqueiam horário; a constraint de exclusão do banco garante que duas ativas
 * do mesmo recurso nunca se sobreponham.
 */
@Entity
@Table(name = "ocupacao")
public class Ocupacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recurso_id", nullable = false)
    private Recurso recurso;

    @Column(name = "reserva_id")
    private Long reservaId;

    @Column(name = "manutencao_id")
    private Long manutencaoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoOcupacao tipo;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fim;

    @Column(nullable = false)
    private boolean ativa;

    protected Ocupacao() {
    }

    private Ocupacao(Recurso recurso, TipoOcupacao tipo, Long reservaId, Long manutencaoId, PeriodoReserva periodo,
            boolean ativa) {
        this.recurso = recurso;
        this.tipo = tipo;
        this.reservaId = reservaId;
        this.manutencaoId = manutencaoId;
        this.inicio = periodo.inicio();
        this.fim = periodo.termino();
        this.ativa = ativa;
    }

    public static Ocupacao deReserva(Recurso recurso, Long reservaId, PeriodoReserva periodo, boolean ativa) {
        return new Ocupacao(recurso, TipoOcupacao.RESERVA, reservaId, null, periodo, ativa);
    }

    public static Ocupacao deManutencao(Recurso recurso, Long manutencaoId, PeriodoReserva periodo) {
        return new Ocupacao(recurso, TipoOcupacao.MANUTENCAO, null, manutencaoId, periodo, true);
    }

    public void liberar() {
        this.ativa = false;
    }

    public void reagendar(PeriodoReserva periodo) {
        this.inicio = periodo.inicio();
        this.fim = periodo.termino();
    }

    public PeriodoReserva periodo() {
        return new PeriodoReserva(inicio, fim);
    }

    public Long getId() {
        return id;
    }

    public Recurso getRecurso() {
        return recurso;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public Long getManutencaoId() {
        return manutencaoId;
    }

    public TipoOcupacao getTipo() {
        return tipo;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public boolean isAtiva() {
        return ativa;
    }
}

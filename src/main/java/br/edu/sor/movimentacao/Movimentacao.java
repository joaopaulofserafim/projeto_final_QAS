package br.edu.sor.movimentacao;

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
import br.edu.sor.identidade.Usuario;

/** Retirada e devolução de um material/equipamento de uma reserva (RN-020, RN-021). Um registro por item. */
@Entity
@Table(name = "movimentacao")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reserva_id", nullable = false)
    private Long reservaId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recurso_id", nullable = false)
    private Recurso recurso;

    @Column(name = "retirada_em", nullable = false)
    private LocalDateTime retiradaEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "retirado_por_id", nullable = false)
    private Usuario retiradoPor;

    @Column(name = "observacao_retirada", length = 300)
    private String observacaoRetirada;

    @Column(name = "prazo_devolucao", nullable = false)
    private LocalDateTime prazoDevolucao;

    @Column(name = "devolvida_em")
    private LocalDateTime devolvidaEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recebido_por_id")
    private Usuario recebidoPor;

    @Column(name = "observacao_devolucao", length = 300)
    private String observacaoDevolucao;

    @Column(nullable = false)
    private boolean avaria;

    protected Movimentacao() {
    }

    Movimentacao(Long reservaId, Recurso recurso, Usuario retiradoPor, LocalDateTime retiradaEm, String observacao,
            LocalDateTime prazoDevolucao) {
        this.reservaId = reservaId;
        this.recurso = recurso;
        this.retiradoPor = retiradoPor;
        this.retiradaEm = retiradaEm;
        this.observacaoRetirada = observacao;
        this.prazoDevolucao = prazoDevolucao;
    }

    void registrarDevolucao(Usuario recebidoPor, LocalDateTime quando, String observacao, boolean avaria) {
        this.recebidoPor = recebidoPor;
        this.devolvidaEm = quando;
        this.observacaoDevolucao = observacao;
        this.avaria = avaria;
    }

    public boolean aberta() {
        return devolvidaEm == null;
    }

    /** Atrasada: devolvida depois do prazo, ou ainda pendente com o prazo vencido. */
    public boolean atrasada(LocalDateTime agora) {
        return devolvidaEm != null ? devolvidaEm.isAfter(prazoDevolucao) : agora.isAfter(prazoDevolucao);
    }

    public Long getId() {
        return id;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public Recurso getRecurso() {
        return recurso;
    }

    public LocalDateTime getRetiradaEm() {
        return retiradaEm;
    }

    public Usuario getRetiradoPor() {
        return retiradoPor;
    }

    public String getObservacaoRetirada() {
        return observacaoRetirada;
    }

    public LocalDateTime getPrazoDevolucao() {
        return prazoDevolucao;
    }

    public LocalDateTime getDevolvidaEm() {
        return devolvidaEm;
    }

    public Usuario getRecebidoPor() {
        return recebidoPor;
    }

    public String getObservacaoDevolucao() {
        return observacaoDevolucao;
    }

    public boolean isAvaria() {
        return avaria;
    }
}

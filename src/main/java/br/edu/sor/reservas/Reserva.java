package br.edu.sor.reservas;

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
import jakarta.persistence.Version;

import br.edu.sor.compartilhado.erro.TransicaoInvalidaException;
import br.edu.sor.disponibilidade.PeriodoReserva;
import br.edu.sor.identidade.Usuario;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @Column(nullable = false)
    private LocalDateTime inicio;

    @Column(nullable = false)
    private LocalDateTime fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoReserva estado;

    @Column(nullable = false, length = 300)
    private String finalidade;

    @Column(length = 500)
    private String justificativa;

    @Column(name = "motivo_decisao", length = 500)
    private String motivoDecisao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decidido_por_id")
    private Usuario decididoPor;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "atualizada_em", nullable = false)
    private LocalDateTime atualizadaEm;

    @Version
    private long versao;

    protected Reserva() {
    }

    Reserva(Usuario solicitante, PeriodoReserva periodo, EstadoReserva estadoInicial, String finalidade,
            String justificativa, LocalDateTime agora) {
        this.solicitante = solicitante;
        this.inicio = periodo.inicio();
        this.fim = periodo.termino();
        this.estado = estadoInicial;
        this.finalidade = finalidade;
        this.justificativa = justificativa;
        this.criadaEm = agora;
        this.atualizadaEm = agora;
    }

    /** Aplica uma transição do modelo de estados; recusa com 409 qualquer transição não prevista. */
    void mudarPara(EstadoReserva destino, LocalDateTime agora) {
        if (!estado.podeIrPara(destino)) {
            throw new TransicaoInvalidaException("A reserva está " + estado + " e não pode passar para " + destino + ".");
        }
        this.estado = destino;
        this.atualizadaEm = agora;
    }

    void registrarDecisao(Usuario decisor, String motivo) {
        this.decididoPor = decisor;
        this.motivoDecisao = motivo;
    }

    /** Alteração pelo dono (RN-029): novo período e dados; recurso restrito volta a exigir aprovação. */
    void reagendar(PeriodoReserva periodo, String finalidade, String justificativa, EstadoReserva novoEstado,
            LocalDateTime agora) {
        this.inicio = periodo.inicio();
        this.fim = periodo.termino();
        this.finalidade = finalidade;
        this.justificativa = justificativa;
        this.estado = novoEstado;
        if (novoEstado == EstadoReserva.SOLICITADA) {
            this.decididoPor = null;
            this.motivoDecisao = null;
        }
        this.atualizadaEm = agora;
    }

    public boolean pertenceA(Long usuarioId) {
        return solicitante.getId().equals(usuarioId);
    }

    public boolean iniciada(LocalDateTime agora) {
        return !agora.isBefore(inicio);
    }

    public PeriodoReserva periodo() {
        return new PeriodoReserva(inicio, fim);
    }

    public Long getId() {
        return id;
    }

    public Usuario getSolicitante() {
        return solicitante;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFim() {
        return fim;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public String getFinalidade() {
        return finalidade;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public String getMotivoDecisao() {
        return motivoDecisao;
    }

    public Usuario getDecididoPor() {
        return decididoPor;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }
}

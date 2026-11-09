package br.edu.sor.notificacao;

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

import br.edu.sor.identidade.Usuario;

@Entity
@Table(name = "notificacao")
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @Column(name = "reserva_id")
    private Long reservaId;

    @Column(nullable = false, length = 40)
    private String tipo;

    @Column(nullable = false, length = 500)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusNotificacao status;

    @Column(length = 300)
    private String erro;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    protected Notificacao() {
    }

    Notificacao(Usuario destinatario, Long reservaId, String tipo, String mensagem, StatusNotificacao status, String erro,
            LocalDateTime criadaEm) {
        this.destinatario = destinatario;
        this.reservaId = reservaId;
        this.tipo = tipo;
        this.mensagem = mensagem;
        this.status = status;
        this.erro = erro;
        this.criadaEm = criadaEm;
    }

    public Long getId() {
        return id;
    }

    public Long getReservaId() {
        return reservaId;
    }

    public String getTipo() {
        return tipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public StatusNotificacao getStatus() {
        return status;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }
}

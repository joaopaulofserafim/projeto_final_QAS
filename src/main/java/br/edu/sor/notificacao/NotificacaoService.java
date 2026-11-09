package br.edu.sor.notificacao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

import br.edu.sor.identidade.Perfil;
import br.edu.sor.identidade.Usuario;
import br.edu.sor.identidade.UsuarioRepository;
import br.edu.sor.reservas.EstadoReserva;
import br.edu.sor.reservas.ReservaEvento;

/**
 * Comunica confirmações, decisões e alterações (RF-031..RF-033). Executa <b>depois do commit</b> e em transação
 * própria: uma falha do provedor fica registrada como FALHA e nunca desfaz a operação de negócio (RN-023).
 */
@Service
@EnableConfigurationProperties(NotificacaoPropriedades.class)
public class NotificacaoService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificacaoService.class);

    private final NotificacaoRepository notificacoes;
    private final UsuarioRepository usuarios;
    private final NotificacaoGateway gateway;
    private final NotificacaoPropriedades propriedades;
    private final Clock relogio;

    public NotificacaoService(NotificacaoRepository notificacoes, UsuarioRepository usuarios, NotificacaoGateway gateway,
            NotificacaoPropriedades propriedades, Clock relogio) {
        this.notificacoes = notificacoes;
        this.usuarios = usuarios;
        this.gateway = gateway;
        this.propriedades = propriedades;
        this.relogio = relogio;
    }

    record Destino(Usuario usuario, String mensagem) {
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void aoMudarReserva(ReservaEvento evento) {
        for (Destino destino : destinos(evento)) {
            entregar(destino, evento);
        }
    }

    @Transactional(readOnly = true)
    public List<Notificacao> minhas(Long usuarioId) {
        return notificacoes.findTop100ByDestinatarioIdOrderByIdDesc(usuarioId);
    }

    List<Destino> destinos(ReservaEvento e) {
        Usuario solicitante = usuarios.getReferenceById(e.solicitanteId());
        long id = e.reservaId();
        String motivo = e.detalhe() == null ? "" : " Motivo: " + e.detalhe();
        List<Destino> destinos = new ArrayList<>();
        switch (e.tipo()) {
            case CRIADA -> {
                if (e.estado() == EstadoReserva.SOLICITADA) {
                    destinos.add(new Destino(solicitante,
                            "Sua solicitação de reserva #" + id + " foi registrada e aguarda aprovação."));
                    responsaveis("Nova solicitação de reserva #" + id + " aguarda sua decisão.", destinos);
                } else {
                    destinos.add(new Destino(solicitante, "Sua reserva #" + id + " foi confirmada."));
                }
            }
            case APROVADA -> destinos.add(new Destino(solicitante, "Sua reserva #" + id + " foi aprovada."));
            case REJEITADA -> destinos.add(new Destino(solicitante, "Sua solicitação #" + id + " foi rejeitada." + motivo));
            case CANCELADA -> destinos.add(new Destino(solicitante, "Sua reserva #" + id + " foi cancelada." + motivo));
            case ALTERADA -> {
                boolean aguarda = e.estado() == EstadoReserva.SOLICITADA;
                destinos.add(new Destino(solicitante, "Sua reserva #" + id + " foi alterada"
                        + (aguarda ? " e aguarda nova aprovação." : ".")));
                if (aguarda) {
                    responsaveis("A solicitação #" + id + " foi alterada e aguarda nova decisão.", destinos);
                }
            }
            case NAO_COMPARECEU -> destinos.add(new Destino(solicitante,
                    "Registramos não comparecimento na reserva #" + id + "."));
        }
        return destinos;
    }

    private void responsaveis(String mensagem, List<Destino> destinos) {
        usuarios.findByPerfilAndAtivoTrue(Perfil.RESPONSAVEL).forEach(r -> destinos.add(new Destino(r, mensagem)));
    }

    private void entregar(Destino destino, ReservaEvento evento) {
        StatusNotificacao status = StatusNotificacao.SIMULADA;
        String erro = null;
        if (propriedades.modo() == NotificacaoPropriedades.Modo.HTTP) {
            try {
                gateway.enviar(new MensagemExterna(destino.usuario().getUsername(), destino.usuario().getEmail(),
                        "Reserva " + evento.tipo().name().toLowerCase(), destino.mensagem(), "reserva-" + evento.reservaId()));
                status = StatusNotificacao.ENVIADA;
            } catch (RuntimeException falha) {
                status = StatusNotificacao.FALHA;
                erro = resumir(falha);
                LOG.warn("Falha ao notificar reserva {}: {}", evento.reservaId(), erro);
                LOG.debug("Detalhe da falha de notificação", falha);
            }
        }
        notificacoes.save(new Notificacao(destino.usuario(), evento.reservaId(), evento.tipo().name(), destino.mensagem(),
                status, erro, LocalDateTime.now(relogio)));
    }

    static String resumir(RuntimeException falha) {
        if (falha instanceof RestClientResponseException http) {
            return "Provedor respondeu HTTP " + http.getStatusCode().value();
        }
        if (falha instanceof ResourceAccessException) {
            return "Provedor indisponível ou tempo esgotado";
        }
        return "Falha inesperada: " + falha.getClass().getSimpleName();
    }
}

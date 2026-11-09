package br.edu.sor.notificacao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.sor.identidade.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/notificacoes")
@Tag(name = "Notificações", description = "Atualizações sobre as reservas do usuário")
public class NotificacaoController {

    private final NotificacaoService servico;

    public NotificacaoController(NotificacaoService servico) {
        this.servico = servico;
    }

    public record NotificacaoVisao(Long id, Long reservaId, String tipo, String mensagem, StatusNotificacao status,
            LocalDateTime criadaEm) {
    }

    @GetMapping
    @Operation(summary = "Minhas notificações (mais recentes primeiro)")
    public List<NotificacaoVisao> minhas(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return servico.minhas(usuario.id()).stream()
                .map(n -> new NotificacaoVisao(n.getId(), n.getReservaId(), n.getTipo(), n.getMensagem(), n.getStatus(),
                        n.getCriadaEm()))
                .toList();
    }
}

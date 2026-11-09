package br.edu.sor.manutencao;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.sor.identidade.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/manutencoes")
@Tag(name = "Manutenção", description = "Bloqueio de recursos (Administrador registra; Responsável consulta)")
public class ManutencaoController {

    private final ManutencaoService servico;

    public ManutencaoController(ManutencaoService servico) {
        this.servico = servico;
    }

    public record NovaManutencao(
            @NotNull(message = "informe o recurso") Long recursoId,
            @NotNull(message = "informe o início") LocalDateTime inicio,
            @NotNull(message = "informe o término") LocalDateTime fim,
            @NotBlank(message = "informe o motivo") @Size(max = 300) String motivo) {
    }

    public record ManutencaoVisao(Long id, Long recursoId, String recursoCodigo, String recursoNome, LocalDateTime inicio,
            LocalDateTime fim, String motivo, boolean ativa, String criadaPor, LocalDateTime criadaEm) {

        static ManutencaoVisao de(Manutencao m) {
            return new ManutencaoVisao(m.getId(), m.getRecurso().getId(), m.getRecurso().getCodigo(),
                    m.getRecurso().getNome(), m.getInicio(), m.getFim(), m.getMotivo(), m.isAtiva(),
                    m.getCriadaPor().getNome(), m.getCriadaEm());
        }
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Listar manutenções, opcionalmente de um recurso e só as ativas")
    public List<ManutencaoVisao> listar(@RequestParam(required = false) Long recursoId,
            @RequestParam(defaultValue = "false") boolean somenteAtivas) {
        return servico.listar(recursoId, somenteAtivas).stream().map(ManutencaoVisao::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @Operation(summary = "Bloquear recurso para manutenção (recusa se houver reserva ativa no intervalo)")
    public ManutencaoVisao registrar(@Valid @RequestBody NovaManutencao dados,
            @AuthenticationPrincipal UsuarioAutenticado ator) {
        return ManutencaoVisao.de(servico.registrar(dados.recursoId(), dados.inicio(), dados.fim(), dados.motivo(), ator));
    }

    @PostMapping("/{id}/cancelar")
    @Transactional
    @Operation(summary = "Cancelar manutenção e liberar o recurso")
    public ManutencaoVisao cancelar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado ator) {
        return ManutencaoVisao.de(servico.cancelar(id, ator));
    }
}

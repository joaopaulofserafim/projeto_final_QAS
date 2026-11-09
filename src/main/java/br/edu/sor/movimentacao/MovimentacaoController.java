package br.edu.sor.movimentacao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/movimentacoes")
@Tag(name = "Movimentações", description = "Retirada e devolução de materiais e equipamentos (Responsável)")
public class MovimentacaoController {

    public enum Situacao {
        ABERTAS,
        TODAS
    }

    private final MovimentacaoService servico;
    private final Clock relogio;

    public MovimentacaoController(MovimentacaoService servico, Clock relogio) {
        this.servico = servico;
        this.relogio = relogio;
    }

    public record NovaRetirada(@NotNull(message = "informe a reserva") Long reservaId,
            @NotNull(message = "informe o recurso") Long recursoId, @Size(max = 300) String observacao) {
    }

    public record Devolucao(@Size(max = 300) String observacao, boolean avaria) {
    }

    public record MovimentacaoVisao(Long id, Long reservaId, Long recursoId, String recursoCodigo, String recursoNome,
            LocalDateTime retiradaEm, String retiradoPor, String observacaoRetirada, LocalDateTime prazoDevolucao,
            LocalDateTime devolvidaEm, String recebidoPor, String observacaoDevolucao, boolean avaria, boolean atrasada) {

        static MovimentacaoVisao de(Movimentacao m, LocalDateTime agora) {
            return new MovimentacaoVisao(m.getId(), m.getReservaId(), m.getRecurso().getId(), m.getRecurso().getCodigo(),
                    m.getRecurso().getNome(), m.getRetiradaEm(), m.getRetiradoPor().getNome(), m.getObservacaoRetirada(),
                    m.getPrazoDevolucao(), m.getDevolvidaEm(),
                    m.getRecebidoPor() == null ? null : m.getRecebidoPor().getNome(), m.getObservacaoDevolucao(),
                    m.isAvaria(), m.atrasada(agora));
        }
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Movimentações abertas (pendências) ou todas; filtro opcional por reserva")
    public List<MovimentacaoVisao> listar(@RequestParam(defaultValue = "TODAS") Situacao situacao,
            @RequestParam(required = false) Long reservaId) {
        LocalDateTime agora = LocalDateTime.now(relogio);
        return servico.listar(situacao == Situacao.ABERTAS, reservaId).stream()
                .map(m -> MovimentacaoVisao.de(m, agora)).toList();
    }

    @PostMapping("/retiradas")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    @Operation(summary = "Registrar retirada de material/equipamento de reserva aprovada ou em uso")
    public MovimentacaoVisao retirar(@Valid @RequestBody NovaRetirada dados, @AuthenticationPrincipal UsuarioAutenticado ator) {
        return MovimentacaoVisao.de(servico.retirar(dados.reservaId(), dados.recursoId(), dados.observacao(), ator),
                LocalDateTime.now(relogio));
    }

    @PostMapping("/{id}/devolucao")
    @Transactional
    @Operation(summary = "Registrar devolução (conclui a reserva quando for o último material)")
    public MovimentacaoVisao devolver(@PathVariable Long id, @Valid @RequestBody(required = false) Devolucao dados,
            @AuthenticationPrincipal UsuarioAutenticado ator) {
        Devolucao corpo = dados == null ? new Devolucao(null, false) : dados;
        return MovimentacaoVisao.de(servico.devolver(id, corpo.observacao(), corpo.avaria(), ator), LocalDateTime.now(relogio));
    }
}

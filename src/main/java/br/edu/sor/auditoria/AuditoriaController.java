package br.edu.sor.auditoria;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Consulta da trilha de auditoria (RF-029). Somente leitura: não existe operação de alteração ou exclusão. */
@RestController
@RequestMapping("/api/v1/auditoria")
@Tag(name = "Auditoria", description = "Histórico imutável de mudanças (Administrador)")
public class AuditoriaController {

    private final AuditoriaService auditoria;

    public AuditoriaController(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }

    public record EventoResposta(Long id, LocalDateTime ocorridoEm, String ator, String acao, String entidade,
            Long entidadeId, String estadoAnterior, String estadoNovo, String detalhes) {

        public static EventoResposta de(EventoAuditoria e) {
            return new EventoResposta(e.getId(), e.getOcorridoEm(), e.getAtor(), e.getAcao(), e.getEntidade(),
                    e.getEntidadeId(), e.getEstadoAnterior(), e.getEstadoNovo(), e.getDetalhes());
        }
    }

    @GetMapping
    @Operation(summary = "Consultar eventos de auditoria (mais recentes primeiro, até 500)")
    public List<EventoResposta> consultar(
            @RequestParam(required = false) String entidade,
            @RequestParam(required = false) Long entidadeId,
            @RequestParam(required = false) String ator,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate) {
        return auditoria.consultar(entidade, entidadeId, ator, acao, de, ate).stream()
                .map(EventoResposta::de)
                .toList();
    }
}

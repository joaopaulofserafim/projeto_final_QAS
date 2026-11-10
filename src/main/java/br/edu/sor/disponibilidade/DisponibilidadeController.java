package br.edu.sor.disponibilidade;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/disponibilidade")
@Tag(name = "Disponibilidade", description = "Consulta de conflitos de sala, professor, material e manutenção")
public class DisponibilidadeController {

    private final DisponibilidadeService servico;

    public DisponibilidadeController(DisponibilidadeService servico) {
        this.servico = servico;
    }

    /** Intervalo ocupado, sem dados de quem reservou (minimização de dados, RNF-002). */
    public record IntervaloOcupado(TipoOcupacao tipo, LocalDateTime inicio, LocalDateTime fim) {
    }

    @GetMapping
    @Operation(summary = "Verificar se os recursos estão livres no período (não reserva nada)")
    public List<DisponibilidadeService.SituacaoRecurso> consultar(
            @RequestParam List<Long> recursoIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return servico.consultar(recursoIds, inicio, fim);
    }

    @GetMapping("/recursos/{recursoId}/agenda")
    @Operation(summary = "Agenda de um recurso (inclusive professor): intervalos ocupados no período")
    public List<IntervaloOcupado> agenda(@PathVariable Long recursoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return servico.agenda(recursoId, inicio, fim).stream()
                .map(o -> new IntervaloOcupado(o.getTipo(), o.getInicio(), o.getFim()))
                .toList();
    }
}

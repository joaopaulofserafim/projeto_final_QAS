package br.edu.sor.relatorios;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.sor.catalogo.TipoRecurso;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/relatorios")
@Tag(name = "Relatórios", description = "Utilização, carga horária, conflitos evitados e painel (Administrador)")
public class RelatorioController {

    private final RelatorioService servico;

    public RelatorioController(RelatorioService servico) {
        this.servico = servico;
    }

    @GetMapping("/utilizacao")
    @Operation(summary = "Horas reservadas e percentual de ocupação por recurso na janela")
    public RelatorioService.RelatorioHoras utilizacao(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim,
            @RequestParam(required = false) TipoRecurso tipo) {
        return servico.utilizacao(inicio, fim, tipo);
    }

    @GetMapping("/carga-horaria")
    @Operation(summary = "Carga horária alocada por professor na janela")
    public RelatorioService.RelatorioHoras cargaHoraria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return servico.cargaHoraria(inicio, fim);
    }

    @GetMapping("/conflitos")
    @Operation(summary = "Conflitos evitados (tentativas recusadas) por motivo e por recurso")
    public RelatorioService.RelatorioConflitos conflitos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        return servico.conflitos(inicio, fim);
    }

    @GetMapping("/resumo")
    @Operation(summary = "Indicadores do painel: reservas por estado, pendências, manutenções e conflitos")
    public RelatorioService.Resumo resumo() {
        return servico.resumo();
    }
}

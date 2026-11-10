package br.edu.sor.reservas;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.edu.sor.auditoria.AuditoriaController.EventoResposta;
import br.edu.sor.identidade.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/reservas")
@Tag(name = "Reservas", description = "Solicitação, consulta e ciclo de vida das reservas")
public class ReservaController {

    private final ReservaService servico;

    public ReservaController(ReservaService servico) {
        this.servico = servico;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Solicitar reserva (Solicitante). Recurso restrito nasce SOLICITADA; os demais, APROVADA")
    public ReservaVisao criar(@Valid @RequestBody NovaReserva pedido, @AuthenticationPrincipal UsuarioAutenticado ator) {
        return servico.criar(pedido, ator);
    }

    @GetMapping("/minhas")
    @Operation(summary = "Minhas reservas, opcionalmente filtradas por estado")
    public List<ReservaVisao> minhas(@RequestParam(required = false) EstadoReserva estado,
            @AuthenticationPrincipal UsuarioAutenticado ator) {
        return servico.minhas(ator, estado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar reserva (dono, Responsável ou Administrador)")
    public ReservaVisao detalhar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado ator) {
        return servico.detalhar(id, ator);
    }

    @GetMapping
    @Operation(summary = "Fila e consulta administrativa (Responsável e Administrador): estado, recurso e período")
    public List<ReservaVisao> listar(
            @RequestParam(required = false) EstadoReserva estado,
            @RequestParam(required = false) Long recursoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate) {
        return servico.listar(new FiltroReservas(estado, recursoId, de, ate));
    }

    @GetMapping("/{id}/historico")
    @Operation(summary = "Histórico auditável da reserva (dono, Responsável ou Administrador)")
    public List<EventoResposta> historico(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado ator) {
        return servico.historico(id, ator).stream().map(EventoResposta::de).toList();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Alterar período e dados da própria reserva antes do início (Solicitante)")
    public ReservaVisao alterar(@PathVariable Long id, @Valid @RequestBody AlteracaoReserva dados,
            @AuthenticationPrincipal UsuarioAutenticado ator) {
        return servico.alterar(id, dados, ator);
    }

    @PostMapping("/{id}/aprovar")
    @Operation(summary = "Aprovar solicitação pendente (somente Responsável)")
    public ReservaVisao aprovar(@PathVariable Long id, @Valid @RequestBody(required = false) Decisao decisao,
            @AuthenticationPrincipal UsuarioAutenticado ator) {
        return servico.aprovar(id, Decisao.motivoDe(decisao), ator);
    }

    @PostMapping("/{id}/rejeitar")
    @Operation(summary = "Rejeitar solicitação pendente com motivo (somente Responsável)")
    public ReservaVisao rejeitar(@PathVariable Long id, @Valid @RequestBody(required = false) Decisao decisao,
            @AuthenticationPrincipal UsuarioAutenticado ator) {
        return servico.rejeitar(id, Decisao.motivoDe(decisao), ator);
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar reserva antes do início (dono ou Administrador). Nada é apagado")
    public ReservaVisao cancelar(@PathVariable Long id, @Valid @RequestBody(required = false) Decisao decisao,
            @AuthenticationPrincipal UsuarioAutenticado ator) {
        return servico.cancelar(id, Decisao.motivoDe(decisao), ator);
    }

    @PostMapping("/{id}/iniciar")
    @Operation(summary = "Registrar início de uso (somente Responsável)")
    public ReservaVisao iniciar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado ator) {
        return servico.iniciarUso(id, ator);
    }

    @PostMapping("/{id}/concluir")
    @Operation(summary = "Concluir reserva em uso (somente Responsável; exige materiais devolvidos)")
    public ReservaVisao concluir(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado ator) {
        return servico.concluir(id, ator);
    }

    @PostMapping("/{id}/nao-comparecimento")
    @Operation(summary = "Registrar não comparecimento após o início (somente Responsável)")
    public ReservaVisao naoComparecimento(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado ator) {
        return servico.registrarNaoComparecimento(id, ator);
    }
}

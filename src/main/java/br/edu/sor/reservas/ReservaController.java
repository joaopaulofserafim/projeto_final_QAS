package br.edu.sor.reservas;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
}

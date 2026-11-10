package br.edu.sor.catalogo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

import br.edu.sor.identidade.Perfil;
import br.edu.sor.identidade.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/recursos")
@Tag(name = "Recursos", description = "Cadastro (Administrador) e pesquisa (todos os perfis)")
public class RecursoController {

    private final RecursoService servico;

    public RecursoController(RecursoService servico) {
        this.servico = servico;
    }

    public record RecursoResposta(Long id, String codigo, String nome, TipoRecurso tipo, Integer capacidade,
            String localizacao, String descricao, boolean restrito, boolean ativo, Set<String> competencias) {

        public static RecursoResposta de(Recurso r) {
            return new RecursoResposta(r.getId(), r.getCodigo(), r.getNome(), r.getTipo(), r.getCapacidade(),
                    r.getLocalizacao(), r.getDescricao(), r.isRestrito(), r.isAtivo(), new TreeSet<>(r.getCompetencias()));
        }
    }

    @GetMapping
    @Operation(summary = "Pesquisar recursos por tipo, texto, capacidade, localização, competência, restrição e disponibilidade")
    public List<RecursoResposta> pesquisar(
            @RequestParam(required = false) TipoRecurso tipo,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Integer capacidadeMinima,
            @RequestParam(required = false) String localizacao,
            @RequestParam(required = false) String competencia,
            @RequestParam(required = false) Boolean restrito,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime disponivelInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime disponivelFim,
            @RequestParam(defaultValue = "false") boolean incluirInativos,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        boolean podeVerInativos = incluirInativos && usuario.tem(Perfil.ADMINISTRADOR);
        FiltroRecursos filtro = new FiltroRecursos(tipo, texto, capacidadeMinima, localizacao, competencia, restrito,
                disponivelInicio, disponivelFim, podeVerInativos);
        return servico.pesquisar(filtro).stream().map(RecursoResposta::de).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar um recurso")
    public RecursoResposta detalhar(@PathVariable Long id) {
        return RecursoResposta.de(servico.buscar(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar recurso (Administrador)")
    public RecursoResposta criar(@Valid @RequestBody DadosRecurso dados, @AuthenticationPrincipal UsuarioAutenticado ator) {
        return RecursoResposta.de(servico.criar(dados, ator));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Alterar dados de um recurso (Administrador)")
    public RecursoResposta alterar(@PathVariable Long id, @Valid @RequestBody DadosRecurso dados,
            @AuthenticationPrincipal UsuarioAutenticado ator) {
        return RecursoResposta.de(servico.alterar(id, dados, ator));
    }

    @PostMapping("/{id}/desativar")
    @Operation(summary = "Desativar recurso sem apagar histórico (Administrador)")
    public RecursoResposta desativar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado ator) {
        return RecursoResposta.de(servico.desativar(id, ator));
    }

    @PostMapping("/{id}/reativar")
    @Operation(summary = "Reativar recurso (Administrador)")
    public RecursoResposta reativar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado ator) {
        return RecursoResposta.de(servico.reativar(id, ator));
    }
}

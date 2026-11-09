package br.edu.sor.identidade;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuários", description = "Gestão de usuários e perfis (Administrador)")
public class UsuarioController {

    private final UsuarioService servico;

    public UsuarioController(UsuarioService servico) {
        this.servico = servico;
    }

    public record UsuarioResumo(Long id, String username, String nome, Perfil perfil) {

        static UsuarioResumo de(UsuarioAutenticado u) {
            return new UsuarioResumo(u.id(), u.username(), u.nome(), u.perfil());
        }
    }

    public record UsuarioResposta(Long id, String username, String nome, String email, Perfil perfil, boolean ativo) {

        static UsuarioResposta de(Usuario u) {
            return new UsuarioResposta(u.getId(), u.getUsername(), u.getNome(), u.getEmail(), u.getPerfil(),
                    u.isAtivo());
        }
    }

    public record NovoUsuario(
            @NotBlank @Size(min = 3, max = 60)
            @Pattern(regexp = "[a-zA-Z0-9._-]+", message = "use apenas letras, números, ponto, hífen ou sublinhado")
            String username,
            @NotBlank @Size(max = 120) String nome,
            @Email @Size(max = 160) String email,
            @NotBlank @Size(min = 8, max = 72, message = "a senha deve ter entre 8 e 72 caracteres") String senha,
            @NotNull Perfil perfil) {
    }

    public record AlteracaoUsuario(
            @NotBlank @Size(max = 120) String nome,
            @Email @Size(max = 160) String email,
            @NotNull Perfil perfil,
            @NotNull Boolean ativo) {
    }

    @GetMapping
    @Operation(summary = "Listar usuários")
    public List<UsuarioResposta> listar() {
        return servico.listar().stream().map(UsuarioResposta::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar usuário")
    public UsuarioResposta criar(@Valid @RequestBody NovoUsuario dados, @AuthenticationPrincipal UsuarioAutenticado ator) {
        return UsuarioResposta.de(servico.criar(dados.username(), dados.nome(), dados.email(), dados.senha(),
                dados.perfil(), ator));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Alterar perfil, dados ou situação (ativo) de um usuário")
    public UsuarioResposta alterar(@PathVariable Long id, @Valid @RequestBody AlteracaoUsuario dados,
            @AuthenticationPrincipal UsuarioAutenticado ator) {
        return UsuarioResposta.de(servico.alterar(id, dados.nome(), dados.email(), dados.perfil(), dados.ativo(), ator));
    }
}

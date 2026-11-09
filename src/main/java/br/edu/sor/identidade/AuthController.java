package br.edu.sor.identidade;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Sessão do usuário. O login (POST /api/v1/auth/login, campos username e password) e o logout
 * (POST /api/v1/auth/logout) são tratados pelo Spring Security; este controlador informa quem está logado.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Login, logout e usuário atual")
public class AuthController {

    @GetMapping("/me")
    @Operation(summary = "Usuário autenticado e seu perfil")
    public UsuarioController.UsuarioResumo eu(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return UsuarioController.UsuarioResumo.de(usuario);
    }
}

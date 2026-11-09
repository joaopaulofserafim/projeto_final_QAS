package br.edu.sor.identidade;

import java.io.IOException;
import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Respostas JSON para autenticação/autorização. Não envia o cabeçalho WWW-Authenticate,
 * para que o navegador não abra a janela nativa de login e não guarde credenciais Basic.
 */
@Component
class RespostasSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler,
        AuthenticationSuccessHandler, AuthenticationFailureHandler {

    private final ObjectMapper json;

    RespostasSeguranca(ObjectMapper json) {
        this.json = json;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
            throws IOException {
        escrever(response, HttpStatus.UNAUTHORIZED, "Não autenticado",
                "Faça login para acessar este recurso.", "NAO_AUTENTICADO");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e)
            throws IOException {
        if (e instanceof CsrfException) {
            escrever(response, HttpStatus.FORBIDDEN, "Sessão inválida",
                    "O token de segurança da sessão está ausente ou expirou. Recarregue a página e tente novamente.",
                    "CSRF_INVALIDO");
            return;
        }
        escrever(response, HttpStatus.FORBIDDEN, "Acesso negado",
                "Seu perfil não permite esta operação.", "ACESSO_NEGADO");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        UsuarioAutenticado usuario = (UsuarioAutenticado) authentication.getPrincipal();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getWriter(), UsuarioController.UsuarioResumo.de(usuario));
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException e) throws IOException {
        escrever(response, HttpStatus.UNAUTHORIZED, "Falha no login",
                "Usuário ou senha inválidos, ou conta desativada.", "CREDENCIAIS_INVALIDAS");
    }

    private void escrever(HttpServletResponse response, HttpStatus status, String titulo, String detalhe,
            String codigo) throws IOException {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detalhe);
        p.setTitle(titulo);
        p.setType(URI.create("https://sor.local/erros/" + codigo.toLowerCase().replace('_', '-')));
        p.setProperty("codigo", codigo);
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        json.writeValue(response.getWriter(), p);
    }
}

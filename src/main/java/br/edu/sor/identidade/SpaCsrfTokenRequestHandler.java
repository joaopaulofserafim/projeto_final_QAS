package br.edu.sor.identidade;

import java.util.function.Supplier;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

/**
 * Proteção CSRF para a interface em JavaScript (padrão "SPA" da documentação do Spring Security):
 * o token vai num cookie legível pelo script e volta no cabeçalho X-XSRF-TOKEN.
 */
final class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler simples = new CsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> token) {
        xor.handle(request, response, token);
        // Garante que o cookie seja emitido já na primeira requisição.
        token.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken token) {
        String cabecalho = request.getHeader(token.getHeaderName());
        return (StringUtils.hasText(cabecalho) ? simples : xor).resolveCsrfTokenValue(request, token);
    }
}

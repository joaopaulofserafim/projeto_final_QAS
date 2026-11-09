package br.edu.sor.identidade;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Autenticação e autorização por perfil (RF-001..003, ADR-005, DP-003, DP-019).
 * <ul>
 * <li>Interface web: login por formulário com sessão + proteção CSRF.</li>
 * <li>Clientes de API e testes: HTTP Basic sem sessão (CSRF não se aplica a credenciais explícitas).</li>
 * <li>Regra padrão: tudo em /api exige autenticação; qualquer outro caminho não listado é negado.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    static final String CSP = "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; "
            + "script-src 'self'; frame-ancestors 'none'; form-action 'self'";

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filtros(HttpSecurity http, RespostasSeguranca respostas) throws Exception {
        http
            .authorizeHttpRequests(regras -> regras
                .requestMatchers("/", "/index.html", "/favicon.ico", "/assets/**", "/webjars/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers("/api/v1/usuarios/**", "/api/v1/auditoria/**", "/api/v1/relatorios/**")
                    .hasRole(Perfil.ADMINISTRADOR.name())
                .requestMatchers(HttpMethod.GET, "/api/v1/manutencoes/**")
                    .hasAnyRole(Perfil.ADMINISTRADOR.name(), Perfil.RESPONSAVEL.name())
                .requestMatchers("/api/v1/manutencoes/**").hasRole(Perfil.ADMINISTRADOR.name())
                .requestMatchers(HttpMethod.GET, "/api/v1/recursos/**", "/api/v1/disponibilidade/**").authenticated()
                .requestMatchers("/api/v1/recursos/**").hasRole(Perfil.ADMINISTRADOR.name())
                .requestMatchers("/api/v1/movimentacoes/**").hasRole(Perfil.RESPONSAVEL.name())
                .requestMatchers(HttpMethod.POST, "/api/v1/reservas").hasRole(Perfil.SOLICITANTE.name())
                .requestMatchers(HttpMethod.PUT, "/api/v1/reservas/*").hasRole(Perfil.SOLICITANTE.name())
                .requestMatchers(HttpMethod.POST, "/api/v1/reservas/*/aprovar", "/api/v1/reservas/*/rejeitar",
                        "/api/v1/reservas/*/iniciar", "/api/v1/reservas/*/concluir",
                        "/api/v1/reservas/*/nao-comparecimento").hasRole(Perfil.RESPONSAVEL.name())
                .requestMatchers(HttpMethod.POST, "/api/v1/reservas/*/cancelar")
                    .hasAnyRole(Perfil.SOLICITANTE.name(), Perfil.ADMINISTRADOR.name())
                .requestMatchers(HttpMethod.GET, "/api/v1/reservas")
                    .hasAnyRole(Perfil.RESPONSAVEL.name(), Perfil.ADMINISTRADOR.name())
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll())
            .httpBasic(basic -> basic.authenticationEntryPoint(respostas))
            .formLogin(form -> form
                .loginPage("/")
                .loginProcessingUrl("/api/v1/auth/login")
                .successHandler(respostas)
                .failureHandler(respostas))
            .logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
            .exceptionHandling(erros -> erros
                .authenticationEntryPoint(respostas)
                .accessDeniedHandler(respostas))
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                .ignoringRequestMatchers("/api/v1/auth/login")
                .ignoringRequestMatchers(request -> request.getHeader(HttpHeaders.AUTHORIZATION) != null))
            .headers(cabecalhos -> cabecalhos
                .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
                .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN)));
        return http.build();
    }
}

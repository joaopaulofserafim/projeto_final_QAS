package br.edu.sor.compartilhado;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/** Contrato publicado em /v3/api-docs e /swagger-ui.html (DP-012). */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI contrato() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema de Organização de Recursos — API")
                        .version("1.0.0-rc1")
                        .description("Reserva de salas, professores, materiais e equipamentos sem conflitos. "
                                + "Autentique com HTTP Basic (usuários de demonstração no README). Erros seguem "
                                + "RFC 7807 com o campo `codigo`."))
                .components(new Components().addSecuritySchemes("basic",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
                .addSecurityItem(new SecurityRequirement().addList("basic"));
    }
}

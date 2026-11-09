package br.edu.sor.suporte;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class ConfiguracaoTeste {

    @Bean
    @Primary
    RelogioControlavel relogioControlavel() {
        return new RelogioControlavel();
    }
}

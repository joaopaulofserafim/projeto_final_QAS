package br.edu.sor.compartilhado;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Relógio único da aplicação, no fuso institucional (DP-015). Testes substituem por um relógio controlável. */
@Configuration
public class RelogioConfig {

    @Bean
    Clock relogio(@Value("${sor.fuso-horario}") String fuso) {
        return Clock.system(ZoneId.of(fuso));
    }
}

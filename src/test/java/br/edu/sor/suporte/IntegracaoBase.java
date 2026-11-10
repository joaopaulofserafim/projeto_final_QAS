package br.edu.sor.suporte;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base dos testes de integração: aplicação completa numa porta aleatória e PostgreSQL 16 real em container
 * (Testcontainers). Um único container é compartilhado por toda a suíte; cada teste começa com o banco limpo.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(ConfiguracaoTeste.class)
public abstract class IntegracaoBase {

    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void banco(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registro.add("spring.datasource.username", POSTGRES::getUsername);
        registro.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected TestRestTemplate http;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected RelogioControlavel relogio;

    @LocalServerPort
    protected int porta;

    protected Api api;

    @BeforeEach
    void prepararBancoLimpo() {
        jdbc.execute("""
                TRUNCATE notificacao, movimentacao, ocupacao, manutencao, conflito_evitado, reserva,
                         recurso_competencia, recurso, evento_auditoria RESTART IDENTITY CASCADE
                """);
        jdbc.update("DELETE FROM usuario WHERE username NOT IN ('admin','responsavel','solicitante','solicitante2')");
        jdbc.update("UPDATE usuario SET ativo = TRUE");
        jdbc.update("UPDATE usuario SET perfil = 'ADMINISTRADOR' WHERE username = 'admin'");
        jdbc.update("UPDATE usuario SET perfil = 'RESPONSAVEL' WHERE username = 'responsavel'");
        jdbc.update("UPDATE usuario SET perfil = 'SOLICITANTE' WHERE username IN ('solicitante','solicitante2')");
        relogio.definir(RelogioControlavel.AGORA_PADRAO);
        api = new Api(http);
    }

    protected long contar(String sql, Object... parametros) {
        Long total = jdbc.queryForObject(sql, Long.class, parametros);
        return total == null ? 0 : total;
    }
}

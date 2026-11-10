package br.edu.sor.identidade;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria um usuário de demonstração por perfil quando ainda não existem (DP-019). A senha vem da configuração
 * (SOR_SENHA_INICIAL) e é gravada só como hash BCrypt: nenhum segredo fica versionado no banco ou no esquema.
 * Em uma implantação real, defina SOR_SENHA_INICIAL e troque as senhas pelo Administrador.
 */
@Component
class UsuariosIniciais implements ApplicationRunner {

    private record Inicial(String login, String nome, String email, Perfil perfil) {
    }

    private static final List<Inicial> USUARIOS = List.of(
            new Inicial("admin", "Administrador do Sistema", "admin@sor.local", Perfil.ADMINISTRADOR),
            new Inicial("responsavel", "Rita Responsável", "responsavel@sor.local", Perfil.RESPONSAVEL),
            new Inicial("solicitante", "Marina Costa", "marina@sor.local", Perfil.SOLICITANTE),
            new Inicial("solicitante2", "Paulo Mendes", "paulo@sor.local", Perfil.SOLICITANTE));

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final Clock relogio;
    private final String segredoInicial;

    UsuariosIniciais(UsuarioRepository usuarios, PasswordEncoder codificador, Clock relogio,
            @Value("${sor.usuarios-iniciais.segredo}") String segredoInicial) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.relogio = relogio;
        this.segredoInicial = segredoInicial;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (Inicial u : USUARIOS) {
            if (!usuarios.existsByUsername(u.login())) {
                usuarios.save(new Usuario(u.login(), u.nome(), u.email(), codificador.encode(segredoInicial), u.perfil(),
                        LocalDateTime.now(relogio)));
            }
        }
    }
}

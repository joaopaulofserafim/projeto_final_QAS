package br.edu.sor.identidade;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.sor.auditoria.AuditoriaService;
import br.edu.sor.compartilhado.erro.NaoEncontradoException;
import br.edu.sor.compartilhado.erro.RegraNegocioException;

/** Gestão de usuários e perfis pelo Administrador (RF-022). Usuários nunca são apagados (DP-005). */
@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder senhas;
    private final AuditoriaService auditoria;
    private final Clock relogio;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder senhas, AuditoriaService auditoria,
            Clock relogio) {
        this.usuarios = usuarios;
        this.senhas = senhas;
        this.auditoria = auditoria;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarios.findAllByOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado."));
    }

    @Transactional
    public Usuario criar(String username, String nome, String email, String senha, Perfil perfil,
            UsuarioAutenticado ator) {
        if (usuarios.existsByUsername(username)) {
            throw new RegraNegocioException("USUARIO_DUPLICADO", "Já existe um usuário com o login '" + username + "'.");
        }
        Usuario novo = usuarios.save(new Usuario(username, nome, email, senhas.encode(senha), perfil,
                LocalDateTime.now(relogio)));
        auditoria.registrar(ator.username(), "USUARIO_CRIADO", "USUARIO", novo.getId(), null, perfil,
                "login=" + username);
        return novo;
    }

    @Transactional
    public Usuario alterar(Long id, String nome, String email, Perfil perfil, boolean ativo,
            UsuarioAutenticado ator) {
        Usuario usuario = buscar(id);
        if (usuario.getId().equals(ator.id()) && (!ativo || perfil != usuario.getPerfil())) {
            throw new RegraNegocioException("AUTO_ALTERACAO",
                    "O Administrador não pode desativar a própria conta nem alterar o próprio perfil.");
        }
        String antes = usuario.getPerfil() + (usuario.isAtivo() ? "" : "/INATIVO");
        usuario.alterar(nome, email, perfil, ativo);
        String depois = perfil + (ativo ? "" : "/INATIVO");
        auditoria.registrar(ator.username(), "USUARIO_ALTERADO", "USUARIO", id, antes, depois, null);
        return usuario;
    }
}

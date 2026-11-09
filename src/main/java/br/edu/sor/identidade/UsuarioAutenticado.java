package br.edu.sor.identidade;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Principal autenticado: carrega apenas o necessário para autorização (id, perfil). */
public record UsuarioAutenticado(Long id, String username, String nome, String senhaHash, Perfil perfil, boolean ativo)
        implements UserDetails {

    public static UsuarioAutenticado de(Usuario u) {
        return new UsuarioAutenticado(u.getId(), u.getUsername(), u.getNome(), u.getSenhaHash(), u.getPerfil(), u.isAtivo());
    }

    public boolean tem(Perfil outro) {
        return perfil == outro;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(perfil.autoridade()));
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    @Override
    public String toString() {
        return "UsuarioAutenticado[id=" + id + ", username=" + username + ", perfil=" + perfil + "]";
    }
}

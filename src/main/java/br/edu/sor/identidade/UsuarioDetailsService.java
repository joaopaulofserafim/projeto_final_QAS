package br.edu.sor.identidade;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarios;

    public UsuarioDetailsService(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return usuarios.findByUsername(username)
                .map(UsuarioAutenticado::de)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
    }
}

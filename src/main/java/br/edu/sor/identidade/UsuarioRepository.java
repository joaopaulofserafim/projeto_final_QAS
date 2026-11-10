package br.edu.sor.identidade;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    List<Usuario> findByPerfilAndAtivoTrue(Perfil perfil);

    List<Usuario> findAllByOrderByNomeAsc();
}

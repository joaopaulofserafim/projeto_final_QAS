package br.edu.sor.disponibilidade;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConflitoEvitadoRepository extends JpaRepository<ConflitoEvitado, Long> {

    List<ConflitoEvitado> findByOcorridoEmGreaterThanEqualAndOcorridoEmLessThan(LocalDateTime de, LocalDateTime ate);
}

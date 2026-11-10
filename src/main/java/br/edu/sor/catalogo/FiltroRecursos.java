package br.edu.sor.catalogo;

import java.time.LocalDateTime;

/** Critérios de pesquisa de recursos (RF-004). Todos são opcionais e combinados com E. */
public record FiltroRecursos(
        TipoRecurso tipo,
        String texto,
        Integer capacidadeMinima,
        String localizacao,
        String competencia,
        Boolean restrito,
        LocalDateTime disponivelInicio,
        LocalDateTime disponivelFim,
        boolean incluirInativos) {
}

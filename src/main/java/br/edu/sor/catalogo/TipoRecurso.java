package br.edu.sor.catalogo;

/** Professor é tratado como recurso alocável (DP-016): a mesma regra de sobreposição protege sua agenda. */
public enum TipoRecurso {
    SALA,
    PROFESSOR,
    MATERIAL,
    EQUIPAMENTO;

    /** Materiais e equipamentos são retirados e devolvidos (RF-019/RF-020). */
    public boolean movimentavel() {
        return this == MATERIAL || this == EQUIPAMENTO;
    }
}

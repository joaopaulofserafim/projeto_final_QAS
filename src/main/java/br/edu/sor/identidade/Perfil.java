package br.edu.sor.identidade;

/** Perfis do sistema. A autoridade Spring Security correspondente é ROLE_{nome}. */
public enum Perfil {
    SOLICITANTE,
    RESPONSAVEL,
    ADMINISTRADOR;

    public String autoridade() {
        return "ROLE_" + name();
    }
}

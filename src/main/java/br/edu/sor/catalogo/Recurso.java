package br.edu.sor.catalogo;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "recurso")
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRecurso tipo;

    private Integer capacidade;

    @Column(length = 120)
    private String localizacao;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private boolean restrito;

    @Column(nullable = false)
    private boolean ativo = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recurso_competencia", joinColumns = @JoinColumn(name = "recurso_id"))
    @Column(name = "competencia", length = 80)
    @BatchSize(size = 100)
    private Set<String> competencias = new LinkedHashSet<>();

    @Version
    private long versao;

    protected Recurso() {
    }

    public Recurso(String codigo, String nome, TipoRecurso tipo) {
        this.codigo = codigo;
        this.nome = nome;
        this.tipo = tipo;
    }

    public void definirDados(String nome, Integer capacidade, String localizacao, String descricao, boolean restrito,
            Set<String> competencias) {
        this.nome = nome;
        this.capacidade = capacidade;
        this.localizacao = localizacao;
        this.descricao = descricao;
        this.restrito = restrito;
        this.competencias.clear();
        if (competencias != null) {
            this.competencias.addAll(competencias);
        }
    }

    public void desativar() {
        this.ativo = false;
    }

    public void reativar() {
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public TipoRecurso getTipo() {
        return tipo;
    }

    public Integer getCapacidade() {
        return capacidade;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isRestrito() {
        return restrito;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Set<String> getCompetencias() {
        return Set.copyOf(competencias);
    }
}

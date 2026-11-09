package br.edu.sor.catalogo;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.sor.auditoria.AuditoriaService;
import br.edu.sor.compartilhado.erro.EntradaInvalidaException;
import br.edu.sor.compartilhado.erro.NaoEncontradoException;
import br.edu.sor.compartilhado.erro.RegraNegocioException;
import br.edu.sor.identidade.UsuarioAutenticado;

/** Cadastro e pesquisa de salas, professores, materiais e equipamentos (RF-004, RF-005, RF-023..RF-025). */
@Service
public class RecursoService {

    private final RecursoRepository recursos;
    private final OcupacaoDeRecursos ocupacoes;
    private final AuditoriaService auditoria;
    private final Clock relogio;

    public RecursoService(RecursoRepository recursos, OcupacaoDeRecursos ocupacoes, AuditoriaService auditoria,
            Clock relogio) {
        this.recursos = recursos;
        this.ocupacoes = ocupacoes;
        this.auditoria = auditoria;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public Recurso buscar(Long id) {
        return recursos.findById(id).orElseThrow(() -> new NaoEncontradoException("Recurso não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<Recurso> pesquisar(FiltroRecursos filtro) {
        boolean filtrarDisponibilidade = filtro.disponivelInicio() != null || filtro.disponivelFim() != null;
        if (filtrarDisponibilidade && (filtro.disponivelInicio() == null || filtro.disponivelFim() == null
                || !filtro.disponivelFim().isAfter(filtro.disponivelInicio()))) {
            throw new EntradaInvalidaException(
                    "Para filtrar por disponibilidade informe início e término, com término posterior ao início.");
        }
        List<Recurso> encontrados = recursos.findAll(especificacao(filtro),
                Sort.by("tipo").ascending().and(Sort.by("codigo").ascending()));
        if (!filtrarDisponibilidade || encontrados.isEmpty()) {
            return encontrados;
        }
        Set<Long> ocupados = ocupacoes.ocupadosNoPeriodo(encontrados.stream().map(Recurso::getId).toList(),
                filtro.disponivelInicio(), filtro.disponivelFim());
        return encontrados.stream().filter(r -> !ocupados.contains(r.getId())).toList();
    }

    @Transactional
    public Recurso criar(DadosRecurso dados, UsuarioAutenticado ator) {
        String codigo = dados.codigo().trim().toUpperCase(Locale.ROOT);
        if (recursos.existsByCodigo(codigo)) {
            throw new RegraNegocioException("CODIGO_DUPLICADO", "Já existe um recurso com o código " + codigo + ".");
        }
        validar(dados);
        Recurso recurso = new Recurso(codigo, dados.nome().trim(), dados.tipo());
        aplicar(recurso, dados);
        recursos.save(recurso);
        auditoria.registrar(ator.username(), "RECURSO_CRIADO", "RECURSO", recurso.getId(), null, "ATIVO",
                "codigo=" + codigo + ", tipo=" + dados.tipo() + ", restrito=" + dados.restrito());
        return recurso;
    }

    @Transactional
    public Recurso alterar(Long id, DadosRecurso dados, UsuarioAutenticado ator) {
        Recurso recurso = buscar(id);
        if (dados.tipo() != recurso.getTipo()) {
            throw new EntradaInvalidaException("O tipo de um recurso não pode ser alterado.");
        }
        validar(dados);
        boolean restritoAntes = recurso.isRestrito();
        aplicar(recurso, dados);
        auditoria.registrar(ator.username(), "RECURSO_ALTERADO", "RECURSO", id, null, null,
                "restrito: " + restritoAntes + " -> " + dados.restrito());
        return recurso;
    }

    /** DP-005: não desativa recurso com reserva ativa futura; nada é apagado. */
    @Transactional
    public Recurso desativar(Long id, UsuarioAutenticado ator) {
        Recurso recurso = buscar(id);
        if (!recurso.isAtivo()) {
            return recurso;
        }
        if (ocupacoes.possuiReservaAtivaApos(id, LocalDateTime.now(relogio))) {
            throw new RegraNegocioException("RECURSO_COM_RESERVAS",
                    "O recurso possui reservas ativas futuras. Cancele ou realoque essas reservas antes de desativá-lo.");
        }
        recurso.desativar();
        auditoria.registrar(ator.username(), "RECURSO_DESATIVADO", "RECURSO", id, "ATIVO", "INATIVO", null);
        return recurso;
    }

    @Transactional
    public Recurso reativar(Long id, UsuarioAutenticado ator) {
        Recurso recurso = buscar(id);
        if (!recurso.isAtivo()) {
            recurso.reativar();
            auditoria.registrar(ator.username(), "RECURSO_REATIVADO", "RECURSO", id, "INATIVO", "ATIVO", null);
        }
        return recurso;
    }

    private static void validar(DadosRecurso dados) {
        if (dados.tipo() == TipoRecurso.SALA && dados.capacidade() == null) {
            throw new EntradaInvalidaException("Informe a capacidade da sala.");
        }
        if (dados.tipo() != TipoRecurso.PROFESSOR && dados.competencias() != null && !dados.competencias().isEmpty()) {
            throw new EntradaInvalidaException("Competências só se aplicam a professores.");
        }
    }

    private static void aplicar(Recurso recurso, DadosRecurso dados) {
        Set<String> competencias = new LinkedHashSet<>();
        if (dados.competencias() != null) {
            dados.competencias().stream().map(String::trim).filter(c -> !c.isEmpty()).forEach(competencias::add);
        }
        recurso.definirDados(dados.nome().trim(), dados.capacidade(), aparar(dados.localizacao()),
                aparar(dados.descricao()), dados.restrito(), competencias);
    }

    private static String aparar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private static Specification<Recurso> especificacao(FiltroRecursos f) {
        return (raiz, consulta, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();
            if (!f.incluirInativos()) {
                condicoes.add(cb.isTrue(raiz.get("ativo")));
            }
            if (f.tipo() != null) {
                condicoes.add(cb.equal(raiz.get("tipo"), f.tipo()));
            }
            if (f.texto() != null && !f.texto().isBlank()) {
                String termo = "%" + f.texto().trim().toLowerCase(Locale.ROOT) + "%";
                condicoes.add(cb.or(cb.like(cb.lower(raiz.get("nome")), termo),
                        cb.like(cb.lower(raiz.get("codigo")), termo)));
            }
            if (f.capacidadeMinima() != null) {
                condicoes.add(cb.greaterThanOrEqualTo(raiz.get("capacidade"), f.capacidadeMinima()));
            }
            if (f.localizacao() != null && !f.localizacao().isBlank()) {
                condicoes.add(cb.like(cb.lower(raiz.get("localizacao")),
                        "%" + f.localizacao().trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (f.restrito() != null) {
                condicoes.add(cb.equal(raiz.get("restrito"), f.restrito()));
            }
            if (f.competencia() != null && !f.competencia().isBlank()) {
                Join<Recurso, String> competencia = raiz.join("competencias");
                condicoes.add(cb.equal(cb.lower(competencia), f.competencia().trim().toLowerCase(Locale.ROOT)));
                consulta.distinct(true);
            }
            return cb.and(condicoes.toArray(Predicate[]::new));
        };
    }
}

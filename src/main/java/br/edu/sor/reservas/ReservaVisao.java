package br.edu.sor.reservas;

import java.time.LocalDateTime;
import java.util.List;

import br.edu.sor.catalogo.Recurso;
import br.edu.sor.catalogo.TipoRecurso;
import br.edu.sor.identidade.Usuario;

/** Representação de uma reserva para a API (montada dentro da transação). */
public record ReservaVisao(
        Long id,
        EstadoReserva estado,
        LocalDateTime inicio,
        LocalDateTime fim,
        String finalidade,
        String justificativa,
        String motivoDecisao,
        Pessoa solicitante,
        Pessoa decididoPor,
        List<RecursoReservado> recursos,
        LocalDateTime criadaEm,
        LocalDateTime atualizadaEm) {

    public record Pessoa(Long id, String nome) {

        static Pessoa de(Usuario u) {
            return u == null ? null : new Pessoa(u.getId(), u.getNome());
        }
    }

    public record RecursoReservado(Long id, String codigo, String nome, TipoRecurso tipo, boolean restrito) {

        static RecursoReservado de(Recurso r) {
            return new RecursoReservado(r.getId(), r.getCodigo(), r.getNome(), r.getTipo(), r.isRestrito());
        }
    }

    static ReservaVisao de(Reserva r, List<Recurso> recursos) {
        return new ReservaVisao(r.getId(), r.getEstado(), r.getInicio(), r.getFim(), r.getFinalidade(),
                r.getJustificativa(), r.getMotivoDecisao(), Pessoa.de(r.getSolicitante()), Pessoa.de(r.getDecididoPor()),
                recursos.stream().map(RecursoReservado::de).toList(), r.getCriadaEm(), r.getAtualizadaEm());
    }
}

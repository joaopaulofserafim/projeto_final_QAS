package br.edu.sor.compartilhado.erro;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Converte erros em respostas RFC 7807 (ProblemDetail) com mensagens compreensíveis em português,
 * sem stack trace nem detalhes internos (RNF-003, ADR-008).
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public record ErroCampo(String campo, String mensagem) {
    }

    @ExceptionHandler(EntradaInvalidaException.class)
    ProblemDetail entradaInvalida(EntradaInvalidaException e) {
        return problema(HttpStatus.BAD_REQUEST, "Dados inválidos", e.getMessage(), "ENTRADA_INVALIDA");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacao(MethodArgumentNotValidException e) {
        List<ErroCampo> erros = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ErroCampo(f.getField(), f.getDefaultMessage()))
                .toList();
        ProblemDetail p = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Corrija os campos destacados e tente novamente.", "ENTRADA_INVALIDA");
        p.setProperty("erros", erros);
        return p;
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ProblemDetail validacaoParametros(HandlerMethodValidationException e) {
        return problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais parâmetros da requisição são inválidos.", "ENTRADA_INVALIDA");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail corpoIlegivel(HttpMessageNotReadableException e) {
        return problema(HttpStatus.BAD_REQUEST, "Requisição mal formatada",
                "O corpo da requisição não pôde ser lido. Verifique o JSON e o formato das datas (aaaa-mm-ddThh:mm:ss).",
                "ENTRADA_INVALIDA");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail tipoInvalido(MethodArgumentTypeMismatchException e) {
        return problema(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "O parâmetro '" + e.getName() + "' tem um valor inválido.", "ENTRADA_INVALIDA");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ProblemDetail parametroAusente(MissingServletRequestParameterException e) {
        return problema(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente",
                "Informe o parâmetro '" + e.getParameterName() + "'.", "ENTRADA_INVALIDA");
    }

    @ExceptionHandler(NaoEncontradoException.class)
    ProblemDetail naoEncontrado(NaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "Não encontrado", e.getMessage(), "NAO_ENCONTRADO");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ProblemDetail rotaInexistente(NoResourceFoundException e) {
        return problema(HttpStatus.NOT_FOUND, "Não encontrado", "O endereço solicitado não existe.", "NAO_ENCONTRADO");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ProblemDetail metodoNaoSuportado(HttpRequestMethodNotSupportedException e) {
        return problema(HttpStatus.METHOD_NOT_ALLOWED, "Operação não permitida",
                "Este endereço não aceita o método " + e.getMethod() + ".", "METODO_NAO_PERMITIDO");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ProblemDetail tipoDeMidia(HttpMediaTypeNotSupportedException e) {
        return problema(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato não suportado",
                "Envie os dados em JSON (Content-Type: application/json).", "FORMATO_NAO_SUPORTADO");
    }

    @ExceptionHandler({AcessoNegadoException.class, AccessDeniedException.class})
    ProblemDetail acessoNegado(RuntimeException e) {
        String mensagem = e instanceof AcessoNegadoException ? e.getMessage() : "Seu perfil não permite esta operação.";
        return problema(HttpStatus.FORBIDDEN, "Acesso negado", mensagem, "ACESSO_NEGADO");
    }

    @ExceptionHandler(ConflitoHorarioException.class)
    ProblemDetail conflitoHorario(ConflitoHorarioException e) {
        ProblemDetail p = problema(HttpStatus.CONFLICT, "Conflito de horário", e.getMessage(), e.getCodigo());
        p.setProperty("conflitos", e.getConflitos());
        return p;
    }

    @ExceptionHandler(RegraNegocioException.class)
    ProblemDetail regraNegocio(RegraNegocioException e) {
        return problema(HttpStatus.CONFLICT, "Operação recusada", e.getMessage(), e.getCodigo());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail edicaoConcorrente(ObjectOptimisticLockingFailureException e) {
        return problema(HttpStatus.CONFLICT, "Registro alterado por outra operação",
                "Os dados foram alterados enquanto você trabalhava. Recarregue e tente novamente.", "EDICAO_CONCORRENTE");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridade(DataIntegrityViolationException e) {
        if (ViolacaoExclusao.causou(e)) {
            return problema(HttpStatus.CONFLICT, "Conflito de horário",
                    "Outra operação ocupou este horário ao mesmo tempo. Consulte a disponibilidade novamente.",
                    "CONFLITO_HORARIO");
        }
        return problema(HttpStatus.CONFLICT, "Operação recusada",
                "A operação viola uma regra de integridade dos dados (por exemplo, código duplicado).",
                "INTEGRIDADE");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail erroInesperado(Exception e) {
        String rastreio = UUID.randomUUID().toString();
        LOG.error("Erro inesperado [rastreio={}]", rastreio, e);
        ProblemDetail p = problema(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Não foi possível concluir a operação. Informe o código de rastreio ao suporte.", "ERRO_INTERNO");
        p.setProperty("rastreio", rastreio);
        return p;
    }

    static ProblemDetail problema(HttpStatus status, String titulo, String detalhe, String codigo) {
        ProblemDetail p = ProblemDetail.forStatusAndDetail(status, detalhe);
        p.setTitle(titulo);
        p.setType(URI.create("https://sor.local/erros/" + codigo.toLowerCase().replace('_', '-')));
        p.setProperty("codigo", codigo);
        return p;
    }
}

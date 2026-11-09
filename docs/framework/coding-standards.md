# Padrões de código

- Código, nomes de domínio e mensagens ao usuário em **português**; termos técnicos do framework em inglês.
- Um pacote por módulo de domínio (ADR-002). Dependências entre módulos só na direção documentada; para inverter, use uma interface (porta) no módulo de cima.
- Controladores finos: validam entrada (`jakarta.validation`), convertem DTO ⇄ domínio e chamam um serviço. Regras de negócio ficam nos serviços/entidades.
- Toda escrita relevante: `@Transactional` + `AuditoriaService.registrar(...)` na mesma transação.
- Toda operação que muda ocupação de recurso: `RecursoRepository.travarEmOrdem` → `DisponibilidadeService.garantirLivre` → gravar (ADR-004).
- Erros: lance `EntradaInvalidaException` (400), `NaoEncontradoException` (404), `AcessoNegadoException` (403), `RegraNegocioException`/`TransicaoInvalidaException`/`ConflitoHorarioException` (409). Nunca devolva stack trace.
- Testes: TDD (teste falhando → código mínimo → refatoração). Testes de integração estendem `IntegracaoBase` e usam o cliente caixa-preta `Api`. Mocks só em testes unitários isolados.
- Commits: Conventional Commits com o id da story, ex.: `feat(1.5): ...`.

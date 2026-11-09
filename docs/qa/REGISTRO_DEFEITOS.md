# Registro de defeitos

**Escopo:** Epic 1 (RC 1.0.0-rc1) · **Atualizado em:** 09/11/2026
Cada defeito tem origem verificável (auditoria ou teste que falhou), prioridade e o commit da correção. Severidade: impacto no produto/avaliação. Prioridade: P0 bloqueia a entrega, P1 essencial, P2 importante, P3 melhoria.

**Resumo:** 12 defeitos registrados — 10 corrigidos, 2 aceitos com justificativa. Nenhum defeito aberto de severidade crítica ou alta.

| ID | Severidade | Prioridade | Encontrado por | Descrição | Correção | Commit | Situação |
|---|---|---|---|---|---|---|---|
| DEF-001 | Alta | P0 | Auditoria do repositório (09/11) | Código da story 1.1 mergeado na `main` pelo PR #1 sem revisão, sem CI e com a story em `InProgress`. | Pipeline em todo push/PR (story 1.2) e QA gate retroativo da 1.1. **Falta:** ativar a proteção da `main` no GitHub (ação manual do dono — ver README). | `b4e8ab2`, `a092292` | Mitigado; ação manual pendente |
| DEF-002 | Média | P1 | Auditoria | Story 1.1 dizia que não havia testes nem build, e o Change Log tinha datas posteriores ao próprio merge. | Notas de reconciliação e linha de Change Log com as datas reais do git. | `a092292` | Corrigido |
| DEF-003 | Média | P1 | Auditoria | RTM contraditória (introdução dizia "sem testes" e a linha do RF-006 dizia "18 aprovados") e sem colunas de risco, teste e evidência exigidas pelo enunciado. | Nova `RTM.md` ligando requisito → risco → teste → evidência; aviso na matriz de origem. | este pacote de documentação | Corrigido |
| DEF-004 | Média | P1 | Auditoria | Metas do professor (80% linhas, 70% branches) não aplicadas; AC9 da 1.1 e ADR-009 diziam não haver meta aprovada. | `jacoco:check` com 0.80/0.70 no `verify`. | `b4e8ab2` | Corrigido |
| DEF-005 | Média | P2 | Auditoria | `core-config.yaml` apontava para `docs/prd` e `docs/architecture` (inexistentes) e faltavam os arquivos `docs/framework/*` carregados pelo `@dev`. | Caminhos apontando para `PRD/` e criação de `tech-stack`, `coding-standards` e `source-tree`. | `f6690cf` | Corrigido |
| DEF-006 | Alta | P0 | Auditoria (comparação com o enunciado) | Estado alternativo "não compareceu", exigido pelo professor, ausente de toda a documentação. | RN-025, modelo de estados e `EstadoReserva.NAO_COMPARECEU` com testes. | `f6690cf`, `e6bc5cf` | Corrigido |
| DEF-007 | Média | P1 | `ErrosApiTest`, `RecursoApiTest`, `UsuarioApiTest` (falharam) | Mensagens de erro do framework em inglês ("Invalid request content.") e sem lista de campos: `spring.mvc.problemdetails.enabled` ativava o tratador padrão do Spring Boot, que tinha precedência. | Propriedade removida; `ApiExceptionHandler` com `@Order(HIGHEST_PRECEDENCE)`. | `0404312` | Corrigido |
| DEF-008 | Baixa | P3 | `SegurancaApiTest.nenhumEndpointDaApiAceitaAcessoAnonimo` | Escrita anônima recebe 403 (barrada pelo filtro CSRF) em vez de 401. Não é falha de segurança: o acesso é negado. | Comportamento mantido e documentado; o teste aceita 401 ou 403. | `0404312` | Aceito |
| DEF-009 | Alta | P0 | `NotificacaoWireMockTest` (falhou) | Toda notificação HTTP falhava com `RST_STREAM: Stream cancelled`: o cliente do JDK tentava HTTP/2 em texto puro (h2c) e o servidor cancelava o fluxo. Em produção nenhuma notificação seria entregue. | Cliente HTTP fixado em HTTP/1.1. | `ec1d4f8` | Corrigido |
| DEF-010 | Média | P1 | Execução do JMeter | O plugin do JMeter era pulado em silêncio porque obedece a `skipTests`, usada no perfil `carga`. | O perfil desliga só o Surefire. | `a395127` | Corrigido |
| DEF-011 | Média | P1 | Execução do JMeter (2ª rodada: 12% de erro) | O plano de carga dependia do estado do banco: a escrita sorteava o dia e colidia com reservas da rodada anterior (347 respostas 409). Defeito do plano, não da aplicação. | A preparação cria salas novas por execução; a escrita usa dias sequenciais nelas. | `a395127` | Corrigido |
| DEF-012 | Baixa | P3 | Execução do JMeter (1ª rodada) | Corrida de 30 pedidos simultâneos levou ~2,1 s com a JVM recém-iniciada (BCrypt + compilação JIT). | Procedimento com aquecimento; com a JVM aquecida, p95 de 684 ms (meta ≤ 1 s). | `a395127` | Aceito |

## Como registrar novos defeitos

1. Abra uma issue no GitHub com o rótulo `defeito`, a severidade e o teste/passo que reproduz.
2. Escreva primeiro um teste que falha reproduzindo o defeito.
3. Corrija em uma branch `fix/...`, referencie a issue no commit (`fix(x.y): ... (closes #N)`) e acrescente a linha nesta tabela.

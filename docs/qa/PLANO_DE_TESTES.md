# Plano de testes

**Produto:** Sistema de Organização de Recursos · **Versão:** 1.0.0-rc1 · **Data:** 09/11/2026
**Base:** ADR-009, requisitos em `PRD/requisitos/`, decisões em `PENDENCIAS_DECISOES.md`, matriz em [`RTM.md`](../../RTM.md).

## 1. Objetivo

Demonstrar, com evidência automatizada e repetível, que o sistema cumpre as regras críticas do enunciado — período válido, nenhuma sobreposição (inclusive na agenda do professor), uma única reserva aceita em pedidos simultâneos, recurso em manutenção não reservável, aprovação somente pelo Responsável, reserva iniciada nunca apagada e auditoria de toda mudança de estado — e as metas de qualidade (80% de linhas, 70% de branches, 0 bugs/vulnerabilidades críticos, 100% dos requisitos críticos na RTM).

## 2. Escopo

**Dentro:** API REST `/api/v1`, regras de domínio, persistência (PostgreSQL), segurança (autenticação, autorização, CSRF, cabeçalhos), integração externa de notificação, interface web, desempenho sob carga.
**Fora:** provedor de identidade institucional (DP-019), envio real de e-mail, testes de acessibilidade com leitor de tela, testes em navegadores além do Chromium.

## 3. Estratégia (pirâmide)

| Nível | Ferramenta | O que verifica | Mocks? | Classes |
|---|---|---|---|---|
| Unitário | JUnit 5 + AssertJ | Regras puras: período, sobreposição, tabela de estados, cálculo de horas, tradução de erros | Não necessários | `PeriodoReservaTest`, `PeriodosTest`, `EstadoReservaTest`, `CalculadoraHorasTest`, `ApiExceptionHandlerTest`, `ViolacaoExclusaoTest` |
| Parametrizado | `@ParameterizedTest` (`@CsvSource`, `@MethodSource`, `@EnumSource`) | Matrizes de casos: 49 transições, 7 intervalos, 26 combinações perfil×operação, 8 filtros | — | presentes em 9 classes |
| Integração | Spring Boot + **Testcontainers (PostgreSQL 16)** | Migrações, constraint de exclusão, trigger de auditoria, transações | **Proibido** (banco real) | `ConstraintExclusaoTest`, `AuditoriaApiTest`, `SistemaOrganizacaoRecursosApplicationTest` |
| API caixa-preta | `TestRestTemplate` por HTTP real (porta aleatória), só JSON | Contrato, status, mensagens, autorização, regras de negócio ponta a ponta no backend | Não | `*ApiTest` |
| Concorrência | Threads + `CountDownLatch` + HTTP + PostgreSQL real | Uma única reserva aceita; sem deadlock; sem conflito falso; decisão única | Não | `ConcorrenciaReservaTest`, `CicloDeVidaReservaTest.aprovacoesSimultaneas…`, `ManutencaoApiTest.manutencaoEReservasSimultaneas…` |
| Contrato externo | **WireMock** | Notificação HTTP: sucesso, erro 500, timeout — sem desfazer a reserva | Só o provedor externo | `NotificacaoWireMockTest` |
| Ponta a ponta | **Playwright (Chromium)** + aplicação + PostgreSQL | Fluxos de tela por perfil, mensagens, celular, CSP | Não | `InterfaceE2ETest` |
| Carga | **JMeter** (`./mvnw -Pcarga verify`) | Percentis, erros e integridade sob 50+ usuários | Não | `src/test/jmeter/carga-sor.jmx` |
| Estático | **JaCoCo** (gate) + **SonarCloud** | Cobertura mínima; bugs, vulnerabilidades, code smells | — | `pom.xml`, CI |

**Regra de mocks (enunciado):** mocks só em teste unitário isolado. Persistência, concorrência e integração são verificadas com PostgreSQL real e WireMock — nenhum teste de integração usa H2 ou repositório mockado.

## 4. Técnica de desenvolvimento: TDD

- Story 1.5 (reservas e concorrência) foi desenvolvida com evidência formal: commit `test(1.5): RED` com 26 de 30 testes falhando (CI vermelho), seguido de `feat(1.5): GREEN` (CI verde) e da refatoração na 1.6 (fluxo único de transição) com a suíte verde antes e depois.
- As demais stories seguiram teste primeiro (testes escritos antes da implementação de cada módulo); o registro está em cada story.

## 5. Priorização por risco

| Prioridade | Risco | Cenários |
|---|---|---|
| P0 | RA-001 dupla reserva | 20 pedidos simultâneos; horários escalonados; professor disputado; pedidos cruzados (deadlock); manutenção × reserva; escrita direta no banco |
| P0 | RA-004 acesso | Matriz perfil×operação; varredura de todo o contrato OpenAPI sem login; dono da reserva; CSRF |
| P0 | Regras do enunciado | Período, estados (49 pares), reserva iniciada, manutenção, aprovação somente pelo Responsável, auditoria |
| P1 | RA-008 integração | Provedor OK, 500, lento |
| P1 | RA-010 desempenho | Metas DP-014 com JMeter |
| P2 | Usabilidade | E2E de fluxos e celular |

## 6. Ambientes

| Ambiente | Banco | Uso |
|---|---|---|
| Local (`./mvnw verify`) | PostgreSQL 16 via Testcontainers (requer Docker) | Desenvolvimento |
| CI (GitHub Actions, `ubuntu-latest`, Java 21) | PostgreSQL 16 via Testcontainers | Todo push e PR; instala o Chromium para E2E (`E2E_OBRIGATORIO=true`) |
| Carga | PostgreSQL 16 (`docker compose`) + JAR | Execução manual documentada em `RELATORIO_CARGA.md` |

## 7. Critérios de entrada e saída

**Entrada de uma story em teste:** story `InReview`, suíte local verde, testes da story escritos antes do código.

**Saída (release candidate):**
- [x] 100% dos testes automatizados passando no CI.
- [x] JaCoCo ≥ 80% linhas e ≥ 70% branches (build falha abaixo).
- [x] 100% dos requisitos críticos com teste e evidência na RTM.
- [x] Teste de concorrência real verde (limitador "dupla reserva").
- [x] Matriz de autorização e varredura anônima verdes (limitador "falha de autorização").
- [x] Metas de carga atendidas e integridade após a carga.
- [ ] SonarCloud sem bugs/vulnerabilidades críticos — depende de configurar o token no GitHub (ver README).
- [x] Nenhum defeito aberto de severidade crítica ou alta.

## 8. Como executar

```bash
./mvnw verify                                    # tudo (precisa de Docker); E2E pula se não houver navegador
./mvnw test -Dtest=ConcorrenciaReservaTest       # só a concorrência
./mvnw -Pcarga verify                            # carga (com a aplicação rodando)
```

Relatórios: `target/surefire-reports/` e `target/site/jacoco/index.html`.

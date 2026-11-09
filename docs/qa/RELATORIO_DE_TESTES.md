# Relatório de testes

**Versão:** 1.0.0-rc1 · **Execução:** 09/11/2026 · **Comando:** `./mvnw verify` (Java 21.0.12.1, PostgreSQL 16 via Testcontainers, Chromium headless)
**CI:** GitHub Actions — [run 42722036100](https://github.com/joaopaulofserafim/projeto_final_QAS/actions/runs/42722036100) (mesma suíte, verde, E2E executados).

## 1. Resumo

| Indicador | Resultado | Meta |
|---|---|---|
| Testes executados | **247** | — |
| Falhas / erros / pulados | **0 / 0 / 0** | 0 |
| Cobertura de linhas (sistema inteiro) | **97,7%** (1.166/1.193) | ≥ 80% |
| Cobertura de branches (sistema inteiro) | **88,0%** (292/332) | ≥ 70% |
| Requisitos críticos com teste e evidência | **11/11** | 100% |
| Requisitos funcionais com teste | 36/36 | — |
| Dupla reserva em 20 pedidos simultâneos | **1 aceito, 19 recusados** (3/3 rodadas) | 1 aceito |
| Carga (50 usuários, 60 s) | 6.164 requisições, **0% erro**, consultas p95 6–8 ms | p95 ≤ 500 ms |

## 2. Resultado por classe

| Classe | Tipo | Testes | Resultado |
|---|---|---:|---|
| `disponibilidade.PeriodoReservaTest` | Unitário + parametrizado (story 1.1) | 18 | ✔ |
| `disponibilidade.PeriodosTest` | Parametrizado | 7 | ✔ |
| `reservas.EstadoReservaTest` | Parametrizado (49 pares de estados + 9) | 58 | ✔ |
| `relatorios.CalculadoraHorasTest` | Parametrizado | 12 | ✔ |
| `compartilhado.erro.ApiExceptionHandlerTest` | Unitário | 5 | ✔ |
| `compartilhado.erro.ViolacaoExclusaoTest` | Unitário | 2 | ✔ |
| `SistemaOrganizacaoRecursosApplicationTest` | Integração | 1 | ✔ |
| `identidade.SegurancaApiTest` | API + parametrizado | 36 | ✔ |
| `identidade.UsuarioApiTest` | API | 6 | ✔ |
| `compartilhado.ErrosApiTest` | API | 6 | ✔ |
| `catalogo.RecursoApiTest` | API + parametrizado | 17 | ✔ |
| `auditoria.AuditoriaApiTest` | API + integração (trigger) | 4 | ✔ |
| `reservas.ReservaApiTest` | API + parametrizado | 22 | ✔ |
| `reservas.ConcorrenciaReservaTest` | Concorrência real | 5 | ✔ |
| `reservas.ConstraintExclusaoTest` | Integração (banco) | 3 | ✔ |
| `reservas.CicloDeVidaReservaTest` | API + concorrência | 16 | ✔ |
| `manutencao.ManutencaoApiTest` | API + concorrência | 6 | ✔ |
| `movimentacao.MovimentacaoApiTest` | API | 6 | ✔ |
| `notificacao.NotificacaoApiTest` | API | 2 | ✔ |
| `notificacao.NotificacaoWireMockTest` | WireMock | 3 | ✔ |
| `relatorios.RelatorioApiTest` | API | 6 | ✔ |
| `e2e.InterfaceE2ETest` | Ponta a ponta (Playwright) | 6 | ✔ |
| **Total** | | **247** | **✔** |

## 3. Cobertura por pacote (JaCoCo)

| Pacote | Linhas | Branches |
|---|---:|---:|
| `auditoria` | 100,0% | 85,0% |
| `catalogo` | 100,0% | 85,5% |
| `compartilhado` / `compartilhado.erro` | 100,0% / 96,9% | — / 91,7% |
| `disponibilidade` | 93,7% | 88,6% |
| `identidade` | 98,8% | 75,0% |
| `manutencao` | 100,0% | 100,0% |
| `movimentacao` | 100,0% | 86,4% |
| `notificacao` | 99,0% | 86,2% |
| `relatorios` | 100,0% | 100,0% |
| `reservas` | 95,2% | 90,4% |
| **Total** | **97,7%** | **88,0%** |

A cobertura alta vem principalmente de testes de API e integração que verificam comportamento (status, corpo, estado do banco e auditoria), não de testes triviais. A interface JavaScript não entra na métrica JaCoCo; ela é verificada pelos 6 testes E2E.

## 4. Evidência de TDD (story 1.5)

| Fase | Commit | CI | Resultado |
|---|---|---|---|
| RED | `d7b9555` `test(1.5): RED …` | [42718786331](https://github.com/joaopaulofserafim/projeto_final_QAS/actions/runs/42718786331) ✖ | 132 testes, 26 falhando (todos de reserva) |
| GREEN | `a9571f5` `feat(1.5): GREEN …` | [42719530475](https://github.com/joaopaulofserafim/projeto_final_QAS/actions/runs/42719530475) ✔ | 132/132 |
| REFACTOR | `e6bc5cf` `feat(1.6): … fluxo único de transição` | verde na suíte seguinte | testes da 1.5 inalterados e verdes |

Detalhes: [`evidencias/tdd-1.5-red.md`](evidencias/tdd-1.5-red.md) e [`evidencias/tdd-1.5-green.md`](evidencias/tdd-1.5-green.md).

## 5. Defeitos encontrados pelos testes

DEF-007 (mensagens em inglês), DEF-008 (403 em escrita anônima), DEF-009 (notificação HTTP nunca entregue — HTTP/2), DEF-010, DEF-011 e DEF-012 (execução da carga). Ver [`REGISTRO_DEFEITOS.md`](REGISTRO_DEFEITOS.md).

## 6. Limitações conhecidas

- E2E só em Chromium; a carga roda com aplicação, banco e JMeter na mesma máquina.
- SonarCloud configurado no pipeline, mas depende do segredo `SONAR_TOKEN` (ainda não criado).
- Testes de acessibilidade limitados a rótulos e layout em 390 px; sem auditoria com leitor de tela.

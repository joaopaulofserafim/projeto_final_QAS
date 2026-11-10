# Relatório final de qualidade — Release Candidate 1.0.0-rc1

**Data:** 10/11/2026 · **Branch:** `main` (PRs #2 a #5 mesclados) · **Escopo:** Epic 1 (stories 1.1 a 1.10) e ajustes de usabilidade e regras RN-031/RN-032

## 1. Conclusão

O RC cumpre os 14 requisitos funcionais do enunciado e as regras críticas, com evidência automatizada no CI. Os quatro limitadores de nota estão afastados com prova executável. O SonarCloud está configurado e o Quality Gate foi aprovado nos pull requests. Ficam pendentes duas ações da equipe (proteção da `main` e distribuição das partes da apresentação), descritas na seção 5.

## 2. Limitadores de nota

| Limitador | Situação | Evidência |
|---|---|---|
| Aplicação que não executa | **Afastado** | `./mvnw spring-boot:run` ou `docker compose --profile app up`; `SistemaOrganizacaoRecursosApplicationTest`; 6 testes E2E no navegador |
| Pipeline ausente | **Afastado** | `.github/workflows/ci.yml` em todo push e PR; runs verdes na branch |
| Falha de autorização | **Afastado** | Matriz de 26 combinações perfil×operação; varredura de todas as operações do contrato OpenAPI sem login; dono da reserva; CSRF (`SegurancaApiTest`) |
| Possibilidade de dupla reserva | **Afastado** | Lock + constraint de exclusão; 20 pedidos HTTP simultâneos → 1 aceito (3/3 rodadas); escrita direta no banco recusada; JMeter 30 simultâneos → 1 aceito; 0 sobreposições após 6.164 requisições |

## 3. Metas mensuráveis

| Meta do professor | Resultado |
|---|---|
| 80% de cobertura de linhas | **97,9%** (gate no build) |
| 70% de cobertura de branches | **88,5%** (gate no build) |
| 100% dos requisitos críticos na RTM | **11/11** com teste e evidência (36/36 RF com teste) |
| 0 bugs ou vulnerabilidades críticas | **Quality Gate do SonarCloud aprovado** nos PRs (0 hotspots de segurança, 0% de duplicação). Os apontamentos da primeira análise (hash de senha no SQL, cookie do CSRF, cálculo de datas, nome de campo) foram corrigidos ou justificados no código antes do merge do PR #2. O PR #5 teve 1 issue nova apontada, que não reprova o gate e está para tratamento. Nenhum defeito crítico/alto aberto no registro. |

**QA gates das 10 stories:** 6 PASS (1.3, 1.4, 1.5, 1.6, 1.7, 1.10), 4 CONCERNS (1.1, 1.2, 1.8, 1.9), 0 FAIL — detalhes em `docs/qa/gates/`.

## 4. Avaliação por dimensão

| Dimensão (peso) | O que foi entregue | Evidência |
|---|---|---|
| Requisitos e regras de negócio (15%) | 19 decisões pendentes resolvidas e registradas; RN-025..RN-032 (inclui feriados e limite por reserva); estado "não compareceu"; modelo de estados implementado e testado em todas as 49 combinações | `PENDENCIAS_DECISOES.md`, `REGRAS_NEGOCIO.md`, `ESTADOS_RESERVA.md`, `EstadoReservaTest` |
| Estratégia e documentação de testes (15%) | Plano de testes por risco, relatório por classe e pacote, stories com cenários e QA gates | `PLANO_DE_TESTES.md`, `RELATORIO_DE_TESTES.md`, `docs/qa/gates/` |
| Qualidade dos testes automatizados (25%) | 283 testes: unitários, parametrizados, integração com PostgreSQL real, API caixa-preta, concorrência real, WireMock, E2E; TDD demonstrado com RED/GREEN no CI | `RELATORIO_DE_TESTES.md`, `evidencias/tdd-1.5-*.md` |
| CI, cobertura e análise estática (15%) | GitHub Actions, gate JaCoCo e Quality Gate do SonarCloud aprovado nos PRs, restrito ao código do projeto | `ci.yml`, `pom.xml`, comentários do SonarCloud nos PRs #2 a #5 |
| Performance, segurança e robustez (10%) | JMeter com metas atendidas; BCrypt, CSRF, CSP, cabeçalhos, erros sem stack trace, falha externa sem reverter negócio | `RELATORIO_CARGA.md`, `SegurancaApiTest`, `ErrosApiTest`, `NotificacaoWireMockTest` |
| Rastreabilidade e gestão de defeitos (10%) | RTM requisito→risco→teste→evidência; 14 defeitos priorizados com commit de correção | `RTM.md`, `REGISTRO_DEFEITOS.md` |
| Organização, apresentação e autoria (10%) | Histórico em commits por story (Conventional Commits), README, roteiro de apresentação | `git log`, `README.md`, `APRESENTACAO.md` — **revisão por pares e participação de todos os membros dependem da equipe** |

## 5. Riscos residuais e pendências

| Item | Impacto | Ação |
|---|---|---|
| `main` sem proteção | Repetição do DEF-001 (ocorreu uma vez, registrada) | Exigir PR + 1 aprovação + CI verde em *Settings → Rules* |
| Revisão por pares e autoria | Critério "Organização, apresentação e autoria"; prova oral | Revisão humana do PR por um colega; cada membro estudar e apresentar uma parte (ver `APRESENTACAO.md`) |
| Decisões de negócio adotadas sem confirmação formal do PO | Professor pode questionar regras (ex.: SOLICITADA ocupa horário) | PO confirmar ou ajustar `PENDENCIAS_DECISOES.md`; os testes indicados mostram o impacto |
| Escopo do Responsável é global | Responsável aprova qualquer recurso | Evolução: responsável por recurso (DP-003) |
| Notificação enviada de forma síncrona após o commit | Com o provedor lento, a resposta da reserva espera até o timeout (2 s), embora a reserva já esteja gravada | Evolução: envio assíncrono (gate 1.8) |
| Notificação com falha não é reenviada | Mensagem perdida fica registrada como FALHA | Evolução: reprocessamento |
| Tela de usuários ausente | Cadastro e edição de **usuários** existem só na API (Swagger); a edição de **recursos** já tem tela | Evolução da interface |
| Recuperação de senha ausente | Não há "esqueci minha senha" (exigiria envio de SMS ou e-mail por um provedor externo) | Evolução: provedor de SMS/e-mail com a mesma interface de notificação |
| Feriados locais dependem de configuração | Só os feriados nacionais são calculados | Informar recessos em `SOR_FERIADOS_EXTRAS` |
| Carga medida em uma única máquina | Números não dimensionam produção | Repetir em ambiente separado se necessário |

## 6. Recomendação

Liberar o RC para a apresentação após a proteção da `main` e a distribuição das partes entre os integrantes. Não há defeito aberto que bloqueie a demonstração.

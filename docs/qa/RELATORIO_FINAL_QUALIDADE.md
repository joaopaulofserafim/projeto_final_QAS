# Relatório final de qualidade — Release Candidate 1.0.0-rc1

**Data:** 09/11/2026 · **Branch:** `feat/epic-1-mvp` · **Escopo:** Epic 1 (stories 1.1 a 1.10)

## 1. Conclusão

O RC cumpre os 14 requisitos funcionais do enunciado e as regras críticas, com evidência automatizada no CI. Os quatro limitadores de nota estão afastados com prova executável. Ficam pendentes três ações manuais do dono do repositório (SonarCloud, proteção da `main` e revisão humana do PR), descritas na seção 5.

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
| 80% de cobertura de linhas | **97,7%** (gate no build) |
| 70% de cobertura de branches | **88,0%** (gate no build) |
| 100% dos requisitos críticos na RTM | **11/11** com teste e evidência (36/36 RF com teste) |
| 0 bugs ou vulnerabilidades críticas | **Não verificado no SonarCloud** (falta o token). Nenhum defeito crítico/alto aberto no registro. |

**QA gates das 10 stories:** 6 PASS (1.3, 1.4, 1.5, 1.6, 1.7, 1.10), 4 CONCERNS (1.1, 1.2, 1.8, 1.9), 0 FAIL — detalhes em `docs/qa/gates/`.

## 4. Avaliação por dimensão

| Dimensão (peso) | O que foi entregue | Evidência |
|---|---|---|
| Requisitos e regras de negócio (15%) | 19 decisões pendentes resolvidas e registradas; RN-025..RN-030; estado "não compareceu"; modelo de estados implementado e testado em todas as 49 combinações | `PENDENCIAS_DECISOES.md`, `REGRAS_NEGOCIO.md`, `ESTADOS_RESERVA.md`, `EstadoReservaTest` |
| Estratégia e documentação de testes (15%) | Plano de testes por risco, relatório por classe e pacote, stories com cenários e QA gates | `PLANO_DE_TESTES.md`, `RELATORIO_DE_TESTES.md`, `docs/qa/gates/` |
| Qualidade dos testes automatizados (25%) | 247 testes: unitários, parametrizados, integração com PostgreSQL real, API caixa-preta, concorrência real, WireMock, E2E; TDD demonstrado com RED/GREEN no CI | `RELATORIO_DE_TESTES.md`, `evidencias/tdd-1.5-*.md` |
| CI, cobertura e análise estática (15%) | GitHub Actions, gate JaCoCo, Sonar configurado e restrito ao código do projeto | `ci.yml`, `pom.xml` — **Sonar pendente do token** |
| Performance, segurança e robustez (10%) | JMeter com metas atendidas; BCrypt, CSRF, CSP, cabeçalhos, erros sem stack trace, falha externa sem reverter negócio | `RELATORIO_CARGA.md`, `SegurancaApiTest`, `ErrosApiTest`, `NotificacaoWireMockTest` |
| Rastreabilidade e gestão de defeitos (10%) | RTM requisito→risco→teste→evidência; 12 defeitos priorizados com commit de correção | `RTM.md`, `REGISTRO_DEFEITOS.md` |
| Organização, apresentação e autoria (10%) | Histórico em commits por story (Conventional Commits), README, roteiro de apresentação | `git log`, `README.md`, `APRESENTACAO.md` — **revisão por pares e participação de todos os membros dependem da equipe** |

## 5. Riscos residuais e pendências

| Item | Impacto | Ação |
|---|---|---|
| SonarCloud não executado | Meta "0 bugs/vulnerabilidades críticas" sem evidência | Criar projeto e segredo `SONAR_TOKEN` (README, seção 6) e conferir o Quality Gate |
| `main` sem proteção | Repetição do DEF-001 | Exigir PR + 1 aprovação + CI verde |
| Revisão por pares e autoria | Critério "Organização, apresentação e autoria"; prova oral | Revisão humana do PR por um colega; cada membro estudar e apresentar uma parte (ver `APRESENTACAO.md`) |
| Decisões de negócio adotadas sem confirmação formal do PO | Professor pode questionar regras (ex.: SOLICITADA ocupa horário) | PO confirmar ou ajustar `PENDENCIAS_DECISOES.md`; os testes indicados mostram o impacto |
| Escopo do Responsável é global | Responsável aprova qualquer recurso | Evolução: responsável por recurso (DP-003) |
| Notificação enviada de forma síncrona após o commit | Com o provedor lento, a resposta da reserva espera até o timeout (2 s), embora a reserva já esteja gravada | Evolução: envio assíncrono (gate 1.8) |
| Notificação com falha não é reenviada | Mensagem perdida fica registrada como FALHA | Evolução: reprocessamento |
| Telas de edição de recurso e de usuários ausentes | Funções existem só na API (Swagger) | Evolução da interface (gate 1.9) |
| Carga medida em uma única máquina | Números não dimensionam produção | Repetir em ambiente separado se necessário |

## 6. Recomendação

Liberar o RC para a apresentação após as três ações manuais. Não há defeito aberto que bloqueie a demonstração.

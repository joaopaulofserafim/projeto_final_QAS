# RTM — Matriz de Rastreabilidade (requisito → risco → teste → evidência)

**Versão:** 1.0.0-rc1 · **Data:** 10/11/2026 · **Fonte dos requisitos:** `PRD/requisitos/` · **Riscos:** `PRD/arquitetura/RISCOS_ARQUITETURAIS.md`
A matriz de origem (persona → jornada → requisito → critério de aceite) continua em [`PRD/requisitos/MATRIZ_RASTREABILIDADE.md`](PRD/requisitos/MATRIZ_RASTREABILIDADE.md). Esta matriz acrescenta **implementação, testes automatizados e evidências**.

**Cobertura da matriz:** 36/36 requisitos funcionais e 14/14 não funcionais com teste automatizado. **Requisitos críticos (11): 11/11 com teste e evidência.**

## Legenda das evidências

| Código | Evidência |
|---|---|
| **CI** | Pipeline do GitHub Actions verde com a suíte completa (283 testes, incluindo E2E): [run 42722036100](https://github.com/joaopaulofserafim/projeto_final_QAS/actions/runs/42722036100) e execuções seguintes (PRs #2 a #5), todas com o Quality Gate do SonarCloud aprovado |
| **TDD** | RED → GREEN da story 1.5: [`tdd-1.5-red.md`](docs/qa/evidencias/tdd-1.5-red.md) (CI vermelho: [run 42718786331](https://github.com/joaopaulofserafim/projeto_final_QAS/actions/runs/42718786331)) e [`tdd-1.5-green.md`](docs/qa/evidencias/tdd-1.5-green.md) (CI verde: [run 42719530475](https://github.com/joaopaulofserafim/projeto_final_QAS/actions/runs/42719530475)) |
| **COB** | Relatório JaCoCo publicado como artefato do CI (`relatorios-de-qualidade`): 97,9% linhas, 88,5% branches |
| **CARGA** | [`docs/qa/RELATORIO_CARGA.md`](docs/qa/RELATORIO_CARGA.md) e [`statistics-oficial-2026-11-09.json`](docs/qa/evidencias/carga/statistics-oficial-2026-11-09.json) |
| **TELA** | Capturas em [`docs/qa/evidencias/telas/`](docs/qa/evidencias/telas/) |
| **GATE** | QA gate da story em [`docs/qa/gates/`](docs/qa/gates/) |

Os testes ficam em `src/test/java/br/edu/sor/` (o pacote está omitido abaixo). Tipos: **U** unitário, **P** parametrizado, **I** integração com PostgreSQL real (Testcontainers), **A** API caixa-preta por HTTP, **C** concorrência real, **W** WireMock, **E** ponta a ponta no navegador, **L** carga (JMeter).

## Requisitos funcionais

| ID | Requisito | Crítico | Risco | Story | Implementação | Testes automatizados | Evidência |
|---|---|:-:|---|---|---|---|---|
| RF-001 | Autenticar usuário | **Sim** | RA-004, RA-012 | 1.3 | `identidade/SecurityConfig`, `POST /api/v1/auth/login`, HTTP Basic | `SegurancaApiTest.cadaPerfilAutentica…` (P), `senhaErradaEhRecusada`, `usuarioDesativadoNaoAutentica`, `loginComSenhaErrada…` (A); `InterfaceE2ETest.loginInvalido…` (E) | CI, GATE |
| RF-002 | Autorização por perfil e escopo | **Sim** | RA-004 | 1.3, 1.5, 1.6 | Regras por URL em `SecurityConfig` + verificação de dono em `ReservaTransacoes` | `SegurancaApiTest.perfilSemPermissaoRecebe403` (P, 26 combinações), `nenhumEndpointDaApiAceitaAcessoAnonimo` (varre o OpenAPI); `ReservaApiTest.solicitanteNaoVeReservaDeOutraPessoa…`; `CicloDeVidaReservaTest.solicitanteNaoCancela…`, `alteracaoSoPeloDono…` (A) | CI, GATE |
| RF-003 | Encerrar sessão | | RA-012 | 1.3 | `POST /api/v1/auth/logout` | `SegurancaApiTest.sessaoWebExigeTokenCsrf…EncerraNoLogout` (A); `InterfaceE2ETest.sairEncerraASessao` (E) | CI |
| RF-004 | Pesquisar recursos | | RA-007 | 1.4 | `GET /api/v1/recursos` (Specification + porta de ocupação) | `RecursoApiTest.pesquisaCombinaFiltros` (P, 8 filtros), `filtroDeDisponibilidadeExigePeriodoCompleto`; `ReservaApiTest.consultaDeDisponibilidadeAgendaEPesquisa…` (A); `InterfaceE2ETest.solicitantePesquisa…` (E) | CI, TELA |
| RF-005 | Consultar detalhes de recurso | | — | 1.4 | `GET /api/v1/recursos/{id}` | `RecursoApiTest.detalhaRecursoComCompetencias`, `recursoInexistenteRetorna404` (A) | CI |
| RF-006 | Validar período informado (término após o início, início futuro, **sem feriados — RN-031**) | **Sim** | RA-001 | 1.1, 1.5 | `disponibilidade/PeriodoReserva`, `Periodos`, `CalendarioAcademico` | `PeriodoReservaTest` (U/P, 18); `PeriodosTest` (P, 7); `ReservaApiTest.periodoInvalido…` (P, inclui feriado e período que cruza feriado), `sobreposicaoNoMesmoRecurso…` (P, 7 intervalos); `CalendarioAcademicoTest` (P, 31: Páscoa, feriados fixos e móveis, recesso configurado, meia-noite); `interfaceConsultaOsFeriadosConfigurados` (A); `ConstraintExclusaoTest.bancoRecusaPeriodoInvertido` (I) | CI, TDD, GATE 1.1 |
| RF-007 | Verificar disponibilidade e conflitos (inclusive professor) | **Sim** | RA-001, RA-002, RA-007 | 1.4, 1.5 | `DisponibilidadeService.consultar/garantirLivre`, `GET /api/v1/disponibilidade` | `ReservaApiTest.sobreposicao…`, `professorNaoPodeSerAlocadoEmDuasSalas…`, `recursoDesativadoNaoPodeSerReservado`, `consultaDeDisponibilidade…` (A); `ManutencaoApiTest.recursoEmManutencao…` (A) | CI, TDD |
| RF-008 | Impedir dupla aceitação concorrente | **Sim** | RA-001 | 1.5 | Lock `SELECT … FOR UPDATE` em ordem + constraint `EXCLUDE USING gist` (ADR-004) | `ConcorrenciaReservaTest` (C, 5 cenários, 20 threads); `ConstraintExclusaoTest` (I, 3); `ManutencaoApiTest.manutencaoEReservasSimultaneas…` (C); `CicloDeVidaReservaTest.aprovacoesSimultaneas…` (C); corrida JMeter 30 simultâneos (L) | CI, TDD, CARGA |
| RF-009 | Criar solicitação de reserva | **Sim** | RA-001 | 1.5 | `POST /api/v1/reservas` | `ReservaApiTest.reservaDeRecursosComunsNasceAprovada…`, `pedidoComVariosRecursosEhTudoOuNada`, `reservaNaoPodePrenderOCatalogoInteiro` (RN-032: 1 sala, 1 professor, 3 itens), `camposObrigatoriosAusentes…`, `recursoInexistenteRetorna404` (A); `InterfaceE2ETest.solicitanteReservaVeResultadoEmJanela…`, `conflitoNoMomentoDoEnvio…` (E) | CI, TDD, TELA |
| RF-010 | Encaminhar recurso restrito para aprovação | **Sim** | RA-004 | 1.5, 1.6 | Estado inicial `SOLICITADA` + justificativa obrigatória (RN-026) | `ReservaApiTest.recursoRestritoExigeJustificativa…`; `CicloDeVidaReservaTest.responsavelVeFilaEAprova…` (A); `SegurancaApiTest` (Admin/Solicitante aprovar → 403) | CI |
| RF-011 | Consultar reservas próprias | | — | 1.5 | `GET /api/v1/reservas/minhas` | `ReservaApiTest.minhasReservasFiltraPorEstado`, `solicitanteNaoVeReserva…` (A); `InterfaceE2ETest.solicitantePesquisa…` (E) | CI |
| RF-012 | Consultar histórico de reserva | | RA-005 | 1.6 | `GET /api/v1/reservas/{id}/historico` | `CicloDeVidaReservaTest.responsavelVeFilaEAprova…`, `historicoDaReservaNaoEhVisivel…` (A) | CI |
| RF-013 | Alterar reserva própria | | RA-001 | 1.6 | `PUT /api/v1/reservas/{id}` (revalida no ponto atômico) | `CicloDeVidaReservaTest.alteracaoRevalidaConflito…`, `alterarReservaAprovadaDeRecursoRestrito…`, `alteracaoSoPeloDonoEAntesDoUso` (A) | CI |
| RF-014 | Cancelar reserva própria | **Sim** | RA-005 | 1.6 | `POST /api/v1/reservas/{id}/cancelar`; sem `DELETE` | `CicloDeVidaReservaTest.solicitanteCancela…`, `solicitanteNaoCancela…`, `reservaIniciadaNaoPodeSerCanceladaNemApagada`, `reservaAprovadaCujoHorarioJaComecou…` (A) | CI |
| RF-015 | Fila de solicitações pendentes | | RA-004 | 1.6 | `GET /api/v1/reservas?estado=SOLICITADA` | `CicloDeVidaReservaTest.responsavelVeFila…`, `consultaAdministrativa…` (A); `InterfaceE2ETest.responsavelAprova…` (E) | CI, TELA |
| RF-016 | Aprovar solicitação restrita | **Sim** | RA-004, RA-001 | 1.6 | `POST /api/v1/reservas/{id}/aprovar` (somente Responsável) | `CicloDeVidaReservaTest.responsavelVeFilaEAprova…`, `aprovarDuasVezes…`, `aprovacoesSimultaneas…` (A/C); `EstadoReservaTest` (P, 58); `InterfaceE2ETest.responsavelAprova…` (E) | CI, GATE |
| RF-017 | Rejeitar com justificativa | | RA-004 | 1.6 | `POST /api/v1/reservas/{id}/rejeitar` | `CicloDeVidaReservaTest.rejeicaoExigeMotivoELiberaOHorario` (A) | CI |
| RF-018 | Consultar disponibilidade docente | | RA-007 | 1.4, 1.5 | Professor = recurso; `GET /api/v1/disponibilidade/recursos/{id}/agenda` | `ReservaApiTest.professorNaoPode…`, `consultaDeDisponibilidadeAgenda…` (A); `ConcorrenciaReservaTest.mesmoProfessorDisputado…` (C) | CI |
| RF-019 | Registrar retirada | | RA-006 | 1.7 | `POST /api/v1/movimentacoes/retiradas` | `MovimentacaoApiTest.retiradaIniciaOUso…`, `retiradaEDevolucaoDuplicadas…`, `soMateriaisEEquipamentos…`, `retiradaExigeReserva…` (A) | CI |
| RF-020 | Registrar devolução | | RA-006 | 1.7 | `POST /api/v1/movimentacoes/{id}/devolucao` | `MovimentacaoApiTest.devolucaoAposOPrazo…`, `reservaComMaterialPendente…` (A) | CI |
| RF-021 | Movimentações e pendências | | RA-006 | 1.7 | `GET /api/v1/movimentacoes?situacao=ABERTAS` | `MovimentacaoApiTest` (atrasada, abertas por reserva) (A); `RelatorioApiTest.resumo…` | CI |
| RF-022 | Gerenciar usuários e permissões | | RA-004, RA-009 | 1.3 | `/api/v1/usuarios` | `UsuarioApiTest` (A, 6) | CI |
| RF-023 | Gerenciar salas | | RA-009 | 1.4 | `/api/v1/recursos` | `RecursoApiTest` (validações P, alteração, desativação) (A); `ReservaApiTest.recursoComReservaFuturaNaoPodeSerDesativado` | CI |
| RF-024 | Gerenciar professores | | RA-007 | 1.4 | `/api/v1/recursos` (tipo PROFESSOR, competências) | `RecursoApiTest.pesquisaCombinaFiltros` (competência), `validacoesDeCadastro…` (A) | CI |
| RF-025 | Gerenciar materiais e equipamentos | | RA-009 | 1.4 | `/api/v1/recursos` (tipos MATERIAL/EQUIPAMENTO) | `RecursoApiTest` (A) | CI |
| RF-026 | Registrar período de manutenção | **Sim** | RA-003 | 1.7 | `ManutencaoService` (mesmo ponto atômico; recusa com lista de reservas) | `ManutencaoApiTest` (A/C, 6) | CI, GATE |
| RF-027 | Consultar reservas administrativamente | | — | 1.6 | `GET /api/v1/reservas?estado&recursoId&de&ate` | `CicloDeVidaReservaTest.consultaAdministrativaFiltra…` (A) | CI |
| RF-028 | Dashboard operacional | | RA-011 | 1.8 | `GET /api/v1/relatorios/resumo`; aba Administração | `RelatorioApiTest.resumoOperacionalDoPainel` (A); `InterfaceE2ETest.interfaceFuncionaEmTelaDeCelular…` (E) | CI, TELA |
| RF-029 | Consultar auditoria | | RA-005 | 1.3, 1.8 | `GET /api/v1/auditoria` | `AuditoriaApiTest.administradorConsultaEventos…`, `filtraPorEntidadeEJanela…`, `apiNaoOfereceAlteracao…` (A) | CI |
| RF-030 | Registrar auditoria de operações relevantes | **Sim** | RA-005 | 1.3–1.7 | `AuditoriaService` (`Propagation.MANDATORY`) + trigger imutável | `AuditoriaApiTest.bancoRecusaAlterarOuApagar…` (I); contagem de eventos em `ReservaApiTest`, `CicloDeVidaReservaTest`, `MovimentacaoApiTest`, `ManutencaoApiTest`, `RecursoApiTest`, `UsuarioApiTest` | CI |
| RF-031 | Confirmar registro de solicitação | | RA-008 | 1.8 | `NotificacaoService` (após commit) | `NotificacaoApiTest` (A); `NotificacaoWireMockTest.notificacaoEnviada…` (W) | CI |
| RF-032 | Comunicar decisões e alterações | | RA-008 | 1.8 | Idem | `NotificacaoApiTest.solicitacaoRestritaNotifica…`, `alteracaoCancelamento…` (A) | CI |
| RF-033 | Falha de notificação não reverte operação | | RA-008 | 1.8 | Envio após commit em transação própria | `NotificacaoWireMockTest.erroDoProvedor…`, `provedorLento…` (W) | CI |
| RF-034 | Utilização por recurso | | RA-011 | 1.8 | `GET /api/v1/relatorios/utilizacao` | `RelatorioApiTest.utilizacao…` (A, 2); `CalculadoraHorasTest` (P, 12) | CI |
| RF-035 | Carga horária, reservas e cancelamentos | | RA-011 | 1.8 | `GET /api/v1/relatorios/carga-horaria`; resumo por estado | `RelatorioApiTest.cargaHorariaPorProfessor`, `resumoOperacional…` (A) | CI |
| RF-036 | Conflitos impedidos | | RA-011 | 1.5, 1.8 | Tabela `conflito_evitado`; `GET /api/v1/relatorios/conflitos` | `RelatorioApiTest.conflitosEvitados…`; `ReservaApiTest.cadaTentativaRecusada…`; `ConcorrenciaReservaTest` (19 registros) | CI, CARGA |

## Requisitos não funcionais

| ID | Requisito | Risco | Verificação automatizada | Evidência |
|---|---|---|---|---|
| RNF-001 | Controle de autenticação e acesso | RA-004 | `SegurancaApiTest` (matriz, varredura OpenAPI, CSRF) | CI |
| RNF-002 | Confidencialidade e minimização | RA-004 | Conflito sem `reservaId` para terceiros (`ReservaApiTest`); agenda sem dono; usuário sem hash (`SegurancaApiTest`, `UsuarioApiTest`); payload mínimo (`NotificacaoWireMockTest`) | CI |
| RNF-003 | Erros seguros e compreensíveis | — | `ErrosApiTest`, `ApiExceptionHandlerTest`, `InterfaceE2ETest.loginInvalido…` | CI, TELA |
| RNF-004 | Integridade sob concorrência | RA-001 | `ConcorrenciaReservaTest`, `ConstraintExclusaoTest`, JMeter (corrida) | CI, CARGA |
| RNF-005 | Integridade de estados e dados | RA-005, RA-006 | `EstadoReservaTest` (49 pares), tudo-ou-nada, auditoria na mesma transação | CI |
| RNF-006 | Desempenho de consultas | RA-010 | JMeter: p95 6–8 ms (meta ≤ 500 ms) | CARGA |
| RNF-007 | Capacidade sob carga | RA-010 | JMeter: 50 usuários + 10 escrevendo, 0% de erro | CARGA |
| RNF-008 | Confiabilidade em falhas | RA-008 | `NotificacaoWireMockTest` (500 e timeout não desfazem a reserva) | CI |
| RNF-009 | Usabilidade e feedback | — | `InterfaceE2ETest` (janela central de confirmação, indisponibilidade e cancelamento; erro de login discreto; cartões bloqueados; campos e seleção limpos após reservar; histórico por cartão) | CI, TELA |
| RNF-010 | Responsividade e acessibilidade | — | `InterfaceE2ETest.interfaceFuncionaEmTelaDeCelular…` (sem rolagem horizontal em 390 px); rótulos em todos os campos | CI, TELA |
| RNF-011 | Integridade da auditoria | RA-005 | `AuditoriaApiTest` (imutável no banco, sem endpoint de alteração) | CI |
| RNF-012 | Observabilidade de falhas | — | Código de rastreio em erro 500 (`ApiExceptionHandlerTest`); log de falha de notificação | CI |
| RNF-013 | Testabilidade | — | 283 testes no CI; gate JaCoCo 80/70; Quality Gate do SonarCloud | CI, COB |
| RNF-014 | Consistência documental | RA-014 | Esta RTM; stories 1.1–1.10 com QA gate | GATE |

## Riscos arquiteturais → mitigação verificada

| Risco | Mitigação implementada | Verificação |
|---|---|---|
| RA-001 dupla reserva | Lock por recurso + constraint de exclusão | `ConcorrenciaReservaTest`, `ConstraintExclusaoTest`, JMeter |
| RA-002 efeito de SOLICITADA | SOLICITADA ocupa horário (DP-001) | `EstadoReservaTest`, `ConcorrenciaReservaTest` |
| RA-003 manutenção x reserva | Manutenção recusada com lista de reservas (DP-004) | `ManutencaoApiTest` |
| RA-004 acesso excessivo | Regras por URL + dono no serviço; Admin não aprova | `SegurancaApiTest` |
| RA-005 reserva e auditoria divergentes | Auditoria `MANDATORY` na mesma transação; trigger imutável | `AuditoriaApiTest`, contagens de eventos |
| RA-006 movimentação x estado | Regras explícitas de RN-030 | `MovimentacaoApiTest` |
| RA-007 agenda docente | Professor como recurso | `ReservaApiTest`, `ConcorrenciaReservaTest` |
| RA-008 canal externo | Envio após commit, falha registrada | `NotificacaoWireMockTest` |
| RA-009 desativação | Desativação lógica bloqueada com reserva futura | `RecursoApiTest`, `ReservaApiTest` |
| RA-010 metas sem número | Metas de DP-014 medidas | `RELATORIO_CARGA.md` |
| RA-011 relatório sem fonte | Critério e fonte em cada relatório | `RelatorioApiTest` |
| RA-012 identidade | Spring Security próprio (DP-019) | `SegurancaApiTest` |
| RA-013 API não descrita | OpenAPI publicado | `SegurancaApiTest` (varre o contrato), `InterfaceE2ETest` |
| RA-014 dependências circulares | Portas `OcupacaoDeRecursos` e `PendenciasDeDevolucao` | Contexto Spring sobe sem ciclos (todos os testes de integração) |

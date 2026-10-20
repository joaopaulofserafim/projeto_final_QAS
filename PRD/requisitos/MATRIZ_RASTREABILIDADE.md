# Matriz de Rastreabilidade — RTM

## Como ler

As linhas ligam objetivos/necessidades de personas a jornadas, RF/RNF, regras e criterios de aceite. Codigos `CA-*` referem-se a criterios documentados em `REQUISITOS_FUNCIONAIS.md` e `V-RNF-*` a verificacoes documentadas em `REQUISITOS_NAO_FUNCIONAIS.md`; nao sao IDs de testes. **Teste futuro: Pendente** significa que nenhum teste foi criado nesta etapa. Componentes C4, diagramas de sequencia, IDs/resultados de teste, defeitos, implementacao e evidencias executadas permanecem sem referencia ate existirem.

## Tipo e status dos registros

| Tipo | Identificadores | Status da definicao | Testes/arquitetura/implementacao |
|---|---|---|---|
| RF | RF-001 a RF-036 | Propostos para validacao; nao aprovados formalmente | Cenarios conceituais registrados; testes executaveis, arquitetura e implementacao pendentes |
| RNF | RNF-001 a RNF-014 | Propostos para validacao; metas quantitativas indicadas como pendentes quando ausentes | Verificacao futura descrita; testes executaveis, arquitetura e implementacao pendentes |
| RN | RN-001 a RN-024 | Regras extraidas; decisoes abertas referenciadas em DP | Testes executaveis, arquitetura e implementacao pendentes |

## Rastreabilidade de requisitos funcionais

| ID | Persona / objetivo ou necessidade | Jornada(s) | RN relacionada(s) | RNF aplicavel(is) | Prioridade | Criterio(s) de aceite | Cenario futuro / teste |
|---|---|---|---|---|---|---|---|
| RF-001 | Todos: acessar area autorizada | J-SOL-001, J-RES-001, J-ADM-001 | RN-015, RN-022 | RNF-001, 002, 003 | Obrigatorio | CA-RF-001-01..02 | Credencial valida/invalida; teste pendente |
| RF-002 | Todos: respeitar perfis e escopos | Todas | RN-010, 015, 019, 022 | RNF-001, 002, 003 | Obrigatorio | CA-RF-002-01..03 | Acesso cruzado negado por perfil/escopo; teste pendente |
| RF-003 | Todos: encerrar sessao | J-SOL-001, J-ADM-001 | RN-015 | RNF-001, 003 | Obrigatorio | CA-RF-003-01 | Contexto encerrado nao autoriza acao; teste pendente |
| RF-004 | Solicitante: encontrar recursos rapidamente | J-SOL-002; J-ADM-002, 004-006 | RN-007, 008, 015 | RNF-001, 002, 003, 006, 009, 010 | Obrigatorio | CA-RF-004-01..03 | Filtros e erro distinto de vazio; teste pendente |
| RF-005 | Todos: consultar caracteristicas e estado | J-SOL-002; J-RES-002, 006-007; J-ADM-004-006 | RN-008, 015 | RNF-001, 002, 003, 009 | Obrigatorio | CA-RF-005-01..02 | Detalhe de recurso desativado; teste pendente |
| RF-006 | Todos: usar periodo valido e entender conflito | J-SOL-003-004, 006; J-RES-003, 005; J-ADM-007 | RN-001, 002 | RNF-003, 004, 005, 013 | Obrigatorio | CA-RF-006-01..03 | Periodo invalido, sobreposto e adjacente; teste pendente |
| RF-007 | Todos: evitar alocar recurso/professor indisponivel | J-SOL-003-004, 006; J-RES-002-003, 005; J-ADM-004-007 | RN-002-004, 006-008 | RNF-003-005, 008 | Obrigatorio | CA-RF-007-01..03 | Conflito, manutencao, professor e falha de consulta; teste pendente |
| RF-008 | Solicitante/Responsavel: evitar dupla reserva | J-SOL-004, 006; J-RES-003; J-ADM-007 | RN-002, 004-006 | RNF-004, 005, 007, 013 | Obrigatorio | CA-RF-008-01..03 | Duas solicitacoes simultaneas; teste pendente |
| RF-009 | Solicitante: pedir recurso e receber estado | J-SOL-004 | RN-001-009, 015 | RNF-001, 003-009 | Obrigatorio | CA-RF-009-01..04 | Pedido valido/invalido, manutencao e conflito; teste pendente |
| RF-010 | Solicitante/Responsavel: encaminhar restrito | J-SOL-004; J-RES-002-003 | RN-009-011 | RNF-001, 002, 005, 009 | Obrigatorio | CA-RF-010-01..02 | Autoaprovacao e responsavel fora de escopo; teste pendente |
| RF-011 | Solicitante: acompanhar reservas proprias | J-SOL-005 | RN-015, 019 | RNF-001-003, 009 | Obrigatorio | CA-RF-011-01..02 | Filtros/estados e propriedade; teste pendente |
| RF-012 | Todos conforme escopo: entender historico | J-SOL-005; J-RES-002, 008; J-ADM-008 | RN-015, 016, 018, 019 | RNF-001, 002, 011 | Obrigatorio | CA-RF-012-01..02 | Historico proprio/alheio e nao editavel; teste pendente |
| RF-013 | Solicitante: alterar propria reserva se elegivel | J-SOL-006 | RN-001-008, 013, 016, 019 | RNF-001, 003-005, 011 | Obrigatorio | CA-RF-013-01..03 | Estado elegivel, conflito, dados obsoletos; teste pendente |
| RF-014 | Solicitante: cancelar propria reserva se elegivel | J-SOL-007 | RN-013, 016, 019, 023 | RNF-001, 003, 005, 008, 011 | Obrigatorio | CA-RF-014-01..03 | Permitido, proibido e repetido; teste pendente |
| RF-015 | Responsavel: localizar fila pendente | J-RES-001-002, 008 | RN-010, 011, 015 | RNF-001-003, 006, 009 | Obrigatorio | CA-RF-015-01..02 | Filtro e isolamento do escopo; teste pendente |
| RF-016 | Responsavel: aprovar restrito se autorizado | J-RES-003 | RN-002-012, 016, 023 | RNF-001, 003-005, 008, 011 | Obrigatorio | CA-RF-016-01..04 | Aprovacao autorizada/negada, stale state, audit; teste pendente |
| RF-017 | Responsavel: rejeitar com motivo | J-RES-002, 004 | RN-010-012, 014, 016, 023 | RNF-001-003, 005, 008, 011 | Obrigatorio | CA-RF-017-01..02 | Rejeicao valida/duplicada/nao autorizada; teste pendente |
| RF-018 | Solicitante/Responsavel: validar docente | J-SOL-002-004; J-RES-005; J-ADM-005 | RN-001-003, 015 | RNF-001-004, 009 | Obrigatorio | CA-RF-018-01..02 | Docente ocupado e consulta indisponivel; teste pendente |
| RF-019 | Responsavel: rastrear retirada | J-RES-006 | RN-010, 016, 020 | RNF-001, 003, 005, 011-013 | Obrigatorio | CA-RF-019-01..03 | Autorizada, fora de escopo, duplicada; teste pendente |
| RF-020 | Responsavel: registrar retorno | J-RES-007 | RN-010, 016, 021 | RNF-001, 003, 005, 008, 011-013 | Obrigatorio | CA-RF-020-01..03 | Retirada ausente, retorno duplicado; teste pendente |
| RF-021 | Responsavel: localizar movimentos pendentes | J-RES-006-008; J-ADM-006 | RN-010, 015, 020-021 | RNF-001-003, 006, 009, 011 | Obrigatorio | CA-RF-021-01..02 | Pendente, sem prazo, fora de escopo; teste pendente |
| RF-022 | Administrador: gerir usuarios/permissoes | J-ADM-001, 003 | RN-015, 017, 022 | RNF-001-003, 005, 011-014 | Obrigatorio | CA-RF-022-01..03 | Alterar acesso e conta com reserva ativa; teste pendente |
| RF-023 | Administrador: gerir salas | J-ADM-004 | RN-008, 017, 022 | RNF-001-005, 011-014 | Obrigatorio | CA-RF-023-01..02 | Cadastro, desativacao com reserva; teste pendente |
| RF-024 | Administrador: gerir professor/competencia | J-ADM-005 | RN-003, 008, 017, 022 | RNF-001-005, 011-014 | Obrigatorio | CA-RF-024-01..02 | Alocacao conflitante e mudanca cadastral; teste pendente |
| RF-025 | Administrador: gerir materiais/equipamentos | J-ADM-006 | RN-007, 008, 017, 022 | RNF-001-005, 011-014 | Obrigatorio | CA-RF-025-01..02 | Manutencao e retirada aberta; teste pendente |
| RF-026 | Administrador: bloquear recurso para manutencao | J-ADM-002, 006-007 | RN-001, 002, 005, 007, 016-017 | RNF-001, 003-008, 011-013 | Obrigatorio | CA-RF-026-01..03 | Periodo invalido, reserva existente, concorrencia; teste pendente |
| RF-027 | Administrador: consultar reservas | J-ADM-002-008 | RN-012, 015, 022 | RNF-001-003, 006, 009, 011 | Obrigatorio | CA-RF-027-01..02 | Filtros autorizados sem alterar estado; teste pendente |
| RF-028 | Administrador: monitorar indicadores | J-ADM-002 | RN-004, 007, 012, 024 | RNF-001-003, 006-009, 012 | Obrigatorio | CA-RF-028-01..03 | Estados, manutencao e falha como nao-zero; teste pendente |
| RF-029 | Administrador/Responsavel autorizado: consultar auditoria | J-SOL-005; J-RES-002, 008; J-ADM-008 | RN-015-018, 022 | RNF-001-003, 006, 011-013 | Obrigatorio | CA-RF-029-01..03 | Consulta, erro e tentativa de edicao; teste pendente |
| RF-030 | Todos: registrar alteracoes relevantes | Jornadas com mudanca de estado/cadastro/movimento | RN-016-018 | RNF-005, 008, 011-014 | Obrigatorio | CA-RF-030-01..03 | Campos completos e falha de auditoria; teste pendente |
| RF-031 | Solicitante: confirmar recebimento | J-SOL-004, 008 | RN-023 | RNF-003, 008-009, 012 | Obrigatorio | CA-RF-031-01..02 | Registro confirmado vs falha de envio; teste pendente |
| RF-032 | Solicitante/Responsavel: receber atualizacoes | J-SOL-008; J-RES-003-004, 008 | RN-014, 016, 023 | RNF-001-003, 008-009, 012 | Obrigatorio | CA-RF-032-01..02 | Decisao e justificativa ao destinatario; teste pendente |
| RF-033 | Todos: preservar operacao se comunicacao falhar | J-SOL-008; J-RES-003-004 | RN-023 | RNF-003, 005, 008, 012 | Obrigatorio | CA-RF-033-01..02 | Falha de entrega sem rollback; teste pendente |
| RF-034 | Administrador: analisar uso por recurso | J-ADM-002, 009 | RN-024 | RNF-001-003, 006, 008-009, 012 | Obrigatorio | CA-RF-034-01..02 | Agregacao por periodo/fonte; teste pendente |
| RF-035 | Administrador: analisar carga/reservas/cancelamentos | J-ADM-009 | RN-012, 024 | RNF-001-003, 006, 008-009, 012 | Obrigatorio | CA-RF-035-01..02 | Contagem pelos estados e periodo definidos; teste pendente |
| RF-036 | Administrador: picos e conflitos evitados | J-ADM-002, 009 | RN-005, 024 | RNF-001-003, 006, 008-009, 012-013 | Obrigatorio | CA-RF-036-01..02 | Sem fonte, metrica indisponivel; com evidencia, agregar; teste pendente |

## Rastreabilidade dos requisitos nao funcionais

| ID | Persona/necessidade | Jornada(s) | RF relacionados | Prioridade | Verificacao/evidencia | Teste, arquitetura e implementacao |
|---|---|---|---|---|---|---|
| RNF-001 | Todos: operacoes autorizadas | Todas | RF-001..036 | Obrigatorio | V-RNF-001; matriz de acesso e testes negados | Pendente |
| RNF-002 | Todos: confidencialidade | SOL-001/002/005/008; RES-002/005; ADM-003/008/009 | RF-001,002,004,005,012,018,022,027,029,032 | Obrigatorio | V-RNF-002; revisar dados por perfil | Pendente |
| RNF-003 | Todos: feedback seguro | Todas | RF-001..036 | Obrigatorio | V-RNF-003; classes de erro/falha | Pendente |
| RNF-004 | Todos: integridade concorrente | SOL-003/004/006; RES-003/005; ADM-004..007 | RF-006..009,013,016,026 | Obrigatorio | V-RNF-004; zero aceite conflitante | Pendente |
| RNF-005 | Todos: consistencia e preservacao | Jornadas de escrita | RF-009,013,014,016,017,019,020,022..030 | Obrigatorio | V-RNF-005; invariantes antes/depois | Pendente |
| RNF-006 | Todos: tempo de resposta | Busca, disponibilidade, filas, dashboard, auditoria, relatorios | RF-004..009,015,018,021,027..029,034..036 | Obrigatorio | V-RNF-006; latencia sob carga de referencia (meta pendente) | Pendente |
| RNF-007 | Todos: volume simultaneo | SOL-002..004; RES-002/003; ADM-002/007 | RF-004,006..009,015,016,026 | Obrigatorio | V-RNF-007; carga/latencia/integridade (meta pendente) | Pendente |
| RNF-008 | Todos: operacao previsivel em falha | Consultas, reservas, notificacoes, auditoria e relatorios | RF-004,007..009,016,017,029..036 | Obrigatorio | V-RNF-008; matriz de falhas | Pendente |
| RNF-009 | Todos: entender resultados | Todas | RF-001..036 | Obrigatorio | V-RNF-009; estado/feedback por fluxo | Pendente |
| RNF-010 | Todos, especialmente solicitante movel | Todas | RF-001..036 | Obrigatorio | V-RNF-010; viewport, teclado e leitura | Pendente |
| RNF-011 | Todos: auditoria integra e consultavel | Escritas criticas e ADM-008 | RF-012,016,017,019,020,022..030 | Obrigatorio | V-RNF-011; campos, leitura e imutabilidade | Pendente |
| RNF-012 | Administrador/equipe: investigar falhas | SOL-003/004/008; RES-003/006/007; ADM-002/007..009 | RF-007..009,016,019,020,026,028..036 | Importante | V-RNF-012; correlacao sem segredos | Pendente |
| RNF-013 | Equipe: comportamentos testaveis | Todas | RF-001..036 | Obrigatorio | V-RNF-013; requisitos e cenarios verificaveis | IDs de teste ainda nao criados |
| RNF-014 | Equipe: rastreabilidade evolutiva | Todas | RF-001..036 | Importante | V-RNF-014; IDs unicos e referencias sem quebra | Componentes/testes futuros pendentes |

## Mapeamento explicito de regras para requisitos funcionais

Esta tabela complementa a rastreabilidade por RF: cada regra de negocio possui vinculos individuais, sem depender da interpretacao de uma faixa de IDs.

| Regra | Requisitos funcionais associados |
|---|---|
| RN-001 | RF-006, RF-009, RF-013, RF-026 |
| RN-002 | RF-006, RF-007, RF-008, RF-013, RF-018, RF-026 |
| RN-003 | RF-007, RF-009, RF-013, RF-016, RF-018 |
| RN-004 | RF-007, RF-008, RF-009, RF-016, RF-028, RF-035 |
| RN-005 | RF-007, RF-008, RF-009, RF-016, RF-026 |
| RN-006 | RF-004, RF-006, RF-009, RF-013, RF-016 |
| RN-007 | RF-004, RF-006, RF-007, RF-009, RF-016, RF-026 |
| RN-008 | RF-004, RF-005, RF-007, RF-009, RF-023, RF-024, RF-025 |
| RN-009 | RF-010, RF-015, RF-016, RF-017, RF-032 |
| RN-010 | RF-002, RF-015, RF-016, RF-017, RF-018, RF-019, RF-020, RF-021 |
| RN-011 | RF-015, RF-016, RF-017 |
| RN-012 | RF-009, RF-010, RF-016, RF-017, RF-019, RF-020, RF-028, RF-030, RF-032 |
| RN-013 | RF-002, RF-013, RF-014, RF-030, RF-032 |
| RN-014 | RF-017, RF-030, RF-032 |
| RN-015 | RF-001, RF-002, RF-003, RF-004, RF-005, RF-006, RF-007, RF-008, RF-009, RF-010, RF-011, RF-012, RF-013, RF-014, RF-015, RF-016, RF-017, RF-018, RF-019, RF-020, RF-021, RF-022, RF-023, RF-024, RF-025, RF-026, RF-027, RF-028, RF-029, RF-030, RF-031, RF-032, RF-033, RF-034, RF-035, RF-036 |
| RN-016 | RF-016, RF-017, RF-019, RF-020, RF-030, RF-032 |
| RN-017 | RF-013, RF-014, RF-019, RF-020, RF-022, RF-023, RF-024, RF-025, RF-026 |
| RN-018 | RF-002, RF-012, RF-021, RF-029, RF-030 |
| RN-019 | RF-002, RF-011, RF-012, RF-013, RF-014 |
| RN-020 | RF-019, RF-021, RF-030 |
| RN-021 | RF-020, RF-021, RF-030 |
| RN-022 | RF-001, RF-002, RF-016, RF-022, RF-023, RF-024, RF-025, RF-026, RF-027, RF-028, RF-029, RF-030 |
| RN-023 | RF-031, RF-032, RF-033 |
| RN-024 | RF-028, RF-034, RF-035, RF-036 |

## Cobertura de jornadas e extensoes futuras

| Persona | Jornadas cobertas | Requisitos que representam capacidades da jornada |
|---|---|---|
| Solicitante | J-SOL-001, J-SOL-002, J-SOL-003, J-SOL-004, J-SOL-005, J-SOL-006, J-SOL-007, J-SOL-008 | RF-001..014, RF-018, RF-031..033 |
| Responsavel | J-RES-001, J-RES-002, J-RES-003, J-RES-004, J-RES-005, J-RES-006, J-RES-007, J-RES-008 | RF-001..002, RF-005, RF-010, RF-015..021, RF-029..033 |
| Administrador | J-ADM-001, J-ADM-002, J-ADM-003, J-ADM-004, J-ADM-005, J-ADM-006, J-ADM-007, J-ADM-008, J-ADM-009 | RF-001..002, RF-004..008, RF-022..030, RF-034..036 |

Componentes arquiteturais, diagramas C4/UML, IDs de testes executaveis, resultados, defeitos, implementacao e evidencias de execucao: **PENDENTES**, a preencher em etapas futuras; nenhuma referencia ficticia foi criada.

## Rastreabilidade arquitetural

Os nomes de componentes e diagramas referem-se a propostas ainda sujeitas a revisao. Coluna de teste registra o proximo cenario futuro, sem criar identificador de teste, resultado ou evidencia inexistente.

| Diagrama | Persona / jornada | RF / RNF / RN | Componentes C4 nivel 3 | ADR | Teste futuro / status |
|---|---|---|---|---|---|
| SEQ-SOL-001 | Solicitante; J-SOL-001 | RF-001..003; RN-015/022; RNF-001..003 | Entrada, Identidade e Autorizacao, Interface Web | ADR-005/008 | Credencial/perfil/logout e acesso negado; pendente |
| SEQ-SOL-002 | Solicitante; J-SOL-002 | RF-004/005/018; RN-003/007/008/015; RNF-001..003/006/009 | Catalogo, Disponibilidade, Consultas e Relatorios, Entrada | ADR-002/005/008 | Filtros, vazio, falha e agenda ausente; pendente |
| SEQ-SOL-003 | Solicitante; J-SOL-003 | RF-006/007/018; RN-001..004/006/007; RNF-003..006/008 | Disponibilidade, Catalogo, Persistencia | ADR-002/003/004 | Invalido, conflito, adjacencia e manutencao; pendente |
| SEQ-SOL-004 | Solicitante; J-SOL-004 | RF-006..010/030..033; RN-001..009/015..017/023; RNF-001/003..005/008/011 | Entrada, Identidade, Reservas, Disponibilidade, Auditoria, Comunicacao, Persistencia | ADR-004/005/006/007 | Pedido restrito/comum, conflito, falha atomica e notificacao; pendente |
| SEQ-SOL-005 | Solicitante; J-SOL-006 | RF-002/006..008/013/030/032; RN-001..008/013/016/019; RNF-001/003..005/011 | Entrada, Identidade, Reservas, Disponibilidade, Auditoria, Persistencia | ADR-002/004/006 | Propriedade, conflito, estado obsoleto; pendente |
| SEQ-SOL-006 | Solicitante; J-SOL-007 | RF-002/014/030/032/033; RN-013/016/019/023; RNF-001/003/005/008/011 | Entrada, Identidade, Reservas, Auditoria, Comunicacao | ADR-005/006/007/008 | Permitido, proibido, duplicado, falha auditavel; pendente |
| SEQ-SOL-007 | Solicitante; J-SOL-005 | RF-002/011/012; RN-015/018/019; RNF-001..003/009/011 | Entrada, Identidade, Consultas e Relatorios, Auditoria | ADR-005/006/008 | Propriedade, filtro, vazio e falha; pendente |
| SEQ-RES-001 | Responsavel; J-RES-001/002/008 | RF-002/005/015/021; RN-010/011/015; RNF-001..003/006/009 | Entrada, Identidade, Consultas e Relatorios, Disponibilidade | ADR-002/005/008 | Escopo, filtros e falha de leitura; pendente |
| SEQ-RES-002 | Responsavel; J-RES-003 | RF-002/006..008/010/015/016/030/032/033; RN-001..012/016/023; RNF-001/003..005/008/011 | Entrada, Identidade, Aprovacao, Reservas, Disponibilidade, Persistencia, Auditoria, Comunicacao | ADR-004/005/006/007 | Aprovacao concorrente autorizada/negada e conflito; pendente |
| SEQ-RES-003 | Responsavel; J-RES-004 | RF-002/017/030/032/033; RN-010..012/014/016/023; RNF-001..003/005/008/011 | Entrada, Identidade, Aprovacao, Reservas, Auditoria, Comunicacao | ADR-005/006/007/008 | Justificativa, escopo, duplicidade e falha externa; pendente |
| SEQ-RES-004 | Responsavel; J-RES-005 | RF-002/006/007/018; RN-001..003/010/015; RNF-001..004/009 | Entrada, Identidade, Disponibilidade, Catalogo, Persistencia | ADR-002/005 | Docente ocupado/agenda ausente/adjacente; pendente |
| SEQ-RES-005 | Responsavel; J-RES-006 | RF-002/019/021/030; RN-010/016/020; RNF-001/003/005/011 | Entrada, Identidade, Movimentacao, Reservas, Auditoria, Persistencia | ADR-002/006 | Reserva elegivel, fora de escopo e retirada duplicada; pendente |
| SEQ-RES-006 | Responsavel; J-RES-007 | RF-002/020/021/030; RN-010/016/021; RNF-001/003/005/011 | Entrada, Identidade, Movimentacao, Reservas, Auditoria, Persistencia | ADR-002/006 | Sem retirada, devolucao duplicada e falha auditavel; pendente |
| SEQ-RES-007 | Responsavel; J-RES-008 | RF-002/015/021/029; RN-010/015/020/021; RNF-001..003/006/009/011 | Entrada, Identidade, Consultas e Relatorios, Auditoria, Persistencia | ADR-002/005/006/008 | Pendencia sem prazo, escopo e falha; pendente |
| SEQ-ADM-001 | Administrador; J-ADM-001 | RF-001..003; RN-015/022; RNF-001..003 | Entrada, Identidade e Autorizacao, Interface Web | ADR-005/008 | Perfil admin, solicitante negado e sessao encerrada; pendente |
| SEQ-ADM-002 | Administrador; J-ADM-003 | RF-002/022/030; RN-015/017/022; RNF-001..005/011 | Entrada, Identidade, Administracao, Persistencia, Auditoria | ADR-002/005/006 | Permissao, conta ativa, impacto e falha de auditoria; pendente |
| SEQ-ADM-003 | Administrador; J-ADM-004..006 | RF-002/004/005/023..025/030; RN-003/007/008/017/022; RNF-001..005/011 | Entrada, Identidade, Administracao, Catalogo, Auditoria, Persistencia | ADR-002/003/006 | Validacao, duplicidade, vinculos ativos e historico; pendente |
| SEQ-ADM-004 | Administrador; J-ADM-007 | RF-002/006..008/026/030; RN-001/002/005/007/016/017; RNF-001/003..008/011 | Entrada, Identidade, Administracao, Disponibilidade, Persistencia, Auditoria | ADR-004/006 | Reserva existente, corrida manutencao/aceite; pendente |
| SEQ-ADM-005 | Administrador; J-ADM-008 | RF-002/012/029/030; RN-015..018/022; RNF-001..003/011/012 | Entrada, Identidade, Consultas e Relatorios, Auditoria, Persistencia | ADR-005/006/008 | Filtros, leitura, negacao e falha; pendente |
| SEQ-ADM-006 | Administrador; J-ADM-002/009 | RF-002/027/028/034..036; RN-004/007/012/024; RNF-001..003/006/008/009/012 | Entrada, Identidade, Consultas e Relatorios, Persistencia | ADR-002/008 | Agregacao definida, falha vs zero, fonte ausente; pendente |
| SEQ-CRIT-001 | Solicitantes A/B e Responsavel; J-SOL-004/J-RES-003 | RF-006..009/016/026/030; RN-001..007/011/016; RNF-004/005/007/011/013 | Reservas, Disponibilidade, Persistencia, Auditoria, Entrada | ADR-003/004/006/009 | Teste concorrente com Testcontainers; IDs ainda nao criados |

## Rastreabilidade C4 e ADR

| Artefato | Capacidade/requisitos | Evidencia e status |
|---|---|---|
| C4 nivel 1 | Personas e limite do sistema; RF-001..RF-036; RNF-001/002/008 | NIVEL_1_CONTEXTO.md; sistema externo de notificacao condicional; proposta |
| C4 nivel 2 | Web, Back-end, persistencia e canal opcional; RNF-004/005/006/008/011 | NIVEL_2_CONTEINERES.md; banco/framework de interface pendentes |
| C4 nivel 3 | Componentes de entrada, identidade, catalogo, disponibilidade, reservas, decisao, movimento, auditoria, comunicacao, consultas/persistencia | NIVEL_3_COMPONENTES.md; componentes conceituais, proposta |
| Estados | RN-011..RN-014; RF-009/016/017/019/020 | ESTADOS_RESERVA.md; transicoes `EM_USO`, `CONCLUIDA` e cancelamento pendentes |
| ADR-001..ADR-009 | Forma, modulos, persistencia, concorrencia, identidade, auditoria, integracao, erros e testes | `PRD/arquitetura/adr/`; status Proposta/Pendente, nenhum aprovado |
| Riscos RA-001..RA-014 | RNF-001..RNF-014 e operacoes criticas | RISCOS_ARQUITETURAIS.md; mitigacoes propostas, sem evidencia executada |

## Rastreabilidade RF para componentes e sequencias

Cada requisito funcional pertence a pelo menos um componente proposto no C4 nivel 3. As sequencias listadas detalham os fluxos obrigatorios que exercitam a capacidade; requisicoes de consulta/admin sem sequencia exclusiva ficam cobertas pelo componente e pela sequencia de consulta/relatorio mais proxima, sem criar diagrama adicional implicitamente.

| Componente C4 nivel 3 | RF associados | Sequencias que detalham a interacao | Estado |
|---|---|---|---|
| Entrada da Aplicacao; Identidade e Autorizacao | RF-001, RF-002, RF-003 | SEQ-SOL-001, SEQ-RES-001, SEQ-ADM-001; autorizacao reaparece em todas as sequencias protegidas | Proposta C4; testes de acesso pendentes |
| Catalogo de Recursos; Consultas e Relatorios | RF-004, RF-005, RF-011, RF-012, RF-015, RF-021, RF-027, RF-029 | SEQ-SOL-002, SEQ-SOL-007, SEQ-RES-001, SEQ-RES-007, SEQ-ADM-005, SEQ-ADM-006 | Proposta C4; consultas gerais usam componente compartilhado |
| Disponibilidade e Integridade | RF-006, RF-007, RF-008, RF-018, RF-026 | SEQ-SOL-003, SEQ-SOL-004, SEQ-RES-002, SEQ-RES-004, SEQ-ADM-004, SEQ-CRIT-001 | Proposta; mecanismo final pendente ADR-004/DP-017/018 |
| Reservas e Transicoes | RF-009, RF-011, RF-013, RF-014, RF-027 | SEQ-SOL-004, SEQ-SOL-005, SEQ-SOL-006, SEQ-SOL-007, SEQ-CRIT-001 | Proposta; estados elegiveis pendentes DP-001/006/007 |
| Decisao de Aprovacao | RF-010, RF-015, RF-016, RF-017 | SEQ-RES-001, SEQ-RES-002, SEQ-RES-003, SEQ-CRIT-001 | Proposta; autorizacao/estado pendentes DP-001/003/006 |
| Administracao de Cadastros; Catalogo de Recursos | RF-022, RF-023, RF-024, RF-025 | SEQ-ADM-002, SEQ-ADM-003 | Proposta; matriz e politicas cadastrais pendentes DP-003/005/016 |
| Movimentacao | RF-019, RF-020, RF-021 | SEQ-RES-005, SEQ-RES-006, SEQ-RES-007 | Proposta; transicao vinculada pendente DP-006/008 |
| Auditoria | RF-012, RF-016, RF-017, RF-019, RF-020, RF-029, RF-030 | SEQ-SOL-004..006, SEQ-RES-002..006, SEQ-ADM-002..005, SEQ-CRIT-001 | Proposta; atomicidade/retencao ADR-006/DP-011 |
| Comunicacao | RF-031, RF-032, RF-033 | SEQ-SOL-004..006, SEQ-RES-002, SEQ-RES-003 | Adaptador condicional; canal pendente DP-009 |
| Consultas e Relatorios | RF-028, RF-034, RF-035, RF-036 | SEQ-ADM-006; dashboard e filtros consultivos em SEQ-RES-001/007 | Proposta; definicoes metricas pendentes DP-010 |

**C4 nivel 1/2:** todos os RF pertencem ao contêiner proposto Aplicacao Back-end; os fluxos de usuario atravessam Aplicacao Web, e dados persistentes passam pela Persistencia Transacional. Essas sao fronteiras propostas, nao mapeamentos de deploy aprovados.

**Testes:** cenarios documentais estao associados; IDs, execucoes, resultados, defeitos e evidencias automatizadas continuam **PENDENTES**. Componentes sao propostas C4 e nao autorizam iniciar codigo ate validacao arquitetural e resolucao das decisoes bloqueantes.
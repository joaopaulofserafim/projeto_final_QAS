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
| RF-006 | Todos: usar periodo valido e entender conflito | J-SOL-003-004, 006; J-RES-003, 005; J-ADM-007 | RN-001, 002 | RNF-003, 004, 005, 013 | Obrigatorio | CA-RF-006-01..03 | Regra temporal base na Story 1.1; 18 testes JUnit aprovados; integracao nos fluxos operacionais pendente |
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

Os componentes C4 e diagramas são propostas. Os cenários de teste abaixo são planos futuros; não são testes existentes nem resultados. As referências a RF/RN/RNF mantêm os identificadores originais.

### Diagramas de sequência e casos de uso

| Diagrama | Jornada(s) | Requisitos/regras principais | Componentes nível 3 | ADRs / questões abertas | Verificação futura (pendente) |
|---|---|---|---|---|---|
| SEQ-SOL-001 | J-SOL-001 | RF-001..003; RN-015, RN-022; RNF-001..003 | Entrada, Identidade e Autorização | ADR-005/008; DP-019 | Credencial, conta/perfil, logout e acesso direto |
| SEQ-SOL-002 | J-SOL-002 | RF-004/005/018; RN-003/007/008/015; RNF-001..003/006/009 | Catálogo, Disponibilidade, Entrada | ADR-002/005/008; DP-016 | Filtros, vazio versus falha e agenda ausente |
| SEQ-SOL-003 | J-SOL-003 | RF-006/007/018; RN-001..004/006/007; RNF-003..006/008 | Disponibilidade, Catálogo, Persistência | ADR-003/004; DP-001/015/016 | Intervalo inválido, sobreposição, adjacência e manutenção |
| SEQ-SOL-004 | J-SOL-004 | RF-006..010/030..033; RN-001..009/015..017/023; RNF-001/003..005/008/011 | Entrada, Identidade, Reservas, Disponibilidade, Auditoria, Comunicação | ADR-004..008; DP-001/002/009/013 | Pedido restrito/comum, conflito, commit/auditoria, falha de comunicação |
| SEQ-SOL-005 | J-SOL-006 | RF-002/006..008/013/030/032; RN-001..008/013/016/019; RNF-001/003..005/011 | Entrada, Identidade, Reservas, Disponibilidade, Auditoria | ADR-004..006; DP-007/013 | Propriedade, estado obsoleto, conflito e rollback |
| SEQ-SOL-006 | J-SOL-007 | RF-002/014/030/032/033; RN-013/016/019/023; RNF-001/003/005/008/011 | Entrada, Identidade, Reservas, Auditoria, Comunicação | ADR-005..008; DP-007/009 | Cancelamento permitido/proibido/repetido e auditoria |
| SEQ-SOL-007 | J-SOL-005 | RF-002/011/012; RN-015/018/019; RNF-001..003/009/011 | Entrada, Identidade, Consultas e Relatórios, Auditoria | ADR-005/006/008; DP-011 | Propriedade, filtros, vazio, falha e privacidade |
| SEQ-RES-001 | J-RES-001/002/008 | RF-002/005/015/021; RN-010/011/015; RNF-001..003/006/009 | Entrada, Identidade, Consultas e Relatórios, Disponibilidade | ADR-002/005/008; DP-003 | Escopo, fila/detalhe, atualização e falha de leitura |
| SEQ-RES-002 | J-RES-003 | RF-002/006..008/010/015/016/030/032/033; RN-001..012/016/023; RNF-001/003..005/008/011 | Entrada, Identidade, Decisão, Reservas, Disponibilidade, Persistência, Auditoria, Comunicação | ADR-004..008; DP-001/003/006 | Aprovação autorizada, corrida, estado obsoleto e auditoria |
| SEQ-RES-003 | J-RES-004 | RF-002/017/030/032/033; RN-010..012/014/016/023; RNF-001..003/005/008/011 | Entrada, Identidade, Decisão, Reservas, Auditoria, Comunicação | ADR-005..008; DP-002/003/009 | Justificativa, escopo, decisão duplicada e falha de canal |
| SEQ-RES-004 | J-RES-005 | RF-002/006/007/018; RN-001..003/010/015; RNF-001..004/009 | Entrada, Identidade, Disponibilidade, Catálogo | ADR-002/005/008; DP-003/016 | Docente ocupado/sem agenda/adjacente; não indicar disponibilidade por falha |
| SEQ-RES-005 | J-RES-006 | RF-002/019/021/030; RN-010/016/020; RNF-001/003/005/011 | Entrada, Identidade, Movimentação, Reservas, Auditoria | ADR-002/006/008; DP-006/008 | Autorização, elegibilidade e duplicidade |
| SEQ-RES-006 | J-RES-007 | RF-002/020/021/030; RN-010/016/021; RNF-001/003/005/011 | Entrada, Identidade, Movimentação, Reservas, Auditoria | ADR-002/006/008; DP-006/008 | Retirada aberta, devolução duplicada e falha auditável |
| SEQ-RES-007 | J-RES-008 | RF-002/015/021/029; RN-010/015/020/021; RNF-001..003/006/009/011 | Entrada, Identidade, Consultas e Relatórios, Auditoria | ADR-005/006/008; DP-003/008/011 | Pendência sem prazo, isolamento de escopo e falha de consulta |
| SEQ-ADM-001 | J-ADM-001 | RF-001..003; RN-015/022; RNF-001..003 | Entrada, Identidade e Autorização | ADR-005/008; DP-019 | Perfil, expiração/logout e usuário sem permissão |
| SEQ-ADM-002 | J-ADM-003 | RF-002/022/030; RN-015/017/022; RNF-001..005/011 | Entrada, Identidade, Administração, Persistência, Auditoria | ADR-002/005/006; DP-003/005 | Alteração de acesso, bloqueio com reserva ativa e falha de auditoria |
| SEQ-ADM-003 | J-ADM-004..006 | RF-002/004/005/023..025/030; RN-003/007/008/017/022; RNF-001..005/011 | Entrada, Identidade, Administração, Catálogo, Persistência, Auditoria | ADR-002/003/006; DP-005/016 | Cadastro/duplicidade, reserva/movimento vinculado e histórico |
| SEQ-ADM-004 | J-ADM-007 | RF-002/006..008/026/030; RN-001/002/005/007/016/017; RNF-001/003..008/011 | Entrada, Identidade, Administração, Disponibilidade, Persistência, Auditoria | ADR-003/004/006; DP-004/017/018 | Reserva existente, manutenção contra aceite e efeito de commit |
| SEQ-ADM-005 | J-ADM-008 | RF-002/012/029/030; RN-015..018/022; RNF-001..003/011/012 | Entrada, Identidade, Consultas e Relatórios, Auditoria | ADR-005/006/008; DP-011 | Acesso de leitura, filtro, minimização, falha e tentativa de mutação |
| SEQ-ADM-006 | J-ADM-002/009 | RF-002/027/028/034..036; RN-004/007/012/024; RNF-001..003/006/008/009/012 | Entrada, Identidade, Consultas e Relatórios, Persistência | ADR-002/008; DP-010/014 | Fórmula aprovada, dado parcial/falha versus zero e origem |
| SEQ-CRIT-001 | J-SOL-004/J-RES-003/J-ADM-007 | RF-006..009/013/016/026/030; RN-001..007/011/016; RNF-004/005/007/011/013 | Entrada, Identidade, Reservas, Decisão, Disponibilidade, Persistência, Auditoria | ADR-003/004/006/009; DP-001/004/006/017/018 | Testcontainers: pedido/pedido, aprovação/nova solicitação, manutenção/aceite; ainda não executado |

### Rastreabilidade de requisitos funcionais para componentes

| Componente (C4 nível 3) | RF sob responsabilidade/co-responsabilidade | Diagramas que evidenciam os principais fluxos | Situação/pendências |
|---|---|---|---|
| Entrada da Aplicação | RF-001..RF-036 (fronteira transversal) | Todas as sequências | Contrato e protocolo ainda não selecionados |
| Identidade e Autorização | RF-001..RF-003; RF-002 transversal em RF-004..RF-036 | Todas as sequências protegidas | DP-003/019; autorização sempre no servidor |
| Catálogo de Recursos | RF-004, RF-005, RF-018, RF-023..RF-025 | SEQ-SOL-002, SEQ-SOL-003, SEQ-RES-004, SEQ-ADM-003 | DP-005/016 |
| Administração de Cadastros | RF-022..RF-026 | SEQ-ADM-002..004 | DP-003/004/005 |
| Disponibilidade e Integridade | RF-006..RF-008, RF-013, RF-016, RF-018, RF-026 | SEQ-SOL-003..005, SEQ-RES-002/004, SEQ-ADM-004, SEQ-CRIT-001 | Mecanismo DP-017/018; estados DP-001 |
| Reservas e Transições | RF-009..RF-014, RF-016..RF-017 | SEQ-SOL-004..007, SEQ-RES-002/003, SEQ-CRIT-001 | Transições DP-001/006/007 |
| Decisão de Aprovação | RF-010, RF-015..RF-017 | SEQ-RES-001..003, SEQ-CRIT-001 | Escopo e critérios DP-002/003/006 |
| Movimentação | RF-019..RF-021 | SEQ-RES-005..007 | DP-006/008 |
| Auditoria | RF-012, RF-016/017, RF-019/020, RF-022..RF-026, RF-029/030 | SEQ-SOL-004..007, SEQ-RES-002/003/005/006, SEQ-ADM-002..005, SEQ-CRIT-001 | Atomicidade ADR-006; retenção DP-011 |
| Comunicação | RF-031..RF-033 | SEQ-SOL-004/006, SEQ-RES-002/003 | Adaptador interno; integração externa não presumida (DP-009) |
| Consultas e Relatórios | RF-011/012, RF-015, RF-021, RF-027..RF-029, RF-034..RF-036 | SEQ-SOL-007, SEQ-RES-001/007, SEQ-ADM-005/006 | Definições DP-010/011 |

### C4, estados e decisões

| Artefato | Requisitos/capacidades | Status documental |
|---|---|---|
| C4 nível 1 | Contexto dos três perfis e limite do sistema | Proposta; nenhuma integração externa confirmada |
| C4 nível 2 | Aplicação Web, Aplicação e persistência transacional | Proposta monólito modular; framework Web, SGBD e ORM em aberto |
| C4 nível 3 | Componentes da aplicação e contratos lógicos | Componentes internos, não contêineres implantáveis |
| Estados da reserva | RN-004, RN-011..RN-014; RF-009/010/016/017/019/020 | Parcial; DP-001/006/007 continuam bloqueando transições/efeitos |
| ADR-001..ADR-009 | Organização, módulos, persistência, concorrência, segurança, auditoria, comunicação, erros e testes | Propostas/recomendações; nenhum sign-off formal ou execução registrados |
| RA-001..RA-014 | Riscos transversais e mitigação proposta | Avaliação documental; nenhuma mitigação implementada ou testada |

**Testes e evidências:** cenários seguem pendentes; não há IDs de teste, resultados, defeitos ou cobertura para registrar. Os caminhos C4/UML/ADR referidos nesta RTM descrevem propostas, não implementação. Os IDs RF/RNF/RN e CA existentes não foram alterados.
# Regras de Negocio — Sistema de Organizacao de Recursos

Fonte: escopo fornecido nesta etapa, personas e jornadas em `PRD/personas/` e `PRD/jornadas/`. Regras marcadas como pendentes nao devem ser resolvidas por implementacao sem decisao de produto. Os IDs RN sao estaveis.

## Tempo e disponibilidade

### RN-001 — Periodo temporal valido
**Descricao:** Em toda solicitacao, reserva, alteracao ou bloqueio com periodo, o termino deve ser posterior ao inicio.  
**Justificativa:** Impedir intervalos nulos ou invertidos.  
**Requisitos associados:** RF-006, RF-009, RF-013, RF-026.  
**Excecoes:** Nenhuma definida; periodo invalido deve ser recusado antes de alterar dados.  
**Exemplo:** 10:00–12:00 e valido; 12:00–12:00 e invalido.

### RN-002 — Definicao de sobreposicao
**Descricao:** Considerando intervalos com inicio inclusivo e termino exclusivo, A e B se sobrepoem quando `inicioA < fimB E inicioB < fimA`. Periodos adjacentes nao se sobrepoem.  
**Justificativa:** Tornar a verificacao temporal deterministica e alinhada ao escopo.  
**Requisitos associados:** RF-006, RF-007, RF-008, RF-013, RF-018, RF-026.  
**Excecoes:** Fuso/calendario institucional e granularidade de horario dependem de decisao pendente DP-015.  
**Exemplo:** 10:00–12:00 conflita com 11:00–13:00; nao conflita com 12:00–14:00.

### RN-003 — Recursos e professores envolvidos
**Descricao:** A disponibilidade deve considerar cada recurso reservado e cada professor alocado a atividade, quando houver. Uma indisponibilidade de recurso ou professor impede confirmar a combinacao conflitante.  
**Justificativa:** Evitar dupla alocacao de espaco, item ou docente.  
**Requisitos associados:** RF-007, RF-009, RF-013, RF-016, RF-018.  
**Excecoes:** Composicao/atomicidade de solicitacoes com varios recursos e professor(es) sem agenda cadastrada precisam de definicao.  
**Exemplo:** Professor alocado das 14:00 as 16:00 nao pode ser alocado em outra atividade sobreposta.

### RN-004 — Estados que ocupam disponibilidade
**Descricao:** `APROVADA` e `EM_USO` representam alocacao aceita e bloqueiam outro uso conflitante no periodo. `REJEITADA`, `CANCELADA` e `CONCLUIDA` nao devem ser tratadas como reserva futura ativa. O efeito de `SOLICITADA` sobre disponibilidade permanece pendente: nao presumir que bloqueia ou nao bloqueia antes da decisao institucional.  
**Justificativa:** A concorrencia exige definir quais registros participam do calculo, mas as jornadas deixam pendente o efeito de solicitacoes em espera.  
**Requisitos associados:** RF-007, RF-008, RF-009, RF-016, RF-028, RF-035.  
**Excecoes:** A regra para reservas retroativas/historicas e fora do escopo descrito.  
**Exemplo:** Uma solicitacao `SOLICITADA` sobreposta exige comportamento conforme decisao pendente DP-001; uma reserva `APROVADA` e considerada ocupante.

### RN-005 — Exclusao mutua de aceites concorrentes
**Descricao:** Duas solicitacoes simultaneas com sobreposicao para o mesmo recurso nao podem resultar em duas reservas aceitas. A confirmacao/aprovacao deve considerar o estado mais recente e produzir no maximo um aceite conflitante. A solicitacao que perder a disputa recebe indisponibilidade/conflito; nao se define estrategia tecnica.  
**Justificativa:** Preservar integridade de agenda.  
**Requisitos associados:** RF-007, RF-008, RF-009, RF-016, RF-026.  
**Excecoes:** A disputa que envolva estado `SOLICITADA` depende de DP-001; a garantia entre aceites permanece obrigatoria.  
**Exemplo:** Dois solicitantes enviam Sala 101, 20/11, 14:00–16:00; no maximo um recebe aceite.

### RN-006 — Consulta nao reserva recurso
**Descricao:** Resultado de pesquisa ou consulta de disponibilidade e informativo; a disponibilidade deve ser novamente verificada ao criar, alterar ou aprovar.  
**Justificativa:** O estado pode mudar entre consulta e confirmacao.  
**Requisitos associados:** RF-004, RF-006, RF-009, RF-013, RF-016.  
**Excecoes:** Nenhuma definida.  
**Exemplo:** Consulta retorna livre, mas outra reserva e aceita antes do envio; a solicitacao posterior recebe conflito.

### RN-007 — Manutencao bloqueia reserva coincidente
**Descricao:** Recurso bloqueado para manutencao nao pode receber reserva cujo periodo se sobreponha ao periodo de manutencao.  
**Justificativa:** Recurso indisponivel nao pode ser alocado.  
**Requisitos associados:** RF-004, RF-006, RF-007, RF-009, RF-016, RF-026.  
**Excecoes:** Tratamento de reservas ja aceitas que coincidem com manutencao e pendente (DP-004); nao cancelar/remanejar automaticamente.  
**Exemplo:** Equipamento em manutencao das 09:00 as 12:00 nao pode ser reservado das 11:00 as 13:00.

### RN-008 — Recurso cadastralmente indisponivel
**Descricao:** Recurso desativado ou inexistente nao pode ser oferecido como elegivel para nova reserva.  
**Justificativa:** Impedir alocacao de cadastro que nao representa recurso utilizavel.  
**Requisitos associados:** RF-004, RF-005, RF-007, RF-009, RF-023, RF-024, RF-025.  
**Excecoes:** Tratamento de reservas anteriores a desativacao depende de DP-005.  
**Exemplo:** Sala desativada deixa de aparecer como reservavel, sem apagar seu historico.

## Aprovacao e estados

### RN-009 — Aprovacao obrigatoria de recurso restrito
**Descricao:** Solicitacao de recurso restrito deve passar por decisao de responsavel autorizado; solicitante nao pode aprovar a propria solicitacao.  
**Justificativa:** Recursos restritos exigem controle e separacao de responsabilidades.  
**Requisitos associados:** RF-010, RF-015, RF-016, RF-017, RF-032.  
**Excecoes:** Criterios de quais recursos sao restritos e dados exigidos para decisao estao pendentes (DP-002).  
**Exemplo:** Solicitacao de equipamento restrito inicia como `SOLICITADA` e aguarda responsavel autorizado.

### RN-010 — Escopo do responsavel
**Descricao:** Responsavel so pode consultar e decidir solicitacoes, agendas e movimentacoes de recursos sob seu escopo. Aprovar/rejeitar exige autorizacao especifica para aquela solicitacao.  
**Justificativa:** Impedir acesso e decisao operacional irrestritos.  
**Requisitos associados:** RF-002, RF-015, RF-016, RF-017, RF-018, RF-019, RF-020, RF-021.  
**Excecoes:** Modelo institucional de atribuicao, substituicao e ausencia permanece pendente (DP-003).  
**Exemplo:** Responsavel do laboratorio A nao decide solicitacao do laboratorio B.

### RN-011 — Decisao sobre solicitacao pendente
**Descricao:** A aprovacao/rejeicao somente pode incidir sobre solicitacao ainda `SOLICITADA`, com permissao valida e revalidacao de disponibilidade. Repetir decisao ja concluida nao cria nova transicao.  
**Justificativa:** Evitar aprovacao duplicada ou obsoleta.  
**Requisitos associados:** RF-015, RF-016, RF-017.  
**Excecoes:** Se o recurso deixar de estar disponivel enquanto aguarda, a decisao e impedida; destino alternativo da solicitacao requer DP-006.  
**Exemplo:** Segunda aprovacao de item ja `APROVADA` recebe estado atualizado e nao repete o efeito.

### RN-012 — Estados e transicoes conhecidas
**Descricao:** Estados de reserva: `SOLICITADA`, `APROVADA`, `EM_USO`, `CONCLUIDA`, `REJEITADA` e `CANCELADA`. A transicao `SOLICITADA` -> `APROVADA` ou `REJEITADA` e permitida a responsavel autorizado, sujeita a validacoes. Atores, pre-condicoes e transicoes de `APROVADA` -> `EM_USO`, `EM_USO` -> `CONCLUIDA`, cancelamentos e demais transicoes nao estao completamente definidos e nao devem ser inferidos.  
**Justificativa:** Preservar o ciclo do escopo sem conceder transicoes nao especificadas.  
**Requisitos associados:** RF-009, RF-010, RF-016, RF-017, RF-019, RF-020, RF-028, RF-030, RF-032.  
**Excecoes:** Definicoes pendentes DP-006 e DP-007.  
**Exemplo:** Responsavel autorizado pode aprovar apenas `SOLICITADA`; registro de retirada nao implica automaticamente `EM_USO`.

### RN-013 — Alterar ou cancelar conforme elegibilidade
**Descricao:** Solicitante pode pedir alteracao/cancelamento apenas de reserva propria e quando autorizado pelo estado e regra aplicavel. Uma reserva iniciada nao pode ser apagada. O mapa de estados elegiveis, campos alteraveis, prazos e transicoes para `CANCELADA` nao foi definido.  
**Justificativa:** Proteger reservas e manter historico sem inventar permissao.  
**Requisitos associados:** RF-002, RF-013, RF-014, RF-030, RF-032.  
**Excecoes:** DP-007.  
**Exemplo:** Tentativa de cancelar reserva alheia e negada; reserva `EM_USO` nunca e excluida.

### RN-014 — Rejeicao com justificativa
**Descricao:** Rejeicao autorizada de solicitacao deve registrar justificativa e informar o solicitante.  
**Justificativa:** Tornar a decisao compreensivel e rastreavel.  
**Requisitos associados:** RF-017, RF-030, RF-032.  
**Excecoes:** Tamanho, categorias e campos obrigatorios da justificativa dependem de DP-002.  
**Exemplo:** Solicitante consulta a justificativa vinculada a solicitacao `REJEITADA`.

## Acesso, auditoria e movimentacao

### RN-015 — Autorizacao por perfil e recurso
**Descricao:** Toda operacao protegida verifica autenticacao, perfil, permissao e escopo no momento da acao; ocultar funcao na interface nao substitui autorizacao.  
**Justificativa:** Impedir operacoes indevidas, inclusive acesso direto.  
**Requisitos associados:** RF-001, RF-002, RF-003, RF-004 a RF-036.  
**Excecoes:** Matriz detalhada de permissoes a definir em DP-003.  
**Exemplo:** Solicitante nao acessa administracao por link direto.

### RN-016 — Auditoria de mudanca de estado
**Descricao:** Cada alteracao de estado de reserva gera registro com autor, instante, reserva/recurso afetado, operacao, estado anterior e novo.  
**Justificativa:** Rastreabilidade e integridade do ciclo de vida.  
**Requisitos associados:** RF-016, RF-017, RF-019, RF-020, RF-030, RF-032.  
**Excecoes:** Nenhuma para transicao confirmada; se o registro auditavel falhar, a transicao nao pode ser apresentada como concluida.  
**Exemplo:** Aprovar registra `SOLICITADA` -> `APROVADA`, responsavel e instante.

### RN-017 — Auditoria de operacoes relevantes
**Descricao:** Alteracoes administrativas, de reserva, manutencao e movimentacao identificam ator, instante, alvo e operacao; quando houver mudanca de valor/estado, preservam anterior e novo.  
**Justificativa:** Permitir investigar inconsistencias e reconstituir operacoes.  
**Requisitos associados:** RF-013, RF-014, RF-019, RF-020, RF-022 a RF-026, RF-030.  
**Excecoes:** Campos auditados e retencao requerem decisao DP-011; segredo e dado sensivel nao necessario nao deve ser copiado.  
**Exemplo:** Bloqueio de manutencao identifica recurso, periodo, autor e instante.

### RN-018 — Historico auditavel protegido
**Descricao:** Usuarios comuns e administrador nao podem apagar ou alterar diretamente os registros de auditoria. Consulta e restrita a perfis autorizados.  
**Justificativa:** Preservar confiabilidade das evidencias.  
**Requisitos associados:** RF-002, RF-012, RF-021, RF-029, RF-030.  
**Excecoes:** Nenhuma operacao de edicao/exclusao e definida; correcao de dado operacional deve ser nova acao auditada.  
**Exemplo:** Administrador consulta evento mas nao o modifica.

### RN-019 — Propriedade das reservas do solicitante
**Descricao:** Solicitante consulta, altera ou pede cancelamento somente de suas proprias reservas, salvo autorizacao expressa ainda nao definida.  
**Justificativa:** Proteger dados e impedir alteracoes entre usuarios.  
**Requisitos associados:** RF-002, RF-011, RF-012, RF-013, RF-014.  
**Excecoes:** Nenhuma excecao de delegacao esta documentada.  
**Exemplo:** ID de reserva de terceiro nao autoriza leitura nem alteracao.

### RN-020 — Retirada autorizada e vinculada
**Descricao:** Retirada deve ser registrada por responsavel autorizado, dentro do escopo e vinculada a reserva/recurso correspondente, identificando quem registrou, quem recebeu e instante real.  
**Justificativa:** Rastrear custodia de materiais/equipamentos.  
**Requisitos associados:** RF-019, RF-021, RF-030.  
**Excecoes:** Elegibilidade exata de estado e politica de retirada por terceiro requer DP-008.  
**Exemplo:** Retirada sem reserva elegivel ou fora do escopo e recusada.

### RN-021 — Devolucao vinculada e nao duplicada
**Descricao:** Devolucao exige movimentacao de retirada aberta correspondente; deve identificar operador e instante real e nao pode ser registrada duas vezes.  
**Justificativa:** Encerrar movimentacao e evitar falso historico de retorno.  
**Requisitos associados:** RF-020, RF-021, RF-030.  
**Excecoes:** Tratamento de atraso, avaria e discrepancia requer DP-008.  
**Exemplo:** Repetir devolucao ja registrada retorna o movimento existente sem criar outro.

### RN-022 — Admin privilegiado sujeito a controles
**Descricao:** Acesso administrativo nao permite ignorar validacoes, aprovar solicitacao restrita por privilegio implicito, apagar reserva iniciada ou alterar auditoria.  
**Justificativa:** Privilégio nao elimina integridade, segregacao ou rastreabilidade.  
**Requisitos associados:** RF-001, RF-002, RF-016, RF-022 a RF-030.  
**Excecoes:** Permissoes administrativas especificas devem ser definidas em DP-003.  
**Exemplo:** Administrador pode consultar historico, mas nao edita evento nem aprova recurso restrito sem papel de responsavel autorizado.

## Comunicacao e relatorios

### RN-023 — Comunicacao nao altera resultado de negocio
**Descricao:** Falha ao enviar notificacao nao reverte reserva, decisao ou movimentacao ja confirmada e auditada; estado atual deve continuar consultavel.  
**Justificativa:** Separar resultado da operacao de entrega da comunicacao.  
**Requisitos associados:** RF-031, RF-032, RF-033.  
**Excecoes:** Reenvio, canal, prazo e fila de falha dependem de DP-009.  
**Exemplo:** Aprovacao permanece `APROVADA` quando a notificacao ao solicitante falha.

### RN-024 — Relatorios com definicoes consistentes
**Descricao:** Indicadores devem explicitar periodo, unidade, criterio e origem; indisponibilidade de dados nao pode aparecer como zero. Conflitos evitados so podem ser contados quando houver evidencia de tentativas impedidas e definicao aprovada.  
**Justificativa:** Evitar conclusoes enganosas em relatorios.  
**Requisitos associados:** RF-028, RF-034, RF-035, RF-036.  
**Excecoes:** Formulas, janelas e criterio de utilizacao permanecem pendentes DP-010.  
**Exemplo:** Se nao houver criterio de “conflito evitado”, relatorio indica metrica pendente/indisponivel, sem estimativa.

## Regras adicionadas pelas decisões de 09/11/2026

As regras abaixo decorrem das decisões registradas em `PENDENCIAS_DECISOES.md` (seção "Registro de decisões — 09/11/2026"). Regras de negócio estão sujeitas a confirmação do PO.

### RN-025 — Não comparecimento
**Descricao:** Reserva `APROVADA` cujo horário de início já passou pode ser marcada pelo Responsável como `NAO_COMPARECEU`, liberando o restante do horário.  
**Justificativa:** Estado alternativo exigido pelo enunciado ("não compareceu").  
**Requisitos associados:** RF-016, RF-028, RF-035.  
**Exemplo:** Reserva 10:00–12:00 sem uso às 10:20 pode ser marcada como não comparecimento; às 09:50 não pode.

### RN-026 — Estado inicial conforme restrição
**Descricao:** Pedido que inclui qualquer recurso restrito nasce `SOLICITADA` e exige justificativa; pedido apenas com recursos comuns nasce `APROVADA`, com aprovação automática auditada.  
**Requisitos associados:** RF-009, RF-010.  
**Exemplo:** Sala comum + professor: `APROVADA`. Laboratório restrito: `SOLICITADA` até decisão do Responsável.

### RN-027 — Estados que ocupam horário (decisão de DP-001)
**Descricao:** `SOLICITADA`, `APROVADA` e `EM_USO` ocupam o horário; `REJEITADA`, `CANCELADA`, `CONCLUIDA` e `NAO_COMPARECEU` liberam. Duas solicitações simultâneas para o mesmo recurso e horário resultam em no máximo uma aceita; a outra recebe conflito.  
**Requisitos associados:** RF-007, RF-008, RF-009.

### RN-028 — Manutenção não atropela reservas
**Descricao:** Um período de manutenção só é registrado se não houver ocupação ativa no intervalo; caso contrário é recusado com a lista das reservas em conflito para tratamento explícito pelo Administrador.  
**Requisitos associados:** RF-026; complementa RN-007.

### RN-029 — Janela de alteração e cancelamento
**Descricao:** Alterar e cancelar só são permitidos em `SOLICITADA` ou `APROVADA` e antes do início. Alteração com recurso restrito volta a exigir aprovação. Nenhuma reserva é apagada.  
**Requisitos associados:** RF-013, RF-014; detalha RN-013.

### RN-030 — Conclusão com materiais pendentes
**Descricao:** Reserva com material ou equipamento retirado e não devolvido não pode ser concluída. A devolução do último item conclui automaticamente a reserva que contém apenas materiais/equipamentos.  
**Requisitos associados:** RF-019, RF-020; detalha RN-020 e RN-021.

## Pendencias de negocio relacionadas

Ver `PENDENCIAS_DECISOES.md`: DP-001 (solicitacao pendente ocupa horario), DP-002 (recursos restritos e dados de decisao), DP-003 (matriz/escopo de permissoes), DP-004 (reservas existentes durante manutencao), DP-005 (desativacao com reservas e criterios cadastrais), DP-006 (transicoes e indisponibilidade durante aprovacao), DP-007 (alteracao/cancelamento), DP-008 (retirada/devolucao), DP-009 (notificacoes), DP-010 (metricas/relatorios), DP-011 (auditoria/retencao), DP-012 (contrato de fluxos publicos/API academica), DP-013 (solicitacao multipla), DP-014 (metas de qualidade), DP-015 (calendario e parametros temporais) e DP-016 (dados de competencia/disponibilidade docente).
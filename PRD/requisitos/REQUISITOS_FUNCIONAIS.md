# Requisitos Funcionais — Sistema de Organizacao de Recursos

Requisitos derivados do escopo, personas e jornadas. IDs sao unicos e estaveis. Prioridade **Obrigatorio** indica capacidade explicitamente exigida pelo escopo; **Importante** indica capacidade desejada sem criterio essencial quantificado; **Futuro** nao foi usado para reduzir escopo. Os criterios `CA-*` sao criterios documentais; nao representam testes implementados. Campos sem definicao institucional estao marcados como pendentes.

## Acesso e pesquisa

### RF-001 — Autenticar usuario
**ID:** RF-001  
**Nome:** Autenticacao de usuario.  
**Descricao:** O sistema deve validar as credenciais e a situacao de acesso antes de permitir operacoes autenticadas.  
**Persona relacionada:** Todos os perfis.  
**Jornada de origem:** J-SOL-001, J-RES-001, J-ADM-001.  
**Objetivo atendido:** Acessar a area adequada com protecao contra uso indevido.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Usuario cadastrado e credenciais fornecidas.  
**Entradas:** Identificacao e credenciais.  
**Processamento esperado:** Validar identidade, estado cadastral e perfil; negar acesso quando invalidos.  
**Saidas:** Acesso autenticado com perfil ou resposta de negacao.  
**Regras de negocio relacionadas:** RN-015, RN-022.  
**Excecoes:** Credenciais invalidas, conta sem acesso ou falha de validacao.  
**Criterios de aceite:** CA-RF-001-01: credenciais validas permitem acesso autenticado. CA-RF-001-02: credenciais invalidas nao abrem area protegida nem revelam qual dado falhou.  
**Cenarios de teste futuros:** DADO usuario ativo e credenciais validas, QUANDO autenticar, ENTAO o sistema identifica seu perfil; DADAS credenciais invalidas, QUANDO tentar entrar, ENTAO o acesso e negado.

### RF-002 — Aplicar autorizacao por perfil e escopo
**ID:** RF-002  
**Nome:** Autorizacao de operacoes.  
**Descricao:** O sistema deve verificar perfil, permissao, propriedade do registro e escopo atribuido antes de cada operacao protegida, inclusive acesso direto.  
**Persona relacionada:** Todos os perfis.  
**Jornada de origem:** J-SOL-001 a J-SOL-008; J-RES-001 a J-RES-008; J-ADM-001 a J-ADM-009.  
**Objetivo atendido:** Executar somente acoes autorizadas e proteger dados de terceiros.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Operacao protegida solicitada.  
**Entradas:** Identidade autenticada, acao, registro/recurso e permissoes vigentes.  
**Processamento esperado:** Confirmar autorizacao para aquela acao e alvo; negar fora do escopo.  
**Saidas:** Operacao autorizada ou acesso negado sem exposicao indevida.  
**Regras de negocio relacionadas:** RN-010, RN-015, RN-019, RN-022.  
**Excecoes:** Permissoes granulares e excecoes de delegacao dependem de DP-003.  
**Criterios de aceite:** CA-RF-002-01: solicitante nao le/altera reserva alheia nem administra o sistema. CA-RF-002-02: responsavel atua somente no escopo permitido. CA-RF-002-03: administrador continua sujeito a regras criticas.  
**Cenarios de teste futuros:** DADO um solicitante com identificador de reserva alheia, QUANDO consultar, ENTAO recebe negacao; DADO responsavel de um escopo, QUANDO acessar recurso fora dele, ENTAO a acao e negada.

### RF-003 — Encerrar sessao
**ID:** RF-003  
**Nome:** Encerramento de sessao autenticada.  
**Descricao:** O sistema deve encerrar o contexto autenticado solicitado e impedir que ele autorize operacoes posteriores.  
**Persona relacionada:** Todos os perfis autenticados.  
**Jornada de origem:** J-SOL-001, J-ADM-001.  
**Objetivo atendido:** Finalizar acesso e proteger dados privados.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Sessao autenticada.  
**Entradas:** Acao de encerrar sessao.  
**Processamento esperado:** Revogar contexto e retornar estado nao autenticado.  
**Saidas:** Confirmacao de encerramento ou falha explicita.  
**Regras de negocio relacionadas:** RN-015.  
**Excecoes:** Falha de encerramento nao deve ser apresentada como sucesso.  
**Criterios de aceite:** CA-RF-003-01: contexto encerrado nao autoriza nova operacao protegida.  
**Cenarios de teste futuros:** DADA sessao autenticada, QUANDO encerrar, ENTAO uma operacao seguinte exige autenticacao.

### RF-004 — Pesquisar recursos
**ID:** RF-004  
**Nome:** Pesquisa filtrada de recursos.  
**Descricao:** O sistema deve permitir pesquisar salas, professores, materiais e equipamentos por criterios pertinentes: tipo, capacidade, localizacao, competencia, data, horario e disponibilidade.  
**Persona relacionada:** Solicitante; Administrador em consulta administrativa.  
**Jornada de origem:** J-SOL-002, J-ADM-002, J-ADM-004, J-ADM-005, J-ADM-006.  
**Objetivo atendido:** Localizar recursos compativeis e consultar inventario.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Usuario autorizado e criterios de pesquisa disponiveis.  
**Entradas:** Tipo e filtros aplicaveis.  
**Processamento esperado:** Validar criterios, pesquisar dados autorizados e aplicar filtros; nao inferir disponibilidade se consulta falhar.  
**Saidas:** Resultados, sem resultados ou erro diferenciado.  
**Regras de negocio relacionadas:** RN-007, RN-008, RN-015.  
**Excecoes:** Filtro invalido, sem correspondencia, recurso desativado ou falha de consulta.  
**Criterios de aceite:** CA-RF-004-01: cada filtro aplicavel limita os resultados. CA-RF-004-02: falha nao e apresentada como lista vazia. CA-RF-004-03: dados fora da permissao nao sao expostos.  
**Cenarios de teste futuros:** DADOS filtros por capacidade/localizacao, QUANDO pesquisar salas, ENTAO resultados atendem aos filtros; DADA consulta indisponivel, QUANDO pesquisar, ENTAO e apresentada falha, nao lista vazia.

### RF-005 — Consultar detalhes de recurso
**ID:** RF-005  
**Nome:** Detalhes e situacao de recurso.  
**Descricao:** O sistema deve apresentar identificacao, caracteristicas, situacao cadastral, restricao e dados de disponibilidade autorizados para o recurso selecionado.  
**Persona relacionada:** Solicitante, Responsavel e Administrador conforme permissao.  
**Jornada de origem:** J-SOL-002, J-RES-002, J-RES-006, J-RES-007, J-ADM-004, J-ADM-005, J-ADM-006.  
**Objetivo atendido:** Avaliar recurso e verificar seu estado antes de agir.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Recurso existente e consulta autorizada.  
**Entradas:** Identificador do recurso.  
**Processamento esperado:** Recuperar detalhe atual e filtrar dados por permissao.  
**Saidas:** Detalhe ou indicacao de inexistencia/negacao.  
**Regras de negocio relacionadas:** RN-008, RN-015.  
**Excecoes:** Recurso removido, desativado ou sem permissao de leitura.  
**Criterios de aceite:** CA-RF-005-01: detalhe distingue indisponibilidade, restricao e manutencao. CA-RF-005-02: recurso inexistente nao e apresentado como disponivel.  
**Cenarios de teste futuros:** DADO recurso desativado, QUANDO consultar, ENTAO situacao e exibida e nao ha indicacao de nova reserva.

## Reservas e disponibilidade

### RF-006 — Validar periodo informado
**ID:** RF-006  
**Nome:** Validacao temporal de solicitacoes e bloqueios.  
**Descricao:** O sistema deve exigir termino posterior ao inicio e avaliar intervalos pelo criterio de sobreposicao definido.  
**Persona relacionada:** Solicitante, Responsavel e Administrador.  
**Jornada de origem:** J-SOL-003, J-SOL-004, J-SOL-006, J-RES-003, J-RES-005, J-ADM-007.  
**Objetivo atendido:** Informar periodos validos e evitar conflitos.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Operacao possui inicio e termino.  
**Entradas:** Inicio, termino e periodo existente a comparar.  
**Processamento esperado:** Aplicar `inicioA < fimB E inicioB < fimA`, com inicio inclusivo e termino exclusivo; rejeitar periodo invalido.  
**Saidas:** Periodo valido sem sobreposicao ou erro/conflito.  
**Regras de negocio relacionadas:** RN-001, RN-002.  
**Excecoes:** Fuso horario/calendario institucional pendente DP-015.  
**Criterios de aceite:** CA-RF-006-01: termino igual/anterior ao inicio e recusado. CA-RF-006-02: 10:00–12:00 conflita com 11:00–13:00. CA-RF-006-03: 10:00–12:00 e 12:00–14:00 nao conflitam.  
**Cenarios de teste futuros:** DADO termino anterior ao inicio, QUANDO enviar, ENTAO recusar; DADOS intervalos adjacentes, QUANDO comparar, ENTAO nao indicar sobreposicao.

### RF-007 — Verificar disponibilidade e conflitos
**ID:** RF-007  
**Nome:** Verificacao de recurso, professor e manutencao.  
**Descricao:** O sistema deve consultar alocacoes que ocupam disponibilidade, agenda de professores envolvidos, manutencao e situacao cadastral antes de informar disponibilidade ou permitir confirmacao.  
**Persona relacionada:** Solicitante, Responsavel e Administrador.  
**Jornada de origem:** J-SOL-003, J-SOL-004, J-SOL-006, J-RES-002, J-RES-003, J-RES-005, J-ADM-004 a J-ADM-007.  
**Objetivo atendido:** Evitar conflito de agenda e uso de recurso indisponivel.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Recurso e periodo identificados.  
**Entradas:** Recurso(s), professor(es), periodo, reservas, manutencao e situacao cadastral.  
**Processamento esperado:** Aplicar RN-004 e RN-007/RN-008; detectar conflito e explicar motivo. Solicitacoes `SOLICITADA` seguem decisao pendente.  
**Saidas:** Disponivel para prosseguir, indisponivel com motivo, ou consulta inconclusiva.  
**Regras de negocio relacionadas:** RN-002, RN-003, RN-004, RN-006, RN-007, RN-008.  
**Excecoes:** Ocupacao por `SOLICITADA` e DP-001; falha de agenda nao significa livre.  
**Criterios de aceite:** CA-RF-007-01: reserva `APROVADA`/`EM_USO` sobreposta impede conflito. CA-RF-007-02: manutencao, professor indisponivel e recurso desativado impedem disponibilidade coincidente. CA-RF-007-03: consulta falha nunca e interpretada como livre.  
**Cenarios de teste futuros:** DADO professor alocado, QUANDO consultar horario coincidente, ENTAO informar conflito; DADO recurso em manutencao, QUANDO consultar periodo, ENTAO informar indisponibilidade.

### RF-008 — Impedir dupla aceitacao concorrente
**ID:** RF-008  
**Nome:** Integridade de reservas concorrentes.  
**Descricao:** O sistema deve garantir que envios/aprovacoes simultaneos nao produzam mais de uma reserva aceita sobreposta para o mesmo recurso, considerando os estados bloqueantes definidos.  
**Persona relacionada:** Solicitante e Responsavel.  
**Jornada de origem:** J-SOL-004, J-SOL-006, J-RES-003, J-ADM-007.  
**Objetivo atendido:** Reservar sem duplicidade e obter resultado confiavel.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Duas ou mais operacoes concorrentes disputam recurso/periodo.  
**Entradas:** Solicitacoes, recurso(s), periodo e estado atual.  
**Processamento esperado:** Revalidar disponibilidade no aceite; uma disputa conflitante pode aceitar no maximo uma operacao; demais recebem conflito/indisponibilidade. Nenhuma estrategia tecnica e prescrita.  
**Saidas:** Resultado confirmado para cada solicitante/decisor e estado final coerente.  
**Regras de negocio relacionadas:** RN-002, RN-004, RN-005, RN-006.  
**Excecoes:** Efeito de solicitacoes pendentes em disputa depende de DP-001.  
**Criterios de aceite:** CA-RF-008-01: nao existem dois aceites conflitantes. CA-RF-008-02: solicitacao que perde disputa recebe indisponibilidade. CA-RF-008-03: consulta anterior nao elimina revalidacao.  
**Cenarios de teste futuros:** DADOS dois solicitantes com mesmo recurso/horario e envio simultaneo, QUANDO processar ambos, ENTAO no maximo um e aceito e o outro recebe conflito.

### RF-009 — Criar solicitacao de reserva
**ID:** RF-009  
**Nome:** Registrar solicitacao de reserva.  
**Descricao:** O sistema deve permitir a solicitante autenticado enviar pedido com recurso(s), periodo e informacoes exigidas para avaliar a atividade.  
**Persona relacionada:** Solicitante.  
**Jornada de origem:** J-SOL-004.  
**Objetivo atendido:** Solicitar recursos para atividade academica e receber confirmacao.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Solicitante autenticado; cadastro consultavel; campos obrigatorios definidos.  
**Entradas:** Solicitante, recurso(s), inicio, termino, finalidade e dados definidos para o pedido.  
**Processamento esperado:** Validar campos, permissao, periodo, disponibilidade, restricao, professor e manutencao no envio; criar registro somente se validacoes permitirem.  
**Saidas:** Identificador/estado confirmado, pendencia de aprovacao ou recusa com motivo.  
**Regras de negocio relacionadas:** RN-001 a RN-009, RN-015.  
**Excecoes:** Estado inicial de recurso comum e composicao multipla pendentes DP-001/DP-002/DP-013.  
**Criterios de aceite:** CA-RF-009-01: periodo invalido nao cria solicitacao. CA-RF-009-02: restrito inicia como `SOLICITADA`. CA-RF-009-03: indisponibilidade/conflito no envio impede aceite. CA-RF-009-04: consulta anterior nao e considerada garantia.  
**Cenarios de teste futuros:** DADO pedido valido para recurso restrito, QUANDO enviar, ENTAO criar `SOLICITADA`; DADO manutencao ou conflito, QUANDO confirmar, ENTAO recusar sem dupla reserva.

### RF-010 — Encaminhar recurso restrito para aprovacao
**ID:** RF-010  
**Nome:** Encaminhamento de solicitacao restrita.  
**Descricao:** Ao receber pedido de recurso restrito, o sistema deve mantê-lo em `SOLICITADA` e disponibiliza-lo para decisao de responsavel autorizado ao recurso.  
**Persona relacionada:** Solicitante e Responsavel.  
**Jornada de origem:** J-SOL-004, J-RES-002, J-RES-003.  
**Objetivo atendido:** Avaliar pedidos especiais no escopo correto.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Recurso marcado restrito e solicitacao valida.  
**Entradas:** Pedido, recurso e atribuicoes dos responsaveis.  
**Processamento esperado:** Identificar restricao e disponibilizar pendencia apenas a responsavel autorizado.  
**Saidas:** Solicitacao aguardando decisao, sem aprovacao presumida.  
**Regras de negocio relacionadas:** RN-009, RN-010, RN-011.  
**Excecoes:** Recurso restrito sem responsavel atribuido requer tratamento a definir (DP-003).  
**Criterios de aceite:** CA-RF-010-01: solicitante nao pode aprovar proprio pedido. CA-RF-010-02: responsavel fora do escopo nao recebe acao de decisao.  
**Cenarios de teste futuros:** DADO recurso restrito, QUANDO registrar pedido, ENTAO torna-lo pendente ao responsavel autorizado; tentativa de autoaprovacao e negada.

### RF-011 — Consultar reservas proprias
**ID:** RF-011  
**Nome:** Listar e filtrar reservas do solicitante.  
**Descricao:** O sistema deve permitir consultar reservas proprias por data/periodo e estado.  
**Persona relacionada:** Solicitante.  
**Jornada de origem:** J-SOL-005.  
**Objetivo atendido:** Acompanhar andamento e situacao atual.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Solicitante autenticado.  
**Entradas:** Identidade autenticada e filtros opcionais.  
**Processamento esperado:** Retornar somente registros proprios autorizados com recurso, periodo e estado atual.  
**Saidas:** Lista, sem resultados ou erro de consulta.  
**Regras de negocio relacionadas:** RN-015, RN-019.  
**Excecoes:** Falha de consulta nao se confunde com lista vazia.  
**Criterios de aceite:** CA-RF-011-01: lista respeita filtros e propriedade. CA-RF-011-02: estado atual distingue os seis estados do dominio.  
**Cenarios de teste futuros:** DADO registros proprios em estados distintos, QUANDO listar, ENTAO exibi-los; registro de terceiro nao e retornado.

### RF-012 — Consultar historico de reserva
**ID:** RF-012  
**Nome:** Exibir historico proprio autorizado.  
**Descricao:** O sistema deve permitir ao solicitante consultar eventos e decisoes de suas reservas, inclusive justificativa de rejeicao quando autorizada.  
**Persona relacionada:** Solicitante; Administrador e Responsavel em seus escopos.  
**Jornada de origem:** J-SOL-005, J-RES-002, J-RES-008, J-ADM-008.  
**Objetivo atendido:** Compreender alteracoes e decisoes sem expor terceiros.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Registro existente e permissao de leitura.  
**Entradas:** Identificador da reserva e identidade/perfil.  
**Processamento esperado:** Recuperar eventos autorizados em ordem compreensivel, sem permitir edicao.  
**Saidas:** Historico autorizado ou negacao/inexistencia.  
**Regras de negocio relacionadas:** RN-015, RN-016, RN-018, RN-019.  
**Excecoes:** Conteudo sensivel deve ser filtrado conforme DP-011.  
**Criterios de aceite:** CA-RF-012-01: solicitante so consulta propria reserva. CA-RF-012-02: registro nao oferece alteracao/exclusao do historico.  
**Cenarios de teste futuros:** DADO mudancas auditadas em reserva propria, QUANDO consultar historico, ENTAO eventos sao exibidos; registro alheio e negado.

### RF-013 — Alterar reserva propria quando permitido
**ID:** RF-013  
**Nome:** Solicitar alteracao de reserva.  
**Descricao:** O sistema deve permitir alterar somente campos e estados autorizados de reserva propria, validando novamente periodo, disponibilidade, professores, restricao e manutencao.  
**Persona relacionada:** Solicitante.  
**Jornada de origem:** J-SOL-006.  
**Objetivo atendido:** Adaptar reserva sem perder historico.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Reserva propria existente e elegivel conforme matriz a definir.  
**Entradas:** Reserva, valores atuais e propostos.  
**Processamento esperado:** Verificar permissao/estado atual; revalidar conflitos; aplicar alteracao atomica e registrar anterior/novo quando permitido.  
**Saidas:** Alteracao confirmada ou recusa com reserva original preservada.  
**Regras de negocio relacionadas:** RN-001 a RN-008, RN-013, RN-016, RN-019.  
**Excecoes:** Estados/campos editaveis e nova aprovacao de recurso restrito pendentes DP-007.  
**Criterios de aceite:** CA-RF-013-01: reserva alheia nao e alterada. CA-RF-013-02: periodo/conflito invalido nao altera dado. CA-RF-013-03: alteracao permitida e auditavel.  
**Cenarios de teste futuros:** DADA reserva elegivel e novo periodo livre, QUANDO alterar, ENTAO atualizar e auditar; DADO conflito ou estado inelegivel, ENTAO manter original.

### RF-014 — Cancelar reserva propria quando permitido
**ID:** RF-014  
**Nome:** Cancelar reserva sem exclusao.  
**Descricao:** O sistema deve permitir solicitar cancelamento de reserva propria apenas em estados autorizados e, se aceito, alterar estado para `CANCELADA` sem apagar reserva/historico.  
**Persona relacionada:** Solicitante.  
**Jornada de origem:** J-SOL-007.  
**Objetivo atendido:** Cancelar reserva propria quando planos mudam.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Reserva propria existente; estado elegivel conforme decisao pendente.  
**Entradas:** Identificador da reserva e confirmacao de cancelamento.  
**Processamento esperado:** Revalidar propriedade/estado; alterar somente se autorizado; auditar e devolver resultado.  
**Saidas:** `CANCELADA` ou recusa com estado preservado.  
**Regras de negocio relacionadas:** RN-013, RN-016, RN-019, RN-023.  
**Excecoes:** Estados elegiveis, justificativa, prazos e efeito em `EM_USO` pendentes DP-007.  
**Criterios de aceite:** CA-RF-014-01: reserva iniciada nunca e apagada. CA-RF-014-02: cancelamento valido gera estado e auditoria. CA-RF-014-03: repeticao nao duplica transicao.  
**Cenarios de teste futuros:** DADA reserva em estado elegivel, QUANDO cancelar, ENTAO mudar para `CANCELADA`; DADA reserva `EM_USO`, QUANDO pedir cancelamento, ENTAO nao apagar e aplicar somente regra decidida.

## Decisoes, movimentacoes e comunicacao

### RF-015 — Consultar fila de solicitacoes pendentes
**ID:** RF-015  
**Nome:** Fila de decisao do responsavel.  
**Descricao:** O sistema deve permitir ao responsavel consultar e filtrar solicitacoes pendentes de seu escopo por estado, recurso e periodo.  
**Persona relacionada:** Responsavel.  
**Jornada de origem:** J-RES-001, J-RES-002, J-RES-008.  
**Objetivo atendido:** Localizar pedidos que exigem decisao.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Responsavel autenticado e com atribuicao.  
**Entradas:** Escopo autenticado e filtros opcionais.  
**Processamento esperado:** Filtrar registros e apresentar contexto autorizado, estado e conflitos atuais.  
**Saidas:** Lista, sem resultados ou falha distinta.  
**Regras de negocio relacionadas:** RN-010, RN-011, RN-015.  
**Excecoes:** Sem atribuicao ou falha de consulta.  
**Criterios de aceite:** CA-RF-015-01: somente solicitacoes do escopo aparecem. CA-RF-015-02: filtros nao alteram estados.  
**Cenarios de teste futuros:** DADO solicitacao do escopo, QUANDO filtrar, ENTAO apresenta; item de outro escopo e negado.

### RF-016 — Aprovar solicitacao restrita
**ID:** RF-016  
**Nome:** Decisao de aprovacao autorizada.  
**Descricao:** O sistema deve permitir que responsavel autorizado aprove solicitacao `SOLICITADA` de recurso restrito somente apos revalidar estado, escopo, disponibilidade, professor e manutencao.  
**Persona relacionada:** Responsavel.  
**Jornada de origem:** J-RES-003.  
**Objetivo atendido:** Aprovar pedido especial disponivel e rastreavel.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Solicitacao pendente, recurso restrito e responsavel autorizado.  
**Entradas:** Solicitacao e confirmacao do responsavel.  
**Processamento esperado:** Revalidar regras; se elegivel, transicionar a `APROVADA`, gravar auditoria e acionar comunicacao.  
**Saidas:** Aprovada ou negada por conflito, estado alterado ou falta de permissao.  
**Regras de negocio relacionadas:** RN-002 a RN-011, RN-012, RN-016, RN-023.  
**Excecoes:** Recurso indisponivel durante espera; tratamento final de solicitacao pendente depende DP-006.  
**Criterios de aceite:** CA-RF-016-01: apenas responsavel autorizado aprova `SOLICITADA`. CA-RF-016-02: revalidacao detecta conflito/manutencao. CA-RF-016-03: aprovar duas vezes nao duplica transicao. CA-RF-016-04: auditoria identifica autor, instante, alvo e estados.  
**Cenarios de teste futuros:** DADA aprovacao autorizada livre, QUANDO confirmar, ENTAO `APROVADA` auditada; DADA perda de disponibilidade ou permissao, ENTAO negar.

### RF-017 — Rejeitar solicitacao com justificativa
**ID:** RF-017  
**Nome:** Decisao de rejeicao rastreavel.  
**Descricao:** O sistema deve permitir ao responsavel autorizado rejeitar solicitacao pendente de seu escopo, registrar justificativa e comunicar resultado.  
**Persona relacionada:** Responsavel e Solicitante.  
**Jornada de origem:** J-RES-002, J-RES-004.  
**Objetivo atendido:** Recusar pedido de forma explicavel e rastreavel.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Solicitacao `SOLICITADA`, responsavel autorizado, justificativa conforme regra definida.  
**Entradas:** Solicitacao e justificativa.  
**Processamento esperado:** Revalidar estado e permissao; transicionar a `REJEITADA`; auditar e comunicar.  
**Saidas:** Rejeicao confirmada ou recusa sem alteracao.  
**Regras de negocio relacionadas:** RN-010 a RN-012, RN-014, RN-016, RN-023.  
**Excecoes:** Formato/criterio da justificativa pendente DP-002.  
**Criterios de aceite:** CA-RF-017-01: rejeicao registra motivo e estados anterior/novo. CA-RF-017-02: estado ja resolvido ou usuario sem escopo impede acao.  
**Cenarios de teste futuros:** DADA solicitacao `SOLICITADA`, QUANDO responsavel autorizado rejeitar com motivo, ENTAO marcar `REJEITADA` e auditar; estado resolvido nao re-rejeita.

### RF-018 — Consultar disponibilidade docente
**ID:** RF-018  
**Nome:** Consultar agenda de professor.  
**Descricao:** O sistema deve permitir consulta autorizada de alocacoes de professor e sinalizar periodos conflitantes, minimizando dados pessoais.  
**Persona relacionada:** Solicitante, Responsavel e Administrador.  
**Jornada de origem:** J-SOL-002, J-SOL-003, J-SOL-004, J-RES-005, J-ADM-005.  
**Objetivo atendido:** Pesquisar por competencia/disponibilidade e validar alocacao docente.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Professor e intervalo identificados; consulta autorizada.  
**Entradas:** Identificador/competencia e intervalo.  
**Processamento esperado:** Validar periodo e comparar alocacoes; nao tratar falha/agenda ausente como disponibilidade.  
**Saidas:** Periodo sem conflito, conflito, sem informacao ou acesso negado.  
**Regras de negocio relacionadas:** RN-001 a RN-003, RN-015.  
**Excecoes:** Escopo de consulta pendente DP-003; fonte e completude da agenda docente pendentes DP-016.  
**Criterios de aceite:** CA-RF-018-01: alocacao sobreposta impede indicar disponibilidade. CA-RF-018-02: consulta nao aprova reserva.  
**Cenarios de teste futuros:** DADO professor ocupado no periodo, QUANDO consultar, ENTAO informar conflito; DADA falha de agenda, ENTAO nao afirmar livre.

### RF-019 — Registrar retirada
**ID:** RF-019  
**Nome:** Registro autorizado de retirada de material/equipamento.  
**Descricao:** O sistema deve permitir registrar retirada vinculada ao recurso/reserva, identificando operador, pessoa que recebeu e instante real.  
**Persona relacionada:** Responsavel.  
**Jornada de origem:** J-RES-006.  
**Objetivo atendido:** Rastrear custodia e identificar usuario do recurso.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Responsavel autorizado; reserva/recurso elegiveis; movimento ainda nao retirado.  
**Entradas:** Reserva, recurso, pessoa que retira, operador e instante.  
**Processamento esperado:** Validar correspondencia, escopo, autorizacao, estado e duplicidade; registrar movimento e auditoria.  
**Saidas:** Retirada confirmada ou recusada.  
**Regras de negocio relacionadas:** RN-010, RN-016, RN-020.  
**Excecoes:** Estado elegivel, terceiro retirante e transicao `EM_USO` pendentes DP-008/DP-006.  
**Criterios de aceite:** CA-RF-019-01: retirada sem reserva elegivel ou autorizacao e negada. CA-RF-019-02: registro diferencia quem operou de quem recebeu. CA-RF-019-03: duplicidade nao cria segundo movimento.  
**Cenarios de teste futuros:** DADA reserva elegivel e responsavel autorizado, QUANDO retirar, ENTAO registrar operador, recebedor e instante; movimento duplicado e recusado.

### RF-020 — Registrar devolucao
**ID:** RF-020  
**Nome:** Registro de devolucao vinculada.  
**Descricao:** O sistema deve permitir registrar devolucao para retirada aberta correspondente, identificando operador e instante real e impedindo duplicidade.  
**Persona relacionada:** Responsavel.  
**Jornada de origem:** J-RES-007.  
**Objetivo atendido:** Confirmar retorno e encerrar pendencia de movimento.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Responsavel autorizado; retirada aberta identificada.  
**Entradas:** Movimento de retirada, recurso, operador e instante.  
**Processamento esperado:** Confirmar correspondencia, registrar retorno, atualizar pendencia e auditoria. Transicao da reserva so conforme regra aprovada.  
**Saidas:** Devolucao confirmada, movimento inexistente, fora de escopo ou duplicado.  
**Regras de negocio relacionadas:** RN-010, RN-016, RN-021.  
**Excecoes:** Avaria, atraso e transicao `CONCLUIDA` pendentes DP-008/DP-006.  
**Criterios de aceite:** CA-RF-020-01: sem retirada correspondente nao se registra devolucao. CA-RF-020-02: devolucao e vinculada e nao duplicada. CA-RF-020-03: auditoria registra operador e instante.  
**Cenarios de teste futuros:** DADA retirada aberta, QUANDO devolver, ENTAO vincular retorno; DADA devolucao existente, QUANDO repetir, ENTAO manter um registro.

### RF-021 — Consultar movimentacoes e pendencias
**ID:** RF-021  
**Nome:** Consultar retiradas, devolucoes e pendencias.  
**Descricao:** O sistema deve permitir ao responsavel consultar movimentacoes e itens retirados sem devolucao dentro de seu escopo.  
**Persona relacionada:** Responsavel e Administrador conforme permissao.  
**Jornada de origem:** J-RES-006, J-RES-007, J-RES-008, J-ADM-006.  
**Objetivo atendido:** Identificar quem utilizou recurso e pendencias de retorno.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Usuario autorizado e movimento consultavel.  
**Entradas:** Escopo, recurso, reserva, periodo e estado de movimento.  
**Processamento esperado:** Correlacionar retirada/devolucao e listar pendentes; classificar atraso somente se prazo definido.  
**Saidas:** Historico, pendencias, vazio ou erro diferenciado.  
**Regras de negocio relacionadas:** RN-010, RN-015, RN-020, RN-021.  
**Excecoes:** Prazo de devolucao pendente DP-008.  
**Criterios de aceite:** CA-RF-021-01: movimento fora do escopo nao aparece. CA-RF-021-02: pendente sem prazo nao e rotulado como atrasado.  
**Cenarios de teste futuros:** DADO retirada sem devolucao, QUANDO consultar, ENTAO listar pendente; sem prazo, nao rotular atraso.

## Administracao, auditoria, notificacoes e relatorios

### RF-022 — Gerenciar usuarios e permissoes
**ID:** RF-022  
**Nome:** Administrar contas e perfis.  
**Descricao:** O sistema deve permitir a administrador autorizado cadastrar, consultar, pesquisar, editar, bloquear/desbloquear usuarios e ajustar permissoes dentro da matriz institucional.  
**Persona relacionada:** Administrador.  
**Jornada de origem:** J-ADM-001, J-ADM-003.  
**Objetivo atendido:** Assegurar acesso correspondente a funcao institucional.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Administrador autenticado; dados institucionais e politica de permissao definidos.  
**Entradas:** Dados cadastrais, identidade alvo, perfil, permissoes e acao.  
**Processamento esperado:** Validar campos, unicidade, permissao e impactos; aplicar alteracao e auditar.  
**Saidas:** Cadastro/alteracao confirmada, consulta ou erro.  
**Regras de negocio relacionadas:** RN-015, RN-017, RN-022.  
**Excecoes:** Matriz, unicidade e usuario com reserva ativa pendentes DP-003/DP-005.  
**Criterios de aceite:** CA-RF-022-01: apenas administrador autorizado altera acesso. CA-RF-022-02: alteracao gera registro rastreavel. CA-RF-022-03: bloqueio nao cancela reservas automaticamente.  
**Cenarios de teste futuros:** DADA autorizacao, QUANDO alterar perfil, ENTAO novo acesso e auditado; bloqueio com reserva ativa nao cancela reserva.

### RF-023 — Gerenciar salas
**ID:** RF-023  
**Nome:** Manter cadastro e situacao de salas.  
**Descricao:** O sistema deve permitir ao administrador autorizado cadastrar, consultar, pesquisar, atualizar e desativar salas com capacidade e localizacao.  
**Persona relacionada:** Administrador.  
**Jornada de origem:** J-ADM-004.  
**Objetivo atendido:** Manter salas corretas, evitar duplicidade e consultar impacto em reservas.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Autorizacao e dados da sala.  
**Entradas:** Identificacao, capacidade, localizacao, situacao e acao.  
**Processamento esperado:** Validar campos/duplicidade, consultar reservas vinculadas, atualizar sem apagar historico e auditar.  
**Saidas:** Cadastro/alteracao/situacao confirmada ou impedimento.  
**Regras de negocio relacionadas:** RN-008, RN-017, RN-022.  
**Excecoes:** Unicidade e reserva preexistente na desativacao pendentes DP-005.  
**Criterios de aceite:** CA-RF-023-01: desativada nao e oferecida para novas reservas. CA-RF-023-02: reserva vinculada nao e cancelada automaticamente.  
**Cenarios de teste futuros:** DADA sala com reserva futura, QUANDO desativar, ENTAO mostrar impacto e preservar reserva.

### RF-024 — Gerenciar professores
**ID:** RF-024  
**Nome:** Manter cadastro, competencias e situacao de professores.  
**Descricao:** O sistema deve permitir administrador autorizado cadastrar, consultar e atualizar professores, competencias e situacao cadastral, mostrando alocacoes pertinentes.  
**Persona relacionada:** Administrador.  
**Jornada de origem:** J-ADM-005.  
**Objetivo atendido:** Manter dados docentes e disponibilidade confiaveis.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Autorizacao e dados institucionais disponiveis.  
**Entradas:** Identificacao, competencias, situacao e alteracao.  
**Processamento esperado:** Validar cadastro; consultar alocacoes e alertar impacto; auditar alteracao sem remanejar reservas implicitamente.  
**Saidas:** Cadastro/detalhe/alteracao ou conflito.  
**Regras de negocio relacionadas:** RN-003, RN-008, RN-017, RN-022.  
**Excecoes:** Unicidade e impacto em reservas existentes pendentes DP-005.  
**Criterios de aceite:** CA-RF-024-01: agenda conflitante nao e indicada como livre. CA-RF-024-02: alteracao cadastral nao apaga alocacao existente.  
**Cenarios de teste futuros:** DADO professor alocado, QUANDO consultar sobreposicao, ENTAO sinalizar conflito; alteracao de situacao preserva reserva.

### RF-025 — Gerenciar materiais e equipamentos
**ID:** RF-025  
**Nome:** Manter cadastro e situacao de itens.  
**Descricao:** O sistema deve permitir administrador autorizado cadastrar, consultar, pesquisar e atualizar materiais/equipamentos, incluindo situacao, reservas e movimentacoes relacionadas.  
**Persona relacionada:** Administrador.  
**Jornada de origem:** J-ADM-006.  
**Objetivo atendido:** Manter inventario consistente e localizar manutencao/movimentacao.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Autorizacao e dados do item.  
**Entradas:** Tipo, identificacao, caracteristicas, situacao e acao.  
**Processamento esperado:** Validar identificacao, consultar reservas/movimentos, atualizar dados sem apagar historico e auditar.  
**Saidas:** Cadastro/detalhe/alteracao e situacao atual.  
**Regras de negocio relacionadas:** RN-007, RN-008, RN-017, RN-022.  
**Excecoes:** Duplicidade, retirada aberta e desativacao com reservas pendentes DP-005.  
**Criterios de aceite:** CA-RF-025-01: manutencao impede disponibilidade coincidente. CA-RF-025-02: alteracao nao elimina movimentacoes.  
**Cenarios de teste futuros:** DADO item com retirada aberta, QUANDO atualizar cadastro, ENTAO preservar o movimento e auditar alteracao.

### RF-026 — Registrar periodo de manutencao
**ID:** RF-026  
**Nome:** Bloquear recurso para manutencao.  
**Descricao:** O sistema deve permitir a administrador autorizado registrar recurso e periodo de manutencao, identificar reservas coincidentes e impedir novas reservas conflitantes.  
**Persona relacionada:** Administrador.  
**Jornada de origem:** J-ADM-002, J-ADM-006, J-ADM-007.  
**Objetivo atendido:** Manter recurso indisponivel durante manutencao.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Recurso ativo, administrador autorizado e periodo informado.  
**Entradas:** Recurso, inicio, termino e motivo conforme politica.  
**Processamento esperado:** Validar periodo, buscar manutencoes e reservas; impedir novas reservas conflitantes; identificar reservas existentes sem cancela-las automaticamente; registrar auditoria.  
**Saidas:** Bloqueio confirmado ou impacto pendente/erro apresentado.  
**Regras de negocio relacionadas:** RN-001, RN-002, RN-005, RN-007, RN-016, RN-017.  
**Excecoes:** Desfecho para reserva preexistente pendente DP-004.  
**Criterios de aceite:** CA-RF-026-01: periodo invalido nao e salvo. CA-RF-026-02: nova reserva coincidente e impedida. CA-RF-026-03: reservas existentes sao identificadas e preservadas ate decisao.  
**Cenarios de teste futuros:** DADA reserva existente coincidente, QUANDO bloquear, ENTAO sinalizar reserva e nao cancelar automaticamente; solicitacao concorrente apos bloqueio nao e aceita.

### RF-027 — Consultar reservas administrativamente
**ID:** RF-027  
**Nome:** Consultar reservas e seus impactos.  
**Descricao:** O sistema deve permitir ao administrador autorizado pesquisar reservas por periodo, recurso, usuario e estado, para operacao e analise de impacto.  
**Persona relacionada:** Administrador.  
**Jornada de origem:** J-ADM-002, J-ADM-003, J-ADM-004, J-ADM-005, J-ADM-006, J-ADM-007, J-ADM-008.  
**Objetivo atendido:** Identificar conflitos, vinculos e inconsistencias.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Administrador autenticado e consulta autorizada.  
**Entradas:** Filtros de periodo, recurso, usuario e estado.  
**Processamento esperado:** Recuperar resultados atuais e detalhes conforme permissao, sem alterar dados.  
**Saidas:** Lista/detalhe, vazio, acesso negado ou falha.  
**Regras de negocio relacionadas:** RN-012, RN-015, RN-022.  
**Excecoes:** Dados sensiveis filtrados conforme DP-011.  
**Criterios de aceite:** CA-RF-027-01: consulta respeita filtros e permissao. CA-RF-027-02: consulta nao muda estado.  
**Cenarios de teste futuros:** DADO filtro por recurso/estado, QUANDO consultar, ENTAO retornar apenas registros correspondentes e autorizados.

### RF-028 — Apresentar dashboard operacional
**ID:** RF-028  
**Nome:** Dashboard administrativo de recursos e reservas.  
**Descricao:** O sistema deve apresentar totais de usuarios, recursos cadastrados/disponiveis/reservados/em manutencao, pendencias, utilizacao e situacoes de atencao conforme definicoes aprovadas.  
**Persona relacionada:** Administrador.  
**Jornada de origem:** J-ADM-002.  
**Objetivo atendido:** Monitorar operacao e localizar riscos/inconsistencias.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Administrador autorizado e dados disponiveis.  
**Entradas:** Periodo/filtros quando definidos.  
**Processamento esperado:** Calcular agregados sem duplicidade, indicar atualizacao e nao tratar falha como zero.  
**Saidas:** Indicadores, data/periodo e estado de dados.  
**Regras de negocio relacionadas:** RN-004, RN-007, RN-012, RN-024.  
**Excecoes:** Definicoes de estado/periodo/atencao pendentes DP-010.  
**Criterios de aceite:** CA-RF-028-01: recurso em manutencao nao conta disponivel no periodo. CA-RF-028-02: falha e distinta de zero. CA-RF-028-03: estados de reserva sao identificaveis.  
**Cenarios de teste futuros:** DADO falha em uma fonte, QUANDO carregar painel, ENTAO marcar dado indisponivel, nao zero.

### RF-029 — Consultar auditoria
**ID:** RF-029  
**Nome:** Pesquisar eventos auditaveis.  
**Descricao:** O sistema deve permitir a perfil autorizado filtrar eventos por periodo, usuario, recurso e operacao e consultar valores de estado anterior/novo quando existentes.  
**Persona relacionada:** Administrador e Responsavel em escopo de leitura definido.  
**Jornada de origem:** J-SOL-005, J-RES-002, J-RES-008, J-ADM-008.  
**Objetivo atendido:** Investigar alteracoes e reconstruir decisoes.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Perfil autorizado e filtros validos.  
**Entradas:** Periodo, usuario, recurso/reserva e acao.  
**Processamento esperado:** Recuperar eventos em modo leitura; proteger informacao sensivel; distinguir vazio de falha.  
**Saidas:** Eventos, vazio, negacao ou falha.  
**Regras de negocio relacionadas:** RN-015, RN-016 a RN-018, RN-022.  
**Excecoes:** Retencao/filtros e escopo detalhado pendentes DP-011.  
**Criterios de aceite:** CA-RF-029-01: somente perfil autorizado consulta. CA-RF-029-02: administrador nao edita/exclui evento. CA-RF-029-03: falha nao aparece como ausencia de evento.  
**Cenarios de teste futuros:** DADO evento filtravel, QUANDO buscar, ENTAO mostrar autor/instante/alvo/operacao; tentativa de edicao e negada.

### RF-030 — Registrar auditoria de operacoes relevantes
**ID:** RF-030  
**Nome:** Registrar autoria e mudancas auditaveis.  
**Descricao:** O sistema deve registrar mudancas de estado e operacoes relevantes em reserva, cadastro, manutencao e movimentacao, incluindo ator, instante, alvo, acao e valores anterior/novo quando aplicavel.  
**Persona relacionada:** Todos os perfis.  
**Jornada de origem:** J-SOL-004, J-SOL-006 a J-SOL-008; J-RES-003, J-RES-004, J-RES-006, J-RES-007; J-ADM-003 a J-ADM-008.  
**Objetivo atendido:** Rastreabilidade e confiabilidade das alteracoes.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Operacao relevante validada.  
**Entradas:** Ator, instante, alvo, operacao, estado/valores anterior e novo.  
**Processamento esperado:** Registrar evento ligado a operacao; nao confirmar mudanca de estado sem auditoria consistente.  
**Saidas:** Operacao confirmada com evento ou falha sem sucesso presumido.  
**Regras de negocio relacionadas:** RN-016, RN-017, RN-018.  
**Excecoes:** Retencao e campos sensiveis pendentes DP-011.  
**Criterios de aceite:** CA-RF-030-01: toda transicao registra os seis elementos exigidos. CA-RF-030-02: falha de auditoria nao declara transicao concluida. CA-RF-030-03: perfil comum nao altera historico.  
**Cenarios de teste futuros:** DADA transicao, QUANDO confirmar, ENTAO evento inclui autor, instante, alvo, operacao, anterior e novo; falha de auditoria nao retorna sucesso.

### RF-031 — Confirmar registro de solicitacao
**ID:** RF-031  
**Nome:** Comunicar recebimento da solicitacao.  
**Descricao:** O sistema deve gerar confirmacao vinculada ao solicitante quando uma solicitacao for efetivamente registrada, incluindo identificacao e estado retornado.  
**Persona relacionada:** Solicitante.  
**Jornada de origem:** J-SOL-004, J-SOL-008.  
**Objetivo atendido:** Saber que o pedido foi recebido e acompanhar seu estado.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Registro confirmado.  
**Entradas:** Evento de registro, solicitante, reserva e estado.  
**Processamento esperado:** Preparar comunicacao sem afirmar estado nao confirmado.  
**Saidas:** Confirmacao apresentada/comunicada ou falha de entrega identificada.  
**Regras de negocio relacionadas:** RN-023.  
**Excecoes:** Canal e retentativa pendentes DP-009.  
**Criterios de aceite:** CA-RF-031-01: solicitacao nao gravada nao recebe confirmacao de sucesso. CA-RF-031-02: confirmacao mostra estado real.  
**Cenarios de teste futuros:** DADO registro confirmado, QUANDO processar retorno, ENTAO comunicar identificacao/estado; falha de entrega nao altera reserva.

### RF-032 — Comunicar decisoes e alteracoes relevantes
**ID:** RF-032  
**Nome:** Notificar aprovacao, rejeicao, cancelamento, alteracao e pendencia.  
**Descricao:** O sistema deve comunicar ao destinatario autorizado mudancas relevantes e acoes que exijam participacao do solicitante, com estado e contexto pertinentes.  
**Persona relacionada:** Solicitante e Responsavel conforme evento.  
**Jornada de origem:** J-SOL-008, J-RES-003, J-RES-004, J-RES-008.  
**Objetivo atendido:** Conhecer decisoes, mudancas e pendencias.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Evento valido e destinatario autorizado identificado.  
**Entradas:** Reserva, evento, estado anterior/novo, decisao e justificativa permitida.  
**Processamento esperado:** Gerar comunicacao vinculada; evitar duplicar evento como nova decisao; limitar dados.  
**Saidas:** Atualizacao para consultar ou falha identificada.  
**Regras de negocio relacionadas:** RN-014, RN-016, RN-023.  
**Excecoes:** Canal, prazo e evento exato para “alteracao relevante” pendentes DP-009.  
**Criterios de aceite:** CA-RF-032-01: aprovacao/rejeicao/cancelamento confirmados comunicam estado atualizado. CA-RF-032-02: justificativa autorizada acompanha rejeicao.  
**Cenarios de teste futuros:** DADA rejeicao auditada, QUANDO notificar, ENTAO solicitante recebe estado e motivo autorizado.

### RF-033 — Tratar falha de notificacao sem reverter operacao
**ID:** RF-033  
**Nome:** Preservar resultado diante de falha de comunicacao.  
**Descricao:** O sistema deve manter consultavel a operacao confirmada mesmo se o envio de notificacao falhar, e indicar falha de comunicacao quando esse estado estiver disponivel.  
**Persona relacionada:** Solicitante, Responsavel e Administrador.  
**Jornada de origem:** J-SOL-008, J-RES-003, J-RES-004.  
**Objetivo atendido:** Confiar no estado da reserva mesmo com falha de entrega.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Operacao de negocio foi confirmada e comunicacao falhou.  
**Entradas:** Resultado confirmado e estado de entrega.  
**Processamento esperado:** Nao reverter estado auditado; manter consulta atual disponivel; registrar/tratar falha conforme capacidade definida.  
**Saidas:** Operacao preservada e falha diferenciada.  
**Regras de negocio relacionadas:** RN-023.  
**Excecoes:** Retentativa, canal, prazo e evidencias de entrega pendentes DP-009.  
**Criterios de aceite:** CA-RF-033-01: falha de entrega nao desfaz aprovacao/cancelamento. CA-RF-033-02: consulta da reserva mostra o estado confirmado.  
**Cenarios de teste futuros:** DADA aprovacao gravada e falha de envio, QUANDO consultar reserva, ENTAO permanece aprovada e auditada.

### RF-034 — Relatar utilizacao por recurso
**ID:** RF-034  
**Nome:** Relatorio de utilizacao e recursos mais/menos usados.  
**Descricao:** O sistema deve permitir ao administrador autorizado consultar utilizacao por recurso e identificar os mais e menos utilizados para periodo e criterio definidos.  
**Persona relacionada:** Administrador.  
**Jornada de origem:** J-ADM-002, J-ADM-009.  
**Objetivo atendido:** Avaliar uso e localizar desequilibrios de alocacao.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Dados e definicao de utilizacao disponiveis.  
**Entradas:** Periodo, tipo/recurso e filtros.  
**Processamento esperado:** Agregar registros conforme definicao; exibir periodo, unidade e atualizacao; sinalizar dados parciais.  
**Saidas:** Relatorio ou motivo de indisponibilidade.  
**Regras de negocio relacionadas:** RN-024.  
**Excecoes:** Formula e janela temporal pendentes DP-010.  
**Criterios de aceite:** CA-RF-034-01: resultado explicita criterio e periodo. CA-RF-034-02: falha/incompletude nao e apresentada como zero confiavel.  
**Cenarios de teste futuros:** DADOS eventos conhecidos e definicao aprovada, QUANDO consultar, ENTAO agregados correspondem a fonte e periodo.

### RF-035 — Relatar carga horaria, reservas e cancelamentos
**ID:** RF-035  
**Nome:** Relatorio temporal de alocacao e reservas.  
**Descricao:** O sistema deve permitir consultar carga horaria alocada, quantidade de reservas por periodo e quantidade de cancelamentos, com criterio de inclusao explicito.  
**Persona relacionada:** Administrador.  
**Jornada de origem:** J-ADM-009.  
**Objetivo atendido:** Analisar demanda e carga de recursos.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Periodo e definicoes metricas disponiveis.  
**Entradas:** Periodo, recurso/tipo e criterios.  
**Processamento esperado:** Agregar periodos e estados incluidos pela definicao aprovada; informar unidade/criterio.  
**Saidas:** Indicadores de carga, reservas e cancelamentos ou indisponibilidade.  
**Regras de negocio relacionadas:** RN-012, RN-024.  
**Excecoes:** Inclusao de estados, formula e janela pendentes DP-010.  
**Criterios de aceite:** CA-RF-035-01: definicao dos estados contabilizados e visivel. CA-RF-035-02: periodo consultado esta identificado.  
**Cenarios de teste futuros:** DADA definicao aprovada, QUANDO consultar periodo com estados conhecidos, ENTAO contagens respeitam o criterio sem duplicidade.

### RF-036 — Relatar conflitos impedidos e periodos de pico
**ID:** RF-036  
**Nome:** Indicadores de conflitos evitados e demanda temporal.  
**Descricao:** O sistema deve apresentar periodos de maior utilizacao e, quando houver evidencia e definicao aprovada, quantidade de tentativas conflitantes impedidas. Nao deve estimar conflitos nao registrados.  
**Persona relacionada:** Administrador.  
**Jornada de origem:** J-ADM-002, J-ADM-009.  
**Objetivo atendido:** Identificar picos e demonstrar conflitos evitados.  
**Prioridade:** Obrigatorio.  
**Pre-condicoes:** Registros e definicoes para cada metrica.  
**Entradas:** Periodo, recurso/tipo e eventos de conflito registrados.  
**Processamento esperado:** Agregar somente evidencia registrada e indicar metrica pendente/indisponivel se fonte ou criterio faltar.  
**Saidas:** Indicadores com periodo/definicao ou aviso de indisponibilidade.  
**Regras de negocio relacionadas:** RN-005, RN-024.  
**Excecoes:** Formula, janela e registro de tentativas impedidas pendentes DP-010.  
**Criterios de aceite:** CA-RF-036-01: conflito evitado nao e inventado/estimado sem fonte. CA-RF-036-02: periodo de pico explicita criterio.  
**Cenarios de teste futuros:** DADA ausencia de fonte de conflitos, QUANDO gerar relatorio, ENTAO indicar metrica indisponivel; com eventos registrados, agregar apenas esses eventos.

## Cobertura de fluxos publicos/API

Os RF-001 a RF-036 especificam comportamentos de negocio independentemente de canal. Nenhuma jornada ou documento de escopo define endpoints, contrato externo, operacao anonima ou API publica. A necessidade de documentar fluxos publicos/API conforme criterio do professor esta registrada como DP-012; nao se criam requisitos de endpoint nem se presume acesso anonimo nesta etapa.
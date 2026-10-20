# Jornadas de utilizacao — Solicitante

Documento derivado de [SOLICITANTE.md](../personas/SOLICITANTE.md). Descreve comportamento esperado, sem definir tecnologias ou arquitetura. Disponibilidade exibida e uma consulta do momento, nao uma garantia de reserva. Para intervalos, considera-se a convencao solicitada de inicio inclusivo e termino exclusivo: `[inicio, termino)`.

## J-SOL-001 — Autenticacao e encerramento de sessao

**ID da jornada:** J-SOL-001  
**Nome:** Autenticar-se, acessar a area autorizada e encerrar a sessao  
**Persona responsavel:** Solicitante (Marina Costa)  
**Objetivo da persona atendido:** Acessar consultas e operacoes proprias com seguranca.  
**Necessidade ou dor relacionada:** Confirmacao de acesso, informacoes confiaveis e protecao contra uso indevido.  
**Prioridade inicial:** Essencial — sem autenticacao nao ha acesso seguro a reservas pessoais.  
**Pre-condicoes:** Usuario possui credenciais; estado cadastral e politicas de acesso devem ser consultaveis.  
**Gatilho:** Usuario abre a aplicacao ou escolhe encerrar a sessao.

### Fluxo principal
1. A interface solicita identificacao e credenciais.
2. O usuario informa os dados e confirma a entrada.
3. O sistema valida as credenciais e a situacao de acesso.
4. O sistema identifica o perfil e as permissoes vigentes; para Solicitante, apresenta as funcoes permitidas.
5. O usuario pode encerrar a sessao; a aplicacao confirma o encerramento e volta a area de acesso.

### Responsabilidades do Front-end
- Apresentar campos de credencial, acao de entrar e acao de sair quando autenticado.
- Indicar carregamento, credenciais invalidas, acesso negado e falha de recuperacao sem revelar dados sensiveis.
- Direcionar para a area correspondente ao perfil; ocultar funcoes nao pertinentes sem tratar ocultacao como controle de acesso.
- Ao sair, limpar a apresentacao de dados privados e impedir navegacao autenticada pela interface.

### Responsabilidades do Back-end
- Validar identidade, credenciais e situacao de acesso; determinar perfil e permissoes efetivas.
- Revalidar autorizacao em cada operacao protegida, inclusive quando solicitada diretamente.
- Encerrar o contexto de autenticacao e rejeitar usos posteriores desse contexto, conforme politica definida.
- Nao revelar se uma identificacao existe ao apresentar falha de credencial, salvo regra institucional aprovada.

### Dados envolvidos
Identificacao, credenciais, situacao de acesso, perfil, permissoes e contexto de sessao; nunca expor segredo de autenticacao na interface.

### Regras de negocio aplicaveis
Somente usuario autenticado pode consultar registros pessoais ou executar operacoes protegidas. Solicitante nao recebe permissao administrativa nem para aprovar recursos restritos.

### Fluxos alternativos
- Credenciais invalidas: manter acesso fechado e permitir nova tentativa sem informar qual dado falhou.
- Perfil sem acesso ou usuario inativo: negar entrada e apresentar orientacao neutra.
- Funcao nao autorizada chamada por link ou navegacao antiga: negar a operacao e retornar a uma area permitida.
- Falha de encerramento: nao declarar a sessao encerrada se o sistema nao confirmar; bloquear a exibicao local de dados e informar a falha.

### Tratamento de erros
Falha de validacao recebe mensagem objetiva; falha interna ou de recuperacao recebe mensagem generica e opcao de tentar novamente. Nenhuma resposta deve expor credenciais ou dados de terceiros.

### Resultado esperado
Usuario autenticado com acesso limitado ao perfil Solicitante, ou acesso negado sem exposicao indevida. Ao sair, o contexto autenticado deixa de autorizar operacoes.

### Condicoes de conclusao
Acesso foi confirmado e direcionado corretamente, ou foi negado; alternativamente, o encerramento foi confirmado.

### Cenarios verificaveis
- **Sucesso:** DADO um solicitante ativo com credenciais validas, QUANDO autenticar, ENTAO deve acessar somente as funcoes permitidas ao perfil.
- **Falha:** DADO credenciais invalidas, QUANDO tentar entrar, ENTAO o acesso deve ser negado sem indicar qual credencial esta incorreta.
- **Limite:** DADO um solicitante autenticado, QUANDO tentar abrir uma funcao administrativa por link direto, ENTAO a operacao deve ser negada no processamento, nao apenas ocultada na interface.
- **Saida:** DADA uma sessao autenticada, QUANDO o solicitante encerrar a sessao, ENTAO operacoes seguintes devem exigir nova autenticacao.

## J-SOL-002 — Consultar e pesquisar recursos

**ID da jornada:** J-SOL-002  
**Nome:** Pesquisar salas, professores, materiais e equipamentos  
**Persona responsavel:** Solicitante (Marina Costa)  
**Objetivo da persona atendido:** Encontrar recursos compativeis com uma atividade academica.  
**Necessidade ou dor relacionada:** Pesquisa rapida, informacao atualizada e dificuldade para localizar recursos.  
**Prioridade inicial:** Essencial — e a entrada principal para planejar uma solicitacao.  
**Pre-condicoes:** Usuario autenticado; recursos consultaveis cadastrados.  
**Gatilho:** Usuario abre a pesquisa de recursos.

### Fluxo principal
1. A interface apresenta tipos de recurso e filtros aplicaveis.
2. O usuario escolhe um ou mais tipos e informa criterios: capacidade/localizacao para sala, competencia para professor, e data/horario e disponibilidade quando desejado.
3. O sistema valida formato e coerencia dos filtros e consulta recursos visiveis ao perfil.
4. A interface apresenta resultados e caracteristicas relevantes, disponibilidade consultada, manutencao e indicacao de restricao.
5. O usuario abre um resultado para consultar detalhes ou continua ajustando os filtros.

### Responsabilidades do Front-end
- Apresentar filtros adequados ao tipo de recurso e manter os criterios usados visiveis.
- Distinguir resultado indisponivel, restrito, em manutencao e sem disponibilidade conhecida.
- Apresentar carregamento, sem resultados e falha de consulta com possibilidade de ajustar ou repetir a busca.
- Nao apresentar resultado de busca como reserva confirmada.

### Responsabilidades do Back-end
- Validar filtros, aplicar autorizacao de consulta e recuperar dados atualizados permitidos.
- Aplicar criterios por tipo e calcular disponibilidade para periodo apenas quando informado.
- Evitar expor dados pessoais ou administrativos sem necessidade para a pesquisa.

### Dados envolvidos
Tipo e identificacao do recurso; capacidade e localizacao; competencias de professores; caracteristicas e estado de materiais/equipamentos; periodo pesquisado; disponibilidade, restricoes e manutencao.

### Regras de negocio aplicaveis
Resultados devem respeitar permissoes e estado cadastral. Recursos em manutencao nao podem ser apresentados como reservaveis no periodo coincidente. Disponibilidade e temporal e pode mudar antes do envio da solicitacao.

### Fluxos alternativos
- Filtros sem correspondencia: apresentar estado sem resultados e permitir remover ou ajustar filtros.
- Recurso desativado ou inexistente ao abrir detalhe: informar que nao esta disponivel para consulta/solicitacao e atualizar os resultados.
- Filtro temporal invalido: apontar campos que precisam de correcao antes da consulta.

### Tratamento de erros
Falha na recuperacao nao deve ser confundida com lista vazia. Informar indisponibilidade temporaria da consulta e nao exibir disponibilidade presumida como atual.

### Resultado esperado
Lista de recursos e detalhes autorizados, com criterios de disponibilidade compreensiveis e sem promessa de reserva.

### Condicoes de conclusao
Resultados foram apresentados, nenhum resultado foi encontrado com feedback apropriado, ou a busca foi encerrada com erro identificado.

### Cenarios verificaveis
- **Sucesso:** DADO que existem salas com capacidade e horario compativeis, QUANDO Marina filtrar por capacidade, localizacao e periodo, ENTAO a lista deve conter apenas resultados compativeis e autorizados.
- **Falha:** DADO que a consulta nao pode recuperar dados, QUANDO Marina pesquisar, ENTAO a interface deve distinguir falha de lista vazia.
- **Limite:** DADO um equipamento em manutencao no periodo pesquisado, QUANDO Marina consultar disponibilidade, ENTAO ele nao deve ser indicado como reservavel nesse periodo.

## J-SOL-003 — Consultar disponibilidade de um recurso

**ID da jornada:** J-SOL-003  
**Nome:** Verificar disponibilidade por recurso e periodo  
**Persona responsavel:** Solicitante (Marina Costa)  
**Objetivo da persona atendido:** Confirmar se o periodo pode prosseguir para solicitacao.  
**Necessidade ou dor relacionada:** Evitar conflitos e informacao desatualizada.  
**Prioridade inicial:** Essencial — antecede a solicitacao e reduz tentativas invalidas.  
**Pre-condicoes:** Usuario autenticado; recurso existente e consultavel.  
**Gatilho:** Usuario seleciona recurso e informa inicio e termino.

### Fluxo principal
1. A interface solicita o periodo e apresenta o recurso selecionado.
2. O usuario informa inicio e termino e, quando pertinente, professor ou recursos associados a atividade.
3. O sistema valida que o termino e posterior ao inicio.
4. O sistema consulta reservas aceitas, periodos de manutencao, restricoes e disponibilidade de professores envolvidos.
5. O sistema detecta sobreposicoes usando intervalos `[inicio, termino)`: sobreposicao parcial ou total conflita; termino igual ao inicio da proxima reserva nao conflita.
6. A interface informa disponibilidade, conflito, manutencao, restricao/aprovacao necessaria ou impossibilidade de continuar.

### Responsabilidades do Front-end
- Solicitar datas/horarios claros e mostrar recurso e fuso/calendario institucional quando definido.
- Validar localmente campos obvios e apresentar resultado, carregamento, conflito e dados possivelmente desatualizados.
- Explicar que a consulta nao reserva nem bloqueia o recurso.

### Responsabilidades do Back-end
- Revalidar periodo e permissao de consulta.
- Consultar todas as alocacoes relevantes do recurso e dos professores envolvidos e os bloqueios de manutencao.
- Aplicar intervalo semiaberto `[inicio, termino)` e identificar conflitos e restricoes vigentes.
- Retornar se pode prosseguir e a razao de impedimento; nao confirmar reserva nesta jornada.

### Dados envolvidos
Identificador do recurso, inicio, termino, professor(es) e recursos associados quando aplicavel, reservas e estados considerados, manutencoes, restricoes e resultado da consulta.

### Regras de negocio aplicaveis
Termino posterior ao inicio; nao sobrepor reserva aceita; disponibilidade de professores obrigatoria; periodo de manutencao bloqueia; recurso restrito pode exigir aprovacao. Resultado e informativo e deve ser novamente validado no envio.

### Fluxos alternativos
- Inicio igual/posterior ao termino: impedir consulta e indicar correcao.
- Recurso inexistente/desativado: informar indisponibilidade do cadastro.
- Professor alocado ou recurso em manutencao: apresentar conflito e impedir indicacao de disponibilidade.
- Consulta concorrente ou dados alterados desde a exibicao: indicar que o resultado pode ter mudado e permitir nova consulta.

### Tratamento de erros
Falha de recuperacao deve ser identificada como falha, sem inferir disponibilidade. A interface oferece nova tentativa sem perder os dados informados.

### Resultado esperado
Resposta atual da consulta com motivos de impedimento ou indicacao de que a solicitacao pode prosseguir, sem assegurar que sera aceita.

### Condicoes de conclusao
O sistema devolveu resultado valido ou explicou por que nao foi possivel consultar.

### Cenarios verificaveis
- **Sucesso:** DADO um periodo valido sem reservas, manutencao ou conflito de professor, QUANDO Marina consultar, ENTAO o sistema deve indicar que pode prosseguir, sem criar reserva.
- **Falha:** DADO termino anterior ao inicio, QUANDO Marina consultar, ENTAO o sistema deve recusar a consulta e solicitar correcao.
- **Limite:** DADA uma reserva que termina as 12:00 e outra consulta iniciando as 12:00, QUANDO verificar sobreposicao, ENTAO nao deve haver conflito apenas por compartilharem esse limite.
- **Conflito:** DADA uma reserva das 10:00 as 12:00, QUANDO consultar das 11:00 as 13:00, ENTAO deve ser informado conflito.

## J-SOL-004 — Criar solicitacao de reserva

**ID da jornada:** J-SOL-004  
**Nome:** Solicitar reserva de recurso comum ou restrito  
**Persona responsavel:** Solicitante (Marina Costa)  
**Objetivo da persona atendido:** Solicitar recurso para uma atividade e acompanhar sua decisao.  
**Necessidade ou dor relacionada:** Confirmacao de envio, evitar conflitos e compreender aprovacao/rejeicao.  
**Prioridade inicial:** Essencial — operacao central do sistema.  
**Pre-condicoes:** Usuario autenticado; recurso consultavel; dados requeridos pelo processo disponiveis.  
**Gatilho:** Usuario escolhe solicitar reserva a partir de um recurso ou consulta.

### Fluxo principal
1. A interface apresenta resumo do recurso e solicita periodo, finalidade e demais informacoes exigidas para avaliar a solicitacao.
2. Marina revisa e confirma o envio.
3. O sistema autentica o solicitante, valida campos e periodo e confere se o usuario pode solicitar o recurso.
4. No momento da confirmacao, o sistema consulta novamente reservas, professores envolvidos, manutencao, estado do cadastro e restricoes; nao confia na consulta anterior.
5. O processamento impede que solicitacoes simultaneas aceitem duas reservas conflitantes para o mesmo recurso/perfil temporal. Em colisao, uma pode prosseguir e a outra recebe indisponibilidade.
6. Se o recurso for restrito, cria solicitacao `SOLICITADA` para decisao de responsavel autorizado. Para recurso comum, o estado final inicial (aceite imediato ou `SOLICITADA`) permanece pendente de definicao; a interface nao o apresenta como aprovado antes de confirmacao explicita do sistema.
7. O sistema registra a operacao e retorna identificacao/estado e, quando aplicavel, informa que aguarda aprovacao.

### Responsabilidades do Front-end
- Apresentar formulario e campos necessarios, sem pedir dados sem finalidade identificada.
- Mostrar resumo antes do envio, validacoes, carregamento e resultado.
- Diferenciar solicitacao recebida, aguardando aprovacao, aprovada e indisponibilidade.
- Em retorno de conflito, manter os dados nao sensiveis para ajuste e nova consulta.

### Responsabilidades do Back-end
- Validar identidade, propriedade, campos obrigatorios e periodo.
- Revalidar disponibilidade e restricoes no instante da decisao; considerar recursos e professores vinculados.
- Garantir resultado sem dupla aceitacao em envios concorrentes, sem pressupor tecnologia/estrategia.
- Criar ou nao a solicitacao conforme resultado e regra do tipo de recurso; registrar auditoria das mudancas de estado.
- Encaminhar recurso restrito somente a responsavel autorizado e informar o resultado ao solicitante conforme fluxo de notificacao.

### Dados envolvidos
Solicitante; recurso(s); periodo; finalidade e justificativa exigidas; professores envolvidos; restricao; estado cadastral, reservas e manutencoes coincidentes; resultado e autoria/data da operacao.

### Regras de negocio aplicaveis
Periodo valido; sem sobreposicao aceita; verificar professor e manutencao; recurso restrito exige aprovacao autorizada. Consulta anterior nao garante disponibilidade. Estados permitidos incluem `SOLICITADA`, `APROVADA`, `REJEITADA`, `CANCELADA`, `EM_USO` e `CONCLUIDA`.

### Fluxos alternativos
- Recurso restrito: criar `SOLICITADA` e informar a espera, sem permitir autoaprovacao.
- Conflito identificado antes ou durante processamento: nao aceitar a reserva conflitante; informar indisponibilidade.
- Professor ja alocado, recurso em manutencao ou desativado: impedir prosseguimento e indicar motivo.
- Dados incompletos: apresentar campos a corrigir sem criar solicitacao.
- Retorno inconclusivo: nao exibir confirmacao de sucesso; permitir consultar minhas reservas para verificar se houve registro.

### Tratamento de erros
Erro de validacao, autorizacao, conflito e indisponibilidade devem ser diferenciados. Falha interna ou de recuperacao nao pode resultar em sucesso presumido; preservar dados do formulario quando seguro.

### Resultado esperado
Solicitacao registrada no estado definido para o tipo de recurso, ou recusada sem aceitar conflito. Em recurso restrito, fica aguardando decisao autorizada.

### Condicoes de conclusao
Usuario recebe resultado confirmado com estado e proxima acao, ou justificativa de que a solicitacao nao foi criada.

### Cenarios verificaveis
- **Sucesso:** DADO um recurso restrito disponivel, QUANDO Marina enviar dados validos, ENTAO deve ser criada solicitacao `SOLICITADA`, sem aprovacoes pelo solicitante.
- **Falha:** DADO termino igual ou anterior ao inicio, QUANDO Marina enviar a solicitacao, ENTAO o sistema deve recusar o periodo e nao criar reserva.
- **Conflito:** DADA uma reserva aceita sobreposta, QUANDO uma nova solicitacao for confirmada, ENTAO nao deve ser aceita e a interface deve informar indisponibilidade.
- **Concorrencia:** DADOS dois solicitantes enviando simultaneamente periodos conflitantes para o mesmo recurso, QUANDO ambos forem processados, ENTAO no maximo um resultado pode ser aceito e o outro deve receber conflito/indisponibilidade.
- **Limite:** DADO que a consulta previa indicou disponibilidade mas surgiu uma reserva antes da confirmacao, QUANDO Marina enviar, ENTAO o sistema deve revalidar e nao aceitar sobreposicao.

## J-SOL-005 — Consultar minhas reservas e historico

**ID da jornada:** J-SOL-005  
**Nome:** Consultar reservas proprias, estados e historico  
**Persona responsavel:** Solicitante (Marina Costa)  
**Objetivo da persona atendido:** Acompanhar solicitacoes e entender suas decisoes.  
**Necessidade ou dor relacionada:** Nao saber se pedido foi recebido/aprovado e nao compreender rejeicao.  
**Prioridade inicial:** Essencial — da previsibilidade e evita contato manual com setores.  
**Pre-condicoes:** Usuario autenticado; pode haver ou nao registros proprios.  
**Gatilho:** Usuario abre “minhas reservas” ou o historico.

### Fluxo principal
1. A interface apresenta filtros por periodo/data e estado.
2. Marina aplica filtros ou consulta a lista completa autorizada.
3. O sistema recupera apenas reservas e historico vinculados ao solicitante.
4. A interface apresenta recurso, periodo, estado atual, decisao/justificativa registrada e detalhes autorizados.
5. Marina abre um item para ver sua sequencia de alteracoes e acoes disponiveis para aquele estado.

### Responsabilidades do Front-end
- Apresentar lista, filtros, estados em linguagem clara e detalhe/historico.
- Diferenciar `SOLICITADA`, `APROVADA`, `EM_USO`, `CONCLUIDA`, `REJEITADA` e `CANCELADA`.
- Mostrar carregamento, sem resultados, dados desatualizados e falha sem expor reservas de terceiros.
- Indicar que acoes dependem do estado sem sugerir que estao disponiveis quando nao confirmadas.

### Responsabilidades do Back-end
- Verificar autenticacao e propriedade de cada reserva consultada.
- Filtrar resultados por identidade e criterios; devolver estado e historico autorizado.
- Nao divulgar justificativas ou dados internos alem do necessario ao solicitante.

### Dados envolvidos
Identidade do solicitante; identificacao do recurso; periodo; estado atual e eventos anteriores; justificativas de decisao; filtros.

### Regras de negocio aplicaveis
Solicitante consulta somente registros proprios. Toda mudanca de estado deve aparecer como evento auditavel conforme permissao de leitura; reserva iniciada nao pode ser apagada.

### Fluxos alternativos
- Lista vazia ou filtro sem correspondencia: indicar sem resultados e permitir alterar filtros.
- Registro inexistente ou de outro usuario: negar detalhe sem confirmar dados privados de terceiro.
- Estado mudou desde a lista: atualizar detalhe e acoes permitidas, indicando dados atualizados.

### Tratamento de erros
Falha de recuperacao nao deve ser apresentada como lista vazia. Oferecer nova tentativa e manter filtros informados.

### Resultado esperado
Visao atualizada das reservas proprias, situacoes, historico e justificativas acessiveis.

### Condicoes de conclusao
Lista ou detalhe/historico e apresentado, ou a interface explica sem resultados, acesso negado ou falha.

### Cenarios verificaveis
- **Sucesso:** DADO que Marina possui solicitacoes em estados diferentes, QUANDO consultar sem filtro, ENTAO deve ver seus estados e periodos atuais.
- **Falha:** DADO um identificador de reserva de outro usuario, QUANDO Marina solicitar o detalhe, ENTAO o sistema deve negar acesso sem expor o registro.
- **Limite:** DADO um filtro sem correspondencia, QUANDO a busca terminar, ENTAO deve apresentar “sem resultados”, distinto de falha de recuperacao.

## J-SOL-006 — Solicitar alteracao de reserva

**ID da jornada:** J-SOL-006  
**Nome:** Alterar dados permitidos de reserva propria  
**Persona responsavel:** Solicitante (Marina Costa)  
**Objetivo da persona atendido:** Ajustar uma reserva quando permitido.  
**Necessidade ou dor relacionada:** Necessidade de adaptar atividade sem perder acompanhamento.  
**Prioridade inicial:** Importante — necessaria para mudancas operacionais, mas subordinada a regras de estado ainda pendentes.  
**Pre-condicoes:** Usuario autenticado; reserva propria existente; acao permitida no estado conforme regra a definir.  
**Gatilho:** Marina escolhe alterar uma reserva propria.

### Fluxo principal
1. A interface apresenta os dados atuais e os campos que podem ser alterados.
2. Marina informa novas informacoes permitidas, como periodo ou recurso, e revisa a alteracao.
3. O sistema verifica identidade, propriedade, estado atual e autorizacao para alteracao.
4. O sistema valida periodo, disponibilidade de recursos/professores, manutencao e restricoes, rechecando conflitos no momento de salvar.
5. Se permitido, aplica alteracao, preserva o registro anterior e gera evento auditavel com autoria, instante e situacao anterior/nova.
6. A interface informa sucesso e novo resumo, ou explica por que nao foi possivel alterar.

### Responsabilidades do Front-end
- Mostrar valores atuais, campos editaveis e resumo comparativo antes da confirmacao.
- Validar formato e orientar sem substituir validacao de negocio.
- Exibir conflito, acesso negado, estado nao elegivel, dados desatualizados e confirmacao.

### Responsabilidades do Back-end
- Verificar propriedade, permissao e estado mais recente.
- Validar novo periodo e todos os recursos/professores associados; impedir sobreposicao e manutencao.
- Evitar sobrescrever uma mudanca concorrente com dados obsoletos.
- Atualizar somente campos permitidos e registrar a mudanca sem apagar historico.

### Dados envolvidos
Reserva e estado; solicitante; valores anteriores e propostos; recurso(s), professor(es), periodo, restricoes, conflitos e autoria.

### Regras de negocio aplicaveis
So alterar registro proprio e estado elegivel; periodo valido; conflito, professor alocado e manutencao impedem alteracao incompatível. Reserva iniciada nao pode ser apagada. Estados permitidos para edicao nao foram especificados.

### Fluxos alternativos
- Reserva de outro usuario ou inexistente: negar sem alterar.
- Estado mudou desde a abertura: nao aplicar com base em dados antigos; atualizar e solicitar nova revisao.
- Novo periodo conflita: manter reserva como estava e informar indisponibilidade.
- Recurso restrito: se a alteracao exigir nova aprovacao ou voltar a `SOLICITADA`, comportamento permanece pendente de regra institucional.

### Tratamento de erros
Qualquer falha antes da confirmacao deve deixar a reserva inalterada. Falha no registro auditavel impede declarar a operacao concluida.

### Resultado esperado
Alteracao permitida aplicada e auditada, ou registro original preservado com motivo da recusa.

### Condicoes de conclusao
Usuario ve o novo estado/dados confirmados ou uma recusa explicita; nao ha alteracao parcial silenciosa.

### Cenarios verificaveis
- **Sucesso:** DADA uma reserva propria em estado elegivel e novo periodo valido sem conflito, QUANDO Marina confirmar a alteracao, ENTAO os dados devem ser atualizados e o evento auditavel preservado.
- **Falha:** DADA uma reserva de outro usuario, QUANDO Marina tentar altera-la, ENTAO a operacao deve ser negada e os dados mantidos.
- **Limite:** DADO que a reserva mudou apos a abertura do formulario, QUANDO Marina salvar valores baseados na versao antiga, ENTAO o sistema deve detectar dados desatualizados e nao sobrescrever a mudanca.
- **Pendente:** DADA uma reserva `EM_USO`, QUANDO Marina tentar altera-la, ENTAO o resultado deve seguir a regra de elegibilidade a definir; a aplicacao nao pode apagar a reserva.

## J-SOL-007 — Cancelar reserva propria

**ID da jornada:** J-SOL-007  
**Nome:** Solicitar cancelamento de reserva  
**Persona responsavel:** Solicitante (Marina Costa)  
**Objetivo da persona atendido:** Cancelar reserva propria quando permitido.  
**Necessidade ou dor relacionada:** Manter informacoes e disponibilidade coerentes quando planos mudam.  
**Prioridade inicial:** Importante — necessario para gerir mudancas, com limites por estado ainda a definir.  
**Pre-condicoes:** Usuario autenticado; reserva propria existente; estado elegivel conforme politica a definir.  
**Gatilho:** Marina seleciona cancelar em uma reserva propria.

### Fluxo principal
1. A interface apresenta resumo da reserva e explica que o cancelamento altera seu estado, sem apagar seu historico.
2. Marina solicita e confirma o cancelamento.
3. O sistema verifica propriedade, permissao, estado atual e se a operacao ainda e elegivel.
4. Se permitido, muda o estado para `CANCELADA`, preserva a reserva e registra quem, quando, qual reserva e a situacao anterior/nova.
5. O sistema atualiza a disponibilidade conforme a regra e informa o resultado ao solicitante e aos destinatarios pertinentes.

### Responsabilidades do Front-end
- Apresentar resumo, acao explicita e confirmacao final; nao remover item da historia.
- Exibir carregamento, cancelamento concluido, acesso negado, estado invalido e conflito de atualizacao.
- Atualizar a lista apenas apos resposta confirmada.

### Responsabilidades do Back-end
- Validar identidade, propriedade e transicao a partir do estado mais atual.
- Negar cancelamento inelegivel, inclusive sem apagar reserva iniciada.
- Alterar estado para `CANCELADA` somente quando autorizado; registrar auditoria completa.
- Atualizar disponibilidade e iniciar comunicacao de cancelamento sem ocultar falha de envio.

### Dados envolvidos
Identidade; reserva; estado anterior; solicitacao de cancelamento; instante e autor; estado posterior; periodo/recurso afetado.

### Regras de negocio aplicaveis
Cancelamento nao e exclusao. Reserva iniciada nao pode ser apagada. Estados elegiveis, prazo, necessidade de justificativa e regra para cancelamento de `SOLICITADA`/`APROVADA` precisam ser definidos.

### Fluxos alternativos
- Estado mudou antes da confirmacao: atualizar detalhe e avaliar novamente elegibilidade.
- Reserva inexistente/alheia: negar.
- Cancelamento nao permitido: preservar estado e informar motivo sem tentar contornar a regra.
- Falha de notificacao apos cancelamento: manter estado auditado e sinalizar pendencia de comunicacao conforme regra a definir.

### Tratamento de erros
Falha antes da mudanca mantem estado original. Falha de auditoria impede confirmar cancelamento. Falha de notificacao nao deve reverter silenciosamente estado ja alterado.

### Resultado esperado
Reserva elegivel transita a `CANCELADA` com auditoria; reserva inelegivel permanece intacta.

### Condicoes de conclusao
Estado e auditoria confirmados e resultado apresentado, ou cancelamento negado com motivo.

### Cenarios verificaveis
- **Sucesso:** DADA uma reserva propria elegivel, QUANDO Marina confirmar o cancelamento, ENTAO o estado deve passar a `CANCELADA` e registrar estado anterior, novo estado, autor e instante.
- **Falha:** DADA uma reserva de outro usuario, QUANDO Marina tentar cancelar, ENTAO o sistema deve negar sem alterar o registro.
- **Limite:** DADA uma reserva `EM_USO`, QUANDO Marina solicitar cancelamento, ENTAO o sistema nao deve apaga-la; a permissao para transicao deve seguir decisao pendente explicitada.
- **Duplicidade:** DADA uma reserva ja `CANCELADA`, QUANDO Marina repetir a acao, ENTAO o sistema deve informar estado atual e nao criar uma segunda transicao incoerente.

## J-SOL-008 — Receber notificacoes e acompanhar atualizacoes

**ID da jornada:** J-SOL-008  
**Nome:** Receber atualizacoes sobre solicitacoes e reservas  
**Persona responsavel:** Solicitante (Marina Costa)  
**Objetivo da persona atendido:** Saber quando uma solicitacao muda ou exige sua acao.  
**Necessidade ou dor relacionada:** Falta de confirmacao, desconhecimento de aprovacao/rejeicao e necessidade de explicacao.  
**Prioridade inicial:** Essencial — sem retorno, estados pendentes e decisoes ficam opacos.  
**Pre-condicoes:** Usuario autenticado ou meio de contato definido pela instituicao; evento relacionado a solicitacao propria.  
**Gatilho:** Solicitacao registrada, aprovada, rejeitada, cancelada, alterada ou exige acao.

### Fluxo principal
1. Uma operacao elegivel gera evento de comunicacao vinculado a reserva e destinatario autorizado.
2. O sistema prepara mensagem com estado, recurso, periodo, decisao/justificativa pertinente e acao requerida, se houver.
3. A interface apresenta a atualizacao ao solicitante; ele abre o detalhe para consultar contexto e historico.
4. Se houver pendencia, Marina segue a acao permitida e consulta o estado atualizado.

### Responsabilidades do Front-end
- Apresentar atualizacoes legiveis e associadas a reserva correta, sem expor dados de terceiros.
- Diferenciar nova informacao de estado atual e permitir abrir detalhe/historico.
- Informar quando uma comunicacao nao foi entregue, se esse estado estiver disponivel, e direcionar para consulta da reserva.

### Responsabilidades do Back-end
- Gerar comunicacao para os eventos definidos: registro, aprovacao, rejeicao, cancelamento, alteracao relevante e pendencia do solicitante.
- Validar destinatario e limitar conteudo ao necessario; preservar justificativa registrada quando permitida.
- Registrar status de processamento/entrega quando suportado; falha de envio nao pode desfazer mudanca de estado.
- Permitir que a reserva seja consultada como fonte atual mesmo se a comunicacao falhar.

### Dados envolvidos
Evento e estado anterior/novo; reserva; destinatario autorizado; recurso e periodo; justificativa; acao pendente; estado da comunicacao.

### Regras de negocio aplicaveis
Somente comunicar dados relacionados ao solicitante e suas reservas. Toda mudanca de estado e auditavel. Canal, prazo, retentativa e tratamento operacional de falha de entrega nao estao especificados.

### Fluxos alternativos
- Falha no envio: manter estado da reserva, registrar/indicar falha quando possivel e manter consulta manual disponivel.
- Evento duplicado: evitar apresentar transicao duplicada como nova decisao.
- Destinatario sem autorizacao: nao enviar dados da reserva e registrar a falha conforme politica.
- Comunicacao recebida apos mudanca subsequente: detalhe deve exibir estado atual e historico, nao apenas texto antigo.

### Tratamento de erros
Falha de recuperacao de atualizacoes deve ser distinguida de ausencia de notificacoes. A interface deve permitir verificar diretamente “minhas reservas”.

### Resultado esperado
Solicitante recebe ou consulta atualizacoes relevantes e identifica estado atual, decisao e eventual proxima acao.

### Condicoes de conclusao
Atualizacao foi apresentada ou falha de comunicacao foi tratada sem alterar indevidamente a reserva; usuario consegue consultar o estado atual.

### Cenarios verificaveis
- **Sucesso:** DADA uma solicitacao rejeitada com justificativa, QUANDO o evento for processado para Marina, ENTAO ela deve receber o estado e a justificativa autorizada vinculados a reserva.
- **Falha:** DADA uma falha de envio, QUANDO a reserva for aprovada, ENTAO o estado aprovado e a auditoria devem permanecer e a consulta da reserva deve refletir o estado atual.
- **Limite:** DADO um evento antigo seguido de nova transicao, QUANDO Marina abrir a comunicacao, ENTAO o detalhe deve distinguir evento historico do estado atual.

## Ambiguidades e decisoes pendentes — Solicitante

- Definir campos obrigatorios de uma solicitacao por tipo de recurso e quais informacoes justificam a aprovacao especial.
- Definir se recurso comum vai diretamente a `APROVADA` ou permanece `SOLICITADA`, e qual ator/sistema confirma essa transicao.
- Definir quais estados permitem alteracao, quais campos podem mudar e se uma alteracao de recurso restrito requer nova aprovacao.
- Definir estados elegiveis, prazo e necessidade de justificativa para cancelamento; a regra firme e nao apagar reserva iniciada.
- Definir como e quando o usuario e avisado de falha de notificacao, e politica de reenvio/canal.
- Definir como tratar solicitacao composta por varios recursos se somente parte deles estiver disponivel; nao ha regra de atomicidade documentada.

## Revisao cruzada

Os oito objetivos operacionais da persona estao cobertos por autenticacao, pesquisa, disponibilidade, criacao, consulta, alteracao, cancelamento e notificacoes. Cada jornada esta ligada a necessidade/dor documentada. Conflitos, concorrencia, recursos restritos, manutencao, privacidade, estados e auditoria foram contemplados. Nao se atribuiu ao solicitante aprovacao, gestao global ou acesso a reservas de terceiros. Permanecem as decisoes listadas acima; nenhuma foi resolvida por suposicao.
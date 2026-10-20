# Jornadas de utilizacao — Responsavel

Documento derivado de [RESPONSAVEL.md](../personas/RESPONSAVEL.md). O Responsavel atua apenas no escopo que lhe foi atribuido; as jornadas nao lhe concedem administracao global. Datas e horarios de movimentacao devem refletir o ocorrido, sem antecipar a transicao de estado que ainda nao foi formalmente atribuida.

## J-RES-001 — Autenticacao e acesso operacional

**ID da jornada:** J-RES-001  
**Nome:** Autenticar e abrir area do Responsavel  
**Persona responsavel:** Responsavel (Rafael Nunes)  
**Objetivo da persona atendido:** Acessar solicitacoes e recursos sob sua responsabilidade.  
**Necessidade ou dor relacionada:** Acesso organizado e limitado ao proprio escopo.  
**Prioridade inicial:** Essencial — acesso autenticado e autorizado e pre-condicao das operacoes.  
**Pre-condicoes:** Credenciais e atribuicoes de escopo cadastradas.  
**Gatilho:** Rafael acessa a aplicacao.

### Fluxo principal
1. Interface solicita credenciais; Rafael as informa.
2. Sistema valida identidade e situacao de acesso.
3. Sistema recupera perfil, permissoes e recursos/solicitacoes atribuidos a Rafael.
4. Interface apresenta painel operacional limitado a esse escopo.

### Responsabilidades do Front-end
- Apresentar entrada, carregamento, erros e painel autenticado.
- Identificar visualmente o escopo e nao mostrar operacoes administrativas globais como acoes do Responsavel.
- Negar navegacao visual a acoes nao pertinentes e atualizar a tela apos mudanca de permissao.

### Responsabilidades do Back-end
- Validar credenciais, perfil e permissoes vigentes.
- Determinar atribuicoes do usuario e verificar autorizacao em cada leitura/escrita, inclusive acesso direto.
- Limitar os resultados aos recursos, filas e historicos pertinentes.

### Dados envolvidos
Identidade, credenciais, situacao de acesso, perfil, permissoes, atribuicoes e recursos vinculados.

### Regras de negocio aplicaveis
Autenticacao e autorizacao sao distintas; estar autenticado nao concede acesso fora do escopo. Responsavel nao administra usuarios, permissoes globais ou auditoria.

### Fluxos alternativos
- Credencial invalida ou perfil sem permissao: negar acesso sem revelar dados desnecessarios.
- Atribuicao inexistente: mostrar painel sem itens e orientar que nao ha escopo operacional atribuido.
- Acesso direto a recurso de outro escopo: negar mesmo que o identificador seja conhecido.

### Tratamento de erros
Falha de validacao, acesso negado e falha interna devem ter mensagens distintas; erro de recuperacao nao pode ser apresentado como ausencia de itens.

### Resultado esperado
Painel acessivel somente com identidade e escopo confirmados.

### Condicoes de conclusao
Painel autorizado apresentado, ou acesso negado com motivo seguro.

### Cenarios verificaveis
- **Sucesso:** DADO Rafael autenticado e atribuido ao laboratorio A, QUANDO abrir a area operacional, ENTAO deve ver apenas recursos e solicitacoes do laboratorio A.
- **Falha:** DADO Rafael sem permissao para um recurso do laboratorio B, QUANDO acessar seu detalhe diretamente, ENTAO o sistema deve negar acesso.
- **Limite:** DADO Rafael autenticado sem atribuicoes vigentes, QUANDO abrir o painel, ENTAO deve ver estado vazio, nao dados globais.

## J-RES-002 — Consultar solicitacoes pendentes

**ID da jornada:** J-RES-002  
**Nome:** Localizar e analisar solicitacoes em seu escopo  
**Persona responsavel:** Responsavel (Rafael Nunes)  
**Objetivo da persona atendido:** Visualizar pedidos pendentes com dados suficientes para decidir.  
**Necessidade ou dor relacionada:** Solicitacoes incompletas, conflitos e falta de visibilidade da fila.  
**Prioridade inicial:** Essencial — base para aprovar ou rejeitar recursos restritos.  
**Pre-condicoes:** Rafael autenticado com escopo; podem existir solicitacoes pendentes.  
**Gatilho:** Rafael abre a fila de solicitacoes.

### Fluxo principal
1. Interface apresenta solicitacoes visiveis e filtros por estado, recurso e periodo.
2. Rafael aplica filtros e seleciona uma solicitacao.
3. Sistema verifica escopo e recupera solicitante, justificativa/finalidade, recurso, periodo, estado e restricoes autorizadas.
4. Sistema consulta disponibilidade e conflitos atuais, incluindo professores e manutencao pertinentes.
5. Interface apresenta detalhe, alertas e acoes que Rafael pode executar conforme autorizacao.

### Responsabilidades do Front-end
- Exibir fila filtravel, estado, periodo, recurso, solicitante e justificativa autorizada.
- Destacar pendencia, conflito, manutencao e informacao faltante sem antecipar decisao.
- Apresentar sem resultados, carregando, dados desatualizados e falha de consulta separadamente.

### Responsabilidades do Back-end
- Aplicar filtros e limitar registros ao escopo antes de retornar dados.
- Verificar autorizacao de leitura da solicitacao e minimizacao dos dados pessoais apresentados.
- Consultar o estado mais recente e disponibilidade relacionada; marcar consulta potencialmente desatualizada se houver mudanca concorrente.

### Dados envolvidos
Solicitacao, estado, recurso, periodo, finalidade/justificativa, solicitante, professor associado, manutencao, conflitos e atribuicao do Responsavel.

### Regras de negocio aplicaveis
Fila limitada por escopo. Somente estados pendentes admitem decisao e somente responsavel autorizado decide recurso restrito. Disponibilidade consultada nao fixa nem garante reserva.

### Fluxos alternativos
- Filtro sem resultado: mostrar fila vazia para os criterios.
- Solicitacao removida/nao encontrada ou escopo alterado: negar detalhe e atualizar lista.
- Dados insuficientes para avaliacao: indicar campos ausentes; decisao segue regra institucional, nao inferida aqui.

### Tratamento de erros
Falha na recuperacao nao equivale a fila vazia. Manter filtros e permitir nova tentativa; nao apresentar estado antigo como atual sem indicacao.

### Resultado esperado
Solicitacoes e detalhes autorizados com contexto suficiente e conflitos visiveis.

### Condicoes de conclusao
Fila/detalhe consultado ou motivo de ausencia, negacao ou falha informado.

### Cenarios verificaveis
- **Sucesso:** DADO que existe solicitacao pendente no escopo de Rafael, QUANDO filtrar por recurso e periodo, ENTAO a fila deve retornar o item com estado e dados necessarios.
- **Falha:** DADO solicitacao pertencente a outro escopo, QUANDO Rafael tentar consultar seu detalhe, ENTAO o sistema deve negar e nao expor dados.
- **Limite:** DADO que nenhum item corresponde aos filtros, QUANDO a consulta termina com sucesso, ENTAO a interface deve indicar sem resultados, nao falha.

## J-RES-003 — Aprovar solicitacao restrita

**ID da jornada:** J-RES-003  
**Nome:** Aprovar solicitacao quando autorizado e ainda elegivel  
**Persona responsavel:** Responsavel (Rafael Nunes)  
**Objetivo da persona atendido:** Autorizar recurso restrito dentro de sua responsabilidade.  
**Necessidade ou dor relacionada:** Evitar aprovacao sem contexto, conflito ou rastreabilidade.  
**Prioridade inicial:** Essencial — concretiza o fluxo de aprovacao previsto para recursos restritos.  
**Pre-condicoes:** Solicitacao `SOLICITADA`; recurso restrito no escopo; Rafael autorizado para decidir.  
**Gatilho:** Rafael seleciona aprovar apos analisar os detalhes.

### Fluxo principal
1. Interface mostra solicitacao, periodo, recurso, justificativa, conflitos e manutencao consultados.
2. Rafael solicita aprovacao e confirma a decisao.
3. Sistema revalida identidade, autorizacao, estado atual `SOLICITADA` e escopo.
4. Sistema reconsulta disponibilidade, professor(es), manutencao, estado do recurso e reservas concorrentes.
5. Se ainda elegivel, sistema registra a aprovacao e transicao `SOLICITADA` -> `APROVADA`, atribuindo autor/instante e dados de auditoria.
6. Sistema retorna resultado e aciona comunicacao ao solicitante; interface mostra aprovacao ou motivo para nao aprovar.

### Responsabilidades do Front-end
- Apresentar informacoes para decisao, alerta de atualizacao e confirmacao.
- Desabilitar acao enquanto processa; nao apresentar sucesso antes de resposta confirmada.
- Mostrar conflito, autorizacao negada, estado alterado por outro ator e falha de comunicacao.

### Responsabilidades do Back-end
- Validar atribuicao/autorizacao de aprovacao para aquele recurso e aquela solicitacao.
- Revalidar estado e regras no momento da aprovacao; impedir dupla aceitacao e aprovacao duplicada.
- Permitir somente transicao valida; registrar ator, instante, reserva/recurso, operacao, estado anterior e novo.
- Comunicar o resultado ao solicitante; falha de notificacao nao desfaz aprovacao auditada.

### Dados envolvidos
Identidade e escopo do Responsavel; solicitacao; recurso(s), professores e periodo; estado atual; conflitos e manutencao; decisao; autor, instante e evento auditavel.

### Regras de negocio aplicaveis
 Somente responsavel autorizado aprova solicitacao restrita. A solicitacao deve permanecer `SOLICITADA`, recurso nao pode estar em manutencao e nao pode haver conflito vigente. A transicao aprovada e auditada. Os estados reconhecidos no ciclo sao `SOLICITADA`, `APROVADA`, `EM_USO`, `CONCLUIDA`, `REJEITADA` e `CANCELADA`; este perfil nao recebe permissao implicita para cancelar.

### Fluxos alternativos
- Recurso tornou-se indisponivel enquanto aguardava: nao aprovar; manter/encaminhar estado conforme regra pendente e informar conflito.
- Estado ja mudou ou solicitacao foi decidida: nao repetir transicao; atualizar detalhe.
- Rafael perdeu permissao/escopo: negar decisao.
- Dois responsaveis tentam aprovar: apenas a primeira decisao valida prevalece; a segunda recebe estado atualizado.

### Tratamento de erros
Falha de auditoria impede confirmar a transicao. Falha de notificacao preserva a decisao e deve ser identificada separadamente.

### Resultado esperado
Solicitacao elegivel passa uma unica vez a `APROVADA` e fica auditada; caso contrario, permanece sem aprovacao e recebe retorno explicito.

### Condicoes de conclusao
Decisao e auditoria confirmadas e interface atualizada, ou aprovacao negada com causa.

### Cenarios verificaveis
- **Sucesso:** DADA solicitacao `SOLICITADA` e disponivel no escopo de Rafael, QUANDO responsavel autorizado aprovar, ENTAO deve ir a `APROVADA` com auditoria completa e solicitante informado.
- **Falha:** DADA uma reserva conflitante surgida durante a espera, QUANDO Rafael tentar aprovar, ENTAO a operacao deve ser impedida e indicar indisponibilidade.
- **Limite:** DADA uma solicitacao ja `APROVADA`, QUANDO Rafael repetir a aprovacao, ENTAO nao deve haver segunda transicao nem novo efeito duplicado.
- **Concorrencia:** DADA solicitacao cuja disponibilidade e concorrida, QUANDO uma aprovacao concorrente ja tiver reservado o mesmo recurso/periodo, ENTAO esta aprovacao deve falhar com estado atual e conflito.

## J-RES-004 — Rejeitar solicitacao com justificativa

**ID da jornada:** J-RES-004  
**Nome:** Rejeitar pedido pendente e informar motivo  
**Persona responsavel:** Responsavel (Rafael Nunes)  
**Objetivo da persona atendido:** Recusar solicitacao inadequada de forma explicavel.  
**Necessidade ou dor relacionada:** Ausencia de justificativa e historico das decisoes.  
**Prioridade inicial:** Essencial — rejeicao e estado alternativo previsto e deve ser rastreavel.  
**Pre-condicoes:** Solicitacao `SOLICITADA` no escopo e Rafael autorizado.  
**Gatilho:** Rafael escolhe rejeitar apos revisar a solicitacao.

### Fluxo principal
1. Interface solicita justificativa e apresenta resumo da solicitacao.
2. Rafael informa e confirma o motivo.
3. Sistema verifica permissao, escopo, estado mais recente e validade do motivo segundo criterios definidos.
4. Sistema muda estado para `REJEITADA`, registra justificativa e auditoria com autor/instante/estado anterior e novo.
5. Sistema retorna confirmacao e comunica o solicitante.

### Responsabilidades do Front-end
- Solicitar justificativa, apresentar limites/campos quando definidos e confirmar antes de enviar.
- Exibir validacao, rejeicao concluida, estado ja alterado e falha de comunicacao.
- Evitar mostrar informacoes de outros usuarios na justificativa.

### Responsabilidades do Back-end
- Verificar autorizacao e estado `SOLICITADA`.
- Aplicar requisitos de justificativa quando forem definidos; nao aceitar decisao fora do escopo.
- Persistir decisao e evento auditavel antes de declarar sucesso.
- Acionar notificacao; preservar decisao se o envio falhar.

### Dados envolvidos
Identidade/escopo; solicitacao; estado anterior; justificativa; estado `REJEITADA`; autor e instante; destinatario e estado da comunicacao.

### Regras de negocio aplicaveis
Responsavel autorizado decide solicitacao pendente. Rejeicao recebe justificativa; toda mudanca de estado e auditada; decisao terminal nao deve ser duplicada.

### Fluxos alternativos
- Justificativa vazia/invalida: solicitar complemento conforme regra a definir, sem alterar estado.
- Estado alterado em paralelo: bloquear rejeicao e atualizar detalhe.
- Sem autorizacao: negar e registrar evento de seguranca conforme politica definida, sem editar reserva.

### Tratamento de erros
Falha ao gravar decisao/auditoria deixa a solicitacao sem rejeicao confirmada. Falha de notificacao e informada separadamente.

### Resultado esperado
Solicitacao muda para `REJEITADA` com motivo auditavel e solicitante informado, ou nao muda e recebe justificativa do impedimento.

### Condicoes de conclusao
Estado e motivo confirmados, ou usuario sabe que a operacao nao ocorreu.

### Cenarios verificaveis
- **Sucesso:** DADA uma solicitacao pendente e justificativa valida, QUANDO Rafael autorizado rejeitar, ENTAO estado `REJEITADA`, motivo e auditoria devem ser registrados.
- **Falha:** DADO Rafael sem escopo para a solicitacao, QUANDO tentar rejeitar, ENTAO a acao deve ser negada e o estado mantido.
- **Limite:** DADA solicitacao ja aprovada, QUANDO Rafael tentar rejeita-la, ENTAO o sistema deve impedir transicao retroativa nao definida e exibir estado atual.
- **Notificacao:** DADA rejeicao gravada e falha de envio, QUANDO a comunicacao falhar, ENTAO a rejeicao auditada deve permanecer e a falha ser tratada separadamente.

## J-RES-005 — Validar alocacao de professores

**ID da jornada:** J-RES-005  
**Nome:** Consultar agenda e identificar conflito de professor  
**Persona responsavel:** Responsavel (Rafael Nunes)  
**Objetivo da persona atendido:** Validar alocacoes de professores e evitar agenda sobreposta.  
**Necessidade ou dor relacionada:** Conflitos de disponibilidade e informacao insuficiente antes da decisao.  
**Prioridade inicial:** Essencial — disponibilidade de professores compoe a regra de reserva.  
**Pre-condicoes:** Rafael autorizado a consultar a agenda pertinente; professor e periodo identificados.  
**Gatilho:** Rafael abre solicitacao que envolve professor ou inicia consulta de agenda.

### Fluxo principal
1. Interface apresenta professor, periodo e alocacoes consultaveis no escopo.
2. Rafael consulta agenda para o intervalo desejado.
3. Sistema valida o periodo e recupera alocacoes existentes e solicitacoes pertinentes, respeitando privacidade.
4. Sistema compara intervalos, identifica sobreposicoes e informa disponibilidade ou conflito.
5. Rafael usa o resultado para validar ou encaminhar a solicitacao conforme suas permissoes; a consulta sozinha nao aprova reserva.

### Responsabilidades do Front-end
- Mostrar periodo, agenda e conflitos com detalhes necessarios, sem dados pessoais excessivos.
- Diferenciar ocupado, disponivel, sem dados e falha de consulta.
- Apresentar acoes de decisao apenas se houver autorizacao e estado elegivel.

### Responsabilidades do Back-end
- Autorizar a consulta e limitar informacao aos dados necessarios ao responsavel.
- Validar intervalo semiaberto `[inicio, termino)` e detectar alocacao sobreposta.
- Considerar a agenda novamente na aprovacao final; resultado de consulta nao cria bloqueio.

### Dados envolvidos
Professor, periodo solicitado, alocacoes existentes, estado de reservas, escopo de Rafael e conflitos encontrados.

### Regras de negocio aplicaveis
Horario de termino deve ser posterior ao inicio; professor nao pode ser considerado disponivel em alocacao sobreposta. Limites contiguos nao se sobrepoem sob a convencao definida.

### Fluxos alternativos
- Professor sem agenda cadastrada: indicar indisponibilidade de informacao, nao disponibilidade garantida.
- Professor ja alocado: destacar conflito e impedir validacao como livre.
- Dados mudaram antes da aprovacao: revalidar na jornada de decisao.
- Rafael sem permissao para aquela agenda: negar consulta.

### Tratamento de erros
Falha ao recuperar agenda nao pode ser interpretada como horario livre; manter solicitacao sem decisao automatica.

### Resultado esperado
Disponibilidade ou conflito do professor claramente identificado para apoiar avaliacao.

### Condicoes de conclusao
Agenda foi consultada com resultado confiavel ou indisponibilidade de dados/escopo foi informada.

### Cenarios verificaveis
- **Sucesso:** DADO professor sem alocacao no periodo valido, QUANDO Rafael autorizado consultar, ENTAO o sistema deve indicar ausencia de conflito, sem aprovar reserva.
- **Falha:** DADO falha na recuperacao da agenda, QUANDO consultar disponibilidade, ENTAO o sistema nao deve indicar professor como livre.
- **Limite:** DADA uma alocacao que termina as 12:00 e nova atividade que inicia as 12:00, QUANDO comparar periodos, ENTAO nao ha sobreposicao pela convencao `[inicio, termino)`.
- **Conflito:** DADA alocacao das 10:00 as 12:00, QUANDO avaliar atividade das 11:00 as 13:00, ENTAO o conflito deve ser apresentado.

## J-RES-006 — Registrar retirada de material/equipamento

**ID da jornada:** J-RES-006  
**Nome:** Registrar retirada vinculada a reserva autorizada  
**Persona responsavel:** Responsavel (Rafael Nunes)  
**Objetivo da persona atendido:** Rastrear quem recebeu um recurso e quando.  
**Necessidade ou dor relacionada:** Falta de controle, itens sem devolucao e dificuldade de identificar usuario.  
**Prioridade inicial:** Essencial — movimento fisico de recursos exige rastreabilidade operacional.  
**Pre-condicoes:** Rafael autorizado para o recurso; reserva existente e elegivel; retirada ainda nao registrada.  
**Gatilho:** Recurso e apresentado para retirada.

### Fluxo principal
1. Interface solicita identificacao da reserva e do recurso.
2. Rafael confere reserva, recurso e pessoa autorizada a retirar; informa ou confirma data/horario real da entrega.
3. Sistema valida escopo, reserva, recurso, estado e inexistencia de retirada duplicada.
4. Sistema registra retirada, responsavel pela operacao, pessoa que recebeu, recurso e instante.
5. Sistema apresenta confirmacao e atualiza a pendencia/historico. Eventual transicao da reserva para `EM_USO` depende da regra de transicao pendente.

### Responsabilidades do Front-end
- Apresentar busca/identificacao de reserva, recurso e pessoa; confirmar dados antes do registro.
- Solicitar data/horario real ou usar o instante confirmado pelo processo, sem inventar valores.
- Exibir retirada concluida, nao autorizada, registro existente e falha.

### Responsabilidades do Back-end
- Validar identidade e escopo de Rafael; confirmar reserva e correspondencia do recurso.
- Verificar estado elegivel, pessoa autorizada e duplicidade.
- Registrar retirada de forma auditavel, distinguindo ator que registrou de pessoa que recebeu.
- Atualizar estado da reserva somente conforme transicao formalmente definida; nao apagar reserva.

### Dados envolvidos
Reserva, estado, recurso, pessoa solicitante/retirante, operador Rafael, instante real, registro de retirada e estado anterior/novo se houver transicao.

### Regras de negocio aplicaveis
Somente Responsavel autorizado no escopo registra movimento. Reserva e recurso devem corresponder; retirada duplicada ou sem autorizacao e recusada. Auditoria inclui ator, instante, alvo, operacao e estado anterior/novo quando aplicavel.

### Fluxos alternativos
- Reserva nao aprovada ou nao encontrada: impedir retirada e explicar que nao ha reserva elegivel.
- Rafael sem escopo ou pessoa diferente da autorizada: negar.
- Retirada ja registrada: exibir movimento existente e nao criar duplicata.
- Recurso em manutencao ou cadastro desativado: nao liberar retirada; escalar conforme processo institucional.

### Tratamento de erros
Falha de registro/auditoria nao deve produzir confirmacao. Falha de consulta impede entrega registrada; usuario pode tentar novamente sem duplicar evento.

### Resultado esperado
Retirada autorizada associada a reserva e registrada com identidade e horario, ou recusada sem alterar dados.

### Condicoes de conclusao
Registro de movimento confirmado e consultavel no historico, ou impedimento informado.

### Cenarios verificaveis
- **Sucesso:** DADA reserva aprovada e Rafael autorizado, QUANDO registrar retirada com recurso e pessoa correspondentes, ENTAO deve haver movimento com operador, retirante e instante.
- **Falha:** DADA reserva pertencente a outro escopo, QUANDO Rafael tentar registrar retirada, ENTAO o sistema deve negar e nao criar movimento.
- **Limite:** DADO movimento de retirada ja registrado, QUANDO a mesma operacao for reenviada, ENTAO nao deve ser criado movimento duplicado.
- **Estado:** DADA reserva cuja transicao a `EM_USO` ainda nao esta definida, QUANDO retirar equipamento, ENTAO o movimento pode ser registrado apenas se autorizado, sem inferir transicao de reserva.

## J-RES-007 — Registrar devolucao

**ID da jornada:** J-RES-007  
**Nome:** Registrar retorno de recurso emprestado  
**Persona responsavel:** Responsavel (Rafael Nunes)  
**Objetivo da persona atendido:** Rastrear retorno e encerrar pendencia de movimentacao.  
**Necessidade ou dor relacionada:** Equipamento nao devolvido e ausencia de historico confiavel.  
**Prioridade inicial:** Essencial — fecha o ciclo de movimentacao do recurso.  
**Pre-condicoes:** Rafael autorizado; retirada aberta correspondente existe; devolucao ainda nao foi registrada.  
**Gatilho:** Recurso e devolvido.

### Fluxo principal
1. Interface solicita identificacao do recurso ou movimento de retirada.
2. Rafael confere item e movimentacao, informa/confirmar data e horario reais de retorno.
3. Sistema valida escopo, retirada pendente e ausencia de devolucao previa.
4. Sistema registra devolucao, operador, instante e referencia a retirada.
5. Sistema atualiza pendencia e historico; eventual transicao `EM_USO` -> `CONCLUIDA` depende da regra de estados a confirmar.
6. Interface confirma devolucao ou informa divergencia.

### Responsabilidades do Front-end
- Apresentar movimento original, recurso e pessoa vinculada antes de confirmar.
- Solicitar ou confirmar data/horario real e permitir apontar divergencia sem inventar classificacao.
- Exibir devolucao registrada, duplicidade, movimento inexistente e falha.

### Responsabilidades do Back-end
- Validar autorizacao, recurso, movimento aberto e correspondencia com retirada.
- Impedir devolucao duplicada; manter trilha de retirada e devolucao.
- Atualizar estado operacional e da reserva apenas segundo regras confirmadas; emitir auditoria completa.

### Dados envolvidos
Recurso, reserva, movimento de retirada, retirante, operador, instante da devolucao, estado da movimentacao e da reserva.

### Regras de negocio aplicaveis
Somente Responsavel autorizado registra retorno no escopo. Devolucao exige retirada correspondente e so pode ser registrada uma vez. Movimento e auditavel; transicao final da reserva ainda precisa de decisao explicita.

### Fluxos alternativos
- Retirada nao encontrada ou de outro escopo: negar.
- Devolucao ja registrada: exibir registro existente e nao duplicar.
- Item devolvido com divergencia/avaria: registrar apenas informacao prevista pela politica; processo de avaria e pendente, sem criar fluxo novo.
- Falha de consulta: nao concluir que nao existe retirada.

### Tratamento de erros
Falha no registro ou auditoria deixa pendencia aberta e nao confirma retorno. Falha de atualizacao de estado deve ser separada do registro fisico e requer regra de consistencia a definir.

### Resultado esperado
Devolucao vinculada a retirada fica registrada e pendencia e atualizada, ou a operacao e recusada com justificativa.

### Condicoes de conclusao
Movimento de devolucao e auditoria confirmados, ou Rafael sabe por que o registro nao ocorreu.

### Cenarios verificaveis
- **Sucesso:** DADA retirada aberta e recurso correto, QUANDO Rafael autorizado registrar o retorno, ENTAO a devolucao deve ficar vinculada ao movimento e conter instante/autor.
- **Falha:** DADA retirada inexistente, QUANDO Rafael tentar registrar devolucao, ENTAO a operacao deve ser negada sem criar movimento.
- **Limite:** DADA devolucao ja registrada, QUANDO Rafael repetir a acao, ENTAO o sistema deve preservar um unico registro.
- **Auditoria:** DADO retorno registrado, QUANDO consultar o historico, ENTAO devem constar a operacao, ator, instante, recurso e referencia correspondente.

## J-RES-008 — Acompanhar pendencias operacionais

**ID da jornada:** J-RES-008  
**Nome:** Consultar aprovacoes, devolucoes e atrasos pendentes  
**Persona responsavel:** Responsavel (Rafael Nunes)  
**Objetivo da persona atendido:** Identificar itens que exigem decisao ou intervencao.  
**Necessidade ou dor relacionada:** Devolucoes atrasadas, filas pouco visiveis e historico dificil de consultar.  
**Prioridade inicial:** Importante — apoia continuidade operacional e reduz pendencias esquecidas.  
**Pre-condicoes:** Rafael autenticado e com escopo ativo.  
**Gatilho:** Rafael abre a visao de pendencias.

### Fluxo principal
1. Interface apresenta categorias de solicitacoes pendentes e movimentos sem devolucao.
2. Rafael filtra por recurso, estado e periodo.
3. Sistema consulta itens autorizados, identifica pendencias e calcula atraso apenas se houver prazo institucional definido.
4. Interface apresenta item, responsavel, idade/periodo da pendencia, estado e historico acessivel.
5. Rafael abre o item e executa acao permitida ou encaminha situacao fora de sua alçada.

### Responsabilidades do Front-end
- Separar aprovacoes pendentes de retiradas sem devolucao; mostrar filtros e indicadores compreensiveis.
- Apresentar “atraso” apenas com prazo definido; senao mostrar pendente sem classificar atraso.
- Oferecer detalhe/historico e acoes dentro do escopo.

### Responsabilidades do Back-end
- Recuperar pendencias por escopo e estado atual.
- Correlacionar reserva, solicitacao, movimentacoes e eventos auditaveis.
- Aplicar criterio de atraso somente conforme prazo aprovado e limitar dados retornados.

### Dados envolvidos
Pendencia, reserva, recurso, estado, solicitante/retirante, data de solicitacao/retirada, prazo (se definido), devolucao e historico.

### Regras de negocio aplicaveis
Consulta limitada a escopo e permissao. Estado deve ser atual; pendencia nao autoriza alteracao de dados ou auditoria. Definicao de atraso depende de prazo institucional.

### Fluxos alternativos
- Sem pendencias: apresentar estado vazio.
- Movimento removido/estado mudou: atualizar a lista e informar que nao esta mais pendente.
- Prazo nao definido: nao classificar como atrasado.
- Falha de recuperacao: informar falha, nao lista vazia.

### Tratamento de erros
Nao apresentar dados antigos como atuais sem aviso; oferecer nova tentativa e preservar filtros.

### Resultado esperado
Pendencias atuais e autorizadas ficam visiveis, com contexto e proxima acao aplicavel.

### Condicoes de conclusao
Fila consultada, vazia ou indisponivel com estado correto e feedback.

### Cenarios verificaveis
- **Sucesso:** DADO equipamento retirado e sem devolucao no escopo de Rafael, QUANDO consultar pendencias, ENTAO deve aparecer como devolucao pendente.
- **Falha:** DADA falha ao recuperar movimentos, QUANDO abrir o painel, ENTAO a interface deve informar falha, nao ausencia de pendencias.
- **Limite:** DADO que nao ha prazo de devolucao definido, QUANDO listar retirada ainda aberta, ENTAO nao deve rotula-la como atrasada.
- **Escopo:** DADA pendencia de outro responsavel, QUANDO Rafael consultar a lista, ENTAO o item nao deve ser exposto sem autorizacao.

## Ambiguidades e decisoes pendentes — Responsavel

- Definir atribuicao de responsavel por recurso, substituicao/ausencia e escopo de acesso a agenda de professores.
- Definir quais dados sao obrigatorios para aprovar/rejeitar e criterios de justificativa.
- Definir resultado e estado de uma solicitacao se o recurso ficar indisponivel durante a espera por aprovacao.
- Definir quem pode disparar cada transicao, em especial `APROVADA` -> `EM_USO` e `EM_USO` -> `CONCLUIDA`, e a relacao exata dessas transicoes com retirada/devolucao.
- Definir prazo de devolucao e criterio de atraso, alem do fluxo para recurso devolvido com avaria ou divergencia.
- Definir como registrar falha de notificacao sem perder o resultado da decisao.

## Revisao cruzada

As oito jornadas cobrem autenticacao, fila, aprovacao, rejeicao, validacao de professores, retirada, devolucao e acompanhamento, vinculadas as responsabilidades/dificuldades da persona. Acoes de decisao e movimentacao validam escopo e estado; alteracoes criticas registram auditoria e nao modificam o proprio registro de auditoria. Conflito temporal, manutencao, duplicidade e falhas foram incluidos. Permanecem sem definicao os prazos, os atores de certas transicoes e os criterios de dados obrigatorios; estes estao explicitados acima.
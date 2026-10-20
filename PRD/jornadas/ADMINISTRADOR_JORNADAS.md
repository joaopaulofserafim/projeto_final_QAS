# Jornadas de utilizacao — Administrador

Documento derivado de [ADMINISTRADOR.md](../personas/ADMINISTRADOR.md). O perfil administra cadastros, acessos e operacao, mas nao pode ignorar regras, apagar reservas iniciadas ou editar registros de auditoria. Onde a regra institucional nao esta definida, a jornada registra a pendencia sem presumir resultado.

## J-ADM-001 — Autenticacao administrativa

**ID da jornada:** J-ADM-001  
**Nome:** Autenticar e acessar operacoes administrativas  
**Persona responsavel:** Administrador (Luciana Prado)  
**Objetivo da persona atendido:** Administrar a plataforma com acesso privilegiado protegido.  
**Necessidade ou dor relacionada:** Evitar usuarios acessando funcionalidades indevidas.  
**Prioridade inicial:** Essencial — protege todas as operacoes administrativas.  
**Pre-condicoes:** Conta administrativa e credenciais validas.  
**Gatilho:** Luciana abre a aplicacao e tenta acessar o painel.

### Fluxo principal
1. Interface solicita credenciais.
2. Luciana informa credenciais e confirma.
3. Sistema valida identidade, estado da conta, perfil e permissoes administrativas.
4. Interface abre painel autorizado, mostra identidade/perfil e disponibiliza apenas operacoes permitidas.
5. Ao encerrar sessao, sistema revoga o contexto autenticado e interface remove dados privados.

### Responsabilidades do Front-end
- Apresentar campos, carregamento, falha de credencial, acesso negado e acao de encerrar.
- Direcionar para painel administrativo apenas apos confirmacao de autorizacao.
- Negar navegacao visual a operacoes nao permitidas, sem substituir validacao do sistema.

### Responsabilidades do Back-end
- Autenticar, autorizar perfil e verificar permissoes atuais em cada operacao privilegiada.
- Negar acesso de usuario autenticado sem permissao administrativa.
- Encerrar sessao/contexto e nao aceitar operacoes posteriores sem autenticacao valida.
- Minimizar informacao revelada em falhas de credencial.

### Dados envolvidos
Identidade, credenciais, estado de conta, perfil, permissoes e contexto de sessao.

### Regras de negocio aplicaveis
Autenticacao nao equivale a autorizacao. Perfil privilegiado ainda esta sujeito a regras e auditoria; nenhuma permissao permite editar registros de auditoria.

### Fluxos alternativos
- Credencial invalida, conta sem acesso ou perfil incorreto: negar entrada.
- Sessao expirada durante acao: exigir autenticacao e nao confirmar operacao nao concluida.
- Acesso por URL direta sem permissao: negar no processamento.

### Tratamento de erros
Distinguir credencial invalida, acesso negado e indisponibilidade interna sem expor detalhes sensiveis. Nao exibir painel com dados antigos se a autorizacao nao puder ser confirmada.

### Resultado esperado
Luciana acessa somente funcoes administrativas autorizadas ou recebe negacao segura.

### Condicoes de conclusao
Painel administrativo autorizado aberto ou tentativa bloqueada; sessao encerrada ao sair.

### Cenarios verificaveis
- **Sucesso:** DADA conta administrativa ativa e credencial valida, QUANDO Luciana autenticar, ENTAO acessa as funcoes administrativas autorizadas.
- **Falha:** DADO usuario solicitante autenticado, QUANDO abrir operacao administrativa, ENTAO o sistema deve negar acesso.
- **Limite:** DADA sessao expirada, QUANDO confirmar alteracao administrativa, ENTAO a acao nao deve ser declarada concluida sem nova autorizacao.

## J-ADM-002 — Consultar dashboard administrativo

**ID da jornada:** J-ADM-002  
**Nome:** Acompanhar indicadores e situacoes operacionais  
**Persona responsavel:** Administrador (Luciana Prado)  
**Objetivo da persona atendido:** Obter visibilidade sobre recursos, reservas e situacoes que exigem atencao.  
**Necessidade ou dor relacionada:** Baixa visibilidade de utilizacao, manutencao, conflitos e inconsistencias.  
**Prioridade inicial:** Importante — consolida consulta operacional, subordinada a definicao dos indicadores.  
**Pre-condicoes:** Luciana autenticada e autorizada; dados operacionais consultaveis.  
**Gatilho:** Luciana abre o dashboard administrativo.

### Fluxo principal
1. Interface solicita ou aplica um periodo de consulta conforme criterio institucional definido.
2. Sistema recupera totais de usuarios, recursos cadastrados/disponiveis/reservados/em manutencao, solicitacoes pendentes, utilizacao e situacoes de atencao.
3. Sistema calcula agregados a partir de estados e periodos vigentes, sem duplicar recurso ou reserva.
4. Interface apresenta indicadores, periodo considerado, momento da atualizacao e itens que requerem atencao.
5. Luciana abre uma categoria para consultar registros permitidos e tratar a situacao na jornada correspondente.

### Responsabilidades do Front-end
- Apresentar categorias solicitadas, periodo/filtros se definidos, data de atualizacao e estados de carregamento/sem dados/erro.
- Distinguir zero resultados de falha; indicar quando os dados podem estar desatualizados.
- Encaminhar para detalhe autorizado, sem transformar dashboard em acao administrativa automatica.

### Responsabilidades do Back-end
- Verificar permissao e consultar dados operacionais coerentes para o periodo.
- Calcular totais/indicadores sem alterar reservas ou cadastros.
- Devolver definicoes/unidades dos indicadores; nao classificar situacao sem criterio institucional.

### Dados envolvidos
Usuarios e estados cadastrais; inventario por tipo/estado; reservas por estado e periodo; manutencoes; utilizacao; situacoes de atencao; instante de atualizacao.

### Regras de negocio aplicaveis
 Indicadores devem refletir estados reais e nao apresentar recurso em manutencao como disponivel. Reservas sao acompanhadas nos estados `SOLICITADA`, `APROVADA`, `EM_USO`, `CONCLUIDA`, `REJEITADA` e `CANCELADA`; consulta administrativa nao concede ao perfil transicao implicita. Definicoes de “disponivel”, “reservado”, periodo e criterios de atencao precisam ser uniformes.

### Fluxos alternativos
- Sem dados: apresentar indicadores sem registros ou zero conforme definicao aprovada.
- Consulta parcial/falha: identificar indicadores indisponiveis, sem substitui-los por zero.
- Dados potencialmente desatualizados: mostrar momento da atualizacao e permitir atualizar consulta.

### Tratamento de erros
Falha de recuperacao nao deve parecer operacao normal com contagens zeradas. Apresentar erro de forma contextual e permitir nova tentativa.

### Resultado esperado
Visao agregada e atualizada da operacao, com links para detalhamento autorizado.

### Condicoes de conclusao
Indicadores apresentados com periodo/atualizacao ou erro/ausencia de dados explicitados.

### Cenarios verificaveis
- **Sucesso:** DADOS registros operacionais consistentes, QUANDO Luciana abrir o painel, ENTAO os totais devem corresponder ao periodo e definicoes apresentados.
- **Falha:** DADA falha na recuperacao de reservas, QUANDO carregar o painel, ENTAO o indicador nao pode ser exibido como zero sem identificacao de erro.
- **Limite:** DADO um recurso em manutencao, QUANDO calcular recursos disponiveis, ENTAO ele nao deve contar como disponivel no periodo bloqueado.

## J-ADM-003 — Gerenciar usuarios e permissoes

**ID da jornada:** J-ADM-003  
**Nome:** Cadastrar, consultar, atualizar, bloquear e ajustar acesso de usuarios  
**Persona responsavel:** Administrador (Luciana Prado)  
**Objetivo da persona atendido:** Manter usuarios e permissoes adequados a funcao institucional.  
**Necessidade ou dor relacionada:** Acessos indevidos ou insuficientes e dificuldade para localizar alteracoes.  
**Prioridade inicial:** Essencial — controle de acesso sustenta segregacao de perfis.  
**Pre-condicoes:** Luciana autorizada para administracao de usuarios; dados institucionais necessarios disponiveis.  
**Gatilho:** Luciana escolhe criar, consultar, pesquisar, editar, bloquear, desbloquear ou alterar permissao.

### Fluxo principal
1. Interface apresenta busca/lista e operacoes autorizadas; Luciana seleciona usuario ou cadastro.
2. Para cadastro, informa dados exigidos; para alteracao, revisa valores atuais e propostos; para bloqueio/permissao, confirma impacto.
3. Sistema valida campos, identidade do alvo, duplicidade conforme regra, permissao de Luciana e consistencia de perfil.
4. Sistema aplica operacao permitida, preserva historico de antes/depois e registra autoria/instante.
5. Interface confirma resultado e apresenta estado/permissoes atualizados.

### Responsabilidades do Front-end
- Oferecer cadastro, pesquisa, detalhe e acoes de editar/bloquear/desbloquear/permissao conforme autorizacao.
- Solicitar confirmacao para alteracoes de acesso e mostrar impactos conhecidos.
- Apresentar validacao, resultado, dados desatualizados, usuario inexistente e acesso negado.

### Responsabilidades do Back-end
- Verificar autorizacao administrativa em cada acao, validar dados e impedir operacoes inconsistentes.
- Aplicar perfil/permissoes solicitados somente dentro das regras institucionais.
- Propagar bloqueio/alteracao de permissao para operacoes seguintes e auditar alteracao com ator, instante, alvo, operacao e valores anterior/novo quando permitido.
- Evitar expor credenciais ou informacao sensivel na consulta.

### Dados envolvidos
Identidade e dados cadastrais necessarios; perfil e permissoes; estado ativo/bloqueado; autor, instante e historico de alteracao.

### Regras de negocio aplicaveis
Cada perfil tem permissoes especificas; administrador nao ignora validacoes. Acoes administrativas sao rastreaveis. Politica de campos, unicidade, bloqueio e matriz fina de permissoes depende de definicao institucional.

### Fluxos alternativos
- Usuario inexistente ou duplicado segundo criterio futuro: impedir operacao e informar.
- Alteracao concorrente: sinalizar dados desatualizados e exigir revisao, sem sobrescrever silenciosamente.
- Bloqueio de usuario com solicitacoes/reservas ativas: apresentar impacto; tratamento dessas reservas e pendente, sem cancelamento automatico.
- Falha de auditoria: nao confirmar mudanca de acesso.

### Tratamento de erros
Erros de campo, autorizacao, conflito de atualizacao e falha interna devem ser diferenciados. Nao mostrar detalhes sensiveis de usuarios a perfis sem permissao.

### Resultado esperado
Usuario consultado ou operacao administrativa aplicada com historico; ou alteracao recusada sem estado parcial silencioso.

### Condicoes de conclusao
Resultado e auditoria confirmados ou motivo de impedimento apresentado.

### Cenarios verificaveis
- **Sucesso:** DADA permissao administrativa e dados validos, QUANDO Luciana atualizar perfil de um usuario, ENTAO a nova permissao deve valer e a mudanca ser auditavel.
- **Falha:** DADO usuario sem autorizacao para administrar usuarios, QUANDO tentar executar a acao, ENTAO o sistema deve negar e preservar dados.
- **Limite:** DADO usuario bloqueado com reserva ativa, QUANDO Luciana bloquear o acesso, ENTAO o sistema deve apresentar impacto e nao cancelar reserva automaticamente sem regra definida.
- **Auditoria:** DADA uma alteracao de permissao concluida, QUANDO consultar historico, ENTAO autoria, instante, alvo e operacao devem estar disponiveis a quem pode auditar.

## J-ADM-004 — Gerenciar salas

**ID da jornada:** J-ADM-004  
**Nome:** Cadastrar, consultar, atualizar e desativar sala  
**Persona responsavel:** Administrador (Luciana Prado)  
**Objetivo da persona atendido:** Manter salas corretamente representadas e consultaveis.  
**Necessidade ou dor relacionada:** Dados incorretos/duplicados, sala cadastrada indevidamente e falta de visibilidade de conflitos.  
**Prioridade inicial:** Essencial — salas sao recurso central do inventario.  
**Pre-condicoes:** Luciana autenticada e autorizada; informacoes institucionais da sala disponiveis.  
**Gatilho:** Luciana inicia cadastro, pesquisa, edicao ou desativacao.

### Fluxo principal
1. Interface apresenta busca/lista e permite iniciar cadastro ou abrir sala existente.
2. Luciana informa ou atualiza identificacao, capacidade, localizacao e demais dados definidos; na desativacao, revisa reservas vinculadas.
3. Sistema valida campos, consistencia, duplicidade e autorizacao; consulta reservas relacionadas antes de desativar.
4. Sistema salva cadastro/alteracao ou desativacao, sem apagar historico; registra auditoria.
5. Interface confirma resultado e disponibilidade/situacao atual da sala.

### Responsabilidades do Front-end
- Apresentar campos de capacidade/localizacao, busca, detalhe e confirmacao de desativacao.
- Mostrar reservas vinculadas e impacto conhecido antes de confirmar.
- Exibir estados de validacao, sala inexistente, dados desatualizados e sucesso/falha.

### Responsabilidades do Back-end
- Autorizar operacao, validar campos e impedir duplicidade conforme criterio a definir.
- Consultar reservas existentes e identificar conflitos de desativacao.
- Atualizar situacao sem apagar reservas iniciadas ou historico e registrar autoria/instante/anterior/novo.
- Fazer sala desativada deixar de ser elegivel para novas reservas, respeitada regra de reservas existentes pendente.

### Dados envolvidos
Identificacao da sala, capacidade, localizacao, estado cadastral, reservas vinculadas, periodo e dados de auditoria.

### Regras de negocio aplicaveis
Dados corretos e consultaveis; salas desativadas ou indisponiveis nao recebem novas reservas. Horarios e reservas existentes nao podem ser apagados para contornar desativacao.

### Fluxos alternativos
- Cadastro duplicado/invalido: nao salvar e indicar campo/criterio.
- Sala inexistente: informar e oferecer retorno a pesquisa.
- Reservas futuras/ativas vinculadas: exibir impacto e nao cancelar automaticamente; regra para desativar mantendo essas reservas precisa ser definida.
- Alteracao concorrente: pedir revisao dos dados atuais.

### Tratamento de erros
Falha de validacao/auditoria mantem cadastro anterior; falha de consulta de reservas impede afirmar que desativacao nao tem impacto.

### Resultado esperado
Sala cadastrada/atualizada/desativada com dados e auditoria consistentes, sem cancelamento implícito de reservas.

### Condicoes de conclusao
Operacao confirmada com situacao atual ou recusada com motivo e sem alteracao parcial.

### Cenarios verificaveis
- **Sucesso:** DADOS identificacao, capacidade e localizacao validas, QUANDO Luciana cadastrar uma sala, ENTAO ela deve ficar consultavel conforme permissoes e com autoria registrada.
- **Falha:** DADA sala inexistente, QUANDO tentar atualizar seu cadastro, ENTAO nenhuma sala deve ser criada/alterada implicitamente.
- **Limite:** DADA sala com reserva futura, QUANDO Luciana solicitar desativacao, ENTAO o sistema deve mostrar vinculo e nao cancelar reserva automaticamente sem regra aprovada.
- **Auditoria:** DADA atualizacao confirmada, QUANDO consultar historico, ENTAO deve ser possivel identificar operacao, ator e valores anteriores/novos permitidos.

## J-ADM-005 — Gerenciar professores

**ID da jornada:** J-ADM-005  
**Nome:** Cadastrar e atualizar professores, competencias e situacao cadastral  
**Persona responsavel:** Administrador (Luciana Prado)  
**Objetivo da persona atendido:** Manter professores e informacoes de competencia/disponibilidade corretos.  
**Necessidade ou dor relacionada:** Dados desatualizados e conflitos de alocacao.  
**Prioridade inicial:** Essencial — agenda de professores deve participar da verificacao de disponibilidade.  
**Pre-condicoes:** Luciana autorizada; dados institucionais necessarios disponiveis.  
**Gatilho:** Luciana pesquisa, cadastra ou atualiza professor.

### Fluxo principal
1. Interface permite buscar professor ou iniciar cadastro/edicao.
2. Luciana informa dados institucionais, competencias e situacao cadastral que estao dentro da politica definida.
3. Sistema valida dados, autorizacao e duplicidade conforme criterio definido; consulta alocacoes relacionadas antes de mudar situacao.
4. Sistema grava cadastro/alteracao, preserva historico e audita a operacao.
5. Interface apresenta competencias e disponibilidade/alocacoes consultaveis, sem confundir cadastro administrativo com aprovacao de reserva.

### Responsabilidades do Front-end
- Apresentar cadastro, pesquisa, competencias e consultas de agenda autorizadas.
- Alertar sobre alocacoes relacionadas a mudanca de situacao.
- Distinguir situacao cadastral, disponibilidade consultada e reserva aprovada.

### Responsabilidades do Back-end
- Validar autorizacao, campos e situacao cadastral.
- Consultar alocacoes antes de mudancas que possam afetar disponibilidade; identificar conflito sem inventar remanejamento.
- Aplicar mudanca permitida e auditar ator, instante, alvo, operacao e valores anterior/novo.
- Impedir que alteracao de cadastro crie ou aceite sobreposicao na agenda.

### Dados envolvidos
Identificacao institucional, competencias, situacao cadastral, disponibilidade/alocacoes, reservas vinculadas e auditoria.

### Regras de negocio aplicaveis
Disponibilidade de professor e considerada ao solicitar/aprovar reserva. Professor alocado nao pode ser indicado disponivel para periodo conflitante. Administrador nao e automaticamente aprovador de reserva restrita.

### Fluxos alternativos
- Professor inexistente/duplicado: apresentar resultado ou erro de validacao.
- Professor com alocacao durante alteracao/desativacao: apresentar conflito e nao apagar reserva.
- Consulta de agenda indisponivel: nao afirmar disponibilidade.
- Alteracao de competencia: nao altera automaticamente reservas existentes sem regra definida.

### Tratamento de erros
Falha de consulta da agenda interrompe decisao dependente; falha de auditoria impede confirmar atualizacao.

### Resultado esperado
Cadastro e competencias coerentes; disponibilidade refletida nas consultas; conflitos nao resolvidos ficam explicitados sem alteracao automatica.

### Condicoes de conclusao
Cadastro/consulta confirmado ou impedimento informado, com dados anteriores preservados se a operacao falhar.

### Cenarios verificaveis
- **Sucesso:** DADO cadastro de professor valido, QUANDO Luciana salvar competencias, ENTAO a atualizacao deve ficar auditada e disponivel para consultas autorizadas.
- **Falha:** DADA alocacao conflitante, QUANDO avaliar disponibilidade do professor para o mesmo horario, ENTAO o sistema nao deve indicá-lo como disponivel.
- **Limite:** DADO professor com reserva futura, QUANDO mudar sua situacao cadastral, ENTAO o impacto deve ser apresentado e a reserva nao deve ser apagada sem regra.
- **Escopo:** DADA consulta administrativa de agenda, QUANDO nao houver permissao para detalhes pessoais, ENTAO somente informacao necessaria a disponibilidade deve ser apresentada.

## J-ADM-006 — Gerenciar materiais e equipamentos

**ID da jornada:** J-ADM-006  
**Nome:** Cadastrar, consultar, editar e atualizar estado de materiais/equipamentos  
**Persona responsavel:** Administrador (Luciana Prado)  
**Objetivo da persona atendido:** Manter inventario, disponibilidade e situacao dos recursos.  
**Necessidade ou dor relacionada:** Itens incorretos/duplicados, manutencao desatualizada e baixa visibilidade de movimentacao.  
**Prioridade inicial:** Essencial — esses recursos podem ser reservados e retirados/devolvidos.  
**Pre-condicoes:** Luciana autorizada e informacoes do item disponiveis.  
**Gatilho:** Luciana abre cadastro ou consulta de material/equipamento.

### Fluxo principal
1. Interface apresenta busca por tipo/identificacao, estado, disponibilidade, reservas e movimentacoes permitidas.
2. Luciana cadastra ou seleciona item e informa/atualiza caracteristicas e situacao.
3. Sistema valida autorizacao, dados, identificacao e reservas/movimentacoes relacionadas.
4. Sistema grava alteracao permitida, preserva movimentos/historico e audita autoria/instante.
5. Interface mostra estado atual e disponibilidade calculada; para movimentacoes, remete ao fluxo do Responsavel quando esta fora do escopo administrativo.

### Responsabilidades do Front-end
- Diferenciar cadastro, estado operacional, disponibilidade temporal e historico de movimentacao.
- Exibir reservas, emprestimos/retiradas pendentes e confirmacao de alteracao.
- Nao oferecer edicao ou exclusao de registro de auditoria/movimentacao como se fosse correcao cadastral.

### Responsabilidades do Back-end
- Validar acesso, identificacao e consistencia de cadastro; detectar duplicidade segundo regra a definir.
- Consultar reservas, manutencao e movimentacoes para determinar impacto de atualizacao.
- Impedir disponibilidade em periodo de manutencao e nao aceitar sobreposicao.
- Auditar alteracoes; preservar trilha de retirada/devolucao e operacoes anteriores.

### Dados envolvidos
Tipo/identificacao, caracteristicas, situacao cadastral/operacional, disponibilidade, reservas, manutencao, movimentos e auditoria.

### Regras de negocio aplicaveis
Item em manutencao nao pode ser reservado no periodo. Administracao cadastral nao deve apagar reserva iniciada nem movimentacao; historico auditavel e somente consultavel.

### Fluxos alternativos
- Item inexistente ou cadastro duplicado: apresentar erro/resultado sem alteracao indevida.
- Item com reserva ou retirada aberta: mostrar vinculacoes e impedir alteracao que oculte o historico; impacto operacional deve seguir politica definida.
- Recurso desativado: nao oferecer novas reservas.
- Movimento exige acao do Responsavel: negar mutacao fora da permissao e orientar fluxo correspondente.

### Tratamento de erros
Falha ao consultar reserva/movimento nao deve ser interpretada como ausencia. Falha de auditoria impede confirmar mudanca relevante.

### Resultado esperado
Recurso atualizado com disponibilidade e movimentacoes coerentes, ou alteracao recusada com estado anterior preservado.

### Condicoes de conclusao
Cadastro/consulta finalizado com resultado e historico coerentes.

### Cenarios verificaveis
- **Sucesso:** DADO equipamento novo e dados validos, QUANDO Luciana cadastrar, ENTAO o item deve aparecer no inventario autorizado com alteracao auditada.
- **Falha:** DADO equipamento em manutencao no periodo, QUANDO consultar sua disponibilidade, ENTAO nao deve ser indicado como reservavel.
- **Limite:** DADO material com retirada aberta, QUANDO Luciana alterar caracteristicas do cadastro, ENTAO o movimento original deve permanecer consultavel e vinculado.
- **Permissao:** DADO que registrar devolucao pertence ao fluxo operacional do Responsavel, QUANDO Luciana nao tiver permissao especifica para essa acao, ENTAO o sistema deve negar a mutacao.

## J-ADM-007 — Bloquear recurso para manutencao

**ID da jornada:** J-ADM-007  
**Nome:** Registrar periodo de manutencao e indisponibilidade  
**Persona responsavel:** Administrador (Luciana Prado)  
**Objetivo da persona atendido:** Impedir uso de recurso indisponivel e manter informacao operacional correta.  
**Necessidade ou dor relacionada:** Falta de informacao de manutencao e reservas sobre itens indisponiveis.  
**Prioridade inicial:** Essencial — manutencao bloqueia novas reservas conflitantes.  
**Pre-condicoes:** Luciana autenticada/autorizada; recurso cadastrado; periodo e motivo institucional disponiveis conforme politica.  
**Gatilho:** Luciana escolhe bloquear recurso para manutencao.

### Fluxo principal
1. Interface apresenta recurso e situacao atual; Luciana seleciona solicitar bloqueio.
2. Luciana informa inicio, termino e motivo/detalhes exigidos pela politica.
3. Sistema valida periodo (termino posterior ao inicio), recurso, autorizacao e consulta reservas/manutencoes existentes.
4. Se nao houver reserva conflitante, sistema registra periodo de manutencao e bloqueia novas reservas sobrepostas; grava auditoria.
5. Se houver reserva existente sobreposta, interface apresenta reservas afetadas e nao cancela nem altera nenhuma automaticamente; aplicacao solicita resolucao segundo politica institucional ainda a definir.
6. Interface apresenta bloqueio confirmado ou resultado pendente por conflito identificado.

### Responsabilidades do Front-end
- Mostrar recurso, periodo, campos exigidos e reservas coincidentes antes da confirmacao.
- Apresentar carregamento, periodo invalido, conflito, bloqueio aplicado, dados desatualizados e falha.
- Quando houver reserva existente, nao oferecer “cancelar todas” como resultado implicito; apresentar que decisao operacional e necessaria.

### Responsabilidades do Back-end
- Validar permissao, recurso e periodo; recuperar reservas e manutencoes vigentes e revalidar ao gravar.
- Impedir novas reservas conflitantes durante manutencao confirmada.
- Identificar reservas existentes que colidem e preservar seus estados; nao aplicar cancelamento automatico.
- Registrar bloqueio/alteracao com ator, instante, recurso, periodo, operacao e estado anterior/novo relevante.

### Dados envolvidos
Recurso, periodo de manutencao, motivo exigido, reservas e movimentos coincidentes, estado operacional, administrador, instante e auditoria.

### Regras de negocio aplicaveis
Periodo valido; recurso em manutencao nao recebe reservas conflitantes. Reserva aceita preexistente pode colidir; nao existe regra documentada para cancelar, manter, reagendar ou impedir o bloqueio. Sistema nao deve inventar esse desfecho.

### Fluxos alternativos
- Periodo invalido ou recurso inexistente: nao gravar.
- Sobreposicao com manutencao ja registrada: evitar duplicidade ou apresentar conflito conforme criterio a definir.
- Reserva existente no periodo: mostrar identificadores/detalhes autorizados e manter reservas sem alteracao ate decisao institucional.
- Dados mudaram antes de confirmar: reconsultar e apresentar novamente o impacto.

### Tratamento de erros
Falha ao consultar reservas impede afirmar que o bloqueio nao tem impacto. Falha na auditoria impede confirmar gravacao. Nenhum erro deve causar cancelamento automatico.

### Resultado esperado
Sem reservas conflitantes, bloqueio auditado e aplicado a novas consultas/reservas. Com reservas conflitantes, conflitos identificados, reservas preservadas e resultado operacional pendente conforme politica.

### Condicoes de conclusao
Bloqueio aplicado sem conflito preexistente ou impacto pendente apresentado com dados preservados; indisponibilidade de consulta/gravacao explicitada.

### Cenarios verificaveis
- **Sucesso:** DADO recurso sem reserva coincidente e periodo valido, QUANDO Luciana confirmar manutencao, ENTAO novas reservas sobrepostas devem ser impedidas e o bloqueio auditado.
- **Falha:** DADO termino anterior ao inicio, QUANDO tentar registrar manutencao, ENTAO a operacao deve ser recusada sem alterar disponibilidade.
- **Limite:** DADA reserva aprovada coincidente com manutencao solicitada, QUANDO Luciana iniciar bloqueio, ENTAO o sistema deve identificar a reserva e nao cancela-la automaticamente.
- **Concorrencia:** DADO solicitante enviando reserva enquanto bloqueio e confirmado, QUANDO ambas as operacoes concorrerem, ENTAO nenhuma reserva conflitante pode ser aceita depois de o bloqueio efetivar-se.

## J-ADM-008 — Consultar auditoria

**ID da jornada:** J-ADM-008  
**Nome:** Pesquisar historico auditavel por periodo, usuario ou recurso  
**Persona responsavel:** Administrador (Luciana Prado)  
**Objetivo da persona atendido:** Investigar alteracoes e localizar inconsistencias.  
**Necessidade ou dor relacionada:** Alteracoes sem autoria e dificuldade para reconstruir eventos.  
**Prioridade inicial:** Essencial — rastreabilidade e requisito explicito de operacoes importantes.  
**Pre-condicoes:** Luciana autenticada e autorizada a consultar os registros pertinentes.  
**Gatilho:** Luciana abre auditoria para investigar ou verificar uma operacao.

### Fluxo principal
1. Interface solicita periodo e permite filtros por usuario, recurso e acao.
2. Luciana informa criterios e envia consulta.
3. Sistema verifica autorizacao, valida filtros e recupera eventos correspondentes.
4. Interface mostra ator, quando, recurso/reserva afetado, operacao, estado anterior e novo, quando disponiveis.
5. Luciana abre detalhe autorizado para reconstruir sequencia, sem editar registros.

### Responsabilidades do Front-end
- Oferecer filtros, ordenacao temporal e detalhe; mostrar “sem resultados” separado de falha.
- Apresentar dados anteriores/novos sem expor segredos ou dados pessoais desnecessarios.
- Tornar claro que auditoria e somente leitura.

### Responsabilidades do Back-end
- Aplicar autorizacao e filtros; retornar eventos auditaveis sem alterar sua origem.
- Cada alteracao de estado deve incluir ator, instante, alvo, operacao, estado anterior e novo, conforme regra.
- Impedir qualquer mutacao/exclusao de registro de auditoria por esta jornada, inclusive para administrador.

### Dados envolvidos
Periodo, identidade do ator, alvo/recurso/reserva, operacao, instante, estado anterior/novo e contexto auditavel permitido.

### Regras de negocio aplicaveis
Toda mudanca de estado gera auditoria. Administrador pode consultar registros autorizados, mas nao os modifica. A consulta deve proteger informacao sensivel.

### Fluxos alternativos
- Filtros sem correspondencia: mostrar lista vazia.
- Evento sem algum campo historico esperado: apresentar lacuna como dado ausente, nao inventar valor.
- Usuario/recurso inacessivel a Luciana por escopo: negar detalhe conforme autorizacao institucional.

### Tratamento de erros
Falha de recuperacao nao equivale a inexistencia de alteracoes. Informar indisponibilidade; preservar criterios para nova tentativa.

### Resultado esperado
Eventos correspondentes ficam consultaveis em modo leitura, com autoria e mudancas visiveis.

### Condicoes de conclusao
Consulta foi apresentada, vazia, negada ou indisponivel com feedback correspondente.

### Cenarios verificaveis
- **Sucesso:** DADO uma reserva com mudanca de estado auditada, QUANDO Luciana pesquisar por periodo/recurso, ENTAO deve encontrar ator, instante, operacao e estados anterior/novo.
- **Falha:** DADA falha na leitura da auditoria, QUANDO consultar, ENTAO o sistema deve informar erro, nao “nenhuma alteracao”.
- **Limite:** DADO administrador com acesso de consulta, QUANDO tentar editar ou excluir um evento auditavel, ENTAO a operacao deve ser negada.
- **Privacidade:** DADO evento com informacao sensivel nao necessaria, QUANDO apresentar resultado, ENTAO essa informacao deve ser omitida ou protegida conforme permissao.

## J-ADM-009 — Consultar relatorios operacionais

**ID da jornada:** J-ADM-009  
**Nome:** Consultar utilizacao, reservas e periodos de demanda  
**Persona responsavel:** Administrador (Luciana Prado)  
**Objetivo da persona atendido:** Acompanhar utilizacao e identificar recursos ociosos ou sobrecarregados.  
**Necessidade ou dor relacionada:** Falta de relatorios e baixa visibilidade operacional.  
**Prioridade inicial:** Importante — apoia decisao operacional, mas metricas dependem de definicoes uniformes.  
**Pre-condicoes:** Luciana autorizada; eventos e reservas disponiveis para o periodo consultado.  
**Gatilho:** Luciana abre relatorios e seleciona periodo/criterios.

### Fluxo principal
1. Interface apresenta relatorios de utilizacao por recurso, mais/menos utilizados, carga horaria alocada, quantidade de reservas, cancelamentos, conflitos evitados e periodos de maior utilizacao.
2. Luciana seleciona periodo, categoria/tipo de recurso e filtros disponiveis.
3. Sistema valida criterios e agrega dados de reservas, estados, disponibilidade, manutencao e eventos de conflito registrados.
4. Interface apresenta resultados, unidade, periodo, definicao da metrica e indicacao de dados incompletos/desatualizados.
5. Luciana consulta detalhes autorizados para identificar inconsistencias ou oportunidades operacionais.

### Responsabilidades do Front-end
- Exibir metricas com unidades, filtros e intervalo; permitir comparar recursos somente com criterio equivalente.
- Mostrar sem dados, dados parciais, carregamento e falha de forma distinta.
- Apresentar definicoes das metricas para evitar interpretar contagens ambiguas como fatos comparaveis.

### Responsabilidades do Back-end
- Autorizar consulta; validar periodo e filtros.
- Agregar somente eventos/registros do intervalo e segundo definicoes aprovadas; manter consistencia de contagem.
- Nao inventar “conflitos evitados” sem eventos confiaveis que identifiquem tentativa impedida.
- Nao alterar reservas como efeito da consulta.

### Dados envolvidos
Periodo; recursos e tipo; periodos de reserva; estado; manutencao; cancelamentos; conflitos detectados/recusados; carga horaria; data de atualizacao.

### Regras de negocio aplicaveis
Relatorios devem respeitar autorizacao e nao expor dados sensiveis desnecessarios. Contagens de utilizacao/cancelamento e conflitos devem ter definicao e janela temporal comuns; resultados de consulta nao alteram registros.

### Fluxos alternativos
- Periodo sem registros: apresentar sem dados/zero conforme definicao.
- Metrica sem fonte ou criterio definido: identifica-la como pendente/indisponivel, sem estimar.
- Dados incompletos: marcar incompletude e nao afirmar total confiavel.
- Filtro invalido: solicitar correcao.

### Tratamento de erros
Falha na agregacao deve ser distinta de zero; informar indisponibilidade sem substituir por estimativa silenciosa.

### Resultado esperado
Relatorio consultavel e interpretavel para o periodo, ou limitacao de definicao/dados explicitada.

### Condicoes de conclusao
Resultado com criterios e atualizacao apresentados ou impossibilidade devidamente informada.

### Cenarios verificaveis
- **Sucesso:** DADOS registros validos no periodo, QUANDO Luciana consultar reservas por recurso, ENTAO totais devem refletir exatamente os estados incluidos pela definicao apresentada.
- **Falha:** DADA falha de agregacao, QUANDO abrir relatorio, ENTAO a falha nao pode ser exibida como total zero.
- **Limite:** DADO que nao existe definicao/fonte confiavel de “conflitos evitados”, QUANDO gerar relatorio, ENTAO a metrica deve ser identificada como indisponivel/pendente, sem estimativa inventada.
- **Manutencao:** DADO um equipamento bloqueado em parte do periodo, QUANDO computar sua disponibilidade, ENTAO a metrica deve respeitar o periodo de bloqueio conforme definicao apresentada.

## Ambiguidades e decisoes pendentes — Administrador

- Definir matriz completa de permissoes, politica de campos/unicidade de cadastros e comportamento para bloqueio de usuario com reserva ativa.
- Definir criterios e impactos de desativar sala, professor, material ou equipamento com reservas/movimentacoes existentes; nao excluir registros nem cancelar reservas automaticamente.
- Definir resolucao de reservas ja aprovadas que colidem com um novo periodo de manutencao. A regra vigente proibe novas reservas durante manutencao, mas nao determina o destino das existentes.
- Definir conjunto/periodo e formula dos indicadores do dashboard, “disponivel”, “reservado” e alertas de atencao.
- Definir formula e fonte para utilizacao, carga horaria, cancelamentos, horarios de pico e “conflitos evitados”; sem registro de tentativa recusada, essa ultima metrica nao e comprovavel.
- Definir filtros de auditoria e retencao/visibilidade de dados sensiveis, sem permitir edicao dos eventos.

## Revisao cruzada

As nove jornadas cobrem autenticacao, dashboard, usuarios, salas, professores, materiais/equipamentos, manutencao, auditoria e relatorios, todas vinculadas aos objetivos e dores da persona. Operacoes administrativas conferem permissao, validam dados e preservam auditoria; administrador nao recebe aprovacao implicita de solicitacoes restritas nem permissao para editar auditoria. Manutencao e conflitos com reservas existentes, concorrencia e dados desatualizados foram tratados. Indicadores sem definicao e impactos cadastrais nao especificados estao marcados como pendencias; nao houve cancelamento ou remanejamento presumido.
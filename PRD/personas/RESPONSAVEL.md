# Persona: Responsavel

## 1. Identificacao

- **Perfil de acesso:** Responsavel
- **Nome ficticio:** Rafael Nunes
- **Funcao:** Responsavel por laboratorio e equipamentos academicos

## 2. Perfil profissional

Rafael acompanha a operacao de um laboratorio e dos materiais e equipamentos sob sua responsabilidade. Sua atuacao e operacional e delimitada: nao administra o sistema como um todo.

## 3. Contexto de uso

Usa a aplicacao para avaliar solicitacoes pendentes relacionadas ao seu escopo, conferir agendas e acompanhar a movimentacao de itens. Tambem consulta reservas aprovadas para preparar recursos e registrar retiradas e devolucoes.

## 4. Responsabilidades

- Avaliar solicitacoes de recursos restritos dentro do escopo que lhe foi atribuido.
- Aprovar ou rejeitar solicitacoes elegiveis, registrando justificativa quando rejeitar.
- Validar alocacoes de professores e identificar conflitos pertinentes ao seu escopo.
- Registrar retirada e devolucao de materiais e equipamentos sob sua responsabilidade.
- Acompanhar pendencias e condicoes dos recursos sob seu acompanhamento.

## 5. Objetivos principais

- Visualizar solicitacoes pendentes com informacoes suficientes para decidir.
- Aprovar solicitacoes restritas para as quais esteja autorizado.
- Rejeitar solicitacoes com justificativa clara.
- Identificar conflitos de horario e de disponibilidade de professores.
- Acompanhar reservas aprovadas e movimentacoes de recursos.
- Consultar historico, localizar pendencias de devolucao e identificar quem utilizou um recurso do seu escopo.

## 6. Motivacoes

Rafael busca manter o uso seguro e organizado dos recursos que acompanha, evitar indisponibilidade inesperada e assegurar que decisoes e movimentacoes possam ser explicadas posteriormente.

## 7. Necessidades

- Lista organizada de solicitacoes pendentes, filtravel pelo seu escopo.
- Dados completos e contexto da solicitacao antes de decidir.
- Destaque para conflitos, manutencao, restricoes e situacoes de risco operacional.
- Confirmacoes explicitas para aprovar, rejeitar e registrar movimentacoes.
- Historico consultavel de decisoes, retiradas e devolucoes.
- Identificacao rapida dos recursos sob sua responsabilidade e das pendencias relacionadas.

## 8. Principais dores e dificuldades

- Solicitacoes incompletas que impedem uma avaliacao segura.
- Conflitos na agenda de professores ou de recursos.
- Falta de controle sobre retirada e devolucao.
- Equipamentos nao devolvidos ou sem responsavel identificavel.
- Decisoes sem justificativa ou sem historico rastreavel.
- Dificuldade para encontrar movimentacoes de um recurso sob seu escopo.

## 9. Comportamentos esperados

- Confere escopo, disponibilidade, periodo e detalhes antes de decidir.
- Aprova somente solicitacoes para as quais possui autorizacao.
- Registra justificativa ao rejeitar e informa dados corretos nas movimentacoes.
- Confirma identidade da reserva e do recurso ao registrar retirada ou devolucao.
- Consulta o historico para resolver pendencias, sem tentar alterar registros de auditoria.
- Encaminha situacoes fora de sua alçada ao administrador ou ao fluxo institucional apropriado.

## 10. Funcionalidades desejadas

- Consulta de solicitacoes pendentes atribuidas ao seu escopo.
- Visualizacao de disponibilidade e conflitos relevantes.
- Acoes de aprovar ou rejeitar solicitacao autorizada, com justificativa na rejeicao.
- Consulta de reservas aprovadas vinculadas aos recursos acompanhados.
- Registro de retirada e devolucao de materiais e equipamentos.
- Consulta de historico de movimentacoes e identificacao de devolucoes pendentes.

## 11. Informacoes que precisa consultar

- Solicitante e finalidade informada, nos limites necessarios a decisao.
- Recurso, periodo, estado e requisitos da solicitacao.
- Disponibilidade do recurso e de professores envolvidos.
- Indicacao de manutencao, restricao ou conflito.
- Dados da reserva e da pessoa responsavel pela retirada.
- Historico de decisoes e movimentacoes dos recursos sob seu escopo.

## 12. Acoes que precisa realizar

- Revisar e decidir solicitacoes restritas autorizadas.
- Registrar motivo ao rejeitar.
- Validar alocacoes de professores no contexto de sua responsabilidade.
- Registrar retirada e devolucao de itens.
- Consultar pendencias e historico operacional.

## 13. Nivel de acesso

Operacional intermediario, autenticado e limitado a recursos, solicitacoes e atividades atribuidos ao seu escopo.

## 14. Permissoes

- Consultar solicitacoes e reservas relacionadas ao escopo de responsabilidade.
- Aprovar ou rejeitar solicitacoes restritas somente quando designado como responsavel autorizado.
- Registrar retiradas e devolucoes dos materiais e equipamentos que acompanha.
- Consultar historico operacional pertinente ao seu escopo.

## 15. Restricoes

- Nao possui acesso irrestrito a administracao do sistema ou a recursos fora de seu escopo.
- Nao configura regras globais, gerencia permissoes administrativas nem administra usuarios.
- Nao altera diretamente registros de auditoria nem apaga historico de decisoes ou movimentacoes.
- Nao aprova solicitacoes fora de sua autorizacao, nem a propria solicitacao por causa do perfil.
- Nao pode aprovar recurso em manutencao ou criar alocacao com horario invalido ou sobreposto.
- Reserva iniciada nao pode ser apagada; alteracoes de estado precisam ser registradas.

## 16. Expectativas em relacao a aplicacao

Rafael espera uma fila de trabalho clara, com contexto suficiente e limites de escopo visiveis. A aplicacao deve impedir decisoes fora de sua autorizacao, destacar conflitos e preservar um historico confiavel de aprovacoes, rejeicoes e movimentacoes.

## 17. Exemplo pratico de utilizacao

Rafael abre as solicitacoes pendentes do laboratorio e revisa o recurso, o periodo, a finalidade e a disponibilidade. Para uma solicitacao restrita dentro de sua alçada, aprova ou rejeita com motivo. Quando o equipamento e retirado, registra a movimentacao vinculada a reserva e, na devolucao, registra o retorno. Se houver atraso, consulta a pendencia e o historico para identificar a reserva e o solicitante.

## 18. Criterios importantes para a experiencia do usuario

- Separacao visivel entre itens do proprio escopo e demais recursos.
- Informacoes essenciais disponiveis antes de uma decisao irreversivel ou relevante.
- Conflitos e manutencao destacados sem depender de interpretacao ambigua.
- Justificativa de rejeicao e confirmacao de retirada/devolucao integradas ao fluxo.
- Historico facilmente consultavel, mas protegido contra edicao pelo responsavel.
- Acoes administrativas globais nao devem aparecer como se fossem parte de seu papel.

## 19. Relacao com as regras criticas do projeto

- **Tempo e concorrencia:** verifica periodo valido, disponibilidade de professores e ausencia de sobreposicao. Uma solicitacao concorrente nao pode resultar em duas reservas aceitas para o mesmo recurso e horario.
- **Manutencao:** recurso em manutencao nao pode ser reservado nem aprovado para uso durante o bloqueio.
- **Aprovacao e estados:** somente responsavel autorizado aprova solicitacao de recurso restrito. A decisao conduz a `APROVADA` ou `REJEITADA`; o fluxo de uso pode seguir para `EM_USO` e `CONCLUIDA`, ou encerrar como `CANCELADA` conforme as regras.
- **Auditoria:** cada mudanca de estado e decisao deve gerar registro auditavel. O responsavel pode consultar o historico pertinente, mas nao o modifica.
- **Seguranca:** autenticacao, autorizacao por escopo e minimizacao de dados impedem acesso a recursos ou informacoes fora de sua atribuicao.

## 20. Cenarios de utilizacao

### Cenario 1: Aprovar solicitacao de equipamento restrito

Rafael recebe uma solicitacao pendente do laboratorio. Confere solicitante, finalidade, periodo, disponibilidade e eventuais conflitos. Sendo responsavel autorizado e nao havendo bloqueio de manutencao, aprova. A decisao muda o estado e fica registrada para auditoria.

### Cenario 2: Rejeitar solicitacao incompleta ou conflitante

Rafael identifica que faltam informacoes essenciais ou que o horario conflita com outra alocacao. Rejeita a solicitacao dentro de sua alçada e registra o motivo. O solicitante recebe a atualizacao e pode compreender a decisao; Rafael nao apaga nem altera o registro de auditoria.

### Cenario 3: Registrar retirada e devolucao

Com uma reserva aprovada, Rafael confere o equipamento e a pessoa vinculada a reserva, registra a retirada e, posteriormente, a devolucao. Se o item continuar pendente, consulta a lista de devolucoes e o historico para localizar a movimentacao.

### Cenario 4: Identificar conflito na alocacao de professor

Ao revisar uma solicitacao que envolve professor, Rafael consulta a disponibilidade no periodo e encontra uma alocacao sobreposta. Nao aprova a combinacao conflitante e encaminha a solicitacao para ajuste ou rejeicao justificada, respeitando sua autorizacao.
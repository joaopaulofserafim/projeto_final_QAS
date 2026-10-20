# Persona: Solicitante

## 1. Identificacao

- **Perfil de acesso:** Solicitante
- **Nome ficticio:** Marina Costa
- **Funcao:** Professora e coordenadora de atividades academicas

## 2. Perfil profissional

Marina organiza aulas, reunioes e atividades de extensao. Ela conhece as necessidades pedagogicas do grupo, mas nao administra o inventario nem as regras gerais de uso dos recursos.

## 3. Contexto de uso

Usa a aplicacao antes de planejar uma atividade e quando precisa acompanhar uma reserva. Pode acessa-la no escritorio ou pelo celular, muitas vezes com pouco tempo para encontrar uma opcao adequada. Precisa consultar salas, professores, materiais e equipamentos por data e horario.

## 4. Responsabilidades

- Informar corretamente a finalidade, o periodo e os recursos solicitados.
- Consultar disponibilidade antes de planejar a atividade.
- Acompanhar a decisao e cumprir as condicoes da reserva aprovada.
- Alterar ou cancelar somente as proprias reservas, quando o estado e as regras permitirem.

## 5. Objetivos principais

- Encontrar rapidamente recursos compativeis com uma atividade.
- Pesquisar salas por capacidade e localizacao.
- Pesquisar professores por competencia e disponibilidade.
- Consultar materiais e equipamentos e verificar disponibilidade por data e horario.
- Criar solicitacoes, consultar seu andamento e seu historico.
- Receber comunicacoes sobre aprovacao, rejeicao, cancelamento ou alteracoes pertinentes.
- Evitar conflitos e entender o motivo quando uma opcao ou solicitacao nao puder ser atendida.

## 6. Motivacoes

Marina quer reduzir o tempo gasto coordenando reservas com diferentes setores e ter previsibilidade para preparar as atividades academicas. Valoriza respostas claras e informacoes confiaveis.

## 7. Necessidades

- Interface simples e responsiva, com pesquisa rapida.
- Informacoes atualizadas e identificacao clara de disponibilidade e restricoes.
- Confirmacao de envio e visualizacao do estado atual da solicitacao.
- Aviso compreensivel quando houver conflito, indisponibilidade ou necessidade de aprovacao.
- Motivo da rejeicao quando registrado e historico das proprias solicitacoes.

## 8. Principais dores e dificuldades

- Solicitar uma sala que ja esta ocupada ou um recurso em manutencao.
- Nao encontrar recursos disponiveis ou receber informacao desatualizada.
- Nao saber se a solicitacao foi recebida, aprovada ou rejeitada.
- Precisar contatar diferentes setores para entender a disponibilidade.
- Receber uma rejeicao sem compreender o motivo.
- Interpretar uma disponibilidade consultada como garantia antes da confirmacao da reserva.

## 9. Comportamentos esperados

- Pesquisa por criterios relevantes e confere data, horario e detalhes antes de solicitar.
- Fornece dados suficientes para a avaliacao, especialmente quando o recurso e restrito.
- Aguarda a decisao de um responsavel autorizado quando a solicitacao exigir aprovacao.
- Revisa o estado antes de pedir alteracao ou cancelamento.
- Consulta apenas os dados e historicos aos quais seu perfil tem acesso.

## 10. Funcionalidades desejadas

- Pesquisa e consulta de salas, professores, materiais e equipamentos.
- Filtros de capacidade, localizacao, competencia, data e horario, conforme o tipo de recurso.
- Visualizacao de disponibilidade, indisponibilidade e necessidade de aprovacao.
- Criacao, acompanhamento, alteracao permitida e cancelamento de solicitacoes proprias.
- Consulta do historico proprio e recebimento de notificacoes relacionadas as solicitacoes.

## 11. Informacoes que precisa consultar

- Tipo, identificacao e caracteristicas relevantes do recurso.
- Disponibilidade para o periodo consultado e conflitos conhecidos.
- Indicacao de manutencao ou de acesso restrito, sem exposicao de dados administrativos desnecessarios.
- Estado da solicitacao, decisao e justificativa de rejeicao quando houver.
- Condicoes e periodo da reserva confirmada.

## 12. Acoes que precisa realizar

- Buscar e comparar recursos disponiveis.
- Informar recurso, inicio, termino e dados da atividade requeridos pelo processo.
- Enviar solicitacao e acompanhar seu estado.
- Solicitar alteracao ou cancelar a propria reserva quando permitido.

## 13. Nivel de acesso

Basico, autenticado e limitado as consultas autorizadas e aos registros proprios.

## 14. Permissoes

- Consultar recursos e disponibilidades que estejam visiveis ao seu perfil.
- Criar solicitacoes em seu nome.
- Consultar o estado e o historico de suas solicitacoes.
- Alterar ou cancelar suas proprias reservas somente quando permitido pelas regras e pelo estado.

## 15. Restricoes

- Nao possui acesso administrativo nem pode gerenciar usuarios, permissoes ou cadastros globais.
- Nao aprova solicitacoes especiais, inclusive as proprias.
- Nao altera nem cancela reservas de outros usuarios.
- Nao pode tratar uma solicitacao como aprovada antes da confirmacao.
- Nao pode reservar recurso em manutencao nem criar horario invalido ou sobreposto.
- Uma reserva iniciada nao pode ser apagada; acoes permitidas devem preservar seu registro e historico.

## 16. Expectativas em relacao a aplicacao

Marina espera que a consulta seja clara, que o envio tenha confirmacao e que cada mudanca de estado seja comunicada de forma compreensivel. Espera que a aplicacao impeca a dupla aceitacao de horarios concorrentes, explique bloqueios e mantenha o historico sem expor informacoes de outros usuarios.

## 17. Exemplo pratico de utilizacao

Marina procura uma sala com capacidade adequada para uma aula em uma data especifica. Consulta localizacao e disponibilidade, informa inicio e termino e envia a solicitacao. Se o recurso for restrito, acompanha o estado solicitado enquanto aguarda um responsavel autorizado. Se o horario ja tiver sido reservado por outra solicitacao concorrente, recebe a indicacao de conflito e precisa escolher outro periodo. Depois, consulta a decisao e, se aprovada, acompanha os dados da reserva.

## 18. Criterios importantes para a experiencia do usuario

- Pesquisa curta e resultados faceis de comparar.
- Estados e restricoes apresentados em linguagem direta.
- Datas e horarios inequivocos, com validacao de termino posterior ao inicio.
- Confirmacao para envio, alteracao e cancelamento.
- Acessibilidade e uso responsivo em dispositivos moveis.
- Nenhuma acao deve sugerir que uma solicitacao pendente ja e uma reserva aprovada.

## 19. Relacao com as regras criticas do projeto

- **Tempo e concorrencia:** informa inicio e termino validos; disponibilidade de salas, equipamentos, materiais e professores deve considerar conflitos. Uma consulta nao garante que uma solicitacao concorrente sera aceita; o sistema nao pode aceitar duas reservas sobrepostas para o mesmo recurso.
- **Aprovacao e estados:** acompanha a transicao `SOLICITADA` para `APROVADA`, `EM_USO` e `CONCLUIDA`, ou os estados alternativos `REJEITADA` e `CANCELADA`. Recursos restritos aguardam aprovacao de responsavel autorizado.
- **Alteracoes:** pode agir somente sobre registros proprios e conforme o estado. Reserva iniciada nao pode ser apagada.
- **Seguranca e auditoria:** autenticacao e autorizacao limitam consultas e acoes; toda alteracao de estado deve ser rastreavel. Informacoes de terceiros nao devem ser expostas.

## 20. Cenarios de utilizacao

### Cenario 1: Encontrar uma sala para uma aula

Marina pesquisa uma sala por capacidade, localizacao, data e horario. Confere as opcoes apresentadas e envia uma solicitacao com inicio anterior ao termino. A aplicacao confirma o recebimento e exibe o estado da solicitacao.

### Cenario 2: Tentar usar um horario ocupado

Marina consulta um horario que parece livre, mas outra solicitacao e aceita para o mesmo recurso antes da dela. Ao enviar, a aplicacao detecta o conflito e nao aceita duas reservas sobrepostas. Ela recebe uma orientacao para consultar outro horario ou recurso.

### Cenario 3: Acompanhar recurso restrito

Marina solicita um equipamento restrito, informa os dados necessarios e ve que a solicitacao esta pendente. Um responsavel autorizado decide; Marina recebe a atualizacao e, se rejeitada, consulta a justificativa registrada. Ela nao consegue aprovar a propria solicitacao.

### Cenario 4: Cancelar uma reserva propria

Marina consulta uma reserva propria ainda elegivel para cancelamento e solicita o cancelamento. A aplicacao atualiza o estado conforme as regras, registra a alteracao e apresenta a confirmacao. Ela nao pode apagar uma reserva iniciada.
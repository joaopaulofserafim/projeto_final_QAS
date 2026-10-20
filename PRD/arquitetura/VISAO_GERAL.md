# Visao Geral da Arquitetura — Sistema de Organizacao de Recursos

**Versao:** 0.1  
**Data:** 2026-10-20  
**Status:** Proposta para revisao; nenhuma decisao estrutural foi aprovada formalmente.  
**Base:** Personas, jornadas, requisitos RF-001..RF-036, RN-001..RN-024, RNF-001..RNF-014 e DP-001..DP-016.

## Objetivo e escopo

Propor uma organizacao modular, rastreavel e testavel para pesquisa e administracao de recursos, reservas, decisoes de aprovacao, movimentacao, auditoria, notificacoes e relatorios. A modelagem antecede implementacao; nao define entidades/tabelas, endpoints, contrato externo, esquema de configuracao, infraestrutura ou tecnologia de persistencia.

**Diretrizes definidas pelo projeto:** Java 21, Spring Boot 3.x, JUnit 5, Testcontainers, WireMock, JaCoCo, SonarCloud, GitHub Actions, JMeter e TDD. Framework de interface, banco de dados, ORM, identidade, mensageria, hospedagem e provedor de notificacoes permanecem sem selecao.

## Proposta arquitetural

Adotar como proposta um **monolito modular**: uma aplicacao Web, um processo back-end Spring Boot e um armazenamento transacional unico. Os limites de modulo sao logicos, com dependencias dirigidas para interfaces/casos de uso e regras centrais sem dependencia da interface ou do mecanismo de persistencia. Nenhum modulo e um servico implantavel separado.

O contêiner back-end abriga os seguintes componentes conceituais:

| Componente | Responsabilidade principal |
|---|---|
| Interface Web | Navegacao, formularios, estados de tela, filtros e feedback; nao e autoridade final de permissao/regra. |
| Entrada da Aplicacao | Receber operacoes, validar forma das entradas, identificar principal autenticado, encaminhar casos de uso e converter resultados/erros para a interface. |
| Identidade e Autorizacao | Autenticar, resolver perfil, propriedade e escopo; proteger cada operacao no servidor. Provedor/mecanismo final pendente. |
| Catalogo de Recursos | Consultar e manter salas, professores, materiais/equipamentos, caracteristicas e situacao cadastral. |
| Administracao de Cadastros | Orquestrar alteracoes administrativas e seus impactos; regras permanecem sujeitas a RF/RN. |
| Disponibilidade e Integridade | Aplicar periodo semiaberto, comparar reservas, docentes e manutencoes e coordenar o ponto atomico que impede duplo aceite. |
| Reservas e Transicoes | Propriedade, ciclo de vida, criacao, alteracao/cancelamento elegiveis e invariantes de estado. |
| Decisao de Aprovacao | Filtrar pedidos do escopo, autorizar decisor, revalidar estado/disponibilidade e registrar aprovacao/rejeicao. |
| Movimentacao | Associar retirada/devolucao a reserva/recurso e identificar operador, destinatario e instante. |
| Auditoria | Registrar eventos de operacoes criticas e oferecer consulta autorizada somente para leitura. |
| Consultas e Relatorios | Consultas por persona, filas, dashboard e agregacoes com definicoes/metas pendentes explicitadas. |
| Comunicacao | Preparar confirmacoes/avisos e integrar um canal somente quando decidido; falha de entrega nao reverte operacao. |
| Adaptadores de Persistencia | Implementar leitura/escrita por fronteiras definidas; a persistencia final deve suportar operacoes atomicas consistentes. |

Os nomes sao fronteiras conceituais, nao pacotes, classes ou controllers definidos. Para evitar ciclos, os casos de uso coordenam chamadas entre modulos; um modulo nao acessa estruturas internas de outro. Regras temporais e de estado ficam em um unico componente de dominio por operacao, nao duplicadas na interface.

## Alternativas e trade-offs

| Alternativa | Beneficios | Custos/riscos | Avaliacao |
|---|---|---|---|
| A. Monolito modular (proposta) | Uma fronteira transacional para reserva/auditoria; operacao simples; encaixa em equipe academica e Testcontainers; modulos testaveis. | Exige disciplina contra dependencias circulares; escala do processo e compartilhada. | Melhor equilibrio para escopo atual; ADR-001 Proposta. |
| B. Servicos distribuidos | Implantacao/escala independentes e limites de operacao separados. | Transacoes entre servicos, consistencia eventual, observabilidade e infraestrutura ampliam risco; prejudica atomicidade inicial. | Sem justificativa no porte/escopo atual; nao recomendado agora. |
| C. Aplicacao unica sem modulos internos | Menos estrutura inicial. | Regras e autorizacao tendem a duplicar/acoplar interface, persistencia e fluxos; piora testabilidade e rastreabilidade. | Rejeitada como organizacao interna; nao impede um unico processo. |

Proposta sujeita a validacao do PO, perspectiva de arquitetura e stakeholders. Nao existe aprovacao multiperspectiva registrada nesta etapa.

## Fluxo de dados e integridade

1. Persona inicia operacao na Interface Web.
2. Entrada da Aplicacao verifica identidade, perfil, propriedade e escopo por meio de Identidade e Autorizacao.
3. Caso de uso aplica regras de dominio e consulta os modulos pertinentes.
4. Disponibilidade reavalia reservas/estados bloqueantes, docentes, manutencao e situacao cadastral no instante de confirmar.
5. Persistencia participa de uma operacao atomica que torna impossivel confirmar dois usos conflitantes; a alternativa fisica depende da selecao do banco e de DP-001/DP-017.
6. Alteracao de estado e evento de auditoria devem confirmar juntos. Se auditoria obrigatoria falhar, a operacao nao pode ser apresentada como concluida.
7. Comunicacao e disparada apos o resultado de negocio confirmado; uma falha externa nao desfaz reserva/aprovacao. Durabilidade/reenvio ainda depende de DP-009.
8. Entrada retorna resultado explicito e a interface apresenta sucesso, conflito, negacao ou falha sem inferir estado.

## Seguranca e erros

- Toda operacao protegida revalida autorizacao no back-end, inclusive leitura direta de identificador. Ocultar controles na interface e apenas orientacao de uso.
- Solicitante: somente registros proprios; nao administra nem aprova recurso restrito. Responsavel: escopo atribuido e decisao autorizada. Administrador: cadastro/configuracao permitidos, sem bypass de regra ou edicao de auditoria.
- Componentes de entrada e acesso minimizam dados; erros externos nao revelam credenciais, detalhes internos ou dados alheios.
- Resultado distingue validacao, conflito, indisponibilidade, acesso negado, recurso inexistente, falha interna e falha de comunicacao.

## Preparacao para TDD

| Nivel futuro | Responsabilidade | Referencia academica |
|---|---|---|
| Unitario | Regras de intervalo, conflito, transicoes permitidas, autorizacao por caso, politica de reserva e agregacoes. Isolar casos de uso e dominio; usar dublês somente aqui quando util. | JUnit 5; TDD Red-Green-Refactor. |
| Integracao | Persistencia real, limites transacionais, concorrencia, auditoria atomica e autorizacao de ponta de entrada. | Testcontainers com o mecanismo de persistencia escolhido (DP-017). |
| Integracao externa | Contrato do adaptador de notificacao e falhas de entrega, se houver integracao HTTP selecionada. | WireMock; nao substituir validacao transacional. |
| HTTP/aceitacao | Verificar entradas, autorizacao, respostas e mensagens de erro dos fluxos publicos que forem definidos. | JUnit 5 e suporte da aplicacao; contrato/API permanece pendente DP-012. |
| Carga | Consultas, concorrencia de reservas e relatorios, comparadas com metas aprovadas. | JMeter; metas/carga DP-014. |
| Qualidade continua | Relatorios de cobertura e analise estatica em CI. | JaCoCo, SonarCloud e GitHub Actions; limiares nao definidos. |

Testes unitarios nao substituem os cenarios de integracao com persistencia real para concorrencia/atomicidade. Nao foram criados testes, IDs de testes, thresholds de cobertura ou pipelines nesta etapa.

## Referencias e status

- C4: [Contexto](c4/NIVEL_1_CONTEXTO.md), [Contêineres](c4/NIVEL_2_CONTEINERES.md), [Componentes](c4/NIVEL_3_COMPONENTES.md).
- UML: [Estados](uml/ESTADOS_RESERVA.md), sequencias por persona e concorrencia em `uml/sequencia/`.
- Decisoes: `adr/ADR-001.md` a `adr/ADR-009.md`, todas Proposta ou Pendente.
- Riscos: [RISCOS_ARQUITETURAIS.md](RISCOS_ARQUITETURAIS.md).
- Decisoes de produto e tecnicas pendentes estao em `../requisitos/PENDENCIAS_DECISOES.md`; a RTM recebeu uma secao de rastreabilidade arquitetural.

**Gate:** modelo pronto para revisao, nao para implementacao. PO/Architect/stakeholder sign-off, decisao do banco, politica de estados, estrategia final de concorrencia e integracao externa seguem pendentes.
# Visão Geral da Arquitetura — Sistema de Organização de Recursos

- **Versão:** 0.2
- **Data:** 2026-10-26
- **Status:** Proposta técnica revisada; não aprovada formalmente e não implementada.
- **Base funcional:** RF-001..RF-036, RN-001..RN-024, RNF-001..RNF-014, personas e jornadas em `PRD/`.
**Escopo desta etapa:** modelagem documental. Não define código, endpoints, esquema físico, infraestrutura, nem evidência de testes executados.

## 1. Síntese da proposta

Adotar **monólito modular** como forma inicial de menor complexidade: uma aplicação Web, um processo de aplicação back-end e uma persistência transacional. Java 21 e Spring Boot 3.x são a referência para o back-end. Os módulos descritos no C4 nível 3 são componentes lógicos no mesmo contêiner/processo, não serviços implantáveis. Framework de interface, SGBD, ORM, mecanismo de identidade, protocolo de comunicação e hospedagem permanecem em aberto.

O foco arquitetural não é antecipar tecnologia, mas preservar quatro propriedades do domínio:

1. **Disponibilidade é uma consulta temporal, não uma promessa de reserva** (RN-006).
2. **Cada aceite que bloqueie disponibilidade é revalidado e serializado em um ponto efetivo e comum a criação, aprovação, alteração e manutenção** (RF-008, RN-005; detalhes em ADR-004). Uma consulta prévia, mutex em memória ou validação apenas na interface não satisfazem a invariante.
3. **Mudança crítica e auditoria confirmam ou falham como uma única unidade lógica/transacional** (RF-030, RN-016..RN-018; ADR-006).
4. **Toda autorização ocorre no servidor por identidade, operação, alvo, propriedade e escopo**, inclusive em leitura/acesso direto; privilégio administrativo não remove as regras (RF-002, RN-010, RN-015, RN-022).

## 2. Usuários, escopo e limites

| Ator | Jornada apoiada | Limite de autorização |
|---|---|---|
| Solicitante | Pesquisar recursos, verificar disponibilidade, registrar e acompanhar suas próprias solicitações, pedir alteração/cancelamento quando permitido | Não lê nem altera reserva alheia, não administra e não aprova a própria solicitação restrita |
| Responsável | Consultar fila, decidir solicitação restrita elegível e registrar retirada/devolução | Somente atribuições e permissões explícitas para recurso/ação; escopo institucional detalhado ainda pendente (DP-003) |
| Administrador | Administrar cadastros/acessos/manutenção e consultar reservas, auditoria e relatórios | Não aprova recurso restrito pelo perfil, não ignora validação nem edita/exclui auditoria |

O sistema inclui interface, aplicação, regras de domínio e persistência operacional. Nenhum sistema de identidade, agenda docente, mensageria, e-mail ou outro provedor externo foi confirmado nas fontes. RF-031..RF-033 requerem comunicação, mas não determinam canal: a fronteira lógica de Comunicação permanece interna e substituível; integração externa só será desenhada após DP-009. Não se cria API pública ou acesso anônimo: DP-012 deve esclarecer eventual critério acadêmico antes de se definir contrato.

## 3. Modelo de organização

O C4 níveis 1–3 define um só sistema e seus contêineres; o nível 3 divide o back-end em componentes conceituais. Donos das regras:

| Responsabilidade | Dono lógico da regra |
|---|---|
| Identidade, permissões, propriedade e escopo | Identidade e Autorização |
| Cadastro, situação e atributos do recurso | Catálogo de Recursos / Administração de Cadastros |
| Intervalos, fontes de ocupação e decisão de disponibilidade | Disponibilidade e Integridade |
| Criação, propriedade e transições da reserva | Reservas e Transições |
| Aprovar/rejeitar dentro do escopo | Decisão de Aprovação, coordenando Reservas e Disponibilidade |
| Retirada e devolução como fatos operacionais | Movimentação; não inferem transições ainda não definidas |
| Registro append-only lógico e consulta autorizada | Auditoria |
| Filas, dashboards e agregações com fórmulas aprovadas | Consultas e Relatórios |
| Preparar comunicação pós-confirmação | Comunicação; canal/provedor não selecionado |
| Adaptar o acesso ao armazenamento | Adaptadores de Persistência |

Entrada da Aplicação é a fronteira de entrada; interface não é autoridade de segurança. Casos de uso dependem de contratos internos, não de estruturas de persistência de outros componentes. A direção e o escopo exatos estão em `c4/NIVEL_3_COMPONENTES.md` e ADR-002.

## 4. Reserva, disponibilidade e consistência

Intervalos são semiabertos `[início, fim)`, com `fim > início`; A e B conflitam se `inícioA < fimB E inícioB < fimA` (RN-001/002). `APROVADA` e `EM_USO` ocupam disponibilidade; `REJEITADA`, `CANCELADA` e `CONCLUÍDA` não são reservas futuras ativas (RN-004). **O efeito de `SOLICITADA` permanece aberto (DP-001)** e não pode ser inferido. A definição de estado bloqueante não altera a obrigação de impedir dois aceites conflitantes.

Criação de reserva, aprovação de solicitação, alteração de período/recursos e inserção de manutenção precisam convergir ao mesmo mecanismo transacional de proteção por recurso/intervalo. Dentro do ponto protegido, o sistema deverá:

1. obter a proteção/ordenação da disputa dos recursos envolvidos;
2. reler estado atual da solicitação/reserva e permissões;
3. consultar alocações ocupantes, manutenção, situação cadastral e alocações docentes pertinentes;
4. rejeitar conflito ou gravar a transição/alteração e evento de auditoria juntos;
5. confirmar resultado individual; em conflito, rollback e resposta de negócio segura.

Esse é um requisito de projeto, não uma técnica de banco escolhida. ADR-004 compara bloqueio transacional por recurso, restrição temporal no armazenamento e isolamento serializável/controle otimista. A escolha depende do armazenamento e de DP-001/017/018. Multi-recurso exige adquirir proteções em ordem determinística para reduzir deadlock; sua semântica tudo-ou-nada/aceite parcial permanece pendente DP-013. O mesmo ponto protege a corrida **aprovação de A versus nova solicitação B**: qualquer vencedor obriga o outro a revalidar. A regra de `SOLICITADA` e o estado inicial de recurso comum seguem pendentes; o modelo não escolhe qual solicitação ganha nem inventa transição.

Adicionar manutenção e aprovar/criar/alterar reserva também concorrem no mesmo ponto de integridade. Reserva existente que coincida com manutenção nunca é cancelada ou remanejada automaticamente; destino de compromisso preexistente depende DP-004. Uma pré-consulta pode exibir impacto, mas a gravação tem que revalidar atomicamente.

## 5. Segurança, auditoria, erros e integrações

- **Autorização server-side:** verificar autorização atual por ação e registro, filtrar por escopo antes de devolver dados e não confiar em identificador fornecido pelo cliente. A matriz/atribuições detalhadas aguardam DP-003; identidade e sessão aguardam DP-019. ADR-005.
- **Minimização:** agendas docentes, usuários, justificativas, relatórios e eventos de auditoria retornam somente campos autorizados (RNF-002; DP-011/016).
- **Auditoria:** ator, instante, alvo, ação e antes/depois quando aplicável; evento não é editável/apagável por operação normal. Falha de auditoria não confirma mudança de negócio. Retenção e tratamento de dados aguardam DP-011.
- **Erros:** separar validação, conflito, negação, inexistência, indisponibilidade, falha interna e comunicação. Não expor detalhes internos; consulta falha não é lista vazia nem “livre”; resposta inconclusiva não é sucesso (RNF-003; ADR-008).
- **Comunicação:** evento/resultado de negócio confirmado precede tentativa de comunicar. Falha de canal jamais reverte transação; canal, destino, reenvio e entrega estão pendentes DP-009 (RN-023; ADR-007). Não se presume envio externo.

## 6. Diretrizes de tecnologia e verificação futura

| Tema | Referência considerada | Estado |
|---|---|---|
| Back-end | Java 21, Spring Boot 3.x | Referência técnica do escopo; não equivale a aplicação implementada |
| Persistência | Um mecanismo transacional compartilhado por dados operacionais e auditoria | Recomendação relacional preliminar; produto, versão e ORM pendentes DP-017 |
| Testes | JUnit 5, Testcontainers e WireMock quando integração HTTP for aprovada | Estratégia futura; nenhum teste foi criado/executado |
| Qualidade/CI | JaCoCo, SonarCloud e GitHub Actions | Referências para etapa de implementação; thresholds e configuração não definidos |
| Carga | JMeter | Cenários após definição de carga/limites em DP-014 |
| Interface, sessão e protocolo | Sem escolha | Não obrigar REST, framework de front-end, provedor de identidade ou sessão específica |

Testes futuros propostos, não evidências: unidade para intervalos, política de autorização e transições definidas; integração com armazenamento real para atomicidade/auditoria e corridas; WireMock apenas para adaptador HTTP externo aprovado; verificação de acessos diretos e erros; JMeter após metas. A corrida concorrente deve incluir solicitação/solicitação, aprovação/nova reserva e manutenção/aprovação. JUnit 5/Testcontainers/WireMock/JaCoCo/SonarCloud/GitHub Actions/JMeter são capacidades indicadas, não prova de configuração disponível ou testes realizados. ADR-009.

## 7. Decisões e bloqueios

ADRs-001..009 foram lidos e revisados como propostas, não como decisões aprovadas. Monólito modular é a alternativa recomendada por custo/consistência, mas ainda carece de validação formal. Decisões de negócio que afetam a disponibilidade, transições, escopo e canais precisam ser fechadas pelas autoridades de produto antes da implementação pertinente.

Pendências de produto/técnicas mantidas em `../requisitos/PENDENCIAS_DECISOES.md`: DP-001..DP-019. Em especial: estado `SOLICITADA` (DP-001), escopo de autorização (DP-003), conflito com manutenção (DP-004), transições (DP-006), alteração/cancelamento (DP-007), canal (DP-009), auditoria (DP-011), carga/metas (DP-014), calendário (DP-015), dados docentes (DP-016), armazenamento (DP-017), mecanismo de concorrência (DP-018) e identidade/sessão (DP-019). Não foram criadas regras de negócio novas.

## 8. Referências cruzadas

- C4: [Nível 1 — Contexto](c4/NIVEL_1_CONTEXTO.md), [Nível 2 — Contêineres](c4/NIVEL_2_CONTEINERES.md), [Nível 3 — Componentes](c4/NIVEL_3_COMPONENTES.md).
- UML: [Estados](uml/ESTADOS_RESERVA.md), [sequências Solicitante](uml/sequencia/SOLICITANTE.md), [Responsável](uml/sequencia/RESPONSAVEL.md), [Administrador](uml/sequencia/ADMINISTRADOR.md), [concorrência](uml/sequencia/CONCORRENCIA.md).
- ADRs: `adr/ADR-001.md`..`adr/ADR-009.md`; riscos: [RISCOS_ARQUITETURAIS.md](RISCOS_ARQUITETURAIS.md).
- Rastreamento: `../requisitos/MATRIZ_RASTREABILIDADE.md`; decisões de produto/técnicas: `../requisitos/PENDENCIAS_DECISOES.md`.

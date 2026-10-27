# UML de Sequência — Concorrência de Reservas

## SEQ-CRIT-001 — Aprovação concorrente com nova solicitação

**Objetivo:** documentar a disputa entre uma solicitação restrita pendente A e uma nova solicitação B para o mesmo recurso/período; nenhum par conflitante pode resultar aceito.
**Base:** J-SOL-004, J-RES-003, J-ADM-007; RF-006..RF-009, RF-016, RF-026, RF-030; RN-001..RN-007, RN-011, RN-016; RNF-004/005/007/011.
**Status:** comportamento arquitetural proposto; banco/mecanismo não selecionados. `SOLICITADA` e estado inicial de recurso comum seguem DP-001. O diagrama não decide qual pedido ganha em termos de negócio.

```mermaid
sequenceDiagram
    %% SEQ-CRIT-001 — Aprovação contra nova solicitação
    actor SolA as Solicitante A
    actor SolB as Solicitante B
    actor Resp as Responsável autorizado
    participant WebA as Aplicação Web A
    participant WebB as Aplicação Web B
    participant Entrada as Entrada da Aplicação
    participant Auth as Identidade e Autorização
    participant Decisao as Decisão de Aprovação
    participant Reserva as Reservas e Transições
    participant Integridade as Disponibilidade e Integridade
    participant Store as Persistência transacional
    participant Audit as Auditoria

    Note over SolA,Store: A é SOLICITADA e ainda não aprovada. B pede o mesmo recurso e período
    par Responsável aprova A
        Resp->>WebA: Confirma aprovação de A
        WebA->>Entrada: Envia decisão
        Entrada->>Auth: Autorizar ator, escopo, alvo e ação
        Auth-->>Entrada: Principal e permissão vigentes
        Entrada->>Decisao: Aprovar A
        Decisao->>Integridade: Tentar aceite de A no ponto comum de integridade
        Integridade->>Store: Iniciar unidade atômica e proteger recurso
        Store->>Store: Reler A, estado atual, ocupações, docente e manutenção
    and Solicitante envia B
        SolB->>WebB: Confirma nova solicitação B
        WebB->>Entrada: Envia solicitação
        Entrada->>Auth: Autorizar identidade e recurso
        Auth-->>Entrada: Principal e permissão vigentes
        Entrada->>Reserva: Registrar pedido B conforme política vigente
        Reserva->>Integridade: Verificar/aceitar B no mesmo ponto de integridade
        Integridade->>Store: Obter proteção para o mesmo recurso
        Store->>Store: Reler ocupações e estado atual
    end
    Note over Integridade,Store: A e B não podem validar e gravar em trilhas independentes. Serialização/constraint ainda depende de DP-017/018
    alt A confirma primeiro e o aceite é elegível
        Store->>Store: Gravar APROVADA + auditoria na mesma unidade atômica
        Store-->>Integridade: Commit de A
        Integridade-->>Decisao: A confirmada
        Decisao-->>Entrada: Aprovação confirmada
        Entrada-->>WebA: Estado atual APROVADA
        WebA-->>Resp: Confirma resultado
        Store-->>Integridade: Revalidar B contra A confirmada
        Integridade-->>Reserva: B conflita. Não aceitar em conflito
        Reserva-->>Entrada: Conflito/indisponibilidade de B
        Entrada-->>WebB: Resultado sem sucesso presumido
        WebB-->>SolB: Informa conflito e mantém dados para ajuste
    else B confirma primeiro sob política definida e ocupa disponibilidade
        Store->>Store: Gravar somente o estado permitido de B + auditoria se aplicável
        Store-->>Integridade: Commit de B conforme política
        Integridade-->>Reserva: Resultado confirmado de B
        Reserva-->>Entrada: Estado real de B
        Entrada-->>WebB: Resultado de B confirmado
        WebB-->>SolB: Exibe estado retornado, não inferido
        Store-->>Integridade: Revalidar A contra B confirmada
        Integridade-->>Decisao: A conflita. Não aprovar
        Decisao-->>Entrada: A não aprovada. Destino não presumido (DP-006)
        Entrada-->>WebA: Estado e conflito atualizados
        WebA-->>Resp: Informa que a aprovação não ocorreu
    else Falha técnica ou auditoria antes do commit
        Store-->>Integridade: Rollback/resultado inconclusivo
        Integridade-->>Entrada: Não confirmar operação
        Entrada-->>WebA: Falha segura. Consultar estado atual
        Entrada-->>WebB: Falha segura. Consultar estado atual
    end
```

## Invariante

Para intervalos semiabertos `[início, fim)`, conflito é `inícioA < fimB E inícioB < fimA`; intervalos adjacentes não conflitam. Para qualquer par de alocações aceitas que bloqueiam disponibilidade para o mesmo recurso, não pode haver sobreposição. `APROVADA` e `EM_USO` bloqueiam (RN-004); manutenção também bloqueia novos aceites coincidentes (RN-007). Efeito de `SOLICITADA` ainda não foi decidido. A regra de concorrência assegura exclusão entre aceites bloqueantes sem definir se pedidos pendentes reservam horário.

## Sequência de proteção requerida

Toda criação/aceite, aprovação, alteração de alocação e inclusão de manutenção que concorra pelo mesmo recurso deve:

1. atravessar o mesmo ponto de integridade;
2. validar autorização, versão/estado atual e período;
3. obter proteção transacional pelo recurso (para múltiplos recursos, ordem determinística);
4. reconsultar os ocupantes, manutenção, cadastro e agenda docente relevante dentro da seção protegida;
5. efetivar mudança e auditoria juntas ou falhar sem anunciar sucesso;
6. converter disputa perdida em conflito de negócio; não expor exceções internas.

**Pré-consulta:** útil para UX, mas não adquire o direito de reservar nem elimina a revalidação no instante da gravação.

## Variações de ordem e questões pendentes

- **Duas novas solicitações simultâneas:** ambas devem passar pelo mesmo ponto protegido. Se a política aprovada resultar em duas aceitações bloqueantes para períodos sobrepostos, somente uma pode confirmar; a segunda relê o vencedor e recebe conflito. Se a política produzir solicitações `SOLICITADA` não bloqueantes, a concorrência entre pendências e a decisão final continuam sujeitas a DP-001, e nenhuma dupla aprovação futura pode contornar a proteção.
- **Aprovação A vence:** commit `SOLICITADA → APROVADA` + auditoria; B revalida e é impedida se conflitante.
- **B vence:** somente se a política do pedido B resultar em estado que bloqueie disponibilidade; aprovação A revalida e não é aceita. O estado final de A se perder disponibilidade permanece DP-006.
- **DP-001: `SOLICITADA` bloqueia?** Se sim, B pode ser impedida enquanto A aguarda, retendo horário; se não, B ainda pode disputar o aceite final. Nenhuma alternativa é selecionada.
- **Manutenção versus aprovação/reserva:** mesma proteção; após manutenção efetivada, novos usos conflitantes são impedidos. Compromisso pré-existente não é cancelado/remanejado; política DP-004.
- **Vários recursos:** atomicidade tudo-ou-nada versus parcial pendente DP-013; locks, se usados, ordenados deterministicamente para reduzir deadlock.
- **Falha/retry:** estado de retorno deve ser distinguível de conflito e sucesso; repetição segura/idempotência e reconciliação de operação inconclusiva precisam ser definidas na seleção técnica (DP-014/017/018).

## Alternativas de mecanismo

| Alternativa | Benefícios | Limitações e riscos | Evidência futura |
|---|---|---|---|
| Bloqueio transacional por recurso | Ordena disputa e permite revalidar perto do commit | Dependente do motor; contenção, timeout e deadlock com vários recursos; mutex de processo não serve | Corridas reais com transações em Testcontainers e mesma chave de recurso |
| Restrição temporal/exclusion do SGBD | Defesa no limite do armazenamento; dificulta bypass por outra escrita | Suporte/modelagem variam; estados bloqueantes e exceções devem estar representados | Testar constraint, adjacência e mapeamento de conflito por SGBD real |
| Serializável/controle otimista e conflito/retry | Detecta atualização concorrente; paralelismo para períodos independentes conforme motor | Abortos, política de retry/idempotência e custo sob carga | Teste de concorrência e carga no motor escolhido |
| Consulta e gravação sem guarda atômica | Nenhum benefício que compense | Rejeitada: aceita race condition e viola RF-008 | Não aplicável |

O mecanismo físico é decisão técnica posterior, não uma regra de negócio. ADR-004 contém a proposta e os critérios de decisão; ADR-003 registra a dependência do SGBD. Nenhuma evidência acima foi executada.

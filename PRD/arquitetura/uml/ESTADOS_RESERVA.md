# UML — Ciclo de Estados da Reserva

**Objetivo:** Distinguir transicoes de negocio confirmadas das que ainda aguardam decisao.  
**Fonte:** RN-011..RN-014, RF-009, RF-016, RF-017, RF-019, RF-020 e DP-006/DP-007.  
**Status:** Modelo parcial; transicoes pendentes nao sao autorizadas por este diagrama.

```mermaid
stateDiagram-v2
    title Estados da reserva — transicoes especificadas
    [*] --> SOLICITADA: Recurso restrito; pedido validado
    SOLICITADA --> APROVADA: Responsavel autorizado; disponibilidade revalidada
    SOLICITADA --> REJEITADA: Responsavel autorizado; justificativa registrada

    state EM_USO
    state CONCLUIDA
    state CANCELADA

    note right of SOLICITADA
      Estado inicial de recurso comum e efeito de SOLICITADA na disponibilidade
      estao pendentes. Ver DP-001.
    end note
    note right of APROVADA
      A transicao para EM_USO e sua condicao/ator nao foram definidos.
      Ver DP-006.
    end note
    note right of CANCELADA
      Estados que permitem cancelamento e transicao para CANCELADA
      dependem de DP-007.
    end note
    note right of REJEITADA
      Estado terminal para a decisao registrada; redecisao nao autorizada.
    end note
```

## Transicoes e atores

| Transicao | Status | Quem pode executar | Pre-condicoes/validacoes | Efeito e auditoria |
|---|---|---|---|---|
| Criacao -> `SOLICITADA` (recurso restrito) | Especificada | Solicitante cria; sistema valida | Autenticacao, pedido completo conforme campos a definir, periodo valido, recurso identificavel | Registra pedido; deve auditar a mudanca de estado. |
| `SOLICITADA` -> `APROVADA` | Especificada para restrito | Responsavel autorizado ao recurso | Estado ainda `SOLICITADA`; escopo valido; recurso/docente disponivel; sem manutencao/conflito; revalidar na decisao | Registra autor, instante, alvo, operacao e estado anterior/novo. |
| `SOLICITADA` -> `REJEITADA` | Especificada | Responsavel autorizado ao recurso | Estado ainda pendente; justificativa registrada; escopo autorizado | Registra decisao/justificativa e auditoria. |
| `APROVADA` -> `EM_USO` | Pendente (DP-006) | Ator nao especificado | Evento/criterio de inicio nao definido; retirada pode ocorrer mas nao implica transicao automaticamente | Nao executar transicao ate validacao da regra; se aprovada, auditar. |
| `EM_USO` -> `CONCLUIDA` | Pendente (DP-006) | Ator nao especificado | Relacao com devolucao e fim da atividade nao definida | Nao executar transicao ate validacao; se aprovada, auditar. |
| Estado elegivel -> `CANCELADA` | Pendente (DP-007) | Solicitante pode pedir propria reserva; ator final/estados elegiveis dependem da regra | Verificar propriedade, estado mais recente, prazo e politica definidos; reserva iniciada nao pode ser apagada | Quando autorizada, muda estado sem apagar reserva e audita anterior/novo. |
| `REJEITADA` ou `CANCELADA` -> outro estado | Nao permitido pela documentacao atual | Nenhum ator autorizado | Transicao de reabertura/reversao nao definida | Negar; preservar historico. |
| Reserva iniciada -> exclusao | Proibida | Ninguem | Reserva iniciada nao pode ser apagada | Preservar registro; correcoes devem ser nova acao rastreavel conforme politica. |

## Transicoes invalidas conhecidas

- Solicitante aprovar a propria solicitacao ou decisao por responsavel fora do escopo.
- Aprovar/rejeitar item ja resolvido, ou aprovar quando recurso/docente esta indisponivel, em manutencao ou conflitante.
- Pular estado ou fazer transicao sem ator/pre-condicao definidos; a implementacao nao deve inferir `APROVADA -> EM_USO` nem `EM_USO -> CONCLUIDA`.
- Apagar reserva iniciada ou editar/excluir evento de auditoria como operacao comum.
- Aceitar dois estados de reserva que ocupem o mesmo recurso em intervalos sobrepostos.

As pendencias DP-001, DP-006 e DP-007 devem ser resolvidas antes de fechar o modelo e gerar casos de teste de todas as transicoes. A regra de disponibilidade por estado esta em RN-004.
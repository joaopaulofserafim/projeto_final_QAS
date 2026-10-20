# UML de Sequencia — Concorrencia de Reservas

## SEQ-CRIT-001 — Duas solicitacoes para o mesmo recurso/periodo

**Objetivo:** demonstrar que consultas simultaneas podem ver disponibilidade anterior, mas dois aceites conflitantes nunca sao confirmados (RF-008, RN-002/RN-005, RNF-004).  
**Cenario:** Solicitante A e Solicitante B tentam Sala 101, 20/11, 14:00–16:00. A disponibilidade consultada antes do envio estava livre.  
**Limite:** `Ponto de integridade` e fronteira conceitual, nao representa tecnologia selecionada. Estados que ocupam disponibilidade e efeito de `SOLICITADA` dependem DP-001.

```mermaid
sequenceDiagram
    title SEQ-CRIT-001 — Solicitacoes concorrentes
    actor A as Solicitante A
    actor B as Solicitante B
    actor Resp as Responsavel autorizado
    participant WebA as Aplicacao Web A
    participant WebB as Aplicacao Web B
    participant Entrada as Entrada da Aplicacao
    participant Auth as Identidade e Autorizacao
    participant Reservas as Reservas e Transicoes
    participant Decisao as Decisao de Aprovacao
    participant Disp as Disponibilidade e Integridade
    participant Persistencia as Persistencia transacional
    participant Auditoria as Auditoria

    par Consulta anterior de A
        A->>WebA: Consulta Sala 101, 14:00-16:00
        WebA->>Entrada: Consultar disponibilidade
        Entrada->>Disp: Avaliar periodo
        Disp->>Persistencia: Ler ocupacao atual
        Persistencia-->>Disp: Livre naquele instante
        Disp-->>WebA: Disponivel (nao reservado)
        WebA-->>A: Apresenta resultado informativo
    and Consulta anterior de B
        B->>WebB: Consulta Sala 101, 14:00-16:00
        WebB->>Entrada: Consultar disponibilidade
        Entrada->>Disp: Avaliar periodo
        Disp->>Persistencia: Ler ocupacao atual
        Persistencia-->>Disp: Livre naquele instante
        Disp-->>WebB: Disponivel (nao reservado)
        WebB-->>B: Apresenta resultado informativo
    end

    par Confirmacao concorrente de A
        A->>WebA: Confirma solicitacao
        WebA->>Entrada: Envia pedido A
        Entrada->>Auth: Validar identidade/permissao A
        Entrada->>Reservas: Solicitar aceite A
        Reservas->>Disp: Revalidar periodo, recurso e estados ocupantes
        Disp->>Persistencia: Ponto atomico: tentar aceitar A
    and Confirmacao concorrente de B
        B->>WebB: Confirma solicitacao
        WebB->>Entrada: Envia pedido B
        Entrada->>Auth: Validar identidade/permissao B
        Entrada->>Reservas: Solicitar aceite B
        Reservas->>Disp: Revalidar periodo, recurso e estados ocupantes
        Disp->>Persistencia: Ponto atomico: tentar aceitar B
    end

    alt A vence o ponto atomico
        Persistencia->>Persistencia: Validar ocupacao + gravar aceite A e auditoria na mesma unidade
        Persistencia-->>Disp: Commit A; A aceito
        Disp-->>Reservas: Sucesso A
        Reservas-->>WebA: Reserva/estado confirmado
        WebA-->>A: Confirma aceite A
        Disp->>Persistencia: Revalidar B contra A ja confirmada
        Persistencia-->>Disp: B conflita
        Disp-->>Reservas: B conflita; nao aceitar
        Reservas-->>WebB: Indisponibilidade atual
        WebB-->>B: Informa conflito e sugere novo periodo/recurso
    else B vence o ponto atomico
        Persistencia->>Persistencia: Validar ocupacao + gravar aceite B e auditoria na mesma unidade
        Persistencia-->>Disp: Commit B; B aceito
        Disp-->>Reservas: Sucesso B
        Reservas-->>WebB: Reserva/estado confirmado
        WebB-->>B: Confirma aceite B
        Disp->>Persistencia: Revalidar A contra B ja confirmada
        Persistencia-->>Disp: A conflita
        Disp-->>Reservas: A conflita; nao aceitar
        Reservas-->>WebA: Indisponibilidade atual
        WebA-->>A: Informa conflito e sugere novo periodo/recurso
    end

    Note over A,Resp: Variante adicional do SEQ-CRIT-001: A ja possui pedido restrito SOLICITADA
    alt DP-001 define SOLICITADA como bloqueante
        B->>WebB: Tenta nova solicitacao para mesmo recurso/periodo
        WebB->>Entrada: Envia pedido B
        Entrada->>Disp: Revalidar ocupacao incluindo SOLICITADA
        Disp-->>Entrada: B indisponivel pela politica aprovada
        Entrada-->>WebB: Conflito/indisponibilidade
        WebB-->>B: Informa que o recurso aguarda decisao
        Resp->>WebA: Decide pedido A
        WebA->>Entrada: Solicita aprovacao
        Entrada->>Decisao: Validar escopo, estado e disponibilidade
        Decisao->>Disp: Revalidar e tentar confirmar A com Persistencia atomica
        Disp->>Persistencia: Gravar A + auditoria sob guarda de integridade
        Persistencia-->>Decisao: A aprovada ou conflito atualizado
    else DP-001 nao define SOLICITADA como bloqueante
        par Responsavel aprova A
            Resp->>WebA: Aprova solicitacao A
            WebA->>Entrada: Envia aprovacao
            Entrada->>Decisao: Revalidar autorizacao e SOLICITADA
            Decisao->>Disp: Revalidar e tentar aceitar A atomicamente
        and Solicitante B cria novo pedido
            B->>WebB: Confirma novo pedido B
            WebB->>Entrada: Envia solicitacao
            Entrada->>Reservas: Revalidar e solicitar aceite B
            Reservas->>Disp: Revalidar e tentar aceitar B atomicamente
        end
        alt A vence
            Disp->>Persistencia: Gravar APROVADA A + auditoria sob guarda atomica
            Persistencia-->>Decisao: A confirmada
            Persistencia-->>Reservas: B conflita; nao aceitar
            Reservas-->>WebB: Indisponibilidade atual
            WebB-->>B: Informa conflito
        else B vence conforme politica de recurso comum
            Disp->>Persistencia: Gravar B + auditoria sob guarda atomica
            Persistencia-->>Reservas: B confirmada
            Persistencia-->>Decisao: A conflita; nao aprovar
            Decisao-->>WebA: A permanece sem aceite; destino depende DP-006
            WebA-->>Resp: Informa conflito e estado atualizado
        end
    end
```

## Invariante e condicao de conflito

Para recurso R e periodos semiabertos `[inicio, fim)`, duas alocacoes sao conflitantes quando `inicioA < fimB E inicioB < fimA`. Alocacoes adjacentes nao conflitam. O estado final deve satisfazer: para qualquer par de reservas aceitas do mesmo recurso, nenhum par conflita. Ambas as confirmacoes podem receber sucesso somente quando os recursos/intervalos nao se sobrepoem ou uma regra institucional aprovada tornar um deles nao bloqueante; no mesmo recurso/horario, exatamente uma pode ser aceita.

Participam da verificacao os estados que RN-004 define como ocupantes (`APROVADA`, `EM_USO`), manutencao e professor alocado. A participacao de `SOLICITADA` ainda depende DP-001 e nao deve ser suposta. A segunda operacao, ao perder a disputa, recebe conflito/indisponibilidade e nao recebe uma reserva aceita. Mudanca de estado e auditoria confirmada devem pertencer a mesma unidade de integridade.

## Variante critica — Aprovacao pendente disputa com nova solicitacao

Uma solicitacao restrita A permanece `SOLICITADA`; uma nova solicitacao B para recurso/periodo igual concorre com a aprovacao de A. Ambas chegam a `Disponibilidade e Integridade` e tentam aceitar no mesmo `Ponto atomico de integridade`:

- Se a aprovacao de A confirmar primeiro, gravar `APROVADA` + auditoria; a confirmacao de B revalida contra A e e recusada como conflito.
- Se a solicitacao B confirmar primeiro segundo a politica de recurso comum, a aprovacao de A revalida e nao pode produzir aceite conflitante; A recebe indisponibilidade e seu destino (permanece `SOLICITADA` ou outro estado autorizado) aguarda DP-006.
- Se DP-001 decidir que `SOLICITADA` bloqueia disponibilidade, B pode ser recusada desde o envio enquanto A aguarda; isso altera fila/experiencia e precisa estar registrado como regra aprovada.

Em qualquer ramo, no maximo uma reserva ocupante e aceita. Resultado de aprovacao nunca e repetido se o estado ja tiver sido resolvido (RN-011). Mudancas sao auditadas; notificacao e pos-confirmacao e nao altera o resultado.

## Alternativas de integridade

| Alternativa | Vantagens | Limitacoes/riscos | Evidencia necessaria |
|---|---|---|---|
| Bloqueio transacional por recurso/periodo | Serializa disputa e torna a leitura/revalidacao proxima do aceite explicita; conceitualmente simples. | Granularidade e semantica dependem do SGBD; bloqueio muito amplo reduz throughput; ordenacao de multi-recursos pode causar deadlock; retentativas precisam ser tratadas. Um mutex apenas em memoria nao protege multiplas instancias. | Teste de integracao com duas transacoes reais e corrida repetida no SGBD escolhido; verificar deadlock/timeout e mensagem de perdedor. |
| Restricao de integridade temporal no armazenamento | Defesa final proxima dos dados; impede que outro caminho de escrita burle a regra. | Suporte para intervalo/exclusion varia por SGBD; modelo de estados bloqueantes e consultas de intervalo podem nao caber em restricao simples; rejeicao deve ser convertida em conflito de negocio. | Testcontainer do banco escolhido verificando sobreposicao, adjacencia, estados, manutencao e erro de constraint convertido em resposta segura. |
| Isolamento serializavel ou controle otimista com conflito/retry | Pode detectar leituras concorrentes sem lock manual amplo; preserva concorrencia para periodos independentes quando suportado. | Pode abortar transacoes sob carga; retry e idempotencia complicam casos de uso; sem repeticao segura a interface pode receber erro transitario. Semantica/performance variam por SGBD. | Teste de concorrencia e carga com banco real, incluindo abort/retry e assertiva de no maximo um aceite. |
| Consulta simples seguida de gravacao sem protecao atomica | Facil de implementar, mas duas leituras veem livre e ambas podem gravar. | Viola RF-008/RN-005; inaceitavel. | Rejeitada. |

## Proposta e decisao

Requisito arquitetural proposto: a criacao/aprovacao deve ter um ponto de confirmacao atomico que revalida estados ocupantes e nao permite dois aceites conflitantes, e deve acoplar auditoria da transicao ao mesmo resultado confirmado. Implementar apenas um `if` de disponibilidade na aplicacao ou mutex local nao atende a invariante.

Nao e possivel aprovar uma tecnica final antes de escolher persistencia e resolver DP-001/DP-017/DP-018. Recomenda-se selecionar entre restricao temporal e bloqueio/serializacao suportados pelo banco, possivelmente em defesa combinada, apos prova de integracao. ADR-004 fica **Pendente**; qualquer alternativa exige validacao de arquitetura e stakeholders, e evidencia real com Testcontainers. Nao se prescreve banco, ORM ou detalhe de isolamento.

**Requisitos:** RF-006..RF-009, RF-016, RF-026, RF-030; RN-001..RN-007, RN-011, RN-016; RNF-004, RNF-005, RNF-007, RNF-011, RNF-013. **Pendencias:** DP-001, DP-004, DP-006, DP-014, DP-017, DP-018.
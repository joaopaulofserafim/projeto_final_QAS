# C4 — Nível 3: Componentes

**Objetivo:** detalhar componentes lógicos dentro dos contêineres Web e Aplicação e seus contratos conceituais.
**Limite:** componentes não são processos, serviços implantáveis, classes, tabelas ou endpoints. Adaptadores de persistência acessam o contêiner de dados; os componentes de domínio não acessam diretamente seu esquema.
**Status:** proposta revisada; não implementada.

```mermaid
C4Component
    title Componentes — Aplicação Web e Aplicação

    Container_Boundary(web, "Aplicação Web (tecnologia pendente)") {
        Component(uiConsultas, "Consultas e filtros", "Interface", "Pesquisa recursos, disponibilidade, filas, reservas e consultas.")
        Component(uiOperacoes, "Formulários e ações", "Interface", "Envia solicitação, decisão, manutenção e movimentação.")
        Component(uiFeedback, "Apresentação de resultados", "Interface", "Distingue sucesso, vazio, conflito, negação e falha.")
        Component(uiClient, "Adaptador de interação", "Interface", "Encaminha operações e interpreta retorno. Não é autoridade de segurança.")
    }

    Container_Boundary(app, "Aplicação — Java 21 / Spring Boot 3.x (referência)") {
        Component(entrada, "Entrada da Aplicação", "Componente", "Valida forma da entrada, invoca casos de uso e normaliza resultados/erros.")
        Component(acesso, "Identidade e Autorização", "Componente", "Resolve principal e valida permissão, propriedade, escopo e alvo em cada operação.")
        Component(catalogo, "Catálogo de Recursos", "Componente", "Consulta identidade, atributos e situação de recursos e docentes.")
        Component(admin, "Administração de Cadastros", "Componente", "Coordena alterações de usuários, permissões, cadastro e situação.")
        Component(dispon, "Disponibilidade e Integridade", "Componente de domínio", "Aplica intervalo, reúne ocupações e coordena ponto atômico comum de aceite/bloqueio.")
        Component(reservas, "Reservas e Transições", "Componente de domínio", "Dono do ciclo conhecido. Cria, altera e transiciona somente estados autorizados.")
        Component(decisao, "Decisão de Aprovação", "Caso de uso", "Coordena autorização, escopo, estado atual, revalidação e decisão restrita.")
        Component(mov, "Movimentação", "Componente", "Registra retirada/devolução vinculadas. Não infere transição da reserva.")
        Component(auditoria, "Auditoria", "Componente", "Registra evento com a mudança na mesma unidade lógica e oferece leitura autorizada.")
        Component(consultas, "Consultas e Relatórios", "Componente", "Prepara consultas, filas e métricas segundo fórmulas aprovadas.")
        Component(comunicacao, "Comunicação", "Porta/adaptador interno", "Produz atualização após confirmação. Canal externo não selecionado.")
        Component(persistencia, "Adaptadores de Persistência", "Adaptador", "Implementa contratos de leitura/escrita e unidade transacional.")
    }

    ContainerDb(store, "Persistência transacional", "SGBD pendente", "Estado operacional e eventos auditáveis.")

    Rel(uiConsultas, uiClient, "Submete critérios")
    Rel(uiOperacoes, uiClient, "Submete ação")
    Rel(uiClient, entrada, "Invoca operação pelo contrato ainda não selecionado")
    Rel(entrada, acesso, "Valida identidade e autorização server-side")
    Rel(entrada, catalogo, "Consulta dados de recurso")
    Rel(entrada, admin, "Executa administração autorizada")
    Rel(entrada, reservas, "Cria/consulta/altera/cancela conforme elegibilidade")
    Rel(entrada, decisao, "Solicita aprovação/rejeição")
    Rel(entrada, mov, "Solicita retirada/devolução")
    Rel(entrada, consultas, "Consulta filas, auditoria e indicadores")
    Rel(entrada, uiFeedback, "Devolve resultado seguro")
    Rel(reservas, dispon, "Revalida período e ocupações")
    Rel(decisao, reservas, "Solicita transição de domínio")
    Rel(decisao, dispon, "Revalida no mesmo ponto de integridade")
    Rel(admin, catalogo, "Atualiza situação/cadastro por contrato")
    Rel(admin, dispon, "Solicita avaliação/registro de manutenção")
    Rel(mov, reservas, "Consulta elegibilidade. Não presume transição")
    Rel(reservas, auditoria, "Inclui evento com mudança")
    Rel(decisao, auditoria, "Inclui decisão com mudança")
    Rel(mov, auditoria, "Inclui fato de custódia")
    Rel(admin, auditoria, "Inclui alteração administrativa")
    Rel(catalogo, persistencia, "Consulta/grava por contrato")
    Rel(admin, persistencia, "Consulta/grava no limite transacional")
    Rel(dispon, persistencia, "Lê e protege disputa no aceite")
    Rel(reservas, persistencia, "Grava estado sob proteção")
    Rel(mov, persistencia, "Grava movimentação")
    Rel(auditoria, persistencia, "Persiste evento junto da operação")
    Rel(consultas, persistencia, "Lê dados/fatos agregáveis")
    Rel(persistencia, store, "Leitura e gravação transacionais")
    Rel(reservas, comunicacao, "Publica resultado confirmado")
    Rel(decisao, comunicacao, "Publica decisão confirmada")
    Rel(comunicacao, entrada, "Retorna resultado de comunicação sem alterar o resultado de negócio")
```

## Responsabilidades e dependências

- **Entrada da Aplicação** recebe operações; converte resultado de domínio para retorno seguro. Não substitui autorização e não acopla domínio a protocolo.
- **Identidade e Autorização** valida no servidor autenticação, perfil, permissão específica, propriedade e escopo para leitura ou escrita. Ocultar ação na Web é apenas UX. Matriz e provedor aguardam DP-003/DP-019.
- **Catálogo e Administração** tratam informações de recurso e alterações cadastrais; impactos existentes são consultados e não cancelados automaticamente (DP-005).
- **Disponibilidade e Integridade** é o único dono do predicado temporal e da orquestração do ponto efetivo de conflito. Atende solicitações, decisões, alterações e manutenção pelo mesmo mecanismo atômico; consultar antes não basta. A técnica concreta aguarda ADR-004/DP-017/018.
- **Reservas e Transições** é o único dono das transições persistidas e da elegibilidade; estados não especificados permanecem proibidos até decisão DP-006/007. Decisão de Aprovação não grava estado por acesso lateral.
- **Movimentação** mantém fato de retirada/devolução vinculado a reserva e recurso; sua persistência/auditoria pode ser atômica, mas não conclui reserva sem regra aprovada.
- **Auditoria** registra ator, instante, alvo, operação e antes/depois aplicáveis, com escrita inseparável da mudança crítica; a consulta é somente leitura e filtrada.
- **Consultas e Relatórios** lê apenas; falha não vira vazio/zero, e métrica sem definição/fonte não é calculada (DP-010).
- **Comunicação** é interna/abstrata, não implica sistema externo. Deve operar depois do commit e falha não desfaz negócio (DP-009).
- **Persistência** é adaptada por contratos internos. Um único armazenamento transacional é recomendado inicialmente; motor, ORM e garantias temporais aguardam DP-017.

## Coerência com demais níveis

Todos os componentes acima residem no único contêiner Aplicação do nível 2. Aplicação Web e Persistência transacional são contêineres; módulos de autenticação, reserva, disponibilidade, relatório, auditoria e comunicação são **componentes**, não contêineres. Não há integração externa assumida.
**Requisitos:** RF-001..RF-036; RN-001..RN-024; RNF-001..RNF-014.
**ADRs:** ADR-001, ADR-002, ADR-004..ADR-009.
**Pendências:** DP-001, DP-003..DP-011, DP-013..DP-019.

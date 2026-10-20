# C4 — Nivel 3: Componentes

**Objetivo:** Detalhar componentes conceituais dos contêineres Web e Back-end e suas responsabilidades/dependencias.  
**Limite:** Elementos sao componentes logicos no processo. Nao representam classes, pacotes fisicos, tabelas ou operacoes HTTP especificas.  
**Status:** Proposta para revisao.

```mermaid
C4Component
    title Componentes — Aplicacao Web e Aplicacao Back-end

    Container_Boundary(web, "Aplicacao Web (framework pendente)") {
        Component(navegacao, "Navegacao por perfil", "UI", "Apresenta funcoes pertinentes ao perfil; nao substitui autorizacao.")
        Component(consultas, "Consultas e filtros", "UI", "Pesquisa recursos, disponibilidade, reservas, filas, auditoria e relatorios.")
        Component(formularios, "Formularios de operacao", "UI", "Captura solicitacoes, decisoes, manutencao e movimentacoes.")
        Component(feedback, "Estados e feedback", "UI", "Apresenta carregando, vazio, conflito, negacao, sucesso e falha.")
        Component(client, "Adaptador de chamadas", "UI", "Encaminha operacoes e interpreta resultados; contrato ainda pendente.")
    }

    Container_Boundary(back, "Aplicacao Back-end (Java 21, Spring Boot 3.x)") {
        Component(entrada, "Entrada da Aplicacao", "Adaptador", "Recebe operacoes, valida forma, encaminha casos de uso e normaliza erros/retornos.")
        Component(acesso, "Identidade e Autorizacao", "Modulo", "Autentica e aplica perfil, propriedade, permissao e escopo por acao.")
        Component(catalogo, "Catalogo de Recursos", "Modulo", "Consulta caracteristicas, competencias e situacao dos recursos.")
        Component(admin, "Administracao de Cadastros", "Modulo", "Gerencia contas, permissoes e cadastros com validacao e rastreio.")
        Component(dispon, "Disponibilidade e Integridade", "Dominio", "Calcula intervalos, conflitos, manutencao e ponto atomico de aceite.")
        Component(reservas, "Reservas e Transicoes", "Dominio", "Cria, consulta e valida alteracoes/transicoes permitidas de reservas.")
        Component(decisao, "Decisao de Aprovacao", "Caso de uso", "Filtra escopo, revalida solicitacao restrita e coordena aprovar/rejeitar.")
        Component(movimento, "Movimentacao", "Dominio/caso de uso", "Registra retirada/devolucao vinculadas e pendencias.")
        Component(relatorios, "Consultas e Relatorios", "Modulo de consulta", "Prepara filas, dashboard, auditoria consultavel e indicadores definidos.")
        Component(auditoria, "Auditoria", "Modulo", "Registra ator/instante/alvo/operacao/antes/depois; consulta e somente leitura.")
        Component(comunicacao, "Comunicacao", "Adaptador/porta", "Prepara confirmacao/avisos depois do commit; canal externo condicional.")
        Component(persistencia, "Adaptadores de Persistencia", "Portas e adaptadores", "Implementa acesso ao armazenamento e operacoes transacionais.")
    }

    ContainerDb(dados, "Persistencia transacional", "SGBD pendente", "Dados operacionais e eventos auditaveis.")
    System_Ext(canal, "Canal de notificacao (se aprovado)", "Provedor pendente.")

    Rel(navegacao, client, "Inicia operacao")
    Rel(consultas, client, "Envia filtros")
    Rel(formularios, client, "Envia comando e dados")
    Rel(client, entrada, "Solicita operacao; contrato pendente")
    Rel(entrada, acesso, "Valida principal e autorizacao")
    Rel(entrada, catalogo, "Consulta recursos")
    Rel(entrada, admin, "Executa administracao autorizada")
    Rel(entrada, reservas, "Solicita operacao de reserva")
    Rel(entrada, decisao, "Solicita decisao")
    Rel(entrada, movimento, "Solicita retirada/devolucao")
    Rel(entrada, relatorios, "Consulta filas, auditoria e indicadores")
    Rel(reservas, dispon, "Revalida periodo e disponibilidade")
    Rel(decisao, reservas, "Solicita transicao validada")
    Rel(decisao, dispon, "Revalida conflito no momento da decisao")
    Rel(movimento, reservas, "Consulta elegibilidade; transicao pendente quando indefinida")
    Rel(admin, dispon, "Atualiza manutencao e consulta impacto")
    Rel(reservas, auditoria, "Registra transicoes na mesma unidade de negocio")
    Rel(decisao, auditoria, "Registra decisao e justificativa")
    Rel(movimento, auditoria, "Registra eventos de custodia")
    Rel(admin, auditoria, "Registra mudancas administrativas")
    Rel(reservas, persistencia, "Le/grava dentro da operacao atomica")
    Rel(dispon, persistencia, "Consulta e protege conflito no aceite")
    Rel(auditoria, persistencia, "Grava evento ligado a operacao")
    Rel(catalogo, persistencia, "Consulta dados autorizados")
    Rel(admin, persistencia, "Grava cadastro e permissao")
    Rel(movimento, persistencia, "Grava retirada/devolucao")
    Rel(relatorios, persistencia, "Consulta dados/fatos agregaveis")
    Rel(persistencia, dados, "Leitura/escrita transacional")
    Rel(reservas, comunicacao, "Publica resultado confirmado")
    Rel(decisao, comunicacao, "Publica aprovacao/rejeicao")
    Rel(comunicacao, canal, "Entrega, se canal selecionado")
    Rel(entrada, feedback, "Retorna resultado/erro")
```

## Dependencias e regras de organizacao

- `Entrada da Aplicacao` e o limite de entrada; identidade/permissao e reavaliada em toda operacao, inclusive leitura.
- `Reservas e Transicoes` e `Disponibilidade e Integridade` sao autoridade unica para invariantes temporais e ciclo de reserva. A interface e os demais modulos nao duplicam a regra de sobreposicao.
- `Decisao de Aprovacao` coordena a transicao em `Reservas e Transicoes`; nao altera diretamente estado persistido.
- `Movimentacao` registra fatos de custodia. Ela nao promove automaticamente reserva a `EM_USO` nem `CONCLUIDA` ate DP-006 ser decidida.
- `Auditoria` e chamada pela mesma operacao de negocio antes de confirmar; a consistencia atomica com a mudanca depende da persistencia escolhida (ADR-006).
- `Adaptadores de Persistencia` implementam contratos usados pelos casos de uso/dominio; componentes nao consultam estruturas internas de outros modulos.
- `Comunicacao` ocorre apos confirmacao de negocio. Falha nao reverte estado; durabilidade/reenvio requer escolha pendente (ADR-007).
- Dependencias entre modulos sao unidirecionais e por contrato. O diagrama nao obriga nomes de classes nem organizacao de pacotes.

**Requisitos relacionados:** RF-001..RF-036; RN-001..RN-024; RNF-001..RNF-014.  
**ADRs:** ADR-001, ADR-002, ADR-005, ADR-006, ADR-007, ADR-008 e ADR-009.  
**Pendencias:** DP-001, DP-003, DP-004, DP-006, DP-009, DP-011, DP-016, DP-017 e DP-018.
# UML de Sequencia — Administrador

Participantes refletem componentes de [C4 nivel 3](../../c4/NIVEL_3_COMPONENTES.md). O privilegio administrativo nao substitui regra de negocio ou auditoria; administrador nao recebe aprovacao implicita de recurso restrito.

## SEQ-ADM-001 — Autenticacao administrativa

**Objetivo:** autenticar a administradora e validar permissoes administrativas (J-ADM-001/RF-001/RF-002/RNF-001).  
**Limite:** autenticacao e autorizacao sao verificadas no servidor; provedor de identidade pendente DP-019.

```mermaid
sequenceDiagram
    %% SEQ-ADM-001 — Autenticacao administrativa
    actor Adm as Administrador
    participant Web as Aplicacao Web
    participant Entrada as Entrada da Aplicacao
    participant Auth as Identidade e Autorizacao
    participant Persistencia as Adaptadores de Persistencia

    Adm->>Web: Informa credenciais
    Web->>Entrada: Solicita autenticacao
    Entrada->>Auth: Validar identidade, estado e perfil administrativo
    Auth->>Persistencia: Consultar identidade/permissoes vigentes
    Persistencia-->>Auth: Conta e perfil
    alt Conta e permissao administrativas validas
        Auth-->>Entrada: Contexto administrativo autorizado
        Entrada-->>Web: Acesso confirmado
        Web-->>Adm: Apresenta painel administrativo permitido
    else Credencial invalida ou sem permissao
        Auth-->>Entrada: Negar sem expor detalhe sensivel
        Entrada-->>Web: Acesso negado
        Web-->>Adm: Apresenta mensagem segura
    end
    Adm->>Web: Encerra sessao
    Web->>Entrada: Solicita encerramento
    Entrada->>Auth: Revogar contexto
    Auth-->>Entrada: Confirmacao ou falha
    Entrada-->>Web: Resultado
    Web-->>Adm: Limpa dados privados quando confirmado
```

**Responsabilidades:** Web apresenta entrada/painel; Entrada coordena; Identidade e Autorizacao verifica perfil/permissoes; persistencia fornece identidade e atribuicoes. A sessao/provedor final depende de DP-019.  
**Requisitos:** RF-001..RF-003; RN-015, RN-022; RNF-001..RNF-003.

## SEQ-ADM-002 — Gerenciamento de usuarios

**Objetivo:** cadastrar, consultar, editar, bloquear/desbloquear e alterar permissoes com controle e trilha (J-ADM-003/RF-022).  
**Limite:** matriz fina, unicidade e impacto em reservas ativas dependem DP-003/DP-005.

```mermaid
sequenceDiagram
    %% SEQ-ADM-002 — Gerenciamento de usuarios
    actor Adm as Administrador
    participant Web as Aplicacao Web
    participant Entrada as Entrada da Aplicacao
    participant Auth as Identidade e Autorizacao
    participant Admin as Administracao de Cadastros
    participant Persistencia as Adaptadores de Persistencia
    participant Auditoria as Auditoria

    Adm->>Web: Pesquisa usuario ou escolhe cadastrar/editar/bloquear/permissao
    Web->>Entrada: Envia consulta/comando
    Entrada->>Auth: Verificar permissao administrativa e alvo
    alt Sem permissao
        Auth-->>Entrada: Negado
        Entrada-->>Web: Acesso negado
        Web-->>Adm: Informa negacao sem alterar dados
    else Autorizado
        Auth-->>Entrada: Permitido
        Entrada->>Admin: Validar campos e tipo de operacao
        Admin->>Persistencia: Consultar usuario, unicidade e reservas ativas
        Persistencia-->>Admin: Cadastro e impactos
        alt Consulta, cadastro duplicado/campos invalidos
            Admin-->>Entrada: Detalhe, lista ou erro de validacao
            Entrada-->>Web: Resultado sem mutacao indevida
            Web-->>Adm: Mostra cadastro, vazio ou campos a corrigir
        else Bloqueio/alteracao de permissao afeta reserva ativa
            Admin-->>Entrada: Impacto detectado. Politica DP-005 pendente
            Entrada-->>Web: Mostrar impacto e pedir decisao conforme regra
            Web-->>Adm: Nao cancela reserva automaticamente
        else Alteracao permitida
            Admin->>Persistencia: Aplicar cadastro/permissao/estado
            Persistencia->>Auditoria: Registrar ator, alvo, acao e antes/depois
            Persistencia-->>Admin: Confirmacao atomica ou falha
            Admin-->>Entrada: Resultado
            Entrada-->>Web: Sucesso ou falha sem estado parcial silencioso
            Web-->>Adm: Exibe estado atualizado
        end
    end
```

**Requisitos:** RF-002, RF-022, RF-030; RN-015, RN-017, RN-022; RNF-001..RNF-005, RNF-011. Auditoria de alteracao e leitura de reservas ativas sao obrigatorias; bloqueio nao cancela reserva implicitamente.

## SEQ-ADM-003 — Cadastro e atualizacao de recursos

**Objetivo:** cadastrar/atualizar salas, professores, materiais e equipamentos preservando integridade (J-ADM-004..J-ADM-006/RF-023..RF-025).  
**Limite:** fluxo comum de validacao e impacto; atributos por tipo dependem da politica institucional.

```mermaid
sequenceDiagram
    %% SEQ-ADM-003 — Cadastro e atualizacao de recursos
    actor Adm as Administrador
    participant Web as Aplicacao Web
    participant Entrada as Entrada da Aplicacao
    participant Auth as Identidade e Autorizacao
    participant Admin as Administracao de Cadastros
    participant Catalogo as Catalogo de Recursos
    participant Persistencia as Adaptadores de Persistencia
    participant Auditoria as Auditoria

    Adm->>Web: Seleciona tipo e informa dados/cadastro existente
    Web->>Entrada: Envia comando de cadastro/atualizacao/desativacao
    Entrada->>Auth: Validar autorizacao para tipo e alvo
    Entrada->>Admin: Validar dados, estado e unicidade definida
    Admin->>Persistencia: Consultar cadastro, reservas e movimentos vinculados
    Persistencia-->>Admin: Registro atual e impactos
    alt Sem autorizacao, cadastro inexistente/duplicado ou dados invalidos
        Admin-->>Entrada: Negar ou solicitar correcao
        Entrada-->>Web: Erro. Preservar estado anterior
        Web-->>Adm: Apresenta motivo
    else Reserva/movimento ativo afetado por desativacao
        Admin-->>Entrada: Impacto. Regra DP-005 pendente
        Entrada-->>Web: Exibir relacoes existentes
        Web-->>Adm: Preservar reservas/historico. Sem cancelamento automatico
    else Cadastro/alteracao valida
        Admin->>Catalogo: Aplicar cadastro ou situacao permitida
        Catalogo->>Persistencia: Gravar dados
        Persistencia->>Auditoria: Registrar autor, instante, alvo e antes/depois
        Persistencia-->>Catalogo: Confirmacao/falha
        Catalogo-->>Admin: Estado atual do recurso
        Admin-->>Entrada: Resultado
        Entrada-->>Web: Resultado atualizado
        Web-->>Adm: Confirma operacao e situacao
    end
```

**Dados/validacoes:** tipo, identificacao, capacidade/localizacao para sala, competencias/situacao docente, caracteristicas/situacao de itens, reservas/manutencoes/movimentos relacionados. **Requisitos:** RF-002, RF-004, RF-005, RF-023..RF-025, RF-030; RN-003, RN-007, RN-008, RN-017, RN-022; RNF-001..RNF-005, RNF-011.

## SEQ-ADM-004 — Bloqueio de manutencao

**Objetivo:** registrar periodo de manutencao, identificar reservas coincidentes e impedir novas reservas conflitantes (J-ADM-007/RF-026).  
**Limite:** nenhuma reserva existente e cancelada ou remanejada automaticamente; solucao de impacto depende DP-004.

```mermaid
sequenceDiagram
    %% SEQ-ADM-004 — Bloqueio de manutencao
    actor Adm as Administrador
    participant Web as Aplicacao Web
    participant Entrada as Entrada da Aplicacao
    participant Auth as Identidade e Autorizacao
    participant Admin as Administracao de Cadastros
    participant Disp as Disponibilidade e Integridade
    participant Persistencia as Adaptadores de Persistencia
    participant Auditoria as Auditoria

    Adm->>Web: Seleciona recurso e periodo de manutencao
    Web->>Entrada: Solicita bloqueio
    Entrada->>Auth: Validar permissao administrativa
    Entrada->>Admin: Validar recurso e periodo
    Admin->>Disp: Verificar termino > inicio e sobreposicoes
    Disp->>Persistencia: Consultar reservas aceitas, solicitacoes e bloqueios
    Persistencia-->>Disp: Itens coincidentes
    alt Periodo invalido, recurso inexistente ou sem autorizacao
        Disp-->>Entrada: Negar sem bloquear
        Entrada-->>Web: Validacao/negacao
        Web-->>Adm: Corrigir ou informar impedimento
    else Reserva preexistente coincide
        Disp-->>Admin: Impacto encontrado
        Admin-->>Entrada: Decisao de politica pendente DP-004
        Entrada-->>Web: Apresentar reservas afetadas
        Web-->>Adm: Nao cancelar/remanejar. Requer decisao institucional
    else Sem reserva preexistente coincidente
        Admin->>Persistencia: Registrar periodo e bloquear novas alocacoes coincidentes
        Persistencia->>Auditoria: Registrar ator, instante, recurso, periodo e operacao
        Persistencia-->>Admin: Confirmado ou conflito atomico
        Admin-->>Entrada: Resultado
        Entrada-->>Web: Atualiza disponibilidade ou informa disputa
        Web-->>Adm: Confirma bloqueio apenas apos gravacao
    end
```

**Concorrencia:** bloqueio e confirmacao de reserva disputam a mesma disponibilidade; a operacao que confirmar primeiro e respeitada, e a seguinte deve revalidar. Nunca aceitar reserva conflitante apos bloqueio efetivado. **Requisitos:** RF-006..RF-008, RF-026, RF-030; RN-001, RN-002, RN-005, RN-007, RN-016, RN-017; RNF-004, RNF-005, RNF-011.

## SEQ-ADM-005 — Consulta de auditoria

**Objetivo:** pesquisar eventos por periodo, usuario, recurso/acao e apresentar dados autorizados sem mutar registro (J-ADM-008/RF-029).  
**Limite:** consulta somente leitura; nem Administrador pode editar/apagar eventos.

```mermaid
sequenceDiagram
    %% SEQ-ADM-005 — Consulta de auditoria
    actor Adm as Administrador
    participant Web as Aplicacao Web
    participant Entrada as Entrada da Aplicacao
    participant Auth as Identidade e Autorizacao
    participant Relatorios as Consultas e Relatorios
    participant Auditoria as Auditoria
    participant Persistencia as Adaptadores de Persistencia

    Adm->>Web: Informa filtros de periodo/usuario/recurso/operacao
    Web->>Entrada: Solicita consulta de eventos
    Entrada->>Auth: Verificar permissao de auditoria e acesso ao escopo
    alt Nao autorizado
        Auth-->>Entrada: Negado
        Entrada-->>Web: Acesso negado
        Web-->>Adm: Informa negacao sem expor evento
    else Autorizado
        Entrada->>Relatorios: Validar filtros e consultar eventos
        Relatorios->>Auditoria: Solicitar leitura filtrada
        Auditoria->>Persistencia: Recuperar eventos sem mutacao
        Persistencia-->>Auditoria: Ator, instante, alvo, acao e estados registrados
        Auditoria-->>Relatorios: Eventos minimizados por permissao
        Relatorios-->>Entrada: Resultado ou vazio
        Entrada-->>Web: Eventos, estado anterior/novo e contexto permitido
        Web-->>Adm: Apresenta consulta somente leitura
    end
    opt Sem resultado
        Web-->>Adm: Mostra vazio, nao erro
    end
    opt Falha de recuperacao
        Persistencia-->>Auditoria: Falha
        Auditoria-->>Entrada: Erro de leitura
        Entrada-->>Web: Falha, nao vazio
        Web-->>Adm: Informa indisponibilidade
    end
    Note over Web,Persistencia: Toda tentativa de editar/apagar auditoria e negada
```

**Requisitos:** RF-002, RF-012, RF-029..RF-030; RN-015..RN-018, RN-022; RNF-001..RNF-003, RNF-011..RNF-012. Retencao e escopo de campos dependem DP-011.

## SEQ-ADM-006 — Geracao de relatorios

**Objetivo:** obter indicadores por recurso e periodo com criterios explicitos (J-ADM-002/J-ADM-009; RF-028/RF-034..RF-036).  
**Limite:** consulta nao altera reservas; nenhuma formula ausente e estimada.

```mermaid
sequenceDiagram
    %% SEQ-ADM-006 — Geracao de relatorios
    actor Adm as Administrador
    participant Web as Aplicacao Web
    participant Entrada as Entrada da Aplicacao
    participant Auth as Identidade e Autorizacao
    participant Relatorios as Consultas e Relatorios
    participant Persistencia as Adaptadores de Persistencia

    Adm->>Web: Seleciona relatorio, periodo e filtros
    Web->>Entrada: Solicita agregacao
    Entrada->>Auth: Validar permissao e escopo de dados
    Entrada->>Relatorios: Validar criterios e metricas aprovadas
    alt Metrica sem formula/fonte definida
        Relatorios-->>Entrada: Indicar indisponivel/pendente (DP-010)
        Entrada-->>Web: Sem valor estimado
        Web-->>Adm: Explica definicao pendente
    else Criterios definidos
        Relatorios->>Persistencia: Consultar reservas, estados, utilizacao, manutencao e eventos de conflito
        Persistencia-->>Relatorios: Dados autorizados e periodo
        Relatorios->>Relatorios: Agregar usando definicao e janela aprovadas
        alt Dados completos
            Relatorios-->>Entrada: Indicadores + unidade/periodo/atualizacao
            Entrada-->>Web: Resultado
            Web-->>Adm: Apresenta totais e criterios
        else Dados incompletos ou consulta falhou
            Relatorios-->>Entrada: Indisponivel/erro, nao zero presumido
            Entrada-->>Web: Estado explicito
            Web-->>Adm: Distingue falha, parcial e sem dados
        end
    end
```

**Requisitos:** RF-002, RF-027..RF-028, RF-034..RF-036; RN-004, RN-007, RN-012, RN-024; RNF-001..RNF-003, RNF-006, RNF-008..RNF-009, RNF-012.  
**Pendencias:** metricas, fonte de conflitos evitados, periodo e estados incluidos (DP-010); metas de desempenho (DP-014).
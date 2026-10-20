# Requisitos Nao Funcionais — Sistema de Organizacao de Recursos

Os RNF descrevem qualidade e restricoes verificaveis sem selecionar tecnologia. Metas numericas que nao constam nas fontes permanecem **PENDENTE DE DEFINICAO**; sua validacao futura requer carga/ambiente de referencia acordados.

## RNF-001 — Controle de autenticacao e acesso
**Categoria:** Seguranca.  
**Descricao:** Operacoes protegidas devem exigir identidade autenticada e autorizacao atual por perfil, permissao, propriedade e escopo; acesso direto sem permissao deve ser negado.  
**Justificativa:** Proteger contas, reservas e operacoes privilegiadas.  
**Personas afetadas:** Todas.  
**Jornadas relacionadas:** Todas as jornadas com operacao autenticada, em especial J-SOL-001, J-RES-001, J-ADM-001.  
**RF relacionados:** RF-001 a RF-036 conforme permissao.  
**Criterio de verificacao:** Tentar cada acao com perfil autorizado e nao autorizado, inclusive acesso direto a alvo alheio/fora do escopo; somente a primeira classe conclui.  
**Metrica/evidencia esperada:** Matriz perfil-acao-alvo com resultados autorizados/negados; cobertura de todos os fluxos protegidos.  
**Prioridade:** Obrigatorio.  
**Pendencias:** Matriz detalhada de papeis e escopo (DP-003).

## RNF-002 — Confidencialidade e minimizacao de dados
**Categoria:** Seguranca e privacidade.  
**Descricao:** Interfaces, consultas, mensagens e relatorios devem expor somente dados necessarios e autorizados; credenciais e informacoes sensiveis nao devem ser reveladas em respostas ou erros.  
**Justificativa:** Evitar acesso indevido e exposicao de dados pessoais.  
**Personas afetadas:** Todas.  
**Jornadas relacionadas:** J-SOL-001, J-SOL-002, J-SOL-005, J-SOL-008, J-RES-002, J-RES-005, J-ADM-003, J-ADM-008, J-ADM-009.  
**RF relacionados:** RF-001, RF-002, RF-004, RF-005, RF-012, RF-018, RF-022, RF-027, RF-029, RF-032.  
**Criterio de verificacao:** Revisar respostas para cada perfil e confirmar que dados alheios/segredos nao estao presentes.  
**Metrica/evidencia esperada:** Casos de acesso e conteudo por perfil; nenhum campo sensivel nao autorizado nos resultados.  
**Prioridade:** Obrigatorio.  
**Pendencias:** Classificacao de dados e escopos detalhados (DP-003, DP-011).

## RNF-003 — Tratamento seguro e compreensivel de erros
**Categoria:** Seguranca e usabilidade.  
**Descricao:** Erros de validacao, conflito, indisponibilidade, acesso negado, recurso inexistente, falha interna e falha de comunicacao devem ser distinguiveis para o usuario sem revelar detalhes internos sensiveis.  
**Justificativa:** Permitir correcao segura e evitar interpretar erro como sucesso, disponibilidade ou lista vazia.  
**Personas afetadas:** Todas.  
**Jornadas relacionadas:** Todas as jornadas que incluem operacao ou consulta.  
**RF relacionados:** RF-001 a RF-036.  
**Criterio de verificacao:** Para cada classe de falha prevista nas jornadas, observar mensagem e estado; nao deve haver confirmacao presumida nem dado interno exposto.  
**Metrica/evidencia esperada:** Catalogo de classe de resultado e mensagem associada; cobertura dos fluxos negativos.  
**Prioridade:** Obrigatorio.  
**Pendencias:** Vocabulário institucional e niveis de detalhe de erro.

## RNF-004 — Integridade sob concorrencia
**Categoria:** Integridade e concorrencia.  
**Descricao:** Operacoes concorrentes de solicitacao, aprovacao, alteracao ou manutencao devem preservar a regra de no maximo um aceite conflitante por recurso/periodo e evitar atualizacoes baseadas em estado obsoleto.  
**Justificativa:** Evitar dupla reserva e estado inconsistente.  
**Personas afetadas:** Solicitante, Responsavel e Administrador.  
**Jornadas relacionadas:** J-SOL-003, J-SOL-004, J-SOL-006, J-RES-003, J-RES-005, J-ADM-004 a J-ADM-007.  
**RF relacionados:** RF-006 a RF-009, RF-013, RF-016, RF-026.  
**Criterio de verificacao:** Executar cenarios concorrentes com mesmo recurso/periodo e verificar invariantes de resultado e mensagem, sem definir estrategia tecnica nesta etapa.  
**Metrica/evidencia esperada:** Zero pares de reservas aceitas conflitantes nos cenarios de verificacao definidos.  
**Prioridade:** Obrigatorio.  
**Pendencias:** Carga/volume de referencia para stress (DP-014); comportamento `SOLICITADA` (DP-001).

## RNF-005 — Integridade de estados e dados
**Categoria:** Confiabilidade e consistencia.  
**Descricao:** Mudanca de estado ou cadastro relevante deve produzir resultado coerente, sem sucesso parcial silencioso; falha de validacao/auditoria nao pode declarar operacao concluida.  
**Justificativa:** Preservar reservas, movimentos e historico.  
**Personas afetadas:** Todas.  
**Jornadas relacionadas:** J-SOL-004, J-SOL-006 a J-SOL-008, J-RES-003, J-RES-004, J-RES-006, J-RES-007, J-ADM-003 a J-ADM-008.  
**RF relacionados:** RF-009, RF-013, RF-014, RF-016, RF-017, RF-019, RF-020, RF-022 a RF-030.  
**Criterio de verificacao:** Introduzir falha em validacao/auditoria e conferir que estado anterior permanece ou resultado fica explicitamente inconclusivo, sem sucesso falso.  
**Metrica/evidencia esperada:** Resultado por etapa e verificacao de invariantes antes/depois.  
**Prioridade:** Obrigatorio.  
**Pendencias:** Politica de recuperacao/operacao inconclusiva e atualizacao (DP-014).

## RNF-006 — Desempenho de consultas e operacoes
**Categoria:** Desempenho.  
**Descricao:** Consultas de recursos, disponibilidade, filas e relatorios devem responder dentro de limite mensuravel acordado para carga e volume de referencia; envio/decisao de reserva deve ter tempo de processamento observavel e compativel com o fluxo.  
**Justificativa:** Viabilizar pesquisa e operacao sem espera impeditiva.  
**Personas afetadas:** Todas.  
**Jornadas relacionadas:** J-SOL-002 a J-SOL-005, J-RES-002, J-RES-005, J-RES-008, J-ADM-002, J-ADM-008, J-ADM-009.  
**RF relacionados:** RF-004 a RF-009, RF-015, RF-018, RF-021, RF-027 a RF-029, RF-034 a RF-036.  
**Criterio de verificacao:** Medir latencia por classe de operacao sob conjunto de dados e carga acordados e comparar com meta aprovada.  
**Metrica/evidencia esperada:** Latencia percentil e volume da carga de referencia. **Meta quantitativa: PENDENTE DE DEFINICAO.**  
**Prioridade:** Obrigatorio.  
**Pendencias:** Limites, percentil, tamanho de dados e carga de referencia (DP-014).

## RNF-007 — Capacidade sob carga
**Categoria:** Desempenho e capacidade.  
**Descricao:** O sistema deve suportar volume acordado de consultas e operacoes simultaneas sem violar integridade de reservas e estados.  
**Justificativa:** Confirmar que uso concorrente nao degrada confiabilidade.  
**Personas afetadas:** Todas.  
**Jornadas relacionadas:** J-SOL-002 a J-SOL-004, J-RES-002, J-RES-003, J-ADM-002, J-ADM-007.  
**RF relacionados:** RF-004, RF-006 a RF-009, RF-015, RF-016, RF-026.  
**Criterio de verificacao:** Exercitar volume simultaneo representativo e verificar integridade, falhas observaveis e limite de desempenho aprovado.  
**Metrica/evidencia esperada:** Usuarios/sessoes simultaneos, taxa de operacoes e latencia. **Meta quantitativa: PENDENTE DE DEFINICAO.**  
**Prioridade:** Obrigatorio.  
**Pendencias:** Carga-alvo e limites (DP-014).

## RNF-008 — Confiabilidade e recuperacao de falhas
**Categoria:** Confiabilidade.  
**Descricao:** Falha de consulta, auditoria ou notificacao deve produzir retorno identificavel; notificacao nao pode reverter operacao confirmada; reserva nao deve ser indicada como disponivel quando consulta falhar.  
**Justificativa:** Preservar informacoes e permitir comportamento previsivel.  
**Personas afetadas:** Todas.  
**Jornadas relacionadas:** J-SOL-002 a J-SOL-008, J-RES-002 a J-RES-008, J-ADM-002 a J-ADM-009.  
**RF relacionados:** RF-004, RF-007 a RF-009, RF-016, RF-017, RF-029 a RF-036.  
**Criterio de verificacao:** Simular indisponibilidade de consulta/notificacao/auditoria e verificar diferenca entre operacao confirmada, falha e estado inconclusivo.  
**Metrica/evidencia esperada:** Matriz de falhas e resultados observados; metas de disponibilidade/recuperacao quantitativas pendentes.  
**Prioridade:** Obrigatorio.  
**Pendencias:** RTO/RPO, disponibilidade, retentativa e recuperacao (DP-009, DP-014).

## RNF-009 — Usabilidade e feedback consistente
**Categoria:** Usabilidade.  
**Descricao:** Cada operacao deve informar carregamento, sucesso, validacao, vazio, conflito, negacao, recurso inexistente/indisponivel, dados desatualizados e falha quando aplicaveis; estados pendentes nao devem parecer aprovados.  
**Justificativa:** Reduzir incerteza e erro de operacao.  
**Personas afetadas:** Todas.  
**Jornadas relacionadas:** Todas.  
**RF relacionados:** RF-001 a RF-036.  
**Criterio de verificacao:** Percorrer cada fluxo e verificar que o feedback corresponde ao resultado e distingue vazio de erro.  
**Metrica/evidencia esperada:** Mapa fluxo/estado/feedback validado nos cenarios de aceite.  
**Prioridade:** Obrigatorio.  
**Pendencias:** Vocabulário e padrao visual podem ser definidos em etapa de UX.

## RNF-010 — Responsividade e acessibilidade
**Categoria:** Usabilidade, responsividade e acessibilidade.  
**Descricao:** Fluxos essenciais devem ser utilizaveis em computador e dispositivo movel, com formularios e mensagens acessiveis e sem perda de informacao ou acao por tamanho de tela.  
**Justificativa:** A persona solicitante usa escritorio e celular; interface simples e acessivel e requisito de experiencia.  
**Personas afetadas:** Todas, especialmente Solicitante.  
**Jornadas relacionadas:** J-SOL-001 a J-SOL-008; demais jornadas usadas em dispositivos moveis.  
**RF relacionados:** RF-001 a RF-036.  
**Criterio de verificacao:** Avaliar fluxos essenciais em larguras desktop e mobile, navegacao por teclado, foco, rotulos e leitura de mensagens.  
**Metrica/evidencia esperada:** Lista de verificacao de acessibilidade e viewport; nivel/criterio quantitativo pendente.  
**Prioridade:** Obrigatorio.  
**Pendencias:** Nivel normativo de acessibilidade e dispositivos-alvo (DP-014).

## RNF-011 — Integridade e consulta da auditoria
**Categoria:** Auditoria e rastreabilidade.  
**Descricao:** Eventos de auditoria devem permanecer consultaveis a perfis autorizados, conter atributos exigidos e nao ser alteraveis/excluiveis por operacao comum, inclusive administrativa.  
**Justificativa:** Evidenciar autoria e estado anterior/novo das operacoes.  
**Personas afetadas:** Todas.  
**Jornadas relacionadas:** J-SOL-004, J-SOL-006 a J-SOL-008, J-RES-003, J-RES-004, J-RES-006 a J-RES-008, J-ADM-003 a J-ADM-008.  
**RF relacionados:** RF-012, RF-016, RF-017, RF-019, RF-020, RF-022 a RF-030.  
**Criterio de verificacao:** Conferir campos para cada evento e tentar leitura/alteracao com perfis autorizados e nao autorizados.  
**Metrica/evidencia esperada:** Cobertura de eventos e verificacao de imutabilidade por operacao comum.  
**Prioridade:** Obrigatorio.  
**Pendencias:** Retencao, integridade de longo prazo e detalhes de visibilidade (DP-011).

## RNF-012 — Observabilidade de operacoes e falhas
**Categoria:** Observabilidade.  
**Descricao:** Falhas de consulta, decisao, manutencao, auditoria e comunicacao devem gerar informacao suficiente para diagnostico operacional sem expor segredo ou dado pessoal desnecessario ao usuario.  
**Justificativa:** Investigar indisponibilidade e diferenciar falha tecnica de regra de negocio.  
**Personas afetadas:** Administrador e equipe responsavel pela operacao; usuarios recebem feedback seguro.  
**Jornadas relacionadas:** J-SOL-003, J-SOL-004, J-SOL-008, J-RES-003, J-RES-006, J-RES-007, J-ADM-002, J-ADM-007 a J-ADM-009.  
**RF relacionados:** RF-007 a RF-009, RF-016, RF-019, RF-020, RF-026, RF-028 a RF-036.  
**Criterio de verificacao:** Provocar classes de falha e verificar que evento permite correlacionar operacao sem incluir credenciais/segredos.  
**Metrica/evidencia esperada:** Catalogo de eventos de falha e evidencia de correlacao.  
**Prioridade:** Importante.  
**Pendencias:** Retencao e alertas operacionais (DP-011, DP-014).

## RNF-013 — Testabilidade e verificabilidade
**Categoria:** Testabilidade.  
**Descricao:** Requisitos, regras e resultados devem ser observaveis por entradas, estados de saida e registros suficientes para validar cenarios positivos, negativos, limite e concorrencia.  
**Justificativa:** Preparar o desenvolvimento futuro orientado a TDD sem prescrever ferramenta.  
**Personas afetadas:** Equipe de produto/desenvolvimento/qualidade e usuarios indiretamente.  
**Jornadas relacionadas:** Todas as jornadas.  
**RF relacionados:** RF-001 a RF-036.  
**Criterio de verificacao:** Cada RF tem criterio de aceite e cenario futuro; cada regra critica tem ao menos um caso positivo e negativo/limite definido.  
**Metrica/evidencia esperada:** RTM completa; rastreabilidade para futuros IDs de teste permanece pendente ate sua criacao.  
**Prioridade:** Obrigatorio.  
**Pendencias:** Estrategia e ferramentas de teste sao etapa posterior; nenhuma selecionada.

## RNF-014 — Manutenibilidade e consistencia documental
**Categoria:** Manutenibilidade.  
**Descricao:** Requisitos, regras e jornadas devem manter identificadores estaveis, linguagem objetiva e referencias cruzadas consistentes. Capacidades funcionais, regras de negocio e atributos de qualidade devem permanecer distinguiveis; mudancas futuras devem preservar IDs nao afetados e atualizar a RTM.  
**Justificativa:** Permitir evolucao clara e rastreavel sem misturar comportamento, regra de dominio e qualidade, nem definir estrutura de codigo nesta etapa.  
**Personas afetadas:** Equipe de produto, arquitetura, desenvolvimento e QA.  
**Jornadas relacionadas:** Todas (documentacao transversal).  
**RF relacionados:** RF-001 a RF-036.  
**Criterio de verificacao:** Revisar unicidade dos IDs, clareza das responsabilidades documentais e referencias RF/RN/RNF/Jornada/criterio na RTM a cada alteracao; uma mudanca isolada nao deve exigir renumerar requisitos nao afetados.  
**Metrica/evidencia esperada:** Nenhum ID duplicado ou referencia quebrada; alteracoes rastreaveis e revisao documental registrada.  
**Prioridade:** Importante.  
**Pendencias:** Processo de controle de versao/aprovacao documental.
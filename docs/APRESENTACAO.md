# Roteiro da apresentação e preparação para a prova oral

**Duração sugerida:** 12 minutos de demonstração + perguntas. Cada membro da equipe apresenta um bloco e deve conseguir explicar o código e os testes desse bloco.

## Antes de começar (5 min antes)

```bash
docker compose up -d banco
```

```bash
./mvnw spring-boot:run
```

Deixe abertos: a interface (`http://localhost:8080`), o Swagger (`/swagger-ui.html`), a página de Actions do GitHub e o relatório JaCoCo do último run.

## Roteiro

| Tempo | Bloco | O que mostrar | Onde está |
|---|---|---|---|
| 0:00 | Problema e arquitetura | Objetivo, três perfis, monólito modular, tabela única de ocupações | `PRD/arquitetura/c4/`, ADR-001, ADR-004 |
| 1:30 | Solicitante | Login `solicitante`; pesquisar salas no período; verificar disponibilidade; reservar sala + professora; repetir e mostrar o **conflito explicado** | Aba Recursos e reservas |
| 3:00 | Recurso restrito | Reservar `LAB-INF-01` (nasce "Aguardando aprovação"); entrar como `responsavel` e aprovar; mostrar o histórico da reserva | Abas Aprovações e Minhas reservas |
| 4:30 | Regras de estado | Iniciar uso e tentar cancelar (recusado); não comparecimento só após o início; `DELETE` não existe (405) | Aba Aprovações; Swagger |
| 5:30 | Manutenção e materiais | Bloquear recurso com reserva ativa (recusado com a lista); retirada e devolução com atraso | Aba Administração e Retiradas |
| 6:30 | Concorrência (ponto alto) | Rodar `./mvnw test -Dtest=ConcorrenciaReservaTest`; explicar lock + constraint; mostrar o resultado do JMeter (30 simultâneos → 1 aceito) | `ConcorrenciaReservaTest`, `RELATORIO_CARGA.md` |
| 8:00 | Qualidade | CI verde, 247 testes, cobertura 97,7%/88,0%, gate no build; commits RED/GREEN da story 1.5 | Actions, `RELATORIO_DE_TESTES.md` |
| 9:30 | Rastreabilidade e defeitos | Uma linha da RTM de ponta a ponta (RF-008); dois defeitos reais encontrados pelos testes (DEF-009 e DEF-011) | `RTM.md`, `REGISTRO_DEFEITOS.md` |
| 11:00 | Relatórios e auditoria | Painel do Administrador, relatório de utilização com critério explícito, auditoria imutável | Aba Administração |

## Perguntas prováveis e respostas curtas

**Como vocês garantem que não existe dupla reserva?**
Duas camadas. Na aplicação, toda operação que muda a ocupação trava as linhas dos recursos com `SELECT … FOR UPDATE`, em ordem de id, relê as ocupações e só então grava, tudo na mesma transação. No banco, a constraint `EXCLUDE USING gist (recurso_id WITH =, periodo WITH &&) WHERE (ativa)` recusa qualquer sobreposição ativa, mesmo que alguém escreva sem passar pelo lock. Provamos com 20 requisições HTTP simultâneas contra um PostgreSQL real e com o JMeter.

**Por que Testcontainers e não H2?**
Porque o que precisamos provar (lock pessimista, constraint de exclusão com `tsrange` e `btree_gist`, trigger de auditoria) é comportamento do PostgreSQL. O H2 não tem esses recursos; um teste nele passaria sem provar nada.

**Onde está o TDD?**
Na story 1.5: o commit `test(1.5): RED` tem 30 testes escritos antes do código, 26 falhando, e o CI desse commit ficou vermelho. O commit seguinte `feat(1.5): GREEN` faz todos passarem; a refatoração do fluxo de transição veio na 1.6 com a suíte verde antes e depois.

**Cobertura de 97% significa qualidade?**
Não sozinha. Os testes verificam comportamento observável: status HTTP, mensagem, estado no banco, eventos de auditoria. Exemplos de testes que acharam defeitos reais: o WireMock revelou que nenhuma notificação HTTP seria entregue (DEF-009); o teste de erros revelou mensagens do framework em inglês (DEF-007).

**Por que uma reserva SOLICITADA já ocupa o horário?**
Decisão DP-001: resolve a disputa no momento do pedido, que é quando os pedidos simultâneos acontecem. A alternativa empurraria a corrida para a aprovação. O custo — um pedido pendente segurar o horário — é mitigado pela rejeição, que libera o horário.

**E se o serviço de notificação cair?**
A notificação é enviada depois do commit, em transação própria. Falha ou lentidão marca a notificação como FALHA e a reserva continua gravada. O WireMock simula erro 500 e timeout.

**Quem pode aprovar?**
Só o perfil Responsável. O Administrador gerencia cadastros e pode cancelar uma reserva para resolver conflito com manutenção, mas não aprova — exigência do enunciado. A matriz de autorização tem um teste para cada combinação proibida.

**O que ficou de fora ou é limitação?**
Escopo do Responsável por recurso, reenvio automático de notificações, carga medida numa única máquina e o SonarCloud, que depende do token do repositório.

# Sistema de Organização de Recursos

Aplicação para reservar **salas, professores, materiais e equipamentos** de uma instituição de ensino **sem conflitos de horário**, com aprovação obrigatória para recursos restritos, bloqueio por manutenção, retirada e devolução de materiais, histórico auditável, notificações e relatórios.

Projeto final de Qualidade e Teste de Software — versão **1.0.0-rc1** (Release Candidate).

**Integrantes:** Diego Sarti Araujo Viana · Lucas Soares Valerio dos Santos · João Paulo Figueiredo Serafim · João Victor Furin Lamas.

| | |
|---|---|
| Stack | Java 21 · Spring Boot 3.5 · PostgreSQL 16 · Flyway · Spring Security · springdoc-openapi · Bootstrap 5 |
| Testes | JUnit 5 · Testcontainers · WireMock · Playwright · JMeter · JaCoCo · SonarCloud |
| Qualidade atual | **283 testes, 0 falhas · cobertura 97,9% linhas / 88,5% branches** (metas 80/70) · **Quality Gate do SonarCloud aprovado** |
| CI | GitHub Actions em todo push e pull request (`.github/workflows/ci.yml`) |

---

## 1. Como executar

**Pré-requisitos:** Java 21 e Docker. O Maven vem pelo wrapper (`./mvnw`).

```bash
docker compose up -d banco
```

```bash
./mvnw spring-boot:run
```

Abra **http://localhost:8080** (interface) ou **http://localhost:8080/swagger-ui.html** (documentação da API).

Alternativa com tudo em containers:

```bash
./mvnw -DskipTests package
```

```bash
docker compose --profile app up -d --build
```

Configuração por variáveis de ambiente: `SOR_DB_URL`, `SOR_DB_USER`, `SOR_DB_PASSWORD`, `SOR_SENHA_INICIAL` (senha dos usuários de demonstração), `SOR_NOTIFICACAO_MODO` (`simulado` ou `http`), `SOR_NOTIFICACAO_URL` e `SOR_FERIADOS_EXTRAS` (recessos da instituição, datas `aaaa-mm-dd` separadas por vírgula).

### Usuários de demonstração

Senha de todos: `Senha@123` (troque em uma implantação real).

| Usuário | Perfil | Pode |
|---|---|---|
| `solicitante` (Marina Costa) | Solicitante | Pesquisar, verificar disponibilidade, reservar, alterar e cancelar as próprias reservas |
| `solicitante2` (Paulo Mendes) | Solicitante | Idem (útil para demonstrar conflito e privacidade) |
| `responsavel` (Rita Responsável) | Responsável | Aprovar/rejeitar, iniciar uso, concluir, registrar não comparecimento, retirada e devolução |
| `admin` | Administrador | Cadastrar e editar recursos, usuários, manutenção, auditoria, relatórios; cancelar reservas (não aprova) |

O banco de demonstração já traz salas (duas restritas), professores com competências, materiais e equipamentos.

### Interface

Portal responsivo (computador e celular), em azul institucional com detalhes em âmbar:

- **Reservar:** recursos separados em Salas, Professores, Equipamentos e Materiais; a disponibilidade de cada cartão é calculada sozinha para o período escolhido, e a lista se atualiza a cada 20 segundos (novos cadastros e horários liberados aparecem sem recarregar). Recurso reservado, em manutenção ou desativado continua visível, em cinza e sem clique.
- **Resultados em janela central:** reserva confirmada, solicitação enviada, recurso indisponível e cancelamento (com confirmação). Avisos flutuantes têm botão para fechar.
- **Minhas reservas:** cada cartão abre o próprio histórico em linha do tempo.
- **Avisos:** selo no menu quando chegam avisos novos.
- **Administração:** abas Visão geral, Recursos (cadastrar, editar, desativar e reativar), Manutenção e Relatórios.
- **Entrada:** botão para mostrar a senha e mensagem de erro discreta abaixo de "Entrar".

---

## 2. O que o sistema faz (requisitos do enunciado)

| # | Requisito | Como verificar |
|---|---|---|
| 1 | Autenticação e autorização por perfil | Entre com cada usuário; `SegurancaApiTest` |
| 2 | Cadastro, edição e consulta de salas, professores e materiais | Administração → Recursos / `POST` e `PUT /api/v1/recursos` |
| 3 | Pesquisa por tipo, capacidade, localização, competência e disponibilidade | Aba Reservar / `GET /api/v1/recursos?...` |
| 4 | Criação, alteração e cancelamento de reservas | `POST /api/v1/reservas`, `PUT /api/v1/reservas/{id}`, `POST .../cancelar` |
| 5 | Detecção de sobreposição, inclusive na agenda do professor | Reserve a mesma sala ou o mesmo professor no mesmo horário |
| 6 | Proteção contra dupla reserva simultânea | `ConcorrenciaReservaTest` e JMeter (30 pedidos → 1 aceito) |
| 7 | Aprovação obrigatória para recursos restritos | Reserve `LAB-INF-01` e aprove como `responsavel` |
| 8 | Bloqueio de recursos em manutenção | `POST /api/v1/manutencoes` |
| 9 | Retirada e devolução de materiais | Aba Retiradas / `/api/v1/movimentacoes` |
| 10 | Histórico auditável | `GET /api/v1/auditoria`, `GET /api/v1/reservas/{id}/historico` |
| 11 | Notificação simulada ou integração com API externa | Aba Avisos; modo `http` testado com WireMock |
| 12 | Relatórios de utilização, carga horária e conflitos evitados | Administração → Relatórios / `/api/v1/relatorios/*` |
| 13 | Interface responsiva e mensagens compreensíveis | Interface web; `InterfaceE2ETest` (inclui tela de celular) |
| 14 | Documentação da API | `/swagger-ui.html` e `/v3/api-docs` |

### Regras críticas

- Término posterior ao início; reservas só com início futuro.
- **Sem reservas em feriados** (RN-031): feriados nacionais (fixos, Carnaval, Sexta-feira Santa e Corpus Christi) são calculados para qualquer ano; recessos da instituição entram por `SOR_FERIADOS_EXTRAS`. A tela avisa antes de enviar e o servidor também recusa.
- **Limite por reserva** (RN-032): no máximo 1 sala, 1 professor e 3 materiais ou equipamentos, para uma reserva não prender o catálogo inteiro. A tela bloqueia na hora e o servidor também recusa.
- Nenhuma sobreposição de ocupações ativas no mesmo recurso (sala, material ou professor), garantida por **lock na aplicação + constraint de exclusão no PostgreSQL** (ADR-004).
- `SOLICITADA`, `APROVADA` e `EM_USO` ocupam o horário; pedidos simultâneos resultam em **uma única reserva aceita**.
- Recurso em manutenção não pode ser reservado; manutenção sobre reserva ativa é recusada (sem cancelamento automático).
- **Somente o Responsável aprova** recursos restritos.
- Reserva iniciada não pode ser cancelada, e **nenhuma reserva é apagada** (não existe `DELETE`).
- Toda mudança de estado gera auditoria **na mesma transação**; a auditoria é imutável no banco.

Ciclo de vida: `SOLICITADA → APROVADA → EM_USO → CONCLUIDA`, com `REJEITADA`, `CANCELADA` e `NAO_COMPARECEU` (diagrama em `PRD/arquitetura/uml/ESTADOS_RESERVA.md`).

---

## 3. API

Contrato completo em `/swagger-ui.html`. Autenticação: HTTP Basic (clientes de API) ou login por formulário com sessão + CSRF (interface web). Erros no formato RFC 7807 com `title`, `detail` em português e `codigo` estável.

```bash
curl -u solicitante:Senha@123 -H 'Content-Type: application/json' \
  -d '{"inicio":"2026-12-10T10:00:00","fim":"2026-12-10T12:00:00","finalidade":"Aula","recursoIds":[1,5]}' \
  http://localhost:8080/api/v1/reservas
```

| Recurso | Endpoints principais |
|---|---|
| Autenticação | `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `GET /api/v1/auth/me` |
| Recursos | `GET/POST /api/v1/recursos`, `PUT /api/v1/recursos/{id}`, `POST .../desativar`, `POST .../reativar` |
| Disponibilidade | `GET /api/v1/disponibilidade`, `GET /api/v1/disponibilidade/recursos/{id}/agenda`, `GET /api/v1/disponibilidade/feriados?ano=` |
| Reservas | `POST /api/v1/reservas`, `GET /api/v1/reservas/minhas`, `GET /api/v1/reservas`, `GET/PUT /api/v1/reservas/{id}`, `POST .../aprovar \| rejeitar \| cancelar \| iniciar \| concluir \| nao-comparecimento`, `GET .../historico` |
| Manutenção | `GET/POST /api/v1/manutencoes`, `POST /api/v1/manutencoes/{id}/cancelar` |
| Movimentações | `GET /api/v1/movimentacoes`, `POST /api/v1/movimentacoes/retiradas`, `POST /api/v1/movimentacoes/{id}/devolucao` |
| Auditoria, notificações, relatórios | `GET /api/v1/auditoria`, `GET /api/v1/notificacoes`, `GET /api/v1/relatorios/{utilizacao,carga-horaria,conflitos,resumo}` |
| Usuários | `GET/POST /api/v1/usuarios`, `PUT /api/v1/usuarios/{id}` |

---

## 4. Testes e qualidade

```bash
./mvnw verify
```

Roda os 283 testes contra um **PostgreSQL real em container** (Testcontainers; precisa de Docker) e aplica o gate de cobertura (falha abaixo de 80% de linhas ou 70% de branches). Os testes E2E usam o Chromium do Playwright; localmente são pulados se o navegador não estiver instalado (`./mvnw exec:java -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"`).

| Documento | Conteúdo |
|---|---|
| [`docs/qa/PLANO_DE_TESTES.md`](docs/qa/PLANO_DE_TESTES.md) | Estratégia, níveis, ferramentas, critérios de saída |
| [`docs/qa/RELATORIO_DE_TESTES.md`](docs/qa/RELATORIO_DE_TESTES.md) | Resultado por classe, cobertura por pacote, evidência de TDD |
| [`RTM.md`](RTM.md) | Requisito → risco → teste → evidência |
| [`docs/qa/REGISTRO_DEFEITOS.md`](docs/qa/REGISTRO_DEFEITOS.md) | Defeitos encontrados, priorizados e tratados |
| [`docs/qa/RELATORIO_CARGA.md`](docs/qa/RELATORIO_CARGA.md) | Plano e resultado do JMeter |
| [`docs/qa/RELATORIO_FINAL_QUALIDADE.md`](docs/qa/RELATORIO_FINAL_QUALIDADE.md) | Avaliação final por critério do professor |
| [`docs/qa/gates/`](docs/qa/gates/) | QA gate de cada story |

Teste de carga (com a aplicação rodando a partir de uma cópia do JAR):

```bash
./mvnw -Pcarga verify
```

---

## 5. Arquitetura e documentação

- Monólito modular (ADR-001): pacotes `identidade`, `catalogo`, `disponibilidade`, `reservas`, `manutencao`, `movimentacao`, `auditoria`, `notificacao`, `relatorios`, `compartilhado` (`docs/framework/source-tree.md`).
- Requisitos, personas e jornadas: `PRD/` · Decisões: `PRD/requisitos/PENDENCIAS_DECISOES.md` · ADRs: `PRD/arquitetura/adr/` · C4 e sequências: `PRD/arquitetura/c4/`, `PRD/arquitetura/uml/`.
- Histórias de desenvolvimento: `docs/stories/1.1` a `1.10` (Epic 1 em `PRD/epic-1-mvp-avaliavel.md`).
- Roteiro da apresentação e da prova oral: [`docs/APRESENTACAO.md`](docs/APRESENTACAO.md).
- As pastas `.aiox-core/`, `.claude/`, `.codex/`, `.gemini/`, `.cursor/`, `.grok/`, `.kimi/`, `.antigravity/` e `.github/agents/` são do framework de agentes AIOX usado no processo; **não são código do produto** e ficam fora da análise do SonarCloud.

---

## 6. Fluxo de trabalho e situação do repositório

- Todo o código entra na `main` por **pull request**, com os checks do CI (`Build, testes e cobertura`) e o **Quality Gate do SonarCloud** verdes. Os PRs mesclados (#1, #2, #4, #5 e os seguintes) guardam o histórico de revisão e de análise.
- **SonarCloud:** configurado (organização `joaopaulofserafim`, projeto `joaopaulofserafim_projeto_final_QAS`); o token fica no segredo `SONAR_TOKEN` do repositório e a análise automática do Sonar está desligada, pois a análise roda no pipeline.
- **Pendente (dono do repositório):** proteger a `main` em *Settings → Rules → Rulesets*, exigindo pull request com 1 aprovação e o check `Build, testes e cobertura` verde.

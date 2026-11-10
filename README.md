# Sistema de Organização de Recursos

Aplicação para reservar **salas, professores, materiais e equipamentos** de uma instituição de ensino **sem conflitos de horário**, com aprovação obrigatória para recursos restritos, bloqueio por manutenção, retirada e devolução de materiais, histórico auditável, notificações e relatórios.

Projeto final de Qualidade e Teste de Software — versão **1.0.0-rc1** (Release Candidate).

**Integrantes:** Diego Sarti Araujo Viana · Lucas Soares Valerio dos Santos · João Paulo Figueiredo Serafim · João Victor Furin Lamas.

| | |
|---|---|
| Stack | Java 21 · Spring Boot 3.5 · PostgreSQL 16 · Flyway · Spring Security · springdoc-openapi · Bootstrap 5 |
| Testes | JUnit 5 · Testcontainers · WireMock · Playwright · JMeter · JaCoCo · SonarCloud |
| Qualidade atual | **247 testes, 0 falhas · cobertura 97,7% linhas / 88,0% branches** (metas 80/70) |
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

Configuração por variáveis de ambiente: `SOR_DB_URL`, `SOR_DB_USER`, `SOR_DB_PASSWORD`, `SOR_NOTIFICACAO_MODO` (`simulado` ou `http`) e `SOR_NOTIFICACAO_URL`.

### Usuários de demonstração

Senha de todos: `Senha@123` (troque em uma implantação real).

| Usuário | Perfil | Pode |
|---|---|---|
| `solicitante` (Marina Costa) | Solicitante | Pesquisar, verificar disponibilidade, reservar, alterar e cancelar as próprias reservas |
| `solicitante2` (Paulo Mendes) | Solicitante | Idem (útil para demonstrar conflito e privacidade) |
| `responsavel` (Rita Responsável) | Responsável | Aprovar/rejeitar, iniciar uso, concluir, registrar não comparecimento, retirada e devolução |
| `admin` | Administrador | Cadastros, usuários, manutenção, auditoria, relatórios; cancelar reservas (não aprova) |

O banco de demonstração já traz salas (uma restrita), professores com competências, materiais e equipamentos.

---

## 2. O que o sistema faz (requisitos do enunciado)

| # | Requisito | Como verificar |
|---|---|---|
| 1 | Autenticação e autorização por perfil | Entre com cada usuário; `SegurancaApiTest` |
| 2 | Cadastro e consulta de salas, professores e materiais | Aba Administração / `POST /api/v1/recursos` |
| 3 | Pesquisa por tipo, capacidade, localização, competência e disponibilidade | Aba Recursos e reservas / `GET /api/v1/recursos?...` |
| 4 | Criação, alteração e cancelamento de reservas | `POST /api/v1/reservas`, `PUT /api/v1/reservas/{id}`, `POST .../cancelar` |
| 5 | Detecção de sobreposição, inclusive na agenda do professor | Reserve a mesma sala ou o mesmo professor no mesmo horário |
| 6 | Proteção contra dupla reserva simultânea | `ConcorrenciaReservaTest` e JMeter (30 pedidos → 1 aceito) |
| 7 | Aprovação obrigatória para recursos restritos | Reserve `LAB-INF-01` e aprove como `responsavel` |
| 8 | Bloqueio de recursos em manutenção | `POST /api/v1/manutencoes` |
| 9 | Retirada e devolução de materiais | Aba Retiradas / `/api/v1/movimentacoes` |
| 10 | Histórico auditável | `GET /api/v1/auditoria`, `GET /api/v1/reservas/{id}/historico` |
| 11 | Notificação simulada ou integração com API externa | Aba Notificações; modo `http` testado com WireMock |
| 12 | Relatórios de utilização, carga horária e conflitos evitados | Aba Administração / `/api/v1/relatorios/*` |
| 13 | Interface responsiva e mensagens compreensíveis | Interface web; `InterfaceE2ETest` (inclui tela de celular) |
| 14 | Documentação da API | `/swagger-ui.html` e `/v3/api-docs` |

### Regras críticas

- Término posterior ao início; reservas só com início futuro.
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
| Disponibilidade | `GET /api/v1/disponibilidade`, `GET /api/v1/disponibilidade/recursos/{id}/agenda` |
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

Roda os 247 testes contra um **PostgreSQL real em container** (Testcontainers; precisa de Docker) e aplica o gate de cobertura (falha abaixo de 80% de linhas ou 70% de branches). Os testes E2E usam o Chromium do Playwright; localmente são pulados se o navegador não estiver instalado (`./mvnw exec:java -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"`).

| Documento | Conteúdo |
|---|---|
| [`docs/qa/PLANO_DE_TESTES.md`](docs/qa/PLANO_DE_TESTES.md) | Estratégia, níveis, ferramentas, critérios de saída |
| [`docs/qa/RELATORIO_DE_TESTES.md`](docs/qa/RELATORIO_DE_TESTES.md) | Resultado por classe, cobertura por pacote, evidência de TDD |
| [`RTM.md`](RTM.md) | Requisito → risco → teste → evidência |
| [`docs/qa/REGISTRO_DEFEITOS.md`](docs/qa/REGISTRO_DEFEITOS.md) | 12 defeitos encontrados, priorizados e tratados |
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

## 6. Ações manuais pendentes (dono do repositório)

1. **SonarCloud:** criar a organização `joaopaulofserafim` e o projeto `joaopaulofserafim_projeto_final_QAS` em sonarcloud.io (importando o repositório) e salvar o token como segredo `SONAR_TOKEN` em *Settings → Secrets and variables → Actions*. O pipeline passa a enviar a análise automaticamente. Se a organização/chave forem diferentes, ajuste `sonar.organization` e `sonar.projectKey` no `pom.xml`.
2. **Proteção da `main`:** em *Settings → Branches*, exigir pull request com 1 aprovação e o check `Build, testes e cobertura` verde.
3. **Revisão por pares:** abrir o PR de `feat/epic-1-mvp` para `main` e obter a revisão de um colega de equipe antes do merge.

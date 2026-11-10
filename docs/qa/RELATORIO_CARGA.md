# Plano e resultado do teste de carga (JMeter)

**Story:** 1.10 · **Data da execução:** 09/11/2026 · **Plano:** [`src/test/jmeter/carga-sor.jmx`](../../src/test/jmeter/carga-sor.jmx)
**Metas (DP-014):** 50 usuários simultâneos; p95 ≤ 500 ms nas consultas; p95 ≤ 1 s na criação de reserva; 0% de erros 5xx; zero dupla reserva depois da carga.

## 1. Plano

| Grupo | Usuários | Duração | O que faz | Critério |
|---|---|---|---|---|
| Preparação | 1 | — | Cria duas salas novas (código com carimbo de tempo) e publica os ids. O teste não depende do estado do banco. | 2xx |
| Corrida pelo mesmo horário | 30 | 1 rodada | 30 `POST /api/v1/reservas` liberados **no mesmo instante** (Synchronizing Timer) para a mesma sala e o mesmo período. | Só 201 ou 409; no banco, exatamente 1 ocupação ativa |
| Consultas sob carga | 50 | 60 s (+10 s de rampa) | Login por formulário uma vez (sessão), depois em laço: pesquisa de salas, consulta de disponibilidade, pesquisa com filtro de disponibilidade e "minhas reservas"; pausa de 200–800 ms. | 200 |
| Reservas sob carga | 10 | 60 s | Criação contínua de reservas (HTTP Basic) em dias distintos na sala própria; pausa de 300–1000 ms. | 201 |

O gate do plugin (`errorRateThresholdInPercent = 1`) falha o build com mais de 1% de erro.

## 2. Ambiente

- Aplicação: JAR `sistema-organizacao-recursos-1.0.0-rc1.jar`, Java 21.0.12.1, `-Xmx1g`, 4 vCPUs.
- Banco: PostgreSQL 16 (container local), mesmo host.
- JMeter 5.6.3 via `jmeter-maven-plugin` 3.8.0 (`./mvnw -Pcarga verify`).
- Limitação: aplicação, banco e gerador de carga na mesma máquina; os números servem para comparar com as metas acadêmicas, não para dimensionar produção.

## 3. Procedimento

```bash
docker compose up -d banco
./mvnw -DskipTests package && cp target/*.jar /tmp/sor.jar && java -jar /tmp/sor.jar   # rodar a partir de uma cópia
./mvnw -Pcarga verify -Dsor.carga.duracao=30    # aquecimento
./mvnw -Pcarga verify                            # medição oficial
```

A aplicação deve rodar a partir de uma **cópia** do JAR: o `verify` do perfil reempacota o JAR em `target/` (ver DEF-010 e a seção 6).

## 4. Resultado da medição oficial (após aquecimento)

**Total: 6.164 requisições em 66 s (94 req/s), 0 erros (0,00%).**

| Operação | N | Erro | Média (ms) | p90 | p95 | p99 | Máx |
|---|---:|---:|---:|---:|---:|---:|---:|
| GET /recursos?tipo=SALA | 1.359 | 0% | 2 | 4 | 6 | 9 | 13 |
| GET /disponibilidade | 1.343 | 0% | 3 | 6 | 7 | 13 | 25 |
| GET /recursos com filtro de disponibilidade | 1.327 | 0% | 4 | 6 | 8 | 13 | 26 |
| GET /reservas/minhas | 1.318 | 0% | 3 | 6 | 7 | 11 | 22 |
| POST /auth/login | 50 | 0% | 84 | 94 | 103 | 110 | 110 |
| POST /reservas (sem conflito) | 734 | 0% | 88 | 98 | 106 | 123 | 166 |
| POST /reservas (corrida, 30 simultâneos) | 30 | 0% | 607 | 684 | 684 | 684 | 684 |

Arquivo bruto: [`evidencias/carga/statistics-oficial-2026-11-09.json`](evidencias/carga/statistics-oficial-2026-11-09.json).

### Integridade após a carga (consultas SQL no banco)

| Verificação | Resultado |
|---|---|
| Respostas da corrida | **1 × 201 e 29 × 409** |
| Ocupações ativas na sala da corrida | **1** (em cada uma das duas execuções) |
| Conflitos evitados registrados na sala da corrida | 29 por execução |
| Pares de ocupações ativas sobrepostas no banco inteiro | **0** |

## 5. Comparação com as metas

| Meta (DP-014) | Medido | Situação |
|---|---|---|
| 50 usuários simultâneos | 50 + 10 escrevendo + 30 na corrida | Atendida |
| p95 ≤ 500 ms nas consultas | 6–8 ms | Atendida |
| p95 ≤ 1 s na criação | 106 ms (operação normal); 684 ms na corrida de 30 pedidos simultâneos | Atendida |
| 0% de erros 5xx | 0 erros em 6.164 requisições | Atendida |
| Zero dupla reserva | 0 sobreposições; 1 aceite por corrida | Atendida |

## 6. Problemas encontrados durante a execução

| Defeito | O que aconteceu | Tratamento |
|---|---|---|
| DEF-010 | O plugin do JMeter foi pulado em silêncio porque obedece a `skipTests`, usada no perfil. | O perfil desliga apenas o Surefire. |
| DEF-011 | Na 2ª rodada, 347 pedidos de escrita (12%) receberam 409: o plano sorteava o dia e colidia com reservas da rodada anterior. Defeito do **plano**, não da aplicação. | A preparação cria salas novas a cada execução; a escrita usa dias sequenciais nelas. Duas execuções seguidas no mesmo banco: 0% de erro. |
| DEF-012 | Primeira rodada com a JVM recém-iniciada: a corrida levou ~2,1 s (BCrypt + compilação JIT). | Procedimento com aquecimento; com a JVM aquecida, p95 de 684 ms. |
| — | Na primeira tentativa a aplicação parou de responder: o `verify` reempacotou o JAR que estava em execução. | Rodar sempre a partir de uma cópia (seção 3). |

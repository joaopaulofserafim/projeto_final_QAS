# Tech stack do projeto (carregado pelo @dev)

| Camada | Escolha | Versão |
|---|---|---|
| Linguagem | Java | 21 |
| Framework | Spring Boot (web, data-jpa, validation, security, actuator) | 3.5.16 |
| Banco | PostgreSQL + Flyway | 16 |
| Testes | JUnit 5, AssertJ, Testcontainers, WireMock, Playwright (Java), Spring Security Test | — |
| Cobertura | JaCoCo (gate: 80% linhas, 70% branches) | 0.8.13 |
| Análise estática | SonarCloud (`sonar-maven-plugin`) | 5.8 |
| Carga | JMeter via `jmeter-maven-plugin` (perfil `carga`) | 3.8.0 |
| Documentação de API | springdoc-openapi | 2.8.17 |
| Interface | HTML + Bootstrap 5 (WebJar) + JavaScript sem framework | 5.3.8 |
| Build | Maven Wrapper (`./mvnw`) | 3.9 |

## Comandos (use estes; não use npm)

```bash
docker compose up -d banco            # PostgreSQL local
./mvnw spring-boot:run                # aplicação em http://localhost:8080
./mvnw verify                         # todos os testes + gate de cobertura (precisa de Docker)
./mvnw -Pcarga verify                 # teste de carga (aplicação rodando)
```

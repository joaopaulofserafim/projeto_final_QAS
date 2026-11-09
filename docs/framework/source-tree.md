# Estrutura do código

```text
src/main/java/br/edu/sor/
├── compartilhado/      relógio institucional e tratamento de erros (ProblemDetail)
├── identidade/         usuários, perfis, Spring Security, login/logout
├── auditoria/          eventos imutáveis e consulta
├── catalogo/           recursos (sala, professor, material, equipamento) e pesquisa
├── disponibilidade/    período, ocupações, conflitos, conflitos evitados
├── reservas/           reservas, estados e transições
├── manutencao/         bloqueios de manutenção
├── movimentacao/       retirada e devolução de materiais/equipamentos
├── notificacao/        notificações simuladas ou via API externa
└── relatorios/         utilização, carga horária, conflitos evitados e resumo
src/main/resources/
├── db/migration/       esquema (Flyway)  ·  db/demo/ dados de demonstração
└── static/             interface web responsiva
src/test/java/br/edu/sor/
├── suporte/            IntegracaoBase (Testcontainers), cliente Api, relógio controlável
├── e2e/                testes de ponta a ponta (Playwright)
└── <módulo>/           testes unitários e de integração por módulo
src/test/jmeter/        plano de carga
```

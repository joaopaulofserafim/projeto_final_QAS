# Evidência TDD — Story 1.5 — fase GREEN

Data: 2026-11-09. Commit: feat(1.5) GREEN (implementação mínima para os testes do commit RED).

```text
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 27.80 s -- in br.edu.sor.reservas.ReservaApiTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.485 s -- in br.edu.sor.reservas.ConcorrenciaReservaTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.357 s -- in br.edu.sor.reservas.ConstraintExclusaoTest
[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 0
```

Repetição do teste de concorrência (3 rodadas consecutivas, 5 cenários cada): 3/3 verdes.

Suíte completa após o GREEN: `./mvnw verify` → 132 testes, 0 falhas; JaCoCo 91,2% linhas e 74,9% branches (gate 80/70 atendido).

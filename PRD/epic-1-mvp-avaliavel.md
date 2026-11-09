# Epic 1 — Sistema de Organização de Recursos avaliável (Release Candidate)

**Objetivo:** entregar, até a semana 46, uma aplicação executável que cumpra os 14 requisitos funcionais do enunciado e elimine os limitadores de nota (aplicação que não executa, pipeline ausente, falha de autorização, possibilidade de dupla reserva), com evidências de qualidade.
**Decisões de base:** `PRD/requisitos/PENDENCIAS_DECISOES.md` (registro de 09/11/2026) e ADR-001..ADR-009 (aceitos).
**Regra de execução:** uma história de backend por vez; cada história passa por implementação com TDD → QA gate → revisão humana no PR.

| Story | Título | Requisitos | Status |
|---|---|---|---|
| 1.1 | Validar períodos e identificar sobreposição | RF-006, RN-001, RN-002 | Done (gate retroativo) |
| 1.2 | Pipeline de CI, cobertura e análise estática | RNF-013, RNF-014 | ver story |
| 1.3 | Fundação: persistência, segurança e erros | RF-001..RF-003, RF-022, RNF-001..RNF-003 | ver story |
| 1.4 | Catálogo e pesquisa de recursos | RF-004, RF-005, RF-023..RF-025 | ver story |
| 1.5 | Reservas, disponibilidade e proteção contra dupla reserva | RF-007..RF-009, RF-011, RF-018, RF-030 | ver story |
| 1.6 | Aprovação e ciclo de vida da reserva | RF-010, RF-012..RF-017, RF-027 | ver story |
| 1.7 | Manutenção, retirada e devolução | RF-019..RF-021, RF-026 | ver story |
| 1.8 | Notificações, auditoria e relatórios | RF-028..RF-036 | ver story |
| 1.9 | Interface responsiva, E2E e documentação da API | RNF-009, RNF-010, DP-012 | ver story |
| 1.10 | Carga, segurança e pacote de evidências | RNF-004..RNF-008 | ver story |

**Linha de corte do RC:** 1.1–1.7 e a documentação de API são obrigatórias; 1.8–1.10 completam os entregáveis. O que não fechar com qualidade é declarado como lacuna no relatório final de qualidade.

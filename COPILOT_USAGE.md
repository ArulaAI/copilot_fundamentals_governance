# Copilot Usage Log

A governance artifact: which Copilot feature you used at each stage, the prompt intent, and the outcome. Fill one row per stage as you go (a stage may use more than one surface).

| Stage | Copilot surface (built-in / agent) | Prompt intent | Outcome / artifact produced |
|-------|------------------------------------|---------------|------------------------------|
| 1 | Built-in `/explain` | Comprehend + enumerate weaknesses per file | `VULNERABILITIES.md` |
| 2 | `java-planning` → `java-validation` | Build + validate remediation plan | `docs/plans/plan.md` |
| 3 | `/tests` (V1) + `java-testing` (V2) | Generate failing security tests | two red tests |
| 4 | `java-scrum-master` → Agent ↔ `java-need-review` | Slice, fix V1/V2, review | `FIXES.md`, `plan.tasks.md` |
| 5 | `java-planning` → `java-validation` | Author + validate secure-future guide | `docs/secure-features-guide.md` |
| 6 | `java-testing` → `java-summarizer` | Final gates + closing hand-off | `SECURITY.md`, `docs/workflow-tracker.md` |
| — | `/hand-off` (`java-summarizer`) | End-of-stage summary (every stage) | `docs/workflow-tracker.md` |

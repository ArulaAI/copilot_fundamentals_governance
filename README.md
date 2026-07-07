# GitHub Copilot Governance Lab — Java (Spring Boot)

A hands-on, 90-minute lab for practicing **secure, governed GitHub Copilot usage** on a real Spring Boot application. You comprehend an intentionally vulnerable codebase, register and prioritize its risks, plan and test remediations, fix the top two vulnerabilities under review, and produce an auditable trail — all driven through Copilot Agent Mode and a set of custom `.github/agents`.

> ⚠️ **This application is intentionally vulnerable.** It exists only for training. Do **not** deploy it or reuse its code in production.

---

## What this is

- **One app, one flow.** A small Spring Boot 3 / Java 17 auth-and-session surface: one service, one repository, two controllers, two Thymeleaf views, and the models that connect them.
- **Behavior-driven vulnerabilities.** The app **builds green and runs**; its weaknesses surface through *behavior*, not build failures. That keeps the lab fast and the signal clean.
- **Governance-first.** Every stage ends with a `/hand-off` summary appended to `docs/workflow-tracker.md`, giving you a complete audit trail of what Copilot did and why.

The lab uses two custom Copilot surfaces:
- **Custom agents** in `.github/agents/` (`java-planning`, `java-validation`, `java-testing`, `java-need-review`, `java-summarizer`).
- **Guardrails** in `.github/instructions/java.instructions.md`, applied automatically to Java/config files.

---

## Prerequisites

- **Java 17+** (Temurin/OpenJDK)
- **Maven 3.9+**
- **VS Code** with **GitHub Copilot** (Agent Mode enabled)

Verify your toolchain:

```bash
./scripts/setup-lab.sh
```

---

## How to run

Open this folder in VS Code (the root `.github/` makes the custom agents discoverable), then:

```bash
mvn validate      # baseline sanity
mvn test          # 1 test passes on the untouched app
mvn verify        # full gate, green baseline
mvn spring-boot:run
```

The app serves **http://localhost:8080** (login page → dashboard). The baseline is green; the two security tests you write during the lab are the only ones that should go red before remediation.

---

## The 90-minute flow at a glance

One continuous flow, six stages (times sum to 90). Run `/hand-off` at the **end of every stage**.

| # | Stage | Min | Copilot surface |
|---|-------|-----|-----------------|
| 1 | Setup, Comprehend & Register | 30 | Built-in `/explain` (per file) |
| 2 | Plan | 12 | `java-planning` → `java-validation` |
| 3 | Security Test Generation (top 2) | 14 | `/tests` (V1), `java-testing` (V2) |
| 4 | Remediation (top 2 only) | 20 | Agent ↔ `java-need-review` |
| 5 | Secure-Future Implementation | 8 | `java-planning` → `java-validation` |
| 6 | Governance Validation & Reporting | 6 | `java-testing` → `java-summarizer` |

**Red → Green:** the security tests assert *secure* behavior, so they **fail before remediation** — that failure is the proof the vulnerability is real. Two reds are expected at the Stage 3 checkpoint; everything else stays green.

---

## Where to go next

- **`LAB_ACTION_GUIDE.md`** — the single source of truth: full stage-by-stage actions, the red→green checkpoint, and ready-to-paste Copilot prompt templates for every step.

### Live-filled artifacts
| File | Filled during |
|------|---------------|
| `VULNERABILITIES.md` | Stage 1 (prioritized register) |
| `docs/plans/plan.md` | Stage 2 (created by `java-planning`) |
| `FIXES.md` | Stage 4 (one row per remediated slice) |
| `docs/secure-features-guide.md` | Stage 5 |
| `docs/workflow-tracker.md` | every stage (via `/hand-off`) |

### Facilitator reference
- **`docs/FACILITATOR_KEY.md`** — the full vulnerability answer key. **Facilitators only.**

---

## Copilot Chat quick reference

> `#` variables and `@` participants → VS Code only. Slash commands (`/explain`, `/fix`, `/tests`, `/doc`, `/simplify`) → available in both VS Code and IntelliJ.

---

*Copyright © 2025 InRhythm, Inc. and Arula.AI. All Rights Reserved. Licensed for use by authorized participants of Arula.AI training programs only — see [LICENSE](LICENSE).*

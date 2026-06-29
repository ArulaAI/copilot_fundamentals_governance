# Lab Action Guide — GitHub Copilot Governance Lab (Java)

---

## Workspace setup

1. **Open the repository root in VS Code.** Not a subfolder. The root `.github/` is what makes Copilot discover the custom agents and the `/hand-off` command.
   - `.github/agents/` — the six `java-*` agents (planning, validation, testing, need-review, scrum-master, summarizer)
   - `.github/instructions/java.instructions.md` — auto-attached coding/security guardrails for every `.java`/`.properties`/`.xml` file
   - `.github/prompts/hand-off.prompt.md` — the `/hand-off` slash command
2. **The terminal is already at the app root** — `pom.xml` is right here. All `mvn` commands run from this directory.
3. **If the Agent Mode dropdown is empty or `/hand-off` isn't offered**, your workspace is opened too deep — reopen at the repository root.

---

## The flow at a glance

| # | Stage | Min | Copilot surface | Key artifacts produced |
|---|-------|-----|-----------------|-------------------------|
| 1 | Setup, Comprehend & Register | 30 | **Built-in `/explain`** (mention `/tests`, `/fix`) | green baseline, app demoed, `VULNERABILITIES.md` (prioritized) |
| 2 | Plan | 12 | **java-planning → java-validation** | `docs/plans/plan.md` (Steps 1–2 = V1, V2) |
| 3 | Security Test Generation (top 2) | 14 | `/tests` (V1), **java-testing** (V2) | two **failing** security tests; red-proof checkpoint |
| 4 | Remediation (top 2 only) | 20 | **java-scrum-master → Agent ↔ java-need-review** | V1+V2 fixed, `plan.tasks.md` backlog, registries updated, tests green |
| 5 | Secure-Future Implementation | 8 | **java-planning → java-validation** | `docs/secure-features-guide.md` (no new code) |
| 6 | Governance Validation & Reporting | 6 | **java-testing → java-summarizer** | final `mvn validate/test/verify`, `SECURITY.md` updated, final `/hand-off`, recap |

The **Bonus** appendix at the end is optional.

### The two named vulnerabilities we fix
- **V1 — Plaintext password exposure (OWASP A02).** Password logged via `System.out.printf` in `AuthService.login`; Base64 `encodedPassword` returned in `AuthResponse` and in the `X-Encoded-Password` response header (`ApiController.login`).
  - **In-scope fix (fits 20 min):** remove the password log (use SLF4J, no secrets); drop `encodedPassword` from `AuthResponse` and remove the `X-Encoded-Password` header.
  - **Backlog (document, do NOT fix here):** Base64 password on disk (`InsecureSessionRepository`), `plainPassword` in `HttpSession`, password rendered in `dashboard.html`.
- **V2 — Automatic privilege escalation (OWASP A01).** `user.getRoles().add("admin")` runs on every login in `AuthService.login`.
  - **In-scope fix:** remove the unconditional admin grant; normal users get `user` only.

---

## test model

Security tests assert **secure** behavior. They therefore **fail before remediation** — and that failure is the *evidence the vulnerability is real*. Remediation turns them green.

> **At the Stage 3 checkpoint, two reds are expected — they prove the vulnerabilities. Everything else stays green.**

Do **not** write characterization tests (tests that lock in current insecure behavior). We assert the *desired* secure state and let it fail first.

---

## Stage 1 — Setup, Comprehend & Register (30 min)

**Goal:** green baseline, app running and demoed, and a prioritized `VULNERABILITIES.md`.

1. **Verify the build is green.** In the terminal:
   ```bash
   ./scripts/setup-lab.sh      # checks Java 17+, Maven, runs mvn validate
   mvn validate && mvn test && mvn verify
   ```
   One existing test passes; the build is green. Vulnerabilities surface through *behavior*, not build failures.
2. **Run the app.** `mvn spring-boot:run` → open **http://localhost:8080** (login → dashboard).
3. **Vulnerabilities to notice:**
   - `POST /api/login` with an **empty body** succeeds as `guest`/`password`.
   - `curl http://localhost:8080/api/debug/sessions` returns the session dump **unauthenticated**.
   - The dashboard renders the **plaintext password** in the page.
   Stop the app with `Ctrl+C`.
4. Skim `SECURITY.md` (reporting policy, the *Known Risks* and *Security Controls* tables you'll fill later) and `.github/instructions/java.instructions.md` (the guardrails Copilot enforces).
5. **Run `/explain` on one pinned file at a time** — `AuthService.java`, `ApiController.java`, `InsecureSessionRepository.java`, `PageController.java`, `dashboard.html`. Paste this prompt for each file, swapping the filename:

   ```text
   /explain Review AuthService.java. Summarize its major responsibilities,
   its dependencies, and any hidden side effects. Then list security weaknesses as
   a table: weakness | OWASP category | affected asset | one-line impact. No fixes.
   ```
6. **Fill `VULNERABILITIES.md`** — one row per weakness.
7. **End of stage:** update `COPILOT_USAGE.md`, then run **`/hand-off`**.

---

## Stage 2 — Plan (12 min)

**Goal:** turn the register into an ordered, validated remediation plan where **Step 1 = V1** and **Step 2 = V2**.

1. **java-planning** — produce the plan:
   ```text
   Using VULNERABILITIES.md, produce a multi-step remediation plan. For each step:
   target file(s), one-line fix, expected post-fix state, success criterion.
   Step 1 = V1 (plaintext password exposure), Step 2 = V2 (automatic privilege
   escalation) — the two highest-priority items. Save to docs/plans/plan.md.
   ```
2. **java-validation** — check the plan against the guardrails:
   ```text
   Validate docs/plans/plan.md against .github/instructions/java.instructions.md.
   Return pass/fail and required corrections before Stage 3.
   ```
3. Confirm `docs/plans/plan.md` exists.
4. **End of stage:** update `COPILOT_USAGE.md`, then run **`/hand-off`**.

---

## Stage 3 — Security Test Generation (top 2) (14 min)

**Goal:** two tests that assert secure behavior and therefore **fail now** (red-proof).

1. **Built-in `/tests`** — V1 test:
   ```text
   /tests Write Five JUnit 5 + MockMvc test asserting the SECURE behavior for V1:
   a successful POST /api/login returns NO X-Encoded-Password header and the body
   exposes no password field.
   ```
2. **java-testing** — V2 test:
   ```text
   Generate five failing JUnit 5 tests for V2: assert a normal login's roles do NOT
   contain "admin". Deterministic. Summarize pass/fail only.
   ```
3. **Checkpoint:** `mvn test` 
4. **End of stage:** update `COPILOT_USAGE.md`, then run **`/hand-off`**.

---

## Stage 4 — Remediation (top 2 ONLY) (20 min)

**Goal:** fix V1, then V2 — smallest diffs — with a review pass per slice, and convert the rest into an owned backlog.

1. **java-scrum-master** — paste this prompt to slice the work and capture the backlog:
   ```text
   Break remediation into tracked task slices with acceptance criteria and an owner:
   Task 1 = V1, Task 2 = V2. Then convert every remaining Open vulnerability in
   VULNERABILITIES.md into a backlog task (owner, acceptance criterion) — these are
   NOT fixed in this session. Save to docs/plans/plan.tasks.md.
   ```
2. **Agent** — paste this prompt to remediate V1:
   ```text
   Fix V1 only, smallest diff: remove the password log (use SLF4J, no secrets) and
   stop returning the Base64 password (AuthResponse field + X-Encoded-Password header).
   Make the V1 test pass. Do not touch unrelated code or the V1 backlog items.
   ```
3. **java-need-review** — paste this prompt to review the slice:
   ```text
   Review this remediation slice for security + guardrail compliance against
   java.instructions.md. Return critical/high issues only.
   ```
4. **Update registries for V1:** set its row in `VULNERABILITIES.md` to **Remediated**; add a new row to `FIXES.md`. Confirm the **V1 test goes green**.
5. **Repeat steps 2–4 for V2**, swapping the Agent prompt for: *"Fix V2 only, smallest diff: remove the unconditional `getRoles().add("admin")` grant in `AuthService.login`. Make the V2 test pass. Do not touch unrelated code or the V2 backlog items."* Then **stop.** Remaining vulnerabilities stay `Open` in `VULNERABILITIES.md` and live as tasks in `plan.tasks.md` = the named backlog.
6. **End of stage:** update `COPILOT_USAGE.md`, then run **`/hand-off`**.

---

## Stage 5 — Secure-Future Implementation (8 min)

**Goal:** describe the proactive controls to adopt next — **no new code**.

1. **java-planning** — paste this prompt to author the guide:
   ```text
   Write docs/secure-features-guide.md describing proactive controls to adopt next:
   Spring Security filter chain, security headers, SLF4J audit logging, upload
   allow-list, secure cookie flags. No code changes. Then validate the guide.
   ```
2. **java-validation** — validate the guide against the guardrails.
3. **End of stage:** update `COPILOT_USAGE.md`, then run **`/hand-off`**.

---

## Stage 6 — Governance Validation & Reporting (6 min)

**Goal:** prove the final state and close the audit trail.

1. **java-testing** — run the final gates in the terminal:
   ```bash
   mvn validate && mvn test && mvn verify
   ```
   All green — now including the two security tests (V1, V2 went green in Stage 4).
2. **Update `SECURITY.md`:** record the two controls added (V1, V2 fixes) in the *Security Controls* table, and the remaining Open vulnerabilities as *Known Risks / Accepted* (the backlog).
3. **Final `/hand-off`** (java-summarizer) → the closing entry in `docs/workflow-tracker.md`.
4. **Recap for the room:**
   - built-in `/explain` vs. the custom-agent loop;
   - what shipped: **2 traced fixes** (register → plan → failing test → fix → review → green);
   - the **named backlog** that was deliberately *documented, not fixed*.

---

## Artifact checklist (everything should be used)

| Artifact | Used in |
|---|---|
| `.github/agents/java-planning` | Stages 2, 5 |
| `.github/agents/java-validation` | Stages 2, 5 |
| `.github/agents/java-testing` | Stages 3, 6 |
| `.github/agents/java-need-review` | Stage 4 |
| `.github/agents/java-scrum-master` | Stage 4 (slices + backlog) |
| `.github/agents/java-summarizer` (`/hand-off`) | every stage |
| `.github/instructions/java.instructions.md` | Stages 1, 2, 4, 5 (guardrail reference) |
| Built-in `/explain` | Stage 1 |
| Built-in `/tests` | Stage 3 (V1) |
| `VULNERABILITIES.md` | Stages 1, 4 |
| `FIXES.md` | Stage 4 |
| `COPILOT_USAGE.md` | every stage |
| `docs/workflow-tracker.md` | every stage (`/hand-off`) |
| `docs/plans/plan.md` | Stage 2 |
| `docs/plans/plan.tasks.md` | Stage 4 |
| `docs/secure-features-guide.md` | Stage 5 |
| `SECURITY.md` | Stages 1 (orient), 6 (record) |
| `docs/FACILITATOR_KEY.md` | facilitator reference (answer key) |

---

## Bonus (optional): Coverage as Governance Evidence

A governance lab cares about *what was verified*, not just what was built. Use this only if time allows — it is **observation, not a gate, and adds no code**.

1. Run `mvn verify` (JaCoCo is already wired into the build).
2. Open the report at **`target/site/jacoco/index.html`**.
3. Read it as evidence, and discuss:
   - Which classes/branches are **covered** vs. **unverified** (e.g., the endpoints and branches no test exercises)?
   - Now that V1 and V2 have tests, what does coverage tell you about the *backlog* — the code paths we deliberately left unfixed and unverified?
   - In a real audit, coverage is **evidence of assurance**: it shows reviewers exactly which security behaviors are proven and which are only asserted in prose.
4. There is **no target percentage** here. The point is to reason about coverage as a governance signal — not to chase a number or generate tests under time pressure.

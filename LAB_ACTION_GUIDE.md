# Lab Action Guide — GitHub Copilot Fundamentals and Governance Lab (Java)

---

## Workspace setup

1. **Open the repository root in VS Code.** Not a subfolder. The root `.github/` is what makes Copilot discover the custom agents and the `/hand-off` command.
   - `.github/agents/` — the five `java-*` agents used in this lab: `java-planning`, `java-validation`, `java-testing`, `java-need-review`, `java-summarizer`
   - `.github/instructions/java.instructions.md` — auto-attached coding/security guardrails for every `.java`/`.properties`/`.xml` file
   - `.github/prompts/hand-off.prompt.md` — the `/hand-off` slash command
2. **The terminal is already at the app root** — `pom.xml` is right here. All `mvn` commands run from this directory.
3. **If the Agent Mode dropdown is empty or `/hand-off` isn't offered**, your workspace is opened too deep — reopen at the repository root.

---

## The flow at a glance

| # | Stage | Min | Copilot surface | Key artifacts produced |
|---|-------|-----|-----------------|-------------------------|
| 1 | Setup, Comprehend & Register | 30 | **Built-in `/explain`** (mention `/tests`, `/fix`) | green baseline, app demoed, `VULNERABILITIES.md` (prioritized) |
| 2 | Plan | 12 | **java-planning → java-validation** | `docs/plans/plan.md` (Critical findings first) |
| 3 | Security Test Generation (top 2) | 14 | `/tests` (plan step 1), **java-testing** (plan step 2) | two **failing** security tests; red-proof checkpoint |
| 4 | Remediation (top 2 only) | 20 | **Agent ↔ java-need-review** | top 2 findings fixed, registries updated, tests green |
| 5 | Secure-Future Implementation | 8 | **java-planning** | `docs/secure-features-guide.md` (no new code) |
| 6 | Governance Validation & Reporting | 6 | **java-summarizer** | final `mvn validate/test/verify`, `SECURITY.md` updated, final `/hand-off`, recap |

The **Bonus** appendix at the end is optional.

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
   mvn validate
   mvn test
   mvn verify
   ```
   One existing test passes; the build is green. Vulnerabilities surface through *behavior*, not build failures.
2. **Run the app.** `mvn spring-boot:run` → open **http://localhost:8080** (login → dashboard).
3. **Vulnerabilities to notice:**
   - `POST /api/login` with an **empty body** succeeds as `guest`/`password`.
   - `curl http://localhost:8080/api/debug/sessions` returns the session dump **unauthenticated**.
   - The dashboard renders the **plaintext password** in the page.
   Stop the app with `Ctrl+C`.
4. Skim `SECURITY.md` (reporting policy, the *Known Risks* and *Security Controls* tables you'll fill later) and `.github/instructions/java.instructions.md` (the guardrails Copilot enforces).
5. **Run `/explain` on ** — `AuthService.java`, `ApiController.java`, `InsecureSessionRepository.java`, `PageController.java`, `dashboard.html`. Paste this prompt:

   ```text
   /explain Review these files together: AuthService.java, ApiController.java,
   InsecureSessionRepository.java, PageController.java.

   For each file, briefly summarize in a table :
   file | responsibility | key dependencies | hidden side effects

   Do not suggest fixes.
   Do not create the vulnerability table yet.
   ```
6. **Fill `VULNERABILITIES.md`** — one row per weakness.
   ```text
   Using the application behavior observed during testing and your review of the source files, complete the `VULNERABILITIES.md` template.

   For each vulnerability you identify:
   - Assign a unique ID (for example, V1, V2, V3...).
   - Give the vulnerability a concise name.
   - Assign an appropriate severity (Critical, High, Medium, or Low).
   - Map it to the most relevant OWASP Top 10 category.
   - List the affected file(s).
   - Describe the security impact in one concise sentence.
   - Set the status to `Open`.

   Document only vulnerabilities that are supported by the current code and observed application behavior.
   ```
7. **End of stage:** run **`/hand-off`**.

---

## Stage 2 — Plan (12 min)

**Goal:** turn the register into an ordered, validated remediation plan with Critical findings first.

1. **java-planning** — produce the plan:
   ```text
   Using VULNERABILITIES.md, produce a multi-step remediation plan. For each step:
   target file(s), one-line fix, expected post-fix state, success criterion.
   Address Critical-severity findings first, then High severity. Number the steps
   in priority order. Save to docs/plans/plan.md.
   ```
2. **java-validation** — check the plan against the guardrails:
   ```text
   Validate docs/plans/plan.md against .github/instructions/java.instructions.md.
   Return pass/fail and required corrections before Stage 3.
   ```
3. Confirm `docs/plans/plan.md` exists.
4. **End of stage:** run **`/hand-off`**.

---

## Stage 3 — Security Test Generation (top 2) (14 min)

**Goal:** two tests that assert secure behavior and therefore **fail now** (red-proof).

1. **Built-in `/tests`** — test for plan step 1 (credential-exposure finding):
   ```text
   /tests Write 2-3 JUnit 5 + MockMvc tests asserting the SECURE behavior for the
   first remediation step in plan.md: Verify that:
   - No password or encoded password is returned in the response.
   - The `X-Encoded-Password` header is not present.
   - Credentials or authentication tokens are not persisted to `target/session-store.txt`.

   Keep the tests deterministic and follow existing project conventions.
   ```
2. **java-testing** — test for plan step 2 (privilege-escalation finding):
   
   ```text
   Generate 2–3 focused JUnit 5 tests for the second remediation step in docs/plans/plan.md.

   Verify that:
   - a normal authenticated user is not assigned the `admin` role.
   - authorization defaults to deny when the user or token is missing.
   
   Keep the tests deterministic.
   Summarize expected pass/fail only.
   Do not modify production code.
   ```
3. **Checkpoint:** `mvn test`
4. **End of stage:** run **`/hand-off`**.

---

## Stage 4 — Remediation (top 2 ONLY) (20 min)

**Goal:** fix plan steps 1 and 2 — smallest diffs — with a review pass per slice. Remaining Open findings in `VULNERABILITIES.md` are the documented backlog; do not fix them.

1. **Agent** — fix plan step 1:
   ```text
   Fix remediation plan step 1 only using the smallest possible code changes.

   Implement the secure behavior by:
   - Stopping the persistence of credentials or authentication tokens to `target/session-store.txt`.
   - Removing any password or encoded password from API responses.
   - Removing the `X-Encoded-Password` response header.

   Make the Stage 3 security tests for remediation step 1 pass.

   Do not modify unrelated functionality or begin work on any other remediation plan items.
   ```
2. **java-need-review** — review the slice:
   ```text
   Review only the changes made for remediation slice.

   Validate that:

   The implementation satisfies the objectives of remediation plan step 1.
   The changes comply with #file:java.instructions.md .
   Ignore vulnerabilities that belong to other remediation plan items or   remain intentionally open in VULNERABILITIES.md.

   Return only findings related to this remediation slice along with PASS/FAIL.
   ```
3. **Update registries for plan step 1:** mark its row in `VULNERABILITIES.md` as **Remediated**; add a row to `FIXES.md`. Confirm the step 1 security test is **green**.
4. **Agent** — fix plan step 2:
   ```text

   Fix remediation plan step 2 only using the smallest possible code changes.

   Implement the secure behavior described in the second remediation step in docs/plans/plan.md. remove the unconditional getRoles().add("admin") grant in AuthService.login.
   Make the Stage 3 security tests for remediation step 2 pass.

   Do not modify unrelated functionality.
   Do not begin work on later remediation plan items.
   ```
5. **java-need-review** — review the slice:
   ```text
      Review only the implementation for remediation plan step 2.

   Validate that:
   - the implementation satisfies the objectives of remediation plan step 2.
   - the changes comply with #file:java.instructions.md.

   Ignore vulnerabilities assigned to other remediation plan items or intentionally left Open in VULNERABILITIES.md.

   Return only findings related to this remediation slice with PASS/FAIL.
   ```
6. **Update registries for plan step 2:** mark its row in `VULNERABILITIES.md` as **Remediated**; add a row to `FIXES.md`. Confirm the step 2 security test is **green**.
7. Run `mvn test`. If any failures occur, use `/fix`, then rerun. All other Open rows in `VULNERABILITIES.md` are the deliberate backlog — do not fix them.
8. **End of stage:** run **`/hand-off`**.

---

## Stage 5 — Secure-Feature Implementation (8 min)

**Goal:** describe the proactive controls to adopt next — **no new code**, no handoff.

1. **java-planning** — paste this prompt to author the guide:
   ```text
   Write docs/secure-features-guide.md describing proactive controls to adopt next:
   Spring Security filter chain, security headers, SLF4J audit logging, upload
   allow-list, secure cookie flags. No code changes.
   ```

---

## Stage 6 — Governance Validation & Reporting (6 min)

**Goal:** prove the final state and close the audit trail.

1. Run the final gates in the terminal:
   ```bash
   mvn validate 
   mvn verify
   ```
   All green — now including the two security tests (plan steps 1 and 2 went green in Stage 4).
2. **Update `SECURITY.md`:** record the two controls added (plan steps 1 and 2 fixes) in the *Security Controls* table, and the remaining Open vulnerabilities as *Known Risks / Accepted* (the backlog).
3. **Review `docs/workflow-tracker.md`** and confirm a hand-off entry exists for every completed stage.
4. **Final `/hand-off`** (java-summarizer) → the closing entry in `docs/workflow-tracker.md`.
5. **Recap for the room:**
   - built-in `/explain` vs. the custom-agent loop;
   - what shipped: **2 traced fixes** (register → plan → failing test → fix → review → green);
   - the **documented backlog** that was deliberately *registered, not fixed*.

---

## Artifact checklist (everything should be used)

| Artifact | Used in |
|---|---|
| `.github/agents/java-planning` | Stages 2, 5 |
| `.github/agents/java-validation` | Stage 2 |
| `.github/agents/java-testing` | Stage 3 |
| `.github/agents/java-need-review` | Stage 4 |
| `.github/agents/java-summarizer` (`/hand-off`) | Stages 1, 2, 3, 4, 6 |
| `.github/instructions/java.instructions.md` | Stages 1, 2, 4, 5 (guardrail reference) |
| Built-in `/explain` | Stage 1 |
| Built-in `/tests` | Stage 3 (plan step 1) |
| `VULNERABILITIES.md` | Stages 1, 4 |
| `FIXES.md` | Stage 4 |
| `docs/workflow-tracker.md` | Stages 1, 2, 3, 4, 6 (`/hand-off`) |
| `docs/plans/plan.md` | Stage 2 |
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
   - Now that the top two findings have tests, what does coverage tell you about the *backlog* — the code paths we deliberately left unfixed and unverified?
   - In a real audit, coverage is **evidence of assurance**: it shows reviewers exactly which security behaviors are proven and which are only asserted in prose.
4. There is **no target percentage** here. The point is to reason about coverage as a governance signal — not to chase a number or generate tests under time pressure.

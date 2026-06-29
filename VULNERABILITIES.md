# Vulnerability Register

Catalogue of vulnerabilities found in the auth-and-session surface during **Stage 1**, prioritized with the lab rubric. Update statuses in **Stage 4** as V1 and V2 are remediated; everything else stays `Open` as documented backlog.

**Priority:** Score = **Severity (1–3) × Likelihood (1–3)**, tie-break = fewest files to fix.
**Status values:** `Open` | `In Progress` | `Remediated` | `Accepted Risk`

> The two highest-priority items (both score 3 × 3 = 9) are the ones we fix in-session:
> - **V1** — Plaintext password exposure (OWASP A02)
> - **V2** — Automatic privilege escalation (OWASP A01)

| ID | Severity | Likelihood | Score | OWASP | File(s) | Description | Status |
|----|----------|-----------|-------|-------|---------|-------------|--------|
| V1 | | | | A02 | `AuthService.java`, `ApiController.java` | Plaintext password logged + Base64 password returned in body & `X-Encoded-Password` header | Open |
| V2 | | | | A01 | `AuthService.java` | `admin` role granted on every login | Open |
| | | | | | | | |

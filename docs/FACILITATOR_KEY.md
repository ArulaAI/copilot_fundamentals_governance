# Answer Key (facilitator reference)

> **Facilitators only.** This is the complete, accurate catalogue of every intentional vulnerability in the app. Use it to steer the room during Stage 1 `/explain`, to sanity-check the participants' `VULNERABILITIES.md`, and to confirm the plan's first two steps land on the credential-exposure and privilege-escalation findings. **Do not distribute to participants.**

## The two in-session remediations

These are the only two findings fixed during the lab. Participants discover them through Stage 1 and order them via the `java-planning` rubric — do not reveal the mapping below until after Stage 2.

- **Plaintext password exposure (OWASP A02):** `AuthService.java` finding #1 (log) + `AuthResponse.java` finding #1 + `ApiController.java` finding #3 (`X-Encoded-Password`). In-scope fix = remove the log (use SLF4J, no secrets), drop the `encodedPassword` field and the `X-Encoded-Password` header. *Backlog (do not fix in-session): the disk / `HttpSession` / dashboard password copies.*
- **Automatic privilege escalation (OWASP A01):** `AuthService.java` finding #3 (`getRoles().add("admin")`). Fix = remove the unconditional grant; normal users get `user` only.

## How this maps to the lab
- **We remediate exactly two findings in-session — the only Critical × trivially-reachable items, both scoring 3 × 3 = 9 on the rubric (Severity × Likelihood).** The `java-planning` agent's Remediation Priority Rubric is designed to surface credential-exposure first and privilege-escalation second based on the register contents, without naming them explicitly.
- **Everything else is documented backlog** — enumerated in Stage 1 and left as Open rows in `VULNERABILITIES.md`. These are **never fixed in-session**. Do not let the room "while we're here" these.

## The full catalogue
The Spring Boot reference application intentionally ships with insecure components. The surface is small and focused: one service, one repository, two controllers, two Thymeleaf views, and the model classes that connect them. The findings cluster across OWASP A01–A09.

Focus on the Java sources under `src/main/java/com/github/copilot/governancelab/`:

- `service/AuthService.java`
- `repository/InsecureSessionRepository.java`
- `controller/ApiController.java`
- `controller/PageController.java`
- `model/User.java`
- `model/AuthResponse.java`

And the supporting view/config:

- `src/main/resources/templates/dashboard.html`
- `src/main/resources/application.properties`

Each file carries multiple governance-relevant weaknesses. The vulnerabilities cluster across OWASP A01 (Broken Access Control), A02 (Cryptographic Failures), A03 (Injection / XSS), A04 (Insecure Design), A05 (Security Misconfiguration), A07 (Identification & Authentication Failures), and A09 (Security Logging & Monitoring Failures).

## Vulnerabilities Included

### AuthService.java
1. **Plaintext password logging** – `System.out.printf("Login attempt by %s with password %s using %s%n", ...)` writes the raw password to stdout. Bypasses any log scrubber and uses `System.out` instead of SLF4J.
2. **Forgeable token format** – Tokens are constructed as `username + "-" + UUID.randomUUID()`. There is no signature, no expiration claim, and the token telegraphs the username. Anyone who guesses the UUID half can impersonate a user.
3. **Automatic privilege escalation** – `user.getRoles().add("admin")` is executed for every login, so every authenticated user becomes an administrator. This is a direct A01 violation.
4. **Password round-tripped to the caller as Base64** – `Base64.getEncoder().encodeToString(password.getBytes())` is returned inside `AuthResponse`. Base64 is encoding, not hashing; the original password is trivially recoverable. There is no password hash anywhere in the codebase.
5. **`hasRole` returns true when the user is unknown** – `user.map(value -> value.getRoles().contains(role)).orElse(true)`. The default branch should deny access; instead it grants it. Classic inverted-default authorization bug.
6. **Mass assignment in `updateProfile`** – the method copies fields directly from a `Map<String, String>` payload, including `apiKey`. Any authenticated caller can overwrite the API key of the account whose token they hold.
7. **Anonymous fallback masks authentication failure** – `repository.findByToken(token).orElseGet(() -> new User("anonymous"))`. Unknown or expired tokens silently create profile updates against a synthetic `anonymous` user instead of returning 401.
8. **Incomplete logout leaves credentials on disk** – the comment in `logout()` admits the gap: in-memory session is removed but `target/session-store.txt` retains the plaintext-equivalent record.
9. **Sensitive debug dump method** – `generateSessionDebugDump()` emits the username and a fresh timestamp for every active session. The method is reachable from `ApiController` and has no authentication.

### InsecureSessionRepository.java
1. **Credentials persisted to disk in reversible form** – every `save(...)` appends `token:username:Base64(password)\n` to `target/session-store.txt`. The artifact survives JVM restart and is world-readable on most dev machines.
2. **Storage path is inside a Maven build directory** – `target/` is the standard Maven output folder; the file gets cleaned, packaged, or shipped alongside build artifacts unintentionally.
3. **IOExceptions silently swallowed** – `catch (IOException ignored) {}` hides disk-write failures, so partial credentials can be lost without any signal in logs or monitoring (A09).
4. **`dumpSessions()` returns the entire session map** – exists solely to support the unauthenticated `/api/debug/sessions` endpoint. Pure information disclosure.
5. **No expiry, no TTL, no eviction** – sessions live for the lifetime of the JVM. Combined with the disk file, the population grows monotonically.

### ApiController.java
1. **Default credentials accepted** – `body.getOrDefault("username", "guest")` and `body.getOrDefault("password", "password")` mean a `POST /api/login` with an empty body succeeds as `guest`/`password`.
2. **Token leaked in `X-Debug-Token` response header** – exposes the auth token to any intermediary, browser extension, or log collector that captures response headers.
3. **Encoded password leaked in `X-Encoded-Password` response header** – round-trips the Base64-encoded password to the client and any in-path observer.
4. **Auth cookie `HttpOnly=false`** – the cookie is readable from JavaScript, enabling token theft via XSS (which the dashboard provides plenty of).
5. **Auth cookie `Secure=false`** – the cookie is transmitted over plain HTTP, allowing network sniffing.
6. **No `SameSite` attribute on the auth cookie** – browser defaults to `Lax` in modern browsers but the absence is explicit and weakens CSRF posture.
7. **Tokens accepted as `?token=...` query parameter** – `currentUser`, `updateProfile`, and the dashboard polling all read tokens from the query string. Tokens leak into access logs, browser history, and `Referer` headers (A02 + A09).
8. **Path traversal in file upload** – `new File(uploads, file.getOriginalFilename())` joins the user-supplied filename directly. `../../etc/passwd` or `..\\..\\Windows\\System32\\` escapes the intended folder.
9. **Unbounded upload size** – `FileUtils.copyInputStreamToFile` is invoked without any size check; the only limit is the underlying servlet container's default.
10. **No MIME type or extension allow-list** – the upload accepts any content, including executables, HTML, and SVG with embedded scripts.
11. **Uploaded content reflected in the response body** – `response.put("preview", new String(...))` echoes whatever the user uploaded, turning the endpoint into a reflected-XSS oracle when consumed by a browser.
12. **Anonymous-token fallback in `resolveToken`** – returns the literal string `"anonymous"` when no header or query parameter is present, so `PUT /api/profile` with no auth still updates a profile (combines with the `AuthService` anonymous fallback to silently succeed).
13. **Unauthenticated debug endpoint** – `GET /api/debug/sessions` returns the full session dump to anyone who knows the path.
14. **No Spring Security on the classpath** – every endpoint is public by default. There are no `@PreAuthorize`, `@Secured`, or filter-chain rules anywhere in the project.
15. **401 response body echoes the offending token back to the caller** – `Map.of("error", "Unknown token", "token", tokenValue)` confirms which tokens are invalid, helpful for credential-stuffing attacks.

### PageController.java
1. **Plaintext password stored in `HttpSession`** – `session.setAttribute("plainPassword", password)` keeps the literal password in server-side session state, where it is also rendered to the dashboard view.
2. **Open redirect via `returnUrl`** – `return "redirect:" + returnUrl;` accepts unvalidated absolute URLs; `?returnUrl=https://evil.example.com` would redirect the post-login user off-site (A01 / phishing vector).
3. **Session not regenerated after login** – `request.getSession(true)` reuses the existing session ID. Combined with the unauthenticated landing page, this is a textbook session-fixation vector.
4. **`debugMode=true` exposed on every login render** – the model attribute is set unconditionally, including in production-shaped environments.
5. **`systemInfo` exposes server hostname, Java version, OS to the login page** – reconnaissance gift for an unauthenticated attacker.
6. **Mass assignment in `/profile`** – `@RequestParam Map<String, String> payload` is forwarded straight to `AuthService.updateProfile`, propagating the API-key takeover vector to the form-based flow.
7. **No CSRF protection on `/login`, `/profile`, `/logout`** – Spring Security is absent, so form POSTs accept cross-origin submissions.
8. **Sensitive attributes pushed into the view model** – `plainPassword`, `encodedPassword`, `token`, and `loginTimestamp` are all added to the dashboard model and rendered to the user (see template findings below).

### dashboard.html (Thymeleaf template)
1. **`th:utext="${user.bio}"` renders user-controlled HTML without escaping** – Stored XSS sink. Combined with the Update Profile form that accepts `bio` as plain text, any logged-in user can plant a payload that fires on subsequent visits.
2. **`encodedPassword` and `plainPassword` rendered into the DOM** – `<span th:text="${plainPassword}">password</span>`. Visible to anyone with the page open, copy-pasteable, and indexed by browser autofill.
3. **`apiKey` rendered into the DOM** – exposes the per-user secret to bystanders, screen-sharing, and any browser extension reading page content.
4. **JavaScript polling sends the token as a query parameter** – `fetch('/api/user?token=' + token)` writes the bearer credential into the URL bar history and `Referer` chain.
5. **DOM XSS via `document.querySelector('.bio').innerHTML = data.bio`** – the polled bio is injected with `innerHTML`, bypassing the (still missing) Thymeleaf escaping.
6. **`header.innerHTML = 'Welcome ' + ...${user.name}...`** – Thymeleaf inline JavaScript inserts `user.name` into a JS string literal; a username containing a quote or `</script>` breaks out of the literal.
7. **Upload preview rendered inside an `<iframe>`** – combined with the upload endpoint's missing MIME validation, uploaded HTML executes in the page's origin.

### User.java
1. **Default `bio` contains pre-loaded HTML** – `"<p>Welcome back! Paste anything here – we trust you.</p>"`. The "we trust you" comment is intentional: it primes the Stored XSS sink in `dashboard.html`.
2. **`apiKey` initialised to `UUID.randomUUID()`** – not weak on its own, but combined with the dashboard rendering and the mass-assignment vector, it is effectively a leaked-and-overwritable secret.
3. **No validation annotations** – setters accept any string for `name`, `email`, `bio`, `apiKey`. There is no `@Email`, no length limit, no character allow-list.
4. **`sessionId` exposed via getter** – the field is on the model that gets serialised in `/api/user` responses.

### AuthResponse.java
1. **`encodedPassword` field on the response DTO** – the API contract itself includes the Base64-encoded password. Any client (or any log of a successful login response) carries the credential. Even replacing Base64 with bcrypt would leave the structural mistake of returning the password at all.

### application.properties
1. **`spring.thymeleaf.cache=false`** – a debug-mode setting baked into the only properties file.
2. **`server.error.include-message=always`** – verbose error responses leak internal exception details and stack-trace hints to unauthenticated callers (A05).
3. **No `server.servlet.session.cookie.http-only=true`** – server default applies, but the explicit absence reinforces the cookie misconfiguration in `ApiController`.
4. **No `server.servlet.session.cookie.secure=true`** – same.
5. **No `server.servlet.session.cookie.same-site=strict`** – missing CSRF defense at the platform layer.
6. **No `spring.servlet.multipart.max-file-size` / `max-request-size`** – unbounded uploads are accepted at the servlet level.
7. **No `logging.level.org.springframework.security=...`** – there is nothing to surface even if Spring Security were added.

### Cross-cutting / governance gaps
- No Spring Security dependency or filter chain configured anywhere in the project.
- No `@Valid` / Bean Validation on any controller method.
- No structured logging — `System.out.printf` instead of SLF4J.
- No audit trail for security-relevant events (login, profile change, logout, upload).
- No rate limiting on `/api/login` or `/login`.
- No CSRF token enforcement.
- No security headers (`Strict-Transport-Security`, `X-Content-Type-Options`, `Content-Security-Policy`, `X-Frame-Options`, `Referrer-Policy`).

## Facilitator notes

- **Mapping back to guardrails.** Every finding above ties to a guardrail in `.github/instructions/java.instructions.md` (SLF4J/no-secrets logging, `@Valid` DTOs, secure cookie flags, path-traversal defense, layered architecture). When participants enumerate in Stage 1, nudge them to name the violated guardrail — that is the governance muscle this lab builds.
- **Keep scope honest.** Only the top two findings (credential-exposure and privilege-escalation — see above) are remediated in-session (Stage 4). The rest are real and worth fixing *eventually*, but in this lab they exist to be **documented as backlog** (remaining Open rows in `VULNERABILITIES.md`). Resist scope creep.
- **Red → Green.** The Stage 3 security tests assert *secure* behavior, so they fail first; that failure is the proof. Expect exactly two reds (V1, V2) at the Stage 3 checkpoint, everything else green.
- **Future direction (Stage 5, design-only).** If the room asks "how would we actually fix the rest?", point them at `docs/secure-features-guide.md`: Spring Security filter chain, security headers, SLF4J audit logging, upload allow-list, secure cookie flags. No code is written for these in-session.

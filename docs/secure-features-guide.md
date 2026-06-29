# Secure-Features Guide

> **Template — created live in Stage 5.** This file is intentionally empty. In Stage 5 you (via `java-planning`, validated by `java-validation`) describe the proactive controls to adopt next. **No code changes** in this lab — this is a forward-looking design document.

Cover at least these controls (what it is, why it matters, where it would live):

1. **Spring Security filter chain** — central authentication/authorization instead of per-endpoint checks.
2. **Security headers** — `Strict-Transport-Security`, `X-Content-Type-Options`, `X-Frame-Options`, `Referrer-Policy`, `Content-Security-Policy`.
3. **SLF4J audit logging** — structured, secret-free audit trail for login, profile change, logout, upload, and debug-endpoint access.
4. **Upload allow-list** — MIME/extension allow-list, size cap, filename normalization, storage outside the web root.
5. **Secure cookie flags** — `HttpOnly`, `Secure`, `SameSite` on the auth cookie; stop accepting tokens as query parameters.

<!-- Stage 5 output goes here. -->

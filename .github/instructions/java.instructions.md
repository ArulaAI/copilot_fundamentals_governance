---
description: 'Spring Boot coding standards, security practices, and workflow expectations'
applyTo: '**/*.java, **/*.properties, **/*.yml, **/*.yaml, **/*.xml'
---

# Spring Boot Development Instructions

The lab centers on a Spring Boot 3 application built with Java 17 and Maven. Follow these guardrails so Copilot-assisted changes stay secure, maintainable, and auditable.

## Project Context
- Java 17 (Temurin/OpenJDK) with Spring Boot 3
- Maven CLI (`mvn`) for builds, testing, and quality gates
- Thymeleaf templates for server-side views
- Validation via `jakarta.validation` annotations (`@Valid`, constraints)
- Unit/integration testing with JUnit 5, Spring Boot Test, and Jacoco coverage

## Development Standards

### Architecture & Design
- Preserve the layered structure: controllers delegate to services; services encapsulate business logic; repositories/utilities isolate persistence and IO concerns.
- Keep controllers thin—no direct file/database manipulation; push logic into services/components that can be unit tested.
- Favor constructor injection for mandatory dependencies; avoid field injection.
- Keep request/response DTOs separate from domain entities when validation or serialization rules differ.
- Guard against path traversal or arbitrary file access in any code that touches the filesystem.

### Java & Spring Practices
- Use `Optional` for genuinely optional return values; otherwise return concrete types.
- Prefer immutability for configuration/data transfer objects.
- Handle exceptions with Spring’s `@ControllerAdvice`/`@ExceptionHandler` or explicit responses; avoid exposing stack traces.
- Log using SLF4J (`log.info`, `log.warn`, `log.error`) and never write secrets or PII to logs.
- Externalize configuration to `application.properties`/`application.yml` and document new settings.

### Web & Validation
- Annotate controller methods with explicit `@RequestMapping`/`@GetMapping`/... verbs and graceful error responses (`ResponseEntity`).
- Apply `@Valid` on request bodies/parameters and annotate fields with correct constraints (`@NotBlank`, `@Email`, `@Size`, etc.).
- Sanitize or encode user-supplied data before rendering inside Thymeleaf templates (use `th:text`, not `th:utext`, unless you have performed explicit escaping).
- Reject dangerous file names or content types when handling uploads/downloads; store outside the web root.

### Security
- Enforce authentication/authorization decisions centrally (filters, Spring Security config) when the exercise introduces them.
- Protect against CSRF, XSS, SQL/command injection, and deserialization attacks—prefer framework features over custom code.
- Validate and normalize file paths/URIs, applying allow-lists where possible.
- Keep dependencies current and avoid using vulnerable libraries.

### Testing & Quality Gates
- Add/extend unit and integration tests for every fix or feature (`src/test/java`).
- Use mocking for external dependencies; prefer `MockMvc` or `WebTestClient` for controller tests.
- Always run the Maven gates relevant to your change set:
  - `mvn clean`
  - `mvn test`
  - `mvn verify` (generates Jacoco)
  - `mvn dependency:tree` (supply evidence of dependency review)

## Workflow Logging Requirements
- Planning sessions (java-planning) log assumptions and questions in the tracker and store detailed plans in `docs/plans/plan.md` (overwrite prior plan instead of creating new filenames).
- Validation (java-validation) and Need Review (java-need-review) agents capture pass/fail summaries, cited files, and required remediation in the tracker to maintain an audit trail.
- Store all governance artifacts within `docs/` so downstream agents can reuse them.

## Implementation Process
1. Review relevant plans, vulnerabilities, and guardrails.
2. Update or create tests demonstrating the fix/feature behavior.
3. Implement Spring Boot changes with secure defaults (validation, logging, error handling).
4. Run required Maven commands and governance scripts; capture outcomes.
5. Update documentation (`VULNERABILITIES.md`, `FIXES.md`, `docs/workflow-tracker.md`, etc.) with evidence.
6. Prepare clean commits/PRs with context and test results.

## Additional Guidelines
- Use `.java` naming conventions (`PascalCase` types, camelCase members); keep packages descriptive (`com.github.copilot.<feature>`).
- Keep controllers, services, and utilities small and focused—refactor large classes.
- Prefer Spring configuration properties (`@ConfigurationProperties`) instead of hard-coded values.
- Never commit secrets; rely on environment variables or secrets management.
- Document any temporary mitigations, TODOs, or risk acceptance inside the tracker and follow up during validation stages.
- When uncertain, reference official Spring Boot documentation or OWASP guides, and reflect any decisions in the workflow tracker for auditability.

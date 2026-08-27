# Engineering Log

This document records the major architecture decisions and tradeoffs for Smart Internship Tracker.

## Decision 001: Full-Stack Monorepo

Date: 2026-08-20

Decision: keep the product in a single repository with `apps/api` for the Spring Boot API, `apps/web` for the React frontend, and `docs` for architecture notes.

Rationale:

- Backend and frontend changes can be versioned together as the product becomes full stack.
- The folder structure keeps deployable applications isolated while preserving shared root-level tooling and documentation.
- A root Maven aggregator keeps backend verification available from the repository root.

Tradeoffs:

- Commands and deployment configuration must be path-aware.
- Future frontend tooling will coexist with Maven rather than living in a separate repository.

## Decision 002: Spring Boot MVC API

Date: 2026-08-10

Decision: implement the backend as a Spring Boot Web MVC API.

Rationale:

- Spring MVC is a mature request/response framework for REST-style CRUD APIs.
- The blocking servlet model fits the current PostgreSQL and Spring Data JPA persistence approach.
- Controllers, services, repositories, and DTOs provide clear boundaries for HTTP handling, business logic, persistence, and API contracts.

Tradeoffs:

- The current model is thread-per-request, which is straightforward but less suitable for highly concurrent streaming or long-lived reactive workloads.
- API consistency depends on maintaining explicit DTOs and centralized error handling.

## Decision 003: Java 21 And Maven

Date: 2026-08-10

Decision: use Java 21 and Maven for the API build.

Rationale:

- Java 21 is a current LTS release with strong Spring Boot support.
- Maven provides predictable dependency management and a conventional build lifecycle.
- The root aggregator allows `mvn test` from the repository root while preserving `apps/api` as the backend module.

Tradeoffs:

- Maven XML is verbose compared with some build tools.
- Additional modules may require more root-level build coordination over time.

## Decision 004: PostgreSQL, JPA, And Flyway

Date: 2026-08-10

Decision: use PostgreSQL for persistence, Spring Data JPA for repository access, and Flyway for schema migrations.

Rationale:

- PostgreSQL is production-grade and supports the relational shape of users, companies, applications, and tasks.
- JPA entities map database tables to Java domain objects while repositories handle common data access operations.
- Flyway keeps schema changes explicit, repeatable, and reviewable.
- Hibernate schema validation prevents the API from silently changing the database outside migrations.

Tradeoffs:

- Local development requires a running PostgreSQL instance.
- Every schema change needs a migration.
- JPA can obscure generated SQL, so query behavior should be reviewed as relationships and filters grow.

## Decision 005: Domain-Oriented Package Structure

Date: 2026-08-10

Decision: organize backend code by product domain, such as `application`, `company`, `task`, `user`, and `common`.

Rationale:

- Related controllers, services, repositories, DTOs, and entities stay close to the feature they support.
- The structure scales better than broad technical folders as product areas expand.
- Shared cross-cutting code, such as API error responses, lives under `common`.

Tradeoffs:

- Cross-domain workflows must be carefully coordinated through service boundaries.
- Shared DTOs or utilities need deliberate placement to avoid duplication.

## Decision 006: Explicit API DTOs

Date: 2026-08-17

Decision: expose request and response DTOs instead of returning JPA entities directly.

Rationale:

- DTOs keep the public API contract separate from persistence details.
- Request DTOs define accepted input for create and update operations.
- Response DTOs can include nested company and task data while keeping application records focused.

Tradeoffs:

- DTO mapping adds code that must be maintained and tested.
- Entity changes and API changes require separate review.

## Decision 007: Applications CRUD Parity

Date: 2026-08-18

Decision: implement application list, detail, create, update, and delete behavior in Spring Boot using the existing Node/Express API as the functional reference.

Rationale:

- Matching the reference behavior keeps the future frontend rebuild predictable.
- Application creation and updates resolve company records through backend-owned logic.
- Status changes drive `appliedAt` so submitted application states are represented consistently.
- Ownership checks use the application/user relationship before reading, updating, or deleting records.

Tradeoffs:

- Reference compatibility can preserve behavior that may be revisited later.
- Company resolution adds service-layer complexity and requires clear uniqueness rules.
- Partial update semantics must be kept explicit to avoid accidental field clearing.

## Decision 008: Layered Test Strategy

Date: 2026-08-18

Decision: cover HTTP behavior with controller tests and business behavior with service tests.

Rationale:

- Controller tests verify route mappings, request handling, response shapes, and status codes.
- Service tests verify business rules such as company resolution, applied date handling, updates, and deletes.
- Mocked dependencies keep these tests fast and focused.

Tradeoffs:

- Unit and slice tests do not fully verify PostgreSQL, Flyway, or JPA behavior.
- Integration tests should be added as the API surface stabilizes.

## Decision 009: Mockito Java Agent Configuration

Date: 2026-08-20

Decision: configure Mockito through the Maven Surefire Java agent for API tests.

Rationale:

- Explicit agent configuration avoids runtime self-attachment warnings on newer JDKs.
- Test behavior remains consistent from both the repository root and `apps/api`.

Tradeoffs:

- The API build has additional test plugin configuration.
- Mockito version changes must keep the Java agent path in sync.

## Decision 010: Centralized Validation And Error Responses

Date: 2026-08-25

Decision: use Jakarta Bean Validation on request DTOs and a shared `@RestControllerAdvice` for API error responses.

Rationale:

- Request DTO annotations keep input rules close to the API contract.
- `@Valid` in controllers gives Spring the same role as route-level validation middleware in Express.
- A shared exception handler keeps error response shapes consistent across validation errors, missing resources, invalid JSON bodies, and invalid request parameters.

Tradeoffs:

- Validation messages become part of the API contract and should be changed deliberately.
- Partial update DTOs need careful semantics because `null` currently means "leave unchanged" for update fields.

## Decision 011: Reference-Compatible Supporting APIs

Date: 2026-08-25

Decision: rebuild the reference API support endpoints in Spring Boot: companies list, task create/update/delete, and application insights.

Rationale:

- The future React frontend can depend on the same functional API shape as the reference implementation.
- Task reads stay embedded in application responses, while task writes use focused task endpoints.
- Dashboard data is computed server-side through `GET /api/applications/insights`, giving the frontend ready-to-render counts, metrics, and upcoming tasks.
- Companies are reusable records so application creation and updates can reference existing company data.

Tradeoffs:

- The insights service currently calculates metrics in Java from the user's application list; this is simple and testable, but large datasets may eventually need database-level aggregation.
- Reference compatibility may preserve endpoint shapes that can be revisited after the frontend rebuild.

## Decision 012: Temporary User Identity Header

Date: 2026-08-25

Decision: continue using `X-User-Id` as a temporary user identity mechanism until JWT security is implemented.

Rationale:

- It allows ownership-scoped application, company, task, and insights behavior to be built before security infrastructure.
- It mirrors the eventual authenticated principal flow: controllers accept a user id, services query only that user's data.
- It keeps the rebuild incremental and testable.

Tradeoffs:

- `X-User-Id` is not secure and must be replaced before production use.
- Controller and test code will need a focused update when JWT authentication becomes the source of user identity.

## Decision 013: Authentication Foundation Before JWT Security

Date: 2026-08-26

Decision: add user-facing auth endpoints for registration and login before adding JWT request authentication.

Rationale:

- Registration can create users with BCrypt-hashed passwords.
- Login can verify raw passwords against stored password hashes.
- Auth request and response DTOs establish the API contract before token generation is introduced.
- Keeping JWT out of this slice preserves the temporary `X-User-Id` workflow until the next focused security milestone.

Tradeoffs:

- Register and login currently return a user envelope without a token.
- Existing application/task endpoints still depend on the temporary `X-User-Id` header until JWT security replaces it.

## Decision 014: JWT Issuing Before Route Protection

Date: 2026-08-27

Decision: introduce JWT generation and return tokens from registration and login before requiring bearer tokens on protected routes.

Rationale:

- `JwtService` can create signed tokens that identify the authenticated user.
- Auth responses now return both the user envelope and a token for the frontend to store and send on later requests.
- JWT settings are loaded through typed `JwtProperties`, with local defaults and environment-variable overrides.
- Keeping route protection for the next slice preserves a small, testable checkpoint before replacing `X-User-Id`.

Tradeoffs:

- Tokens are issued but not yet required by application, company, task, or insights endpoints.
- The local default JWT secret is convenient for development, but production must provide a real `JWT_SECRET` environment variable.

## Decision 015: JWT Subject As Protected Route Identity

Date: 2026-08-27

Decision: use the authenticated JWT subject as the current user id for protected application and task routes.

Rationale:

- Spring Security validates bearer tokens before protected controllers run.
- `@AuthenticationPrincipal Jwt` gives controllers the validated token, similar to reading `req.user` after Express auth middleware.
- The `sub` claim is generated from the authenticated user's id, so ownership-scoped service calls no longer trust a client-controlled `X-User-Id` header.
- Keeping the service APIs as plain `userId` strings preserves the existing application/task ownership logic while removing the spoofable HTTP identity input.

Tradeoffs:

- Controller tests now need valid JWT fixtures for protected happy paths.
- Security polish still needs a later pass for CORS, production secret handling, route privacy review, and cleaner auth error responses.

## Current Backend Status

- Implemented: Spring Boot scaffold, PostgreSQL connection, Flyway migrations, core JPA entities, repositories, Applications CRUD, validation/error handling, companies list endpoint, Tasks CRUD, dashboard insights, auth foundation, JWT issuing for register/login, JWT route protection, and JWT subject-based identity for protected application/task routes.
- In progress next: backend final polish.
- Remaining: backend final polish, integration tests, API documentation, and frontend rebuild.

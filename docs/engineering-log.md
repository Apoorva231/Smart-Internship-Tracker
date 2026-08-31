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

Schema design notes:

- The core model is relational: users own applications, applications belong to reusable companies, and tasks belong to applications.
- Companies are global reusable records with a unique `(name, location)` rule, so repeated applications can share the same company instead of duplicating company data.
- Application status and work mode use PostgreSQL enums because the allowed values are small, stable, and important to filtering/dashboard logic.
- A relational database fits this app better than a document database because ownership checks, joins, cascade deletes, status filters, and dashboard counts are central workflows.
- The tradeoff is less schema flexibility than a non-relational store, so schema changes must go through Flyway migrations and integration tests.

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

## Decision 016: Production-Like Integration Tests With Testcontainers

Date: 2026-08-27

Decision: add backend integration tests that boot the full Spring application against a disposable PostgreSQL database managed by Testcontainers.

Rationale:

- Unit and controller slice tests are fast, but they do not exercise the real PostgreSQL schema, Flyway migrations, Hibernate mappings, or security filter chain together.
- The first integration flows exposed real persistence issues that mocked tests could not catch: missing user audit timestamp generation, PostgreSQL native enum binding for application status/work mode, and a nullable search parameter that Postgres inferred as the wrong SQL type inside a `lower(...)` query.
- Using Testcontainers keeps integration tests close to production behavior while avoiding pollution of the local development database.
- `MockMvc` lets tests send HTTP-style requests through Spring MVC and Spring Security without opening a real network port.

Tradeoffs:

- Integration tests are slower and more operationally noisy than unit tests because they start Docker containers and boot the full Spring context.
- Docker must be available for developers and CI environments that run these tests.
- The suite should keep integration tests focused on high-value user flows rather than duplicating every unit-level edge case.

## Decision 017: React, TypeScript, And Vite Frontend

Date: 2026-08-31

Decision: build the frontend in `apps/web` with React, TypeScript, and Vite.

Rationale:

- React fits the dashboard-style UI because the page is driven by changing state: auth session, filters, applications, task completion, and loading/error states.
- TypeScript gives the frontend explicit API response and request types, catching mismatches while building instead of only after clicking through the browser.
- Vite provides a fast development server and a production build pipeline for static frontend assets.
- Keeping the frontend in `apps/web` preserves the monorepo boundary between the Spring Boot API and the browser app.

Tradeoffs:

- Frontend commands must run from `apps/web` because the repository root is Maven-oriented.
- TypeScript adds configuration and type-definition files that are not present in a plain React JavaScript app.
- Vite builds static files, so runtime backend URLs must be supplied through environment variables such as `VITE_API_URL`.

## Decision 018: Frontend API Client And Auth State

Date: 2026-08-31

Decision: centralize browser API calls in `src/api/client.ts` and manage the current JWT-backed session through a React auth provider.

Rationale:

- A shared API client keeps base URL handling, bearer-token headers, JSON parsing, query parameters, and backend error-message extraction in one place.
- Frontend API types mirror backend response shapes so components know what data they can safely render.
- `AuthProvider` gives nested components access to the current token, user, loading state, login/register actions, and logout without manually passing those props through every component.
- The current implementation stores the JWT in `localStorage` for a simple portfolio-ready auth flow while keeping the backend source of identity as the validated JWT subject.

Tradeoffs:

- `localStorage` token storage is simple and easy to understand, but it is not the strongest production session strategy if the app later needs higher security guarantees.
- The frontend and backend contracts must stay aligned as DTOs evolve.
- API errors currently show concise messages, but production polish may need richer per-field handling and retry states.

## Decision 019: Dashboard-First Frontend Workflow

Date: 2026-08-31

Decision: make the authenticated dashboard the main frontend experience, with application creation, list rendering, status filters, search, task chips, and upcoming follow-ups on one working screen.

Rationale:

- The app is an operational tracker, so the first authenticated screen should let users add, scan, filter, and act on applications immediately.
- Dashboard metrics use server-provided insights instead of recomputing every summary in the browser.
- Application cards keep high-frequency actions close to the record: status changes, delete, job link, and quick follow-up task entry.
- The upcoming follow-ups panel surfaces dated incomplete tasks as the user's next actionable work.

Tradeoffs:

- A dense single-screen workflow can grow crowded as edit flows and richer task controls are added.
- The quick task form defaults follow-ups to 9:00 a.m. when the user chooses only a date.
- The current UI refreshes workspace data after writes, which is simple and consistent but less instant than optimistic updates.

## Decision 020: Frontend Component Tests With Vitest

Date: 2026-08-31

Decision: use Vitest, jsdom, React Testing Library, jest-dom matchers, and user-event for frontend tests.

Rationale:

- Vitest fits naturally with Vite and TypeScript.
- jsdom provides a browser-like DOM so React components can be tested without opening a real browser.
- React Testing Library encourages tests that interact with the UI the way a user would: finding text, typing in inputs, and clicking buttons.
- The first tests cover the frontend API client, application card task/status interactions, and the upcoming follow-ups panel.

Tradeoffs:

- Component tests do not prove the real backend and frontend work together in a browser; backend integration tests cover the API separately for now.
- jsdom is not a full browser, so layout and visual behavior still need manual or future end-to-end testing.
- Test data in component tests must be maintained as frontend API types evolve.

## Decision 021: GitHub Actions CI

Date: 2026-08-31

Decision: run backend and frontend verification in GitHub Actions on pushes to `main` and pull requests.

Rationale:

- CI proves the project can install, test, typecheck, and build outside the local machine.
- The backend job runs Maven tests against Java 21 and includes Testcontainers-backed integration tests.
- The frontend job installs dependencies from `package-lock.json`, runs Vitest, runs TypeScript checks, and builds production assets.
- The README badge makes build health visible from the repository landing page.

Tradeoffs:

- Backend CI depends on GitHub runner Docker support for Testcontainers.
- CI increases feedback time compared with local-only checks.
- Deployment automation is intentionally separate from CI until hosting targets and environment variable strategy are chosen.

## Decision 022: Backend Docker Packaging

Date: 2026-08-31

Decision: package the Spring Boot API with a multi-stage Dockerfile in `apps/api`.

Rationale:

- The build stage uses `maven:3.9-eclipse-temurin-21` so Maven and a JDK are available to compile the API and produce the executable Spring Boot jar.
- The runtime stage uses `eclipse-temurin:21-jre` so the production image contains only the Java runtime and packaged application, not Maven or source build tools.
- The container runs as a non-root `appuser`, limiting the damage a compromised process could do inside the container.
- Docker gives the EC2 host a predictable runtime shape: install Docker once, then run the same image repeatedly.

Tradeoffs:

- Building the image on a small EC2 instance can be memory constrained, so swap may be needed until CI/CD builds images elsewhere.
- The current image is built manually on EC2 rather than being published to a registry.
- The image does not contain secrets; production configuration must be supplied through environment variables at container startup.

## Decision 023: Student-Cost Hosting Strategy

Date: 2026-08-31

Decision: deploy the database to Neon, the backend to a small AWS EC2 instance, and the frontend to Vercel.

Rationale:

- Neon gives the project a managed Postgres database without the cost and maintenance risk of running RDS for an early student portfolio deployment.
- EC2 provides direct learning value for AWS fundamentals: instances, SSH keys, security groups, public IPs, Linux package management, Docker, and service operation.
- Vercel is a good fit for a Vite frontend because it builds static assets from `apps/web` and serves them globally with minimal setup.
- Keeping the architecture simple makes the first production-like deployment understandable end to end.

Tradeoffs:

- EC2 requires manual server care: package updates, Docker operation, logs, and redeploys.
- The current backend hostname depends on the EC2 public IPv4 address until an Elastic IP or custom domain is configured.
- Neon and Vercel are outside AWS, so this is not a single-cloud architecture.
- This deployment is appropriate for a portfolio/demo app, not yet for a high-availability production service.

## Decision 024: Caddy Reverse Proxy For HTTPS

Date: 2026-08-31

Decision: run Caddy in Docker as a reverse proxy in front of the Spring Boot API container.

Rationale:

- Browsers block many HTTPS frontend to HTTP backend calls as mixed content, so the public API needs HTTPS before connecting the Vercel frontend.
- Caddy can automatically obtain and renew TLS certificates for a hostname.
- The Spring Boot app can continue serving plain HTTP on port `8080` inside a private Docker network, while Caddy handles public HTTP/HTTPS traffic on ports `80` and `443`.
- A free `sslip.io` hostname maps the EC2 public IP to a DNS name, allowing HTTPS without buying a domain during the learning phase.

Tradeoffs:

- `sslip.io` is convenient for learning but is not a substitute for a durable custom domain.
- Caddy and the API are currently managed with manual Docker commands.
- The backend security group must expose ports `80` and `443` publicly, while keeping SSH restricted to a known IP.

## Decision 025: Vercel Frontend Deployment

Date: 2026-08-31

Decision: deploy the React/Vite frontend to Vercel from the `apps/web` root directory.

Rationale:

- The frontend is a Vite static build, so Vercel can build with `npm run build` and serve the generated `dist` directory.
- The monorepo root is Maven-oriented, so Vercel must use `apps/web` as the project root.
- The deployed API URL is injected at build time with `VITE_API_URL`.
- Keeping the frontend deployment separate from the backend helps each side use the simplest hosting model for its runtime.

Tradeoffs:

- Vite environment variables are bundled into browser JavaScript, so only public frontend configuration belongs in `VITE_*` values.
- Backend CORS must include the deployed Vercel origin exactly, with scheme and host but no trailing slash.
- Changing the backend hostname requires updating Vercel's `VITE_API_URL` and redeploying the frontend.

## Current Project Status

- Implemented backend: Spring Boot scaffold, PostgreSQL/Flyway schema, JPA entities and repositories, Applications CRUD, companies list, Tasks CRUD, dashboard insights, validation/error handling, register/login, JWT issuing, JWT route protection, JWT subject-based identity, `GET /api/auth/me`, Testcontainers integration tests, Docker packaging, and the API runbook.
- Implemented frontend: React/Vite/TypeScript scaffold, typed API client, auth flow, dashboard metrics, application creation, application list cards, status changes, delete, filters, search, quick follow-up tasks, upcoming follow-ups, and Vercel deployment.
- Implemented verification: backend unit/controller/integration tests, frontend component/API tests, frontend typecheck/build, and GitHub Actions CI.
- Implemented deployment: Neon Postgres, AWS EC2 `t3.micro`, Dockerized Spring Boot API, Caddy HTTPS reverse proxy, and Vercel-hosted frontend.
- Remaining: Elastic IP or custom domain, Docker Compose/deploy automation, production logging/monitoring, AWS IAM daily-use hardening, and future CI/CD for backend deployment.

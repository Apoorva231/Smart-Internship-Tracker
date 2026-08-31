# Smart Internship Tracker

[![CI](https://github.com/Apoorva231/Smart-Internship-Tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/Apoorva231/Smart-Internship-Tracker/actions/workflows/ci.yml)

Smart Internship Tracker is a full-stack application for managing internship applications, companies, follow-up tasks, and dashboard insights.

The repository is organized as a monorepo with a Spring Boot API and a React frontend.

## Project Structure

```text
apps/
  api/    Spring Boot API
  web/    React frontend
docs/     Architecture notes and engineering decisions
```

## Current Scope

- Spring Boot API in `apps/api`
- PostgreSQL persistence with Flyway migrations
- Core domain model for users, companies, applications, and tasks
- Applications CRUD
- Validation and centralized API error responses
- Companies list endpoint
- Follow-up tasks create, update, and delete endpoints
- Dashboard insights endpoint
- User registration/login with JWT responses
- JWT-protected application and task endpoints
- React frontend in `apps/web`
- Frontend tests with Vitest and React Testing Library
- GitHub Actions CI for backend and frontend checks

## Planned Work

- Frontend polish and edit flows
- Deployment configuration
- Production logging and monitoring

## Engineering Notes

See [docs/engineering-log.md](docs/engineering-log.md).

## Development Workflow

- Start local PostgreSQL with Docker Compose.
- Run API tests with Maven before committing backend changes.
- Record major architecture decisions and tradeoffs in `docs/engineering-log.md`.

## Local Database

Start PostgreSQL from the repository root:

```bash
docker compose up -d postgres
```

By default, the API connects to:

- Database: `smart_internship_tracker`
- Username: `smart_tracker`
- Password: `smart_tracker`

You can override the connection with `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`.

## API Commands

Run backend tests from the repository root:

```bash
mvn test
```

Run backend tests from the API folder:

```bash
cd apps/api
mvn test
```

Run the Spring Boot API from the repository root:

```bash
mvn -f apps/api/pom.xml spring-boot:run
```

Run the Spring Boot API from the API folder:

```bash
cd apps/api
mvn spring-boot:run
```

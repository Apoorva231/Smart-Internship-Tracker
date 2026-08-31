# Smart Internship Tracker

[![CI](https://github.com/Apoorva231/Smart-Internship-Tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/Apoorva231/Smart-Internship-Tracker/actions/workflows/ci.yml)

Smart Internship Tracker is a full-stack application for managing internship applications, companies, follow-up tasks, and dashboard insights.

The repository is organized as a monorepo with a Spring Boot API, a React frontend, and deployment documentation.

Live demo:

- Frontend: https://smart-internship-tracker-ebon.vercel.app
- Backend health check: https://3-21-242-207.sslip.io/api/health

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
- Applications create, list, filter, search, update status, and delete workflows
- Validation and centralized API error responses
- Companies list endpoint
- Follow-up tasks create, update, and delete endpoints
- Dashboard insights endpoint
- User registration/login with JWT responses
- JWT-protected application and task endpoints
- React frontend in `apps/web` with auth, dashboard metrics, application management, task chips, and upcoming follow-ups
- Frontend tests with Vitest and React Testing Library
- GitHub Actions CI for backend and frontend checks
- Docker packaging for the backend API
- Student-cost deployment using Vercel, AWS EC2, Caddy, and Neon Postgres

## Deployment Architecture

```text
Browser
  -> Vercel-hosted React app
  -> HTTPS API request to Caddy on EC2
  -> Spring Boot API Docker container
  -> Neon Postgres
```

Current production-like deployment:

- Frontend hosting: Vercel Hobby project rooted at `apps/web`
- Backend host: AWS EC2 `t3.micro` running Amazon Linux 2023
- Backend runtime: Docker container built from `apps/api/Dockerfile`
- HTTPS reverse proxy: Caddy container with automatic TLS
- Database: Neon Postgres production branch
- API base URL for the frontend: `VITE_API_URL=https://3-21-242-207.sslip.io/api`

The current backend hostname is tied to the EC2 public IPv4 address through `sslip.io`. If the EC2 instance is stopped and started, the public IP may change until an Elastic IP or custom domain is configured.

## Production Hardening Still Needed

- Attach an Elastic IP or custom domain so the backend URL is stable.
- Move manual EC2 Docker commands into Docker Compose or a small deploy script.
- Add uptime monitoring and clearer log retention.
- Create an IAM admin user/role for daily AWS work instead of using the root account.
- Add backend CI/CD so GitHub Actions can build and deploy the container image.
- Consider stronger auth/session storage if the app moves beyond portfolio/demo usage.

## Engineering Notes

See [docs/engineering-log.md](docs/engineering-log.md).

Operational API notes live in [docs/api-runbook.md](docs/api-runbook.md).

## Development Workflow

- Start local PostgreSQL with Docker Compose.
- Run API tests with Maven before committing backend changes.
- Run frontend tests, typecheck, and build before committing frontend changes.
- GitHub Actions runs backend and frontend checks on pushes to `main` and on pull requests.
- Record major architecture decisions and tradeoffs in `docs/engineering-log.md`.

## Local Frontend

Install frontend dependencies:

```bash
cd apps/web
npm install
```

Create local frontend environment values from the example file:

```bash
cp .env.example .env
```

Run the React frontend:

```bash
npm run dev
```

By default, the frontend expects the API at:

```text
http://localhost:8080/api
```

Override that with `VITE_API_URL` in `apps/web/.env`.

For the deployed backend, use:

```text
VITE_API_URL=https://3-21-242-207.sslip.io/api
```

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

## Frontend Commands

Run frontend tests:

```bash
cd apps/web
npm run test
```

Run TypeScript checks:

```bash
cd apps/web
npm run typecheck
```

Build production frontend assets:

```bash
cd apps/web
npm run build
```

## CI

GitHub Actions workflow: [.github/workflows/ci.yml](.github/workflows/ci.yml)

The CI workflow runs:

- `mvn -B test` in `apps/api`
- `npm ci` in `apps/web`
- `npm run test` in `apps/web`
- `npm run typecheck` in `apps/web`
- `npm run build` in `apps/web`

## Deployment Notes

Backend deployment currently happens manually on the EC2 instance:

```bash
git pull
sudo docker build -t smart-internship-tracker-api:latest apps/api
sudo docker stop smart-api
sudo docker rm smart-api
sudo docker run -d \
  --name smart-api \
  --network smart-net \
  --env-file ~/smart-api.env \
  --restart unless-stopped \
  smart-internship-tracker-api:latest
```

The backend environment file lives on the EC2 instance at `~/smart-api.env` and must not be committed. It contains production values for `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, and `CORS_ALLOWED_ORIGINS`.

Frontend deployment happens through Vercel from the same GitHub repository with:

```text
Root Directory: apps/web
Build Command: npm run build
Output Directory: dist
Install Command: npm install
```

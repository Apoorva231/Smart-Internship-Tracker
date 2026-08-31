# API Runbook

This runbook documents how to run, test, configure, and call the Smart Internship Tracker Spring Boot API.

## Backend Commands

Start local PostgreSQL from the repository root:

```bash
docker compose up -d postgres
```

Run the full backend test suite from the repository root:

```bash
mvn -f apps/api/pom.xml test
```

Run all Maven modules from the repository root:

```bash
mvn test
```

Run only the backend integration tests:

```bash
mvn -f apps/api/pom.xml -Dtest=BackendIntegrationTest test
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

## Environment Variables

The API reads configuration from `apps/api/src/main/resources/application.properties`.

Local development defaults. These values are for running the app on your machine only; production should replace every secret or database credential.

| Variable | Local default | Purpose |
| --- | --- | --- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5433/smart_internship_tracker` | Local Docker PostgreSQL JDBC URL |
| `DATABASE_USERNAME` | `smart_tracker` | Local Docker PostgreSQL username |
| `DATABASE_PASSWORD` | `smart_tracker` | Local Docker PostgreSQL password; do not reuse in production |
| `JWT_SECRET` | `01234567890123456789012345678901` | Local fallback signing secret; do not reuse in production |
| `JWT_EXPIRATION_MINUTES` | `60` | JWT lifetime in minutes |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed frontend origin |

Production requirements:

- Set `SPRING_PROFILES_ACTIVE=prod`.
- Set `JWT_SECRET` to a real long random secret. The production profile rejects the local default JWT secret.
- Set `CORS_ALLOWED_ORIGINS` to the deployed frontend origin.
- Set `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` to the production database connection.

Spring Boot uses port `8080` by default unless `server.port` is configured.

## Current Deployment

The current portfolio deployment uses:

- Frontend: Vercel at `https://smart-internship-tracker-ebon.vercel.app`
- Backend HTTPS proxy: Caddy at `https://3-21-242-207.sslip.io`
- Backend API container: `smart-api` on a private Docker network
- Backend image: `smart-internship-tracker-api:latest`
- Database: Neon Postgres
- EC2 host OS: Amazon Linux 2023

Request path:

```text
Browser
  -> Vercel frontend
  -> https://3-21-242-207.sslip.io/api/...
  -> Caddy container
  -> smart-api:8080
  -> Neon Postgres
```

The `sslip.io` hostname maps the EC2 public IPv4 address into DNS. If the EC2 public IP changes, update the Caddyfile hostname, Vercel `VITE_API_URL`, and backend CORS origin as needed. An Elastic IP or custom domain should replace this before treating the deployment as stable.

## Production Environment File

On EC2, production runtime configuration is stored outside Git:

```text
~/smart-api.env
```

Template:

```env
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://<neon-host>/<database>?sslmode=require&channelBinding=require
DATABASE_USERNAME=<neon-role>
DATABASE_PASSWORD=<neon-password>
JWT_SECRET=<long-random-secret>
JWT_EXPIRATION_MINUTES=60
CORS_ALLOWED_ORIGINS=http://3.21.242.207,http://localhost:5173,https://smart-internship-tracker-ebon.vercel.app
```

Do not commit this file. It contains database credentials and the JWT signing secret.

Generate a JWT secret on the EC2 host:

```bash
openssl rand -hex 32
```

Restrict the env file so only the EC2 user can read/write it:

```bash
chmod 600 ~/smart-api.env
```

## Backend Docker Image

Build the backend image from the repository root:

```bash
sudo docker build -t smart-internship-tracker-api:latest apps/api
```

The Dockerfile is multi-stage:

- Build stage: Maven and JDK compile the app and create the executable Spring Boot jar.
- Runtime stage: JRE-only image runs `java -jar app.jar` as the non-root `appuser`.

If building on a `t3.micro`, add swap first to reduce memory-related build failures:

```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
free -h
```

## Docker Network

Create the private network used by Caddy and the API container:

```bash
sudo docker network create smart-net
```

Containers attached to the same Docker network can reach each other by container name. Caddy reaches the API at:

```text
smart-api:8080
```

## Run The API Container

Start the API container without exposing it directly to the internet:

```bash
sudo docker run -d \
  --name smart-api \
  --network smart-net \
  --env-file ~/smart-api.env \
  --restart unless-stopped \
  smart-internship-tracker-api:latest
```

Useful API container commands:

```bash
sudo docker ps
sudo docker logs --tail=100 smart-api
sudo docker stop smart-api
sudo docker rm smart-api
```

## Caddy HTTPS Proxy

Caddy handles public HTTPS and forwards traffic to the API container over the private Docker network.

Create `~/Caddyfile`:

```text
3-21-242-207.sslip.io {
    reverse_proxy smart-api:8080
}
```

Start Caddy:

```bash
sudo docker run -d \
  --name caddy \
  --network smart-net \
  -p 80:80 \
  -p 443:443 \
  -v ~/Caddyfile:/etc/caddy/Caddyfile:ro \
  -v caddy_data:/data \
  -v caddy_config:/config \
  --restart unless-stopped \
  caddy:2
```

The `caddy_data` and `caddy_config` Docker volumes preserve certificate and config state across container restarts.

Useful Caddy commands:

```bash
sudo docker logs --tail=100 caddy
sudo docker restart caddy
```

## Health Checks

From the EC2 host, verify the API container through Caddy:

```bash
curl https://3-21-242-207.sslip.io/api/health
```

Expected response:

```json
{"status":"ok"}
```

From a browser, verify:

```text
https://3-21-242-207.sslip.io/api/health
```

Then verify the full app through Vercel:

```text
https://smart-internship-tracker-ebon.vercel.app
```

Create an account, add an application, reload the page, sign out, and sign back in. Persistence after reload/login confirms the path from Vercel to Caddy to Spring Boot to Neon is working.

## Manual Backend Redeploy

Current manual redeploy process on EC2:

```bash
cd ~/Smart-Internship-Tracker
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
sudo docker logs --tail=100 smart-api
curl https://3-21-242-207.sslip.io/api/health
```

This should eventually move to Docker Compose or CI/CD so deployment is less manual.

## Authentication Flow

Registration creates a user and returns a JWT, so a newly registered user can be treated as logged in by the frontend.

Register:

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "name": "Apoorva",
  "email": "apoorva@example.com",
  "password": "Password123!"
}
```

Login:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "apoorva@example.com",
  "password": "Password123!"
}
```

Both endpoints return:

```json
{
  "user": {
    "id": "user_...",
    "name": "Apoorva",
    "email": "apoorva@example.com",
    "city": "Montreal, QC"
  },
  "token": "..."
}
```

Password rules:

- 10 to 72 characters
- At least one lowercase letter
- At least one uppercase letter
- At least one number
- At least one symbol

Duplicate registration emails return `409 Conflict`. Invalid login credentials return `401 Unauthorized`.

## Protected Routes

Protected routes require:

```http
Authorization: Bearer <token>
```

The JWT `sub` claim is the authenticated user id. Controllers read it with `@AuthenticationPrincipal Jwt`, similar to reading `req.user` after Express auth middleware.

Protected route groups:

- `/api/auth/me`
- `/api/applications`
- `/api/applications/**`
- `/api/tasks/**`
- `/api/companies`
- `/api/companies/**`

Public routes:

- `/api/auth/register`
- `/api/auth/login`
- `/api/health`

Do not use `X-User-Id` for new code. Protected application and task routes use the JWT subject for ownership checks.

Missing or invalid authentication returns a JSON response whose `message` is `Unauthorized`.

CORS is applied to `/api/**` through Spring Security. The local default allowed origin is `http://localhost:5173`, matching Vite. Allowed methods are `GET`, `POST`, `PATCH`, `DELETE`, and `OPTIONS`; allowed headers are `Authorization` and `Content-Type`.

## Endpoint Summary

### Health

`GET /api/health`

Public health check. Returns:

```json
{
  "status": "ok"
}
```

### Auth

`POST /api/auth/register`

Creates a user and returns a user envelope plus JWT.

Required body fields:

- `name`
- `email`
- `password`

`POST /api/auth/login`

Authenticates a user and returns a user envelope plus JWT.

Required body fields:

- `email`
- `password`

`GET /api/auth/me`

Protected. Returns the authenticated user for an existing bearer token.

Returns:

```json
{
  "user": {
    "id": "user_...",
    "name": "Apoorva",
    "email": "apoorva@example.com",
    "city": "Montreal, QC"
  }
}
```

### Applications

`GET /api/applications`

Protected. Lists applications owned by the authenticated user.

Optional query parameters:

- `status`: one of `SAVED`, `APPLIED`, `INTERVIEW`, `TECHNICAL`, `OFFER`, `REJECTED`, `ARCHIVED`
- `search`: text search across role, company name, and company location

Returns:

```json
{
  "applications": []
}
```

`POST /api/applications`

Protected. Creates an application for the authenticated user.

Required fields:

- `role`
- Either `companyId` or `companyName`

Optional fields:

- `companyLocation`
- `companyWebsite`
- `companyIndustry`
- `companySize`
- `status`
- `workMode`
- `priority`
- `deadline`
- `jobUrl`
- `salaryRange`
- `contactName`
- `contactEmail`
- `notes`

Defaults:

- `status`: `SAVED`
- `workMode`: `HYBRID`
- `priority`: `2`
- `companyLocation`: `Montreal, QC`
- `companyIndustry`: `Technology`

`GET /api/applications/insights`

Protected. Returns dashboard data for the authenticated user:

- `counts`: application counts by status
- `metrics`: total, active, interview, offer, and high-priority totals
- `upcomingTasks`: up to five incomplete dated tasks

`GET /api/applications/{id}`

Protected. Returns one application owned by the authenticated user. Missing or non-owned applications return `404 Not Found`.

`PATCH /api/applications/{id}`

Protected. Updates one application owned by the authenticated user. Missing fields are left unchanged.

`DELETE /api/applications/{id}`

Protected. Deletes one application owned by the authenticated user and returns `204 No Content`.

### Tasks

`POST /api/applications/{applicationId}/tasks`

Protected. Creates a task for an application owned by the authenticated user.

Required body fields:

- `title`

Optional body fields:

- `dueDate`

New tasks default to `completed: false`.

`PATCH /api/tasks/{taskId}`

Protected. Updates a task owned through the authenticated user's application. Missing fields are left unchanged.

Optional body fields:

- `title`
- `dueDate`
- `completed`

`DELETE /api/tasks/{taskId}`

Protected. Deletes a task owned through the authenticated user's application and returns `204 No Content`.

### Companies

`GET /api/companies`

Protected. Lists reusable company records. Company records are global reusable data, but the endpoint still requires a valid JWT.

Returns:

```json
{
  "companies": []
}
```

## Error Responses

Most API errors return JSON with a `message` field. Validation errors also include an `errors` object keyed by field name.

Validation failure:

```json
{
  "message": "Validation failed",
  "errors": {
    "fieldName": "Validation message"
  }
}
```

Common statuses:

- `400 Bad Request`: validation failure, invalid JSON body, or invalid request parameter
- `401 Unauthorized`: missing/invalid bearer token or invalid login credentials
- `404 Not Found`: missing or non-owned application/task, or missing company reference
- `409 Conflict`: duplicate registration email

## Integration Test Notes

Backend integration tests use Testcontainers to start a disposable PostgreSQL database for the test run. Docker must be running.

The integration tests boot the full Spring application, run Flyway migrations against real PostgreSQL, send requests through Spring MVC/Security with `MockMvc`, and verify behavior across controllers, services, repositories, and JWT authentication.

Current integration coverage lives in:

```text
apps/api/src/test/java/com/smartinternshiptracker/BackendIntegrationTest.java
```

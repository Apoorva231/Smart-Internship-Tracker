# Smart Internship Tracker

A full-stack internship tracking app for managing applications, companies, follow-up tasks, and dashboard insights.

The current implementation focus is the Spring Boot API. The repository has been structured as a monorepo so a React frontend can live beside the backend later.

## Project Structure

```text
apps/
  api/    Spring Boot API
  web/    Future React frontend
docs/     Engineering notes and architecture decisions
```

## Development Approach

- Build feature by feature.
- Commit meaningful milestones to GitHub.
- Keep an engineering log of architectural decisions and tradeoffs.

## Planned Features

- Health check endpoint
- PostgreSQL database integration
- User registration and login
- JWT-based authentication
- Company and application tracking
- Follow-up tasks
- Dashboard insights
- React frontend

## Engineering Notes

See [docs/engineering-log.md](docs/engineering-log.md).

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

# Smart Internship Tracker

A Spring Boot backend built from the ground up for tracking internship applications, follow-ups, companies, and application status.

This project is intentionally developed in small, professional increments so each backend concept is easy to understand and review.

## Development Approach

- Build feature by feature.
- Commit meaningful milestones to GitHub.
- Keep an engineering log of architectural decisions and tradeoffs.

## Planned Backend Features

- Health check endpoint
- PostgreSQL database integration
- User registration and login
- JWT-based authentication
- Company and application tracking
- Follow-up tasks
- Dashboard insights

## Engineering Notes

See [docs/engineering-log.md](docs/engineering-log.md).

## Local Database

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Run the application:

```bash
mvn spring-boot:run
```

By default, the app connects to:

- Database: `smart_internship_tracker`
- Username: `smart_tracker`
- Password: `smart_tracker`

You can override the connection with `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD`.

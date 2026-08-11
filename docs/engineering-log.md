# Engineering Log

This document tracks major architecture decisions, tradeoffs, and implementation notes as Smart Internship Tracker is built.

## Decision 001: Build As A Greenfield Spring Boot Backend

Date: 2026-08-10

We will build the backend from scratch instead of porting the existing Montreal Internship Tracker implementation.

Why:

- The goal is to learn Spring Boot deeply, not copy an existing backend.
- Small increments make each concept easier to understand.
- A fresh history makes GitHub commits more meaningful for portfolio review.

Tradeoffs:

- Building from scratch is slower than porting.
- Some product behavior will be guided by the previous tracker as reference.
- We need to be disciplined about documenting decisions as the app grows.

## Decision 002: Use Java 21 And Maven

Date: 2026-08-10

We will use Java 21 and Maven for the Spring Boot backend.

Why:

- Java 21 is an LTS release and widely used in Spring Boot projects.
- Maven is already installed and configured locally.
- Maven is common in Spring tutorials, docs, and enterprise codebases.

Tradeoffs:

- Gradle can be more flexible for large builds.
- Maven XML is more verbose, but it is explicit and beginner-friendly.

## Decision 003: Manage Schema Changes With Flyway

Date: 2026-08-10

We will use PostgreSQL for the application database, Spring Data JPA for persistence, and Flyway for versioned schema migrations.

Why:

- PostgreSQL is production-grade and common in backend roles.
- JPA lets the Java domain model map cleanly to relational data.
- Flyway keeps schema changes explicit, reviewable, and repeatable.
- Setting Hibernate to `validate` prevents the app from silently changing the schema outside migrations.

Tradeoffs:

- Local development now needs a running PostgreSQL database.
- Every schema change requires a migration, which is a little slower but much safer.
- JPA can hide SQL details, so we should still inspect generated queries as the model grows.

## Commit Strategy

We will push after successful logical milestones, such as:

- Initial project setup
- Spring Boot scaffold
- Database integration
- Authentication
- Application CRUD
- Task management
- Dashboard insights

# 0001 — Microservices over a monolith

- Status: Accepted
- Date: 2026-07-19

## Context

VibeUs is a learning-oriented backend meant to demonstrate realistic backend
architecture end-to-end, not just a CRUD tutorial. We need clear service
boundaries, independent data ownership, and both sync and async communication.

## Decision

Build the system as cooperating services (gateway, auth, user, music) behind an
API gateway, each independently deployable and owning its own data.

## Consequences

- **Pros**: clear boundaries, independent scaling/deploys, realistic patterns
  (gateway, service-to-service calls, events).
- **Cons**: more operational overhead than a monolith (multiple processes,
  distributed concerns, eventual consistency).
- Local development is orchestrated via Docker Compose for infrastructure and
  per-service Spring Boot apps.

# 0004 — Kafka for asynchronous events

- Status: Accepted
- Date: 2026-07-19

## Context

Some work should not block the request path (notifications, indexing, analytics)
and multiple consumers may react to the same fact independently.

## Decision

Use Kafka for domain events. user-service publishes `user.registered` after the
profile is committed; consumers react asynchronously. Events are JSON, keyed by
aggregate id, with additive/backward-compatible evolution.

## Consequences

- **Pros**: decoupled producers/consumers, replayable log, easy to add new
  consumers without touching producers.
- **Cons**: eventual consistency; requires thinking about idempotency and
  ordering; more infrastructure to run locally.
- Sync REST is still used when the caller needs an immediate answer
  (e.g. auth → user profile creation).

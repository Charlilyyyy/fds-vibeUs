# 0003 — Database per service

- Status: Accepted
- Date: 2026-07-19

## Context

Services must be able to evolve their schemas independently and avoid hidden
coupling through a shared database.

## Decision

Each service owns a dedicated PostgreSQL database (`vibeus_auth_db`,
`vibeus_user_db`, `vibeus_music_db`). Services reference other aggregates by id
only and never read another service's tables.

## Consequences

- **Pros**: strong encapsulation, independent schema evolution, clear ownership.
- **Cons**: no cross-service joins or foreign keys; cross-service consistency is
  achieved via sync calls (auth → user) or async events (`user.registered`).
- Requires discipline: data duplication is intentional and kept minimal.

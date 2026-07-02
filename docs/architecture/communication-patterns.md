# Communication Patterns

## Overview

VibeUs integrates services using two patterns:

1. **Synchronous REST** — request/response when an immediate outcome is required
2. **Asynchronous Kafka messaging** — fire-and-forget domain events when loose coupling is preferred

This document defines when to use each pattern, concrete MVP flows, topic and API contracts, and error-handling expectations.

---

## Pattern Selection Guide

| Situation | Pattern | Example |
|-----------|---------|---------|
| Client needs immediate HTTP response | REST via gateway | Login, get profile, create artist |
| One service must complete before another continues the same user action | Sync REST (service-to-service) | Auth creates profile during registration |
| Side effects can happen after the main transaction | Kafka event | Notify future consumers that a user registered |
| Client uploads binary file | REST multipart | Track audio upload |
| Read-heavy cross-domain query | REST (MVP) | Get catalog by id |
| Cross-domain query at scale | Event + materialized view | Post-MVP search indexing |

**MVP rule:** Default to REST for reads and command APIs exposed to clients. Use Kafka when the producing service should not know or wait for downstream consumers.

---

## Synchronous REST

### Client → Gateway → Service

All external traffic uses a single entry point:

```
Client
  │  HTTP (JSON)
  ▼
gateway-service :8081
  │  proxy + JWT filter
  ▼
target-service :8082 | :8083 | :8084
  │
  ▼
HTTP response (JSON)
```

| Property | Value |
|----------|-------|
| Protocol | HTTP/1.1 |
| Format | JSON request/response bodies |
| Auth header | `Authorization: Bearer <jwt>` on protected routes |
| Version prefix | `/api/v1` |
| Timeouts | Gateway route timeout configurable (e.g. 30s local) |

**Gateway does not transform** business payloads — it routes and enforces security.

---

### Public vs protected routes

| Route class | JWT required | Examples |
|-------------|--------------|----------|
| Public | No | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` |
| Protected | Yes | `/api/v1/users/**`, `/api/v1/artists/**`, etc. |

Invalid or missing JWT → **401 from gateway**; downstream service is not invoked.

---

### Service-to-service: auth → user (registration)

The only **required** synchronous internal call in MVP:

```
auth-service :8082
  │
  │  POST http://user-service:8083/internal/users
  │  Content-Type: application/json
  │  Body: CreateUserProfileRequest { userId, email, username, ... }
  ▼
user-service :8083
  │
  ├─ 201 Created + UserResponse
  └─ (then) publish UserRegisteredEvent to Kafka
```

| Property | Value |
|----------|-------|
| Client library | Spring Cloud OpenFeign (preferred) or `RestClient` |
| Caller | auth-service only |
| Callee path | `/internal/users` |
| Gateway exposure | **None** — not listed in gateway routes |
| Idempotency | Duplicate create with same `userId` should return 409 |

**Failure handling:**

| Failure | Auth service behavior |
|---------|----------------------|
| User service 4xx | Propagate or map to registration failure; do not leave orphan credentials without compensation |
| User service 5xx / timeout | Retry policy (limited) or fail registration with 503/500 |
| User service success, Kafka publish fails | Profile exists; event may be missing — log and document (at-least-once intent) |

---

### JWT propagation to downstream services

After gateway validation, downstream services receive the original `Authorization` header (or trusted internal headers if implemented later).

| Service | How identity is resolved |
|---------|--------------------------|
| user-service | `UserContext` reads JWT `sub` as `userId` |
| music-service | JWT required; MVP may not bind catalog rows to user id |
| auth-service | Issues token; login/register are unauthenticated endpoints |

Services **do not** call auth on every request to validate tokens in MVP — gateway is the primary enforcement point; services parse JWT locally with shared secret/public key.

---

### Internal API security (MVP)

`/internal/**` endpoints are protected by:

- **Network:** localhost / Docker compose network only
- **No gateway route:** not reachable from public clients
- **Optional:** static internal API key header in a later hardening pass

Do not rely on obscurity alone in production; MVP documents the local-dev posture.

---

## Asynchronous Messaging (Kafka)

### Role in MVP

| Component | Responsibility |
|-----------|----------------|
| Kafka broker | Durable event log |
| user-service | Producer of `UserRegisteredEvent` |
| Consumers | None required in MVP |
| Kafka UI | Human verification of published events |

---

### Topic naming convention

```
<domain>.<event-action>
```

| Topic | Producer | Consumers (MVP) |
|-------|----------|-----------------|
| `user.registered` | user-service | None (verify in Kafka UI) |

**Future topics (documented, not implemented):**

| Topic | Producer | Potential consumer |
|-------|----------|-------------------|
| `catalog.track.created` | music-service | search-service |
| `playlist.updated` | playlist-service | recommendation-service |

Use lowercase, dot-separated names. Avoid environment prefix in topic name for local dev (`user.registered`, not `dev.user.registered`) unless multi-tenant broker sharing demands it.

---

### Event: `UserRegisteredEvent`

Published after profile row is committed to `vibeus_user_db`.

**Payload (JSON):**

```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "username": "jazzfan",
  "email": "user@example.com",
  "occurredAt": "2026-06-30T12:00:00Z"
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `userId` | UUID string | Yes | Matches auth and profile primary key |
| `username` | String | Yes | Profile username |
| `email` | String | Yes | Registration email |
| `occurredAt` | ISO-8601 instant | Yes | Event timestamp |

**Producer configuration (guidelines):**

| Setting | MVP value |
|---------|-----------|
| Key | `userId` (string) — stable partitioning by user |
| Serializer | `StringSerializer` key, `JsonSerializer` value (or Jackson manual) |
| Acknowledgement | `acks=1` acceptable locally |
| Retries | Enable limited retries on transient broker errors |

**Delivery semantics:** At-least-once. Consumers added later must be **idempotent** (dedupe by `userId` + `occurredAt` or event id).

---

### Producer flow (user-service)

```
POST /internal/users (from auth)
  │
  ├─ 1. BEGIN transaction
  ├─ 2. INSERT profile
  ├─ 3. COMMIT transaction
  ├─ 4. kafkaTemplate.send("user.registered", userId, event)
  └─ 5. Return 201 to auth
```

**Ordering:** Publish **after** DB commit to avoid events for rolled-back profiles.

**Post-MVP improvement:** Transactional outbox table + relay worker for guaranteed publish.

---

## End-to-End Flows

### Flow A: Login (sync only)

```
Client → Gateway → auth-service → 200 + JWT
```

No Kafka. No cross-service call.

---

### Flow B: Registration (sync + async)

```
Client → Gateway → auth-service
                      │
                      ├─ sync → user-service /internal/users
                      │            └─ async → Kafka user.registered
                      └─ 201 → Client
```

---

### Flow C: Create artist (sync only)

```
Client → Gateway → music-service → vibeus_music_db
                └─ 201 → Client
```

No Kafka in MVP for catalog mutations.

---

### Flow D: Upload track audio (sync + object storage)

```
Client → Gateway → music-service
                      ├─ MinIO PUT object
                      ├─ UPDATE track.audio_url
                      └─ 200 → Client
```

MinIO is infrastructure accessed synchronously by music-service — not a separate microservice call.

---

## Sequence Diagram: Registration

```
Client          Gateway         Auth            User            Kafka
  │                │              │               │               │
  │ POST /register │              │               │               │
  │───────────────►│─────────────►│               │               │
  │                │              │ POST /internal/users          │
  │                │              │──────────────►│               │
  │                │              │               │ INSERT profile│
  │                │              │               │──────────────►│
  │                │              │               │ publish event │
  │                │              │◄──────────────│               │
  │                │◄─────────────│  201          │               │
  │◄───────────────│  201         │               │               │
```

---

## Error & Response Propagation

| Layer | Responsibility |
|-------|----------------|
| Gateway | 401 auth failures; 502/504 if downstream unreachable |
| Domain service | 400 validation, 404 not found, 409 conflict, 500 safe body |
| Feign client (auth) | Map user-service errors to registration failure response |

**Do not leak** internal stack traces or downstream hostnames in client-facing JSON.

---

## What We Do Not Use in MVP

| Pattern | Status |
|---------|--------|
| Kafka request-reply | Not used |
| Saga orchestrator | Manual compensation in auth only |
| gRPC between services | REST only |
| WebSockets | Not used |
| Event sourcing | CRUD + occasional events |
| Redis pub/sub | Redis not integrated in app layer |

---

## Future Communication Extensions

| Need | Likely pattern |
|------|----------------|
| Search index update | music-service produces `catalog.*` events; search-service consumes |
| Playlist sync | playlist-service REST + `playlist.*` events |
| Email on register | notification-service consumes `user.registered` |
| Cache invalidation | Redis subscriber on catalog update events |

---

## Implementation Checklist

- [ ] Gateway routes documented and match [mvp-scope.md](../requirements/mvp-scope.md)
- [ ] Feign client interface in auth-service for user internal API
- [ ] `user.registered` topic created (auto-create or compose init)
- [ ] `UserRegisteredEvent` record/class shared or duplicated consistently in user-service
- [ ] Kafka bootstrap: `localhost:9092` (host) / `vibeus-kafka:29092` (container network)
- [ ] Internal routes excluded from gateway configuration
- [ ] JWT secret shared between auth (sign) and gateway (verify)

---

## Related Documents

| Document | Link |
|----------|------|
| Architecture overview | [overview.md](overview.md) |
| Service boundaries | [service-boundaries.md](service-boundaries.md) |
| Technology stack | [tech-stack.md](tech-stack.md) |
| Deployment topology | `deployment-topology.md` (upcoming) |
| Functional — users | [functional-users.md](../requirements/functional-users.md) |
| Non-functional — messaging | [non-functional.md](../requirements/non-functional.md) |

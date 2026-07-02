# Architecture Overview

## Summary

VibeUs is built as a **microservices backend** behind a **single API gateway**. Four independently deployable Spring Boot services — gateway, auth, user, and music — each own a slice of domain logic and (where applicable) a dedicated PostgreSQL database. Clients interact only with the gateway; internal services communicate over HTTP for synchronous workflows and Kafka for asynchronous domain events.

This document establishes the architectural style, system context, and guiding principles. Detailed stack choices, boundaries, communication patterns, deployment topology, and naming conventions are covered in companion architecture documents.

---

## Architectural Style

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Style | Microservices | Aligns with streaming-domain separation (identity, profiles, catalog, media) |
| Entry point | API Gateway | Centralized routing, JWT enforcement, CORS |
| Data | Database per service | Independent schema evolution; no shared tables |
| Auth model | Stateless JWT | Horizontally scalable; no session affinity |
| Async integration | Kafka events | Decouple registration side effects from request path |
| Sync integration | REST | Simple request/response for auth → user profile creation |
| Deployment (MVP) | Local Docker Compose + JARs | Reproducible dev environment without K8s complexity |

---

## System Context

External actors and systems interacting with VibeUs at MVP:

```
┌─────────────────┐         ┌──────────────────────────────────────────┐
│  API Client     │  HTTPS  │              VibeUs Backend                 │
│  (Postman,      │ ──────► │  ┌─────────┐    ┌──────┐ ┌──────┐ ┌───────┐ │
│   future app)   │         │  │ Gateway │───►│ Auth │ │ User │ │ Music │ │
└─────────────────┘         │  │  :8081  │    └──┬───┘ └──┬───┘ └───┬───┘ │
                            │  └─────────┘       │        │         │     │
                            │                    ▼        ▼         ▼     │
                            │              PostgreSQL  PostgreSQL  PostgreSQL
                            │              (auth)      (user)     (music) │
                            │                    │        │         │     │
                            │                    └────────┼─────────┘     │
                            │                             ▼               │
                            │                    Kafka / MinIO / Redis    │
                            └──────────────────────────────────────────┘
```

**Primary actor:** Developer or API client exercising REST endpoints through the gateway.

**External systems (infrastructure):** PostgreSQL, MinIO, Kafka, Redis — managed via Docker Compose in local development.

---

## Container Diagram (MVP Services)

```
                    ┌─────────────────────────────────────┐
                    │         gateway-service           │
                    │  Spring Cloud Gateway :8081       │
                    │  • Route definitions              │
                    │  • JWT authentication filter      │
                    │  • CORS                           │
                    └───────────┬─────────────────────────┘
                                │
          ┌─────────────────────┼─────────────────────┐
          │                     │                     │
          ▼                     ▼                     ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  auth-service   │  │  user-service   │  │  music-service  │
│     :8082       │  │     :8083       │  │     :8084       │
│                 │  │                 │  │                 │
│ • Register      │  │ • Profile CRUD  │  │ • Catalog CRUD  │
│ • Login         │──► • Internal API  │  │ • File upload   │
│ • JWT issue     │  │ • Kafka produce │  │ • MinIO client  │
└────────┬────────┘  └────────┬────────┘  └────────┬────────┘
         │                    │                    │
         ▼                    ▼                    ▼
   vibeus_auth_db      vibeus_user_db       vibeus_music_db
```

The gateway has **no database**. Auth calls user over HTTP during registration; user publishes to Kafka after profile creation; music reads/writes MinIO for binary assets.

---

## Request Flow (Typical Protected Call)

```
1. Client ──► Gateway :8081
              │
              ├─ Public route?  → forward without JWT
              │
              └─ Protected route?
                    ├─ Validate JWT (signature, expiry)
                    ├─ Invalid → 401
                    └─ Valid → forward to target service :8082–8084
                                    │
                                    └─ Service validates claims / loads resource → response
```

---

## Registration Flow (Cross-Service)

```
Client
  │ POST /api/v1/auth/register
  ▼
Gateway → Auth Service
              │
              ├─ Save credentials (vibeus_auth_db)
              │
              └─ HTTP POST /internal/users → User Service
                        │
                        ├─ Save profile (vibeus_user_db)
                        └─ Publish UserRegisteredEvent → Kafka
              │
              ▼
         201 Created → Client
```

Registration is the primary **synchronous multi-service** workflow in MVP. It defines the pattern for explicit service coupling where immediate consistency is required.

---

## Architecture Principles

### 1. Single responsibility per service

Each service maps to one bounded context: credentials (auth), profiles (user), catalog and media (music), edge routing (gateway).

### 2. Smart endpoints, dumb pipes

REST and Kafka carry data; business rules live in services. Avoid shared libraries that smuggle domain logic across boundaries in MVP.

### 3. Fail fast at the edge

Authentication failures are rejected at the gateway before downstream load accrues.

### 4. Explicit over implicit integration

Service URLs, topic names, and route paths are configuration — documented and discoverable, not hard-coded magic strings scattered without reference.

### 5. Evolve without rewrite

Playlist, search, and streaming services can be added as new containers with new databases; gateway gains new routes. Core four services remain stable.

---

## What This Architecture Optimizes For

| Optimized for | Not optimized for (MVP) |
|---------------|-------------------------|
| Learning microservice boundaries | Millions of RPS |
| Local reproducibility | Multi-region failover |
| Clear security perimeter | Zero-trust service mesh |
| Incremental feature addition | Minimal operational footprint |

---

## Alternatives Considered

| Alternative | Why not chosen for MVP |
|-------------|------------------------|
| **Monolith** | Hides service boundaries; weaker learning value for distributed patterns |
| **BFF per client** | No clients in MVP; gateway suffices |
| **Service mesh (Istio)** | Operational overhead disproportionate for local dev |
| **Shared database** | Violates per-service data ownership; couples deployments |
| **gRPC internal / REST external** | REST keeps tooling simple for learning scope |

---

## Document Map

| Topic | Document |
|-------|----------|
| Languages, frameworks, infra products | `tech-stack.md` (upcoming) |
| Bounded contexts & data ownership | `service-boundaries.md` (upcoming) |
| REST vs Kafka usage | `communication-patterns.md` (upcoming) |
| Ports, compose, networking | `deployment-topology.md` (upcoming) |
| Packages, naming, API prefixes | `conventions.md` (upcoming) |
| Locked MVP scope | [mvp-scope.md](../requirements/mvp-scope.md) |

---

## Related Requirements

| Requirement area | Reference |
|------------------|-----------|
| Functional | [requirements/](../requirements/) |
| Non-functional | [non-functional.md](../requirements/non-functional.md) |
| MVP scope | [mvp-scope.md](../requirements/mvp-scope.md) |

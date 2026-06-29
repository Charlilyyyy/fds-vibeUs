# Learning Goals

## Purpose

VibeUs is intentionally built as a **learning project**. This document states what builders should be able to do, know, and explain after working through the MVP — and maps those outcomes to the technologies and patterns used along the way.

## Primary Learning Outcomes

By completing the MVP described in [features](features.md), a builder should be able to:

1. **Decompose a domain into microservices** with clear ownership and separate databases
2. **Implement stateless JWT authentication** and enforce it at an API gateway
3. **Build REST APIs** with validation, pagination, and consistent error handling in Spring Boot
4. **Integrate object storage** for binary uploads (images and audio) alongside relational metadata
5. **Publish and consume domain events** with Kafka for loosely coupled workflows
6. **Run a multi-container local stack** with Docker Compose and connect services to it confidently
7. **Document and defend architectural decisions** in interviews or design reviews

---

## Technical Skills by Area

### Java & Spring Boot

| Skill | What you will practice |
|-------|------------------------|
| Spring Boot 3 on Java 21 | Project structure, auto-configuration, profiles |
| Spring Web | REST controllers, request/response DTOs, validation annotations |
| Spring Data JPA | Entities, repositories, relationships, auditing (`BaseEntity`) |
| Spring Security | Stateless sessions, password encoding, security filter chains |
| Spring Cloud Gateway | Routes, predicates, filters, JWT validation at the edge |
| OpenFeign (or REST client) | Synchronous service-to-service calls (auth → user) |
| Lombok | Reduce boilerplate in entities and DTOs |
| Spring Actuator | Health endpoints for local and container checks |

**Goal:** Comfortably navigate a multi-module Spring codebase and add a new endpoint following existing patterns.

### Microservices Architecture

| Skill | What you will practice |
|-------|------------------------|
| Service boundaries | Auth credentials vs user profile vs music catalog |
| Database per service | No shared tables; eventual consistency where needed |
| API gateway pattern | Single public URL; internal service ports hidden |
| Sync vs async integration | REST for request/response; Kafka for fire-and-forget events |
| Internal vs public APIs | Token validation and internal-only profile creation |

**Goal:** Draw a diagram of VibeUs services, databases, and message flows from memory and explain why each box exists.

### Security

| Skill | What you will practice |
|-------|------------------------|
| JWT structure | Claims, signing, expiration, bearer header |
| Gateway-level auth | Block unauthenticated traffic before it hits domain services |
| Password handling | BCrypt or equivalent; never store plaintext |
| Authorization basics | User can only access own profile; public register/login routes |

**Goal:** Trace a protected request from client → gateway → user service and identify where the token is parsed and trusted.

### Data & Persistence

| Skill | What you will practice |
|-------|------------------------|
| PostgreSQL | One database per service; connection config in `application.yml` |
| JPA modeling | Artist → Album → Track relationships; enums for genre |
| Migrations / DDL | Schema creation via JPA or explicit scripts |
| Pagination | Pageable list endpoints for catalog resources |

**Goal:** Add a new catalog field end-to-end: entity, DTO, service, controller, validation.

### Object Storage (MinIO)

| Skill | What you will practice |
|-------|------------------------|
| S3-compatible API | Buckets, keys, content types |
| Multipart uploads | Controller receiving files; service storing blobs |
| URL exposure | Return stable references in API responses |
| Error handling | Missing bucket, invalid file type, size limits |

**Goal:** Upload an album cover and retrieve catalog JSON that includes the stored file reference.

### Messaging (Kafka)

| Skill | What you will practice |
|-------|------------------------|
| Producers | Publish `UserRegisteredEvent` (or equivalent) after profile creation |
| Topics & serialization | JSON payloads; topic naming conventions |
| Kafka UI | Verify messages during development |
| Event-driven thinking | When to use events vs direct HTTP calls |

**Goal:** Register a user and observe the corresponding event in Kafka UI without manual topic seeding.

### Infrastructure & DevOps (Local)

| Skill | What you will practice |
|-------|------------------------|
| Docker Compose | PostgreSQL, Redis, MinIO, Kafka, Zookeeper (or KRaft), Kafka UI |
| Environment configuration | Ports, credentials, service URLs via env vars |
| Networking between containers | Services reach infra by hostname on compose network |
| Troubleshooting | Logs, connection refused, wrong database name |

**Goal:** Start infrastructure from a cold machine, then boot all four application services and hit the gateway successfully.

### API Design & Quality

| Skill | What you will practice |
|-------|------------------------|
| Consistent responses | `ApiResponse<T>`, error body shape |
| Exception handling | `GlobalExceptionHandler` for 400, 401, 404, 409 |
| Input validation | Bean Validation on request DTOs |
| REST conventions | HTTP verbs, status codes, resource naming |

**Goal:** Call an invalid endpoint or send bad input and interpret the error JSON without reading server logs.

---

## Architecture & System Design Thinking

Beyond framework mechanics, VibeUs reinforces design judgment:

- **When to split a service** — e.g., auth credentials live separately from profile presentation data
- **Trade-offs of sync calls** — simplicity vs coupling vs latency (auth calling user on register)
- **Trade-offs of async events** — decoupling vs eventual consistency and debugging difficulty
- **MVP scoping** — saying no to playlists, search, and streaming until the core slice works
- **Failure modes** — what happens when Kafka is down, MinIO is misconfigured, or token expires

---

## Skills Mapped to MVP Features

| MVP feature | Primary skills gained |
|-------------|----------------------|
| Register / login | Spring Security, JWT, validation |
| User profile CRUD | JPA, authenticated controllers, Feign/internal API |
| Catalog CRUD | Domain modeling, pagination, duplicate handling |
| File upload | MinIO client, multipart HTTP, storage errors |
| API gateway | Spring Cloud Gateway, route config, JWT filter |
| Docker Compose | Container orchestration at dev scale |
| Kafka event | Producer setup, event contract design |

---

## Self-Assessment Checklist

Use this before considering the MVP “learned”:

- [ ] I can start all infrastructure containers and explain what each one does
- [ ] I can register and log in, then call a protected endpoint with the returned token
- [ ] I can explain why auth and user are separate services with separate databases
- [ ] I can create an artist, album, and track through the API
- [ ] I can upload at least one image and one audio file to object storage
- [ ] I can list gateway routes and which are public vs protected
- [ ] I can find a published Kafka message after user registration
- [ ] I can describe three features deferred to post-MVP and why

---

## Stretch Goals (Optional)

After the MVP, builders may deepen knowledge with:

| Stretch topic | Suggested direction |
|---------------|-------------------|
| Redis caching | Cache hot catalog reads; invalidate on update events |
| Integration tests | Testcontainers for Postgres, Kafka, MinIO |
| OpenAPI / Swagger | Document each service’s endpoints |
| Playlist service | New bounded context with its own DB |
| Observability | Structured logging, correlation IDs across gateway hops |

These align with nice-to-have items in [features](features.md) and are not required to meet core learning outcomes.

---

## Relationship to Other Documents

| Document | Connection |
|----------|------------|
| [Problem statement](problem-statement.md) | Why hands-on learning beats reading alone |
| [Vision](vision.md) | Long-term destination after skills are in place |
| [Target users](target-users.md) | Who these goals are written for |
| [Value proposition](value-proposition.md) | Why this skill set transfers to industry roles |
| [Features](features.md) | What to build to practice each skill |

Together, these documents complete the **idea and vision** foundation for VibeUs before requirements, architecture, and implementation work begin.

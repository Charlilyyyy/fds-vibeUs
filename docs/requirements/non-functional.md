# Non-Functional Requirements

## Overview

Non-functional requirements (NFRs) describe **how** the VibeUs backend should behave — quality attributes that apply across auth, user, music, and gateway services. MVP targets **local development reliability** and **production-shaped patterns** without global-scale SLAs.

---

## NFR-SEC-01: Stateless Authentication

| Attribute | Requirement |
|-----------|-------------|
| Session model | No server-side HTTP sessions for API authentication |
| Token type | Signed JWT access token |
| Validation | Gateway validates token before forwarding protected requests |
| Expiry | Access tokens expire within a configured window (e.g. 15–60 minutes) |

**Rationale:** Stateless auth simplifies horizontal scaling and matches common microservice practice.

**Post-MVP:** Refresh tokens, token revocation lists.

---

## NFR-SEC-02: Password & Credential Protection

| Attribute | Requirement |
|-----------|-------------|
| Storage | Passwords stored as adaptive hashes (BCrypt or equivalent) |
| Transport | HTTPS assumed in non-local deployments; local HTTP acceptable for dev |
| Logging | Passwords and tokens never written to application logs |
| Responses | Auth errors use generic messages (no user enumeration) |

---

## NFR-SEC-03: Secrets & Configuration

| Attribute | Requirement |
|-----------|-------------|
| JWT secret / keys | Loaded from environment variables or external config — not hard-coded |
| Database credentials | Externalized per service `application.yml` + env overrides |
| MinIO / Kafka credentials | Externalized; compose `.env` or env files for local dev |
| Repository | No secrets committed to version control |

---

## NFR-SEC-04: Authorization Boundaries

| Attribute | Requirement |
|-----------|-------------|
| Gateway | Enforces authentication on protected routes |
| User service | Users may only update/deactivate their own profile on `{userId}` routes |
| Internal APIs | `/internal/**` not exposed on public gateway routes |
| Music service | Authenticated access required for MVP catalog mutations |

**Post-MVP:** Role-based access (admin vs user), rate limiting, input sanitization audits.

---

## NFR-SEC-05: Input Validation

| Attribute | Requirement |
|-----------|-------------|
| Request DTOs | Jakarta Bean Validation on all public write endpoints |
| File uploads | Content-type allow lists and maximum file size enforced |
| Error shape | Validation failures return HTTP 400 with field-level detail where safe |

---

## NFR-API-01: API Versioning

| Attribute | Requirement |
|-----------|-------------|
| Strategy | URI path versioning: `/api/v1/...` |
| MVP scope | Single version (`v1`) across auth, user, and music |
| Breaking changes | Require new version (`v2`) rather than silent contract breaks |

**Post-MVP:** Deprecation headers, aggregated OpenAPI across services.

---

## NFR-API-02: Consistent Response Contract

| Attribute | Requirement |
|-----------|-------------|
| Success wrapper | `ApiResponse<T>` with status code, message, data |
| Error wrapper | `ErrorResponse` with timestamp, status, message, optional field errors |
| HTTP semantics | Correct status codes (201 create, 204 delete, 401 unauthorized, 404 not found, 409 conflict) |

All services follow the same envelope so clients and gateway error forwarding stay predictable.

---

## NFR-API-03: CORS

| Attribute | Requirement |
|-----------|-------------|
| Configuration | Gateway defines allowed origins, methods, and headers |
| MVP | Permissive local setup acceptable (e.g. `localhost` frontend ports) |
| Production | Restrict to known client origins via configuration |

---

## NFR-SCALE-01: Service Independence

| Attribute | Requirement |
|-----------|-------------|
| Deployment unit | Each service is an independently runnable Spring Boot JAR |
| Data isolation | One PostgreSQL database per service — no shared tables |
| Coupling | Prefer REST for sync calls; Kafka for async side effects |

**Rationale:** Enables independent development, testing, and future scaling per service.

---

## NFR-SCALE-02: Scalability Posture (MVP)

| Attribute | MVP target | Future |
|-----------|------------|--------|
| Concurrent users | Tens on a developer machine | Horizontal pod autoscaling in K8s |
| Catalog size | Thousands of entities | Indexing, caching, read replicas |
| File storage | MinIO single-node | S3 / multi-node object storage |
| Messaging | Single Kafka broker | Clustered Kafka |

MVP does not require load testing; design should not block later horizontal scale (stateless services, externalized state).

---

## NFR-PERF-01: Response Time (Local MVP)

| Attribute | Target |
|-----------|--------|
| Auth login / register | < 2 seconds under local dev conditions |
| Catalog read by id | < 1 second |
| Paginated list | < 2 seconds for default page size |
| File upload | Bounded by file size cap and local disk I/O |

These are guidelines for local development, not contractual SLAs.

---

## NFR-PERF-02: Pagination Defaults

| Attribute | Requirement |
|-----------|-------------|
| Default page size | 20 |
| Maximum page size | 100 (reject or cap larger values) |

Prevents accidental full-table scans from client bugs.

---

## NFR-REL-01: Health & Readiness

| Attribute | Requirement |
|-----------|-------------|
| Actuator | Each service exposes health endpoint |
| Dependencies | Health may reflect database connectivity where configured |
| Gateway | Health route available for smoke tests |

---

## NFR-REL-02: Failure Handling

| Attribute | Requirement |
|-----------|-------------|
| Downstream unavailable | Gateway returns appropriate 502/503 with safe body |
| Registration partial failure | Auth + user flow defines compensating action (documented in functional auth requirements) |
| Kafka publish failure | Logged; MVP may proceed with at-least-once intent — document chosen behavior |

---

## NFR-DATA-01: Persistence & Auditing

| Attribute | Requirement |
|-----------|-------------|
| ORM | Spring Data JPA |
| Auditing | `createdAt` / `updatedAt` on entities via shared `BaseEntity` pattern |
| Identifiers | UUIDs for user ids and catalog entities |
| Soft delete | `active` flag preferred over hard delete for user and catalog entities |

---

## NFR-DATA-02: Referential Integrity

| Attribute | Requirement |
|-----------|-------------|
| Cross-service | No foreign keys across databases; reference ids only |
| Within music service | JPA relationships enforce album → artist, track → album |
| Invalid references | Return 404, not 500, for missing linked entities |

---

## NFR-OPS-01: Local Development Experience

| Attribute | Requirement |
|-----------|-------------|
| Infrastructure | Docker Compose starts PostgreSQL, Redis, MinIO, Kafka, Kafka UI |
| Service ports | Gateway 8081, Auth 8082, User 8083, Music 8084 |
| Startup | Documented order: infrastructure first, then services |
| Reproducibility | New developer can follow docs without undisclosed manual steps |

---

## NFR-OPS-02: Observability (MVP Baseline)

| Attribute | Requirement |
|-----------|-------------|
| Logging | Structured or clear SLF4J logs per service with correlation-friendly request ids where feasible |
| Metrics | Actuator metrics endpoint optional for MVP |
| Tracing | Post-MVP (Jaeger / Zipkin / OpenTelemetry) |

---

## NFR-OPS-03: Containerization Readiness

| Attribute | Requirement |
|-----------|-------------|
| Build | Each service buildable with Maven wrapper |
| Config | Twelve-factor style — config via environment |
| Images | Dockerfiles post-MVP; compose covers infra in MVP |

---

## NFR-MSG-01: Event-Driven Messaging

| Attribute | Requirement |
|-----------|-------------|
| Broker | Apache Kafka for domain events |
| Serialization | JSON payloads for human inspection in Kafka UI |
| Minimum event | `UserRegisteredEvent` published after profile creation |
| Consumers | None required for MVP beyond manual verification |

**Delivery semantics:** At-least-once acceptable for MVP learning scope; idempotent consumers recommended when added.

---

## NFR-MAINT-01: Code Consistency

| Attribute | Requirement |
|-----------|-------------|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Boilerplate | Lombok where aligned with existing service templates |
| Package layout | Controller → service → repository → entity/dto per service |
| Exceptions | Shared `GlobalExceptionHandler` pattern per service |

---

## NFR-MAINT-02: Documentation

| Attribute | Requirement |
|-----------|-------------|
| Requirements | Functional and non-functional docs maintained under `docs/requirements/` |
| Setup | README runbook for infra and services (expanded in later work) |
| API | OpenAPI/Swagger post-MVP; requirements docs define contracts until then |

---

## NFR Compliance Matrix (MVP)

| Category | ID | MVP required |
|----------|-----|--------------|
| Security | NFR-SEC-01 – 05 | Yes |
| API | NFR-API-01 – 03 | Yes |
| Scalability | NFR-SCALE-01 – 02 | 01 yes; 02 design-only |
| Performance | NFR-PERF-01 – 02 | Yes (guidelines) |
| Reliability | NFR-REL-01 – 02 | Yes |
| Data | NFR-DATA-01 – 02 | Yes |
| Operations | NFR-OPS-01 – 03 | 01 yes; 02 baseline; 03 partial |
| Messaging | NFR-MSG-01 | Yes |
| Maintainability | NFR-MAINT-01 – 02 | Yes |

---

## Related Documents

| Document | Link |
|----------|------|
| Functional — auth | [functional-auth.md](functional-auth.md) |
| Functional — users | [functional-users.md](functional-users.md) |
| Functional — music | [functional-music.md](functional-music.md) |
| Feature prioritization | [features.md](../features.md) |
| User stories | `user-stories.md` (upcoming) |
| MVP scope | `mvp-scope.md` (upcoming) |

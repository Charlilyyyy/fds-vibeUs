# MVP Scope Lock

## Purpose

This document **locks** the minimum viable product for VibeUs. All implementation, testing, and documentation work should trace to items marked **in scope** below. Anything marked **out of scope** requires an explicit scope-change decision before work begins.

**Status:** Locked upon completion of requirements documentation.  
**Vertical slice goal:** Register → login → profile → catalog CRUD → media upload → gateway-secured access → Kafka registration event.

---

## In Scope: Application Services

| Service | Port | Database | Responsibility |
|---------|------|----------|----------------|
| **Gateway** | 8081 | None | Route proxy, JWT validation, CORS, public/protected path rules |
| **Auth** | 8082 | `vibeus_auth_db` | Registration, login, JWT issuance, password hashing |
| **User** | 8083 | `vibeus_user_db` | Profile CRUD, internal profile creation, `UserRegisteredEvent` |
| **Music** | 8084 | `vibeus_music_db` | Artist/album/track/genre catalog, MinIO uploads |

**Service count:** 4 application services. No additional deployable services in MVP.

### Gateway routes (locked)

| Path prefix | Downstream | Notes |
|-------------|------------|-------|
| `/api/v1/auth/**` | Auth (8082) | Register and login public; other auth routes per functional spec |
| `/api/v1/users/**` | User (8083) | JWT required |
| `/api/v1/artists/**` | Music (8084) | JWT required |
| `/api/v1/albums/**` | Music (8084) | JWT required |
| `/api/v1/tracks/**` | Music (8084) | JWT required |
| `/api/v1/genres/**` | Music (8084) | JWT required |

**Not routed through public gateway:** `/internal/**` on user service (auth-only).

---

## In Scope: Infrastructure

| Component | Role in MVP | Required |
|-----------|-------------|----------|
| **PostgreSQL** | Per-service databases | Yes |
| **MinIO** | Artist images, album covers, track audio | Yes |
| **Apache Kafka** | `UserRegisteredEvent` producer | Yes |
| **Kafka UI** | Developer verification of events | Yes |
| **Redis** | Present in compose; caching **not** implemented in MVP | Infra only |
| **Docker Compose** | Local orchestration of all infra | Yes |

### Database lock

| Database | Owner service |
|----------|---------------|
| `vibeus_auth_db` | Auth |
| `vibeus_user_db` | User |
| `vibeus_music_db` | Music |

No shared schemas. No cross-database foreign keys.

---

## In Scope: Functional Capabilities

Summarized from locked requirement documents:

| Area | In scope | Reference |
|------|----------|-----------|
| Register & login | Yes | [functional-auth.md](functional-auth.md) |
| JWT at gateway | Yes | [functional-auth.md](functional-auth.md), [non-functional.md](non-functional.md) |
| Profile create (internal) + `/me` CRUD | Yes | [functional-users.md](functional-users.md) |
| UserRegisteredEvent → Kafka | Yes | [functional-users.md](functional-users.md) |
| Artist / album / track / genre CRUD | Yes | [functional-music.md](functional-music.md) |
| Paginated list endpoints | Yes | [functional-music.md](functional-music.md) |
| Image & audio upload to MinIO | Yes | [functional-music.md](functional-music.md) |
| ApiResponse / ErrorResponse / GlobalExceptionHandler | Yes | [non-functional.md](non-functional.md) |
| Actuator health per service | Yes | [non-functional.md](non-functional.md) |

### P0 user stories (must pass for MVP sign-off)

From [user-stories.md](user-stories.md):

- US-AUTH-01, US-AUTH-02, US-AUTH-03
- US-USER-01
- US-GW-02
- US-OPS-01

### P1 user stories (core MVP completeness)

- US-MUSIC-01 through US-MUSIC-04
- US-MEDIA-01 through US-MEDIA-03
- US-GW-01

---

## Out of Scope (Explicitly Excluded)

### Services not in MVP

| Service | Status |
|---------|--------|
| Playlist service | Out — future bounded context |
| Search service | Out — requires indexing pipeline |
| Streaming service | Out — dedicated delivery path |
| Notification service | Out — no Kafka consumers required in MVP |

### Features not in MVP

| Feature | Rationale |
|---------|-----------|
| Refresh tokens & logout token revocation | Stateless access-token MVP |
| Email verification & OAuth2 | Auth complexity deferred |
| Redis caching layer | Infra provisioned; no cache usage |
| Rate limiting | Post-MVP hardening |
| OpenAPI / Swagger aggregation | Requirements docs are contract until later |
| CI/CD pipelines & K8s | Local compose sufficient |
| Service Dockerfiles | JAR + compose infra for MVP |
| Play count increment on play | No streaming client |
| Public anonymous catalog browse | All catalog routes authenticated |
| Admin RBAC | Single `ROLE_USER` sufficient locally |
| Hard delete of users/catalog | Soft delete (`active` flag) only |

### Client applications

| Item | Status |
|------|--------|
| Web, mobile, or desktop player | Out |
| Postman collection | Optional helper; not a deliverable gate |

---

## Technology Stack Lock

| Layer | Choice |
|-------|--------|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| API gateway | Spring Cloud Gateway |
| Persistence | Spring Data JPA + PostgreSQL |
| Security | Spring Security + JWT |
| Object storage | MinIO (S3-compatible API) |
| Messaging | Apache Kafka |
| Build | Maven (wrapper per service) |
| Local infra | Docker Compose |

---

## Communication Patterns Lock

| Pattern | Use case |
|---------|----------|
| REST (sync) | Client → gateway → services; auth → user internal profile creation |
| Kafka (async) | User service publishes `UserRegisteredEvent` after profile creation |
| JWT propagation | Gateway validates; downstream services read claims |

**No** synchronous Kafka request-reply in MVP. **No** shared database transactions across services.

---

## Repository Layout (Planned)

Implementation will add (names locked for consistency):

```
gateway-service/
auth-service/
user-service/
music-service/
infrastructure/          # docker-compose, env templates
```

Package root: `com.vibeus.<service>` (not finalized in this document — confirmed in architecture documentation).

---

## MVP Success Criteria

MVP is **complete** when all of the following are true:

1. **Infrastructure** — `docker compose up` brings up PostgreSQL, Redis, MinIO, Kafka, and Kafka UI without manual intervention beyond documented env setup.
2. **Onboarding** — Register and login through gateway; `GET /api/v1/users/me` returns profile matching JWT subject.
3. **Security** — Protected routes return 401 without token; public auth routes work without token.
4. **Catalog** — At least one artist, album, and track created via API; list endpoints paginate correctly.
5. **Media** — At least one image and one audio file uploaded; URLs persisted on entities.
6. **Events** — `UserRegisteredEvent` visible in Kafka UI after registration.
7. **Quality** — Errors use consistent response shape; health endpoints return UP for all four services.

---

## Delivery Sequence (Recommended)

| Order | Deliverable |
|-------|-------------|
| 1 | Repository bootstrap + shared patterns |
| 2 | Docker Compose infrastructure |
| 3 | Auth service |
| 4 | User service + Kafka producer |
| 5 | Music service (catalog then MinIO) |
| 6 | API gateway + JWT filter |
| 7 | End-to-end smoke test through gateway |

This sequence aligns with upcoming architecture and implementation work; order may adjust but **scope items above do not change** without revision of this document.

---

## Scope Change Process

To add an out-of-scope item to MVP:

1. Document the change and rationale in a short note (ADR or requirements amendment).
2. Update [features.md](../features.md) and affected functional requirement files.
3. Re-assess P0/P1 user stories in [user-stories.md](user-stories.md).

---

## Requirements Index

| Document | Description |
|----------|-------------|
| [functional-auth.md](functional-auth.md) | Authentication & identity |
| [functional-users.md](functional-users.md) | User profiles |
| [functional-music.md](functional-music.md) | Catalog & media |
| [non-functional.md](non-functional.md) | Quality attributes |
| [user-stories.md](user-stories.md) | Stories & acceptance criteria |
| [features.md](../features.md) | Must-have vs nice-to-have (idea stage) |

---

## Sign-Off Checklist

- [x] Services and ports defined
- [x] Databases named and isolated
- [x] Infrastructure components listed
- [x] In-scope features traced to requirement docs
- [x] Out-of-scope items explicitly listed
- [x] P0/P1 stories identified
- [x] MVP success criteria documented
- [x] Tech stack and communication patterns locked

**MVP scope is locked.** Implementation may proceed.

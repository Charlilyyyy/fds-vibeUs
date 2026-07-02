# Conventions — Packages, Naming & Structure

## Overview

Consistent naming across services reduces cognitive load when switching between gateway, auth, user, and music codebases. This document locks **repository layout**, **Java packages**, **API paths**, **database objects**, and **configuration keys** for VibeUs.

---

## Repository Layout

```
fds-vibeUs/                          # Git repository root
├── docs/                            # Vision, requirements, architecture
├── infrastructure/                  # docker-compose, env templates
├── gateway-service/
├── auth-service/
├── user-service/
└── music-service/
```

| Rule | Convention | Example |
|------|------------|---------|
| Service folder | kebab-case + `-service` suffix | `auth-service` |
| Infrastructure folder | lowercase single word | `infrastructure` |
| Documentation | lowercase paths under `docs/` | `docs/architecture/overview.md` |
| No shared parent POM (MVP) | Each service is independent Maven module | — |

---

## Maven Coordinates

| Field | Pattern | Examples |
|-------|---------|----------|
| `groupId` | `com.vibeus` | all services |
| `artifactId` | `<name>-service` | `auth-service`, `music-service` |
| `version` | `0.0.1-SNAPSHOT` | until tagged release |
| Main class | `com.vibeus.<module>.<Module>ServiceApplication` | `AuthServiceApplication` |

```xml
<groupId>com.vibeus</groupId>
<artifactId>auth-service</artifactId>
<version>0.0.1-SNAPSHOT</version>
```

---

## Java Base Packages

| Service | Base package | Application class |
|---------|--------------|-------------------|
| gateway-service | `com.vibeus.gateway` | `GatewayServiceApplication` |
| auth-service | `com.vibeus.auth` | `AuthServiceApplication` |
| user-service | `com.vibeus.user` | `UserServiceApplication` |
| music-service | `com.vibeus.music` | `MusicServiceApplication` |

**Rule:** One base package per service. No cross-service Java module dependencies in MVP (duplicate small DTOs/events if needed rather than shared JAR).

---

## Package Structure (Per Service)

Standard layered layout for auth, user, and music:

```
com.vibeus.<module>/
├── <Module>ServiceApplication.java
├── config/                 # @Configuration, @ConfigurationProperties
├── controller/             # REST controllers (@RestController)
├── service/                # Interfaces
│   └── impl/               # @Service implementations
├── repository/             # Spring Data JPA interfaces
├── entity/                 # @Entity classes
├── dto/
│   ├── request/            # Incoming payloads
│   └── response/           # Outgoing payloads
├── exception/              # Custom exceptions + GlobalExceptionHandler
├── security/               # SecurityConfig, filters, UserContext
├── client/                 # OpenFeign clients (auth → user)
├── events/                 # Kafka event records
└── kafka/                  # Producers (and consumers later)
```

**gateway-service** uses a reduced set:

```
com.vibeus.gateway/
├── GatewayServiceApplication.java
├── config/                 # RouteConfig, CorsConfig, JwtProperties
├── filter/                 # JwtAuthenticationFilter (GatewayFilter)
└── exception/              # Optional gateway error handling
```

**music-service** additions:

```
com.vibeus.music/
├── enums/                  # AlbumType, etc.
└── (FileStorageService in service/)
```

---

## Class Naming

| Layer | Pattern | Example |
|-------|---------|---------|
| Controller | `<Resource>Controller` | `AuthController`, `ArtistController` |
| Internal controller | `Internal<Resource>Controller` | `InternalUserController` |
| Service interface | `<Resource>Service` | `UserService`, `JwtService` |
| Service impl | `<Resource>ServiceImpl` | `AuthServiceImpl` |
| Repository | `<Entity>Repository` | `UserRepository`, `TrackRepository` |
| Entity | Singular noun | `User`, `Artist`, `Track` |
| Request DTO | `<Action><Resource>Request` | `RegisterRequest`, `CreateAlbumRequest` |
| Response DTO | `<Resource>Response` | `UserResponse`, `AuthenticationResponse` |
| Wrapper | `ApiResponse`, `ErrorResponse` | shared shape per service |
| Exception | `<Reason>Exception` | `DuplicateResourceException` |
| Feign client | `<Target>ServiceClient` | `UserServiceClient` |
| Event record | `<Domain><Action>Event` | `UserRegisteredEvent` |
| Kafka producer | `<Domain>EventProducer` | `UserEventProducer` |
| Config properties | `<Area>Properties` | `JwtProperties`, `MinioProperties` |

Use **PascalCase** for types, **camelCase** for fields and methods.

---

## API Path Conventions

### Public REST (via gateway)

| Rule | Value |
|------|-------|
| Version prefix | `/api/v1` |
| Resource plural nouns | `/users`, `/artists`, `/albums`, `/tracks`, `/genres` |
| Auth namespace | `/api/v1/auth` |
| Current user shortcut | `/api/v1/users/me` |
| Path variables | camelCase in code; URLs lowercase | `{userId}`, `{artistId}`, `{albumId}`, `{trackId}` |

### Examples

```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
GET    /api/v1/users/me
GET    /api/v1/artists/{artistId}
POST   /api/v1/tracks/{trackId}/track      # audio upload
POST   /api/v1/artists/{artistId}/image    # image upload
```

### Internal REST (not on gateway)

| Rule | Value |
|------|-------|
| Prefix | `/internal` |
| Example | `POST /internal/users` |

Controller `@RequestMapping` should align with these paths; gateway strips nothing beyond proxying.

---

## HTTP Method Semantics

| Method | Usage |
|--------|-------|
| `GET` | Read single or list |
| `POST` | Create resource or action (login, upload) |
| `PUT` | Full/partial update of resource |
| `DELETE` | Soft delete / deactivate in MVP |

Avoid `PATCH` in MVP unless a service already standardizes on it.

---

## JSON & DTO Field Naming

| Context | Convention |
|---------|--------------|
| JSON properties | **camelCase** | `firstName`, `durationInSeconds`, `coverImageUrl` |
| Java record/DTO fields | camelCase | matches JSON |
| Enum values | **SCREAMING_SNAKE** in JSON | `SINGLE`, `EP`, `ALBUM` |

---

## Database Naming

### Database names

```
vibeus_auth_db
vibeus_user_db
vibeus_music_db
```

Pattern: `vibeus_<context>_db`

### Table & column names (JPA defaults)

| Element | Convention | Example |
|---------|------------|---------|
| Table | snake_case plural | `users`, `artists`, `track_genres` |
| Column | snake_case | `password_hash`, `release_date`, `audio_url` |
| Join table | `<owner>_<related>` plural | `track_artists`, `track_genres` |
| Primary key | `id` (UUID) | — |
| Soft delete flag | `active` boolean | default `true` |
| Audit columns | `created_at`, `updated_at` | via `BaseEntity` |

Use `@Table(name = "...")` and `@Column(name = "...")` when Java field names differ (e.g. `durationInSeconds` → `duration_seconds`).

---

## Kafka Naming

| Element | Convention | Example |
|---------|------------|---------|
| Topic | `<domain>.<action>` lowercase | `user.registered` |
| Event type | PascalCase + `Event` suffix | `UserRegisteredEvent` |
| Message key | String form of entity id | `userId.toString()` |

---

## MinIO Naming

| Element | Convention | Example |
|---------|------------|---------|
| Bucket config keys | lowercase plural | `artists`, `albums`, `tracks` |
| Object key | UUID + extension | `a1b2c3d4-....jpg` |
| Config prefix | `minio.` in YAML | `minio.endpoint`, `minio.buckets.artists` |

---

## Configuration Keys

### Spring application names

```yaml
spring:
  application:
    name: auth-service   # matches artifact folder
```

| Service | `spring.application.name` |
|---------|---------------------------|
| gateway | `gateway-service` |
| auth | `auth-service` |
| user | `user-service` |
| music | `music-service` |

### Environment variables (SCREAMING_SNAKE)

| Variable | Used by |
|----------|---------|
| `JWT_SECRET` | auth, gateway |
| `JWT_EXPIRATION_MS` | auth |
| `USER_SERVICE_URL` | auth |
| `MINIO_ENDPOINT` | music |
| `MINIO_ACCESS_KEY` | music |
| `MINIO_SECRET_KEY` | music |
| `KAFKA_BOOTSTRAP_SERVERS` | user |

YAML uses **kebab-case** for custom properties (`minio.public-url`); env vars use **SCREAMING_SNAKE**.

---

## Security & JWT Claims

| Claim | Purpose |
|-------|---------|
| `sub` | User UUID |
| `email` | Optional |
| `roles` or `authorities` | e.g. `ROLE_USER` |
| `iat`, `exp` | Issued at, expiration |

Header: `Authorization: Bearer <token>`

---

## Exception & Error Codes

| HTTP status | When |
|-------------|------|
| 400 | Validation failure |
| 401 | Missing/invalid auth |
| 403 | Authenticated but not allowed |
| 404 | Resource not found |
| 409 | Duplicate resource |
| 500 | Unexpected server error |

`GlobalExceptionHandler` in each service maps exceptions to `ErrorResponse` with consistent fields: `timestamp`, `status`, `message`, optional `errors` map.

---

## Logging

| Rule | Example |
|------|---------|
| Logger per class | `private static final Logger log = LoggerFactory.getLogger(Foo.class);` or Lombok `@Slf4j` |
| Never log | passwords, full JWT, MinIO secret keys |
| Registration/login | log user id or email hash at INFO sparingly |

---

## Git & Documentation Conventions

| Item | Convention |
|------|------------|
| Commit messages | Imperative mood, concise prefix | `docs: ...`, `feat(auth): ...`, `fix(gateway): ...` |
| Branch names | kebab-case | `feat/auth-service`, `docs/architecture` |
| PR titles | Same spirit as commits | — |

Do not use legacy project names from the reference implementation in paths, packages, or commits.

---

## Test Package Mirror

```
src/test/java/com/vibeus/<module>/
└── <Module>ServiceApplicationTests.java   # context load smoke test
```

Integration tests (post-MVP): `...integration/` or `*IT.java` suffix with Testcontainers.

---

## Per-Service Controller Mapping Summary

| Service | `@RequestMapping` base |
|---------|------------------------|
| auth | `/api/v1/auth` |
| user (public) | `/api/v1/users` |
| user (internal) | `/internal/users` |
| music — artists | `/api/v1/artists` |
| music — albums | `/api/v1/albums` |
| music — tracks | `/api/v1/tracks` |
| music — genres | `/api/v1/genres` |
| gateway | no MVC controllers — route config only |

---

## Duplication vs Shared Library (MVP Decision)

| Artifact | Approach |
|----------|----------|
| `ApiResponse`, `ErrorResponse`, `BaseEntity` | **Duplicate** per service in MVP |
| `UserRegisteredEvent` | Defined in user-service; auth does not need the class |
| JWT parsing utilities | Duplicate minimal code in gateway + services OR small internal copy |

Introduce a shared `vibeus-common` module only when duplication becomes painful — not in initial bootstrap.

---

## Architecture Document Index

| Document | Description |
|----------|-------------|
| [overview.md](overview.md) | System architecture |
| [tech-stack.md](tech-stack.md) | Technologies and versions |
| [service-boundaries.md](service-boundaries.md) | Data ownership |
| [communication-patterns.md](communication-patterns.md) | REST and Kafka |
| [deployment-topology.md](deployment-topology.md) | Ports and compose |
| **conventions.md** | This document |

---

## Convention Checklist (Bootstrap)

- [ ] Each service uses `com.vibeus.<module>` base package
- [ ] Folder names match `*-service` pattern
- [ ] Public APIs start with `/api/v1`
- [ ] Internal user API under `/internal/users`
- [ ] Databases named `vibeus_*_db`
- [ ] Kafka topic `user.registered`
- [ ] JWT claim `sub` = user UUID
- [ ] No shared Maven module until explicitly scoped

**Architecture documentation set is complete.** Implementation may begin with repository bootstrap and infrastructure compose.

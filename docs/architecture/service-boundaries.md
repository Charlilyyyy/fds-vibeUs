# Service Boundaries & Data Ownership

## Overview

Each VibeUs microservice is a **bounded context** with exclusive ownership of its persistent data. Services reference each other by **identifier only** (UUID) — never by shared tables or cross-database foreign keys.

This document defines what each service owns, what it must not own, and how data is partitioned across PostgreSQL, MinIO, and Kafka.

---

## Boundary Principles

| Principle | Rule |
|-----------|------|
| **Single writer** | Only one service may insert/update/delete rows in its database |
| **No shared DB** | One PostgreSQL database per domain service |
| **Reference by ID** | Cross-context links use UUIDs carried in APIs or events |
| **No distributed transactions** | Saga-style compensation or eventual consistency across services |
| **Gateway is stateless** | No domain data; routing and security only |

---

## Service Map

| Service | Bounded context | Persistent store | Port |
|---------|-----------------|------------------|------|
| **gateway-service** | Edge routing & JWT enforcement | None | 8081 |
| **auth-service** | Credentials & token issuance | `vibeus_auth_db` | 8082 |
| **user-service** | User profiles & identity presentation | `vibeus_user_db` | 8083 |
| **music-service** | Catalog metadata & media assets | `vibeus_music_db` + MinIO buckets | 8084 |

---

## gateway-service

### Owns

- Route definitions (path → downstream URI)
- JWT validation filter configuration
- CORS policy
- Public vs protected path rules

### Does not own

- User credentials, profiles, or catalog entities
- Any database connection
- Kafka producers or consumers
- MinIO buckets

### Data stores

**None.** Configuration only (YAML + environment variables).

---

## auth-service

### Owns

- User **authentication** lifecycle: register, login
- Password hashes and credential records
- JWT signing keys/secrets (configured, not stored in DB)
- Optional future: refresh token store

### Does not own

- Display name, bio, profile image URL
- Artist/album/track catalog
- Binary media files

### Database: `vibeus_auth_db`

| Table / entity (conceptual) | Key fields | Notes |
|-----------------------------|------------|-------|
| `users` (credentials) | `id` (UUID), `email`, `password_hash`, `enabled` | `id` is source of truth for user identity |

**Email uniqueness** is enforced in auth DB. User service stores a copy of email on the profile for display and lookup — synchronized at registration, not updated via profile API in MVP.

### Outbound dependencies

| Target | Purpose | Pattern |
|--------|---------|---------|
| user-service | Create profile after registration | Sync HTTP (`POST /internal/users`) |

### Inbound callers

- gateway-service (public auth routes)
- Clients via gateway only

---

## user-service

### Owns

- User **profile** data tied to auth user id
- Profile CRUD for authenticated users
- `UserRegisteredEvent` publication

### Does not own

- Passwords or token signing
- Music catalog or playlists
- Uploaded audio/image blobs (profile image URL may point externally; MVP stores URL string only)

### Database: `vibeus_user_db`

| Table / entity | Key fields | Notes |
|----------------|------------|-------|
| `users` (profiles) | `id` (UUID, same as auth), `username`, `email`, `first_name`, `last_name`, `bio`, `profile_image_url`, `active` | Primary key matches auth user id |

**Username uniqueness** enforced in user DB.

### Kafka ownership

| Topic (example name) | Producer | Payload |
|----------------------|----------|---------|
| `user.registered` | user-service | `UserRegisteredEvent` |

Topic naming finalized in [communication-patterns.md](communication-patterns.md). User service is the **sole producer** of profile-created events in MVP.

### API surfaces

| Surface | Path prefix | Exposure |
|---------|-------------|----------|
| Public (via gateway) | `/api/v1/users/**` | JWT required |
| Internal | `/internal/users` | Auth service only; not on public gateway |

### Outbound dependencies

- Kafka broker (produce only in MVP)

### Inbound callers

- gateway-service (profile APIs)
- auth-service (internal create)

---

## music-service

### Owns

- **Catalog domain:** artists, albums, tracks, genres
- JPA join tables: `track_artists`, `track_genres`
- MinIO object keys and bucket organization
- URLs pointing to stored media (`imageUrl`, `coverImageUrl`, `audioUrl`)

### Does not own

- User accounts or authentication
- Playlists, listening history, search indexes (future services)
- Global CDN configuration

### Database: `vibeus_music_db`

| Table | Key fields | Relationships |
|-------|------------|---------------|
| `artists` | `id`, `name`, `bio`, `image_url`, `verified`, `active` | — |
| `albums` | `id`, `title`, `type`, `release_date`, `cover_image_url`, `artist_id`, `active` | FK → `artists` (intra-DB) |
| `tracks` | `id`, `title`, `duration_seconds`, `audio_url`, `cover_image_url`, `play_count`, `album_id`, `active` | FK → `albums` (intra-DB) |
| `genres` | `id`, `name`, `description`, `active` | — |
| `track_artists` | `track_id`, `artist_id` | Many-to-many |
| `track_genres` | `track_id`, `genre_id` | Many-to-many |

All foreign keys exist **only within** `vibeus_music_db`. No `user_id` on catalog tables in MVP (any authenticated user may manage catalog locally).

### MinIO ownership

| Bucket (example) | Content | Written by |
|------------------|---------|------------|
| `vibeus-artists` | Artist images | music-service |
| `vibeus-albums` | Album covers | music-service |
| `vibeus-tracks` | Audio files, track covers | music-service |

Bucket names are configuration; music-service is the **only** application writer.

### Outbound dependencies

- MinIO (read/write objects)
- PostgreSQL (`vibeus_music_db`)

### Inbound callers

- gateway-service (all `/api/v1/artists|albums|tracks|genres/**` routes)

---

## Cross-Service Identity

```
┌─────────────────────────────────────────────────────────────┐
│                     User identity (UUID)                     │
│              Created at registration in auth                 │
└───────────────────────────┬─────────────────────────────────┘
                            │
            ┌───────────────┼───────────────┐
            ▼               ▼               ▼
     vibeus_auth_db   vibeus_user_db   (future: playlists DB)
     credentials      profile          user_id reference only
```

- **Auth** generates or assigns `userId` (UUID) at registration.
- **User** profile row uses the **same** UUID as primary key.
- **Music** does not reference `userId` in MVP catalog tables.
- **JWT `sub` claim** carries `userId` for authorization in user and music services.

---

## What Lives Outside Services (Shared Infrastructure)

| Infrastructure | Ownership model |
|----------------|-----------------|
| PostgreSQL server | Shared process; **logical** DB isolation per service |
| Kafka cluster | Shared broker; topics named by domain convention |
| MinIO | Shared server; buckets scoped to music domain |
| Redis | Shared instance; no MVP application owner |

Infrastructure is **not** a microservice and holds no business rules.

---

## Coupling Matrix

| From → To | auth | user | music | gateway |
|-----------|------|------|-------|---------|
| **auth** | — | HTTP create profile | — | — |
| **user** | — | — | — | — |
| **music** | — | — | — | — |
| **gateway** | route | route | route | — |
| **client** | via GW | via GW | via GW | entry |

**Design goal:** Music and user services do not call auth synchronously on every request; gateway JWT validation + local claim parsing suffice.

---

## Registration: Boundary Crossing

The one mandatory **write** path across services in MVP:

```
1. auth-service    WRITES  vibeus_auth_db.credentials
2. auth-service    CALLS   user-service POST /internal/users
3. user-service    WRITES  vibeus_user_db.profiles
4. user-service    PRODUCES Kafka user.registered
```

If step 3 fails, auth must compensate (delete credential or mark incomplete) — behavior defined in [functional-auth.md](../requirements/functional-auth.md).

---

## Future Bounded Contexts (Out of MVP)

| Future service | Would own | References |
|----------------|-----------|------------|
| **playlist-service** | Playlists, playlist_tracks | `user_id`, `track_id` by UUID |
| **search-service** | Search index documents | Catalog ids from events or sync |
| **streaming-service** | Playback sessions, signed URLs | `track_id`, storage URLs |

Each would receive its own database and gateway route prefix without migrating data out of music or user services.

---

## Anti-Patterns to Avoid

| Anti-pattern | Why forbidden |
|--------------|---------------|
| Auth service querying `vibeus_user_db` | Breaks encapsulation |
| Shared `users` table across auth and user | Couples deployments |
| Music service storing files in PostgreSQL BYTEA | Wrong store for blobs |
| Gateway writing audit rows to PostgreSQL | Gateway must stay stateless |
| Kafka event as sole source of profile truth | Profile DB remains authoritative |

---

## Boundary Checklist (Implementation)

- [ ] Each service connects to exactly one PostgreSQL database name
- [ ] No second datasource in a single service for MVP
- [ ] Internal user API not registered in gateway routes
- [ ] MinIO client exists only in music-service
- [ ] Kafka producer for `UserRegisteredEvent` exists only in user-service
- [ ] JWT secret shared only between auth (sign) and gateway (verify)

---

## Related Documents

| Document | Link |
|----------|------|
| Architecture overview | [overview.md](overview.md) |
| Technology stack | [tech-stack.md](tech-stack.md) |
| Communication patterns | `communication-patterns.md` (upcoming) |
| Functional — auth | [functional-auth.md](../requirements/functional-auth.md) |
| Functional — users | [functional-users.md](../requirements/functional-users.md) |
| Functional — music | [functional-music.md](../requirements/functional-music.md) |
| MVP scope | [mvp-scope.md](../requirements/mvp-scope.md) |

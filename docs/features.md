# Feature Prioritization

## Purpose

This document separates **must-have** capabilities (MVP) from **nice-to-have** enhancements (post-MVP). The split keeps scope achievable for local development while preserving a clear roadmap for future work.

## MVP Principle

The MVP delivers a **vertical slice** of a music streaming backend: a user can register, authenticate, manage a profile, browse and manage catalog metadata, upload media assets, and access everything through a secured gateway — with at least one cross-service event flowing through Kafka.

---

## Must-Have Features

### Identity & Access

| Feature | Description | Owner |
|---------|-------------|-------|
| User registration | Create account with validated credentials | Auth service |
| User login | Authenticate and receive JWT access token | Auth service |
| Token validation | Gateway and services verify token claims | Gateway + Auth |
| Password security | Encoded passwords, stateless sessions | Auth service |
| Role / claims in token | Distinguish standard user from future admin roles | Auth service |

### User Profiles

| Feature | Description | Owner |
|---------|-------------|-------|
| Profile creation | Internal profile provisioned after registration | User service |
| Get profile | Authenticated user reads own profile | User service |
| Update profile | Authenticated user updates display name, bio, etc. | User service |
| Registration event | Publish event when profile is created | User service → Kafka |

### Music Catalog

| Feature | Description | Owner |
|---------|-------------|-------|
| Artists CRUD | Create, read, update, delete, list artists | Music service |
| Albums CRUD | Manage albums linked to artists | Music service |
| Tracks CRUD | Manage tracks linked to albums | Music service |
| Genres | Classify catalog entries by genre | Music service |
| Pagination & validation | Consistent list endpoints and input checks | Music service |

### Media Storage

| Feature | Description | Owner |
|---------|-------------|-------|
| Object storage integration | S3-compatible bucket for binary files | Music service + MinIO |
| Image upload | Artist images and album cover art | Music service |
| Track asset upload | Audio file upload with metadata linkage | Music service |
| File validation | Type and size limits, clear error responses | Music service |

### API Gateway

| Feature | Description | Owner |
|---------|-------------|-------|
| Route forwarding | Single entry point to Auth, User, Music | Gateway service |
| Public vs protected routes | `/auth/register`, `/auth/login` open; rest secured | Gateway service |
| JWT filter | Reject invalid or missing tokens at the edge | Gateway service |
| CORS | Allow configured origins for future clients | Gateway service |

### Infrastructure (Local)

| Feature | Description |
|---------|-------------|
| PostgreSQL | Separate database per service (`vibeus_auth_db`, `vibeus_user_db`, `vibeus_music_db`) |
| Docker Compose | One-command startup for databases, Redis, MinIO, Kafka |
| Service ports | Gateway 8081, Auth 8082, User 8083, Music 8084 |
| Kafka UI | Inspect topics and messages during development |

### Cross-Cutting

| Feature | Description |
|---------|-------------|
| Consistent API responses | Shared `ApiResponse`, error payload shape |
| Global exception handling | Validation, not-found, duplicate, unauthorized errors |
| Health checks | Spring Actuator endpoints per service |
| Base documentation | README, setup steps, API overview |

---

## Nice-to-Have Features (Post-MVP)

### User Experience & Catalog

| Feature | Rationale for deferral |
|---------|------------------------|
| Playlists (create, edit, share) | Requires new service, ownership model, and sync logic |
| Full-text search | Needs search index (Elasticsearch or similar) and indexing pipeline |
| Recommendations | ML / collaborative filtering out of scope for learning MVP |
| Listening history & analytics | Event volume and storage patterns add complexity |
| Social features (follow artists, share) | Depends on stable user graph and notification flow |

### Playback & Delivery

| Feature | Rationale for deferral |
|---------|------------------------|
| Dedicated streaming service | Separate concern from catalog CRUD; needs range requests, CDN patterns |
| Adaptive bitrate / transcoding | Media pipeline beyond object storage upload |
| Offline download rights | Client and DRM concerns |

### Platform & Operations

| Feature | Rationale for deferral |
|---------|------------------------|
| Redis caching for hot reads | Valuable optimization after baseline APIs work |
| Rate limiting at gateway | Important for production; optional for local MVP |
| API versioning (`/v1`, `/v2`) | Can introduce once contracts stabilize |
| Centralized config (Spring Cloud Config) | Env files sufficient for local dev |
| Distributed tracing (Jaeger, Zipkin) | Add when debugging multi-hop flows becomes painful |
| Kubernetes deployment | Docker Compose is enough for learning environment |
| CI/CD pipelines | Document manual build first; automate later |

### Security Hardening

| Feature | Rationale for deferral |
|---------|------------------------|
| Refresh tokens | Access-token MVP proves gateway flow; refresh adds rotation logic |
| OAuth2 / social login | Third-party identity is orthogonal to core JWT lesson |
| Admin moderation APIs | Needs admin role workflows and audit logging |
| Secrets manager integration | `.env` / compose secrets adequate locally |

### Planned Service Scaffolds (Future)

These services may appear as empty modules or README stubs before full implementation:

- **Playlist service** — user-owned collections of track references
- **Search service** — query artists, albums, tracks across catalog
- **Streaming service** — signed URLs or streaming proxy for audio delivery

---

## Feature Decision Matrix

| Capability | MVP | Post-MVP | Notes |
|------------|-----|----------|-------|
| Register / login | ✓ | | Foundation for all protected APIs |
| JWT at gateway | ✓ | | Core security pattern |
| User profile CRUD | ✓ | | Distinct from auth credentials |
| Artist / album / track CRUD | ✓ | | Core catalog |
| MinIO uploads | ✓ | | Binary assets |
| Kafka user event | ✓ | | Proves async integration |
| Playlists | | ✓ | New bounded context |
| Search | | ✓ | Indexing pipeline |
| Streaming playback | | ✓ | Separate delivery path |
| Redis cache | | ✓ | Performance layer |
| Refresh tokens | | ✓ | Token lifecycle hardening |

---

## Scope Guardrails

When evaluating new work during MVP, ask:

1. **Does it unblock register → login → profile → catalog → upload → gateway?** If no, defer it.
2. **Does it require a new service or database?** If yes, strongly consider post-MVP unless it is gateway, auth, user, or music.
3. **Can it be mocked or stubbed for learning?** If yes, document the stub and move on.

---

## Relationship to Other Documents

[Problem statement](problem-statement.md) explains why scope discipline matters. [Vision](vision.md) and [value proposition](value-proposition.md) describe the long-term picture. [Target users](target-users.md) inform which personas MVP serves first. [Learning goals](learning-goals.md) map MVP features to skills builders will practice.

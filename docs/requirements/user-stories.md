# User Stories & Acceptance Criteria

## Overview

User stories describe MVP behavior from the perspective of **builders** (developers exercising the API) and **hypothetical product users** (listeners and contributors). Each story links to functional requirements and includes testable acceptance criteria.

**Format:** `As a <role>, I want <goal>, so that <benefit>.`

---

## Epic 1: Account & Authentication

### US-AUTH-01: Register a new account

**As a** new user,  
**I want** to register with email and password through the gateway,  
**so that** I can access the VibeUs platform.

**Acceptance criteria:**

- [ ] `POST /api/v1/auth/register` via gateway (port 8081) accepts valid email, password, and username
- [ ] Response is HTTP 201 with user identifier in body
- [ ] Duplicate email returns HTTP 409 with clear error message
- [ ] Invalid email format or short password returns HTTP 400 with validation details
- [ ] Password is not returned in any response
- [ ] Profile record is created in user service (verifiable via subsequent login + `/users/me`)

**Maps to:** FR-AUTH-01, FR-AUTH-08

---

### US-AUTH-02: Log in and receive a token

**As a** registered user,  
**I want** to log in with my credentials,  
**so that** I receive a JWT to call protected APIs.

**Acceptance criteria:**

- [ ] `POST /api/v1/auth/login` returns HTTP 200 with access token and expiry metadata
- [ ] Wrong password returns HTTP 401 with generic error message
- [ ] Unknown email returns HTTP 401 (same message as wrong password)
- [ ] Token is a valid JWT with `sub` claim matching user id

**Maps to:** FR-AUTH-02, FR-AUTH-03

---

### US-AUTH-03: Access protected routes with a token

**As an** authenticated user,  
**I want** the gateway to accept my bearer token,  
**so that** my requests reach downstream services.

**Acceptance criteria:**

- [ ] Request with valid `Authorization: Bearer <token>` to `/api/v1/users/me` returns HTTP 200
- [ ] Request without token to protected route returns HTTP 401 from gateway
- [ ] Request with expired or tampered token returns HTTP 401
- [ ] `POST /api/v1/auth/register` and `POST /api/v1/auth/login` work without a token

**Maps to:** FR-AUTH-04, NFR-SEC-01

---

## Epic 2: User Profile

### US-USER-01: View my profile

**As an** authenticated user,  
**I want** to view my profile via `/users/me`,  
**so that** I can confirm my account details after registration.

**Acceptance criteria:**

- [ ] `GET /api/v1/users/me` returns HTTP 200 with username, email, and profile fields
- [ ] Response uses `ApiResponse` wrapper with success message
- [ ] Unauthenticated request returns HTTP 401

**Maps to:** FR-USER-03

---

### US-USER-02: Update my profile

**As an** authenticated user,  
**I want** to update my display name, bio, and related fields,  
**so that** my profile reflects my preferences.

**Acceptance criteria:**

- [ ] `PUT /api/v1/users/me` with valid payload returns HTTP 200 with updated profile
- [ ] Changing username to an existing username returns HTTP 409
- [ ] Invalid field lengths return HTTP 400
- [ ] Email cannot be changed through this endpoint

**Maps to:** FR-USER-04

---

### US-USER-03: View another user's profile

**As an** authenticated user,  
**I want** to fetch a profile by user id,  
**so that** I can see public profile information for other accounts.

**Acceptance criteria:**

- [ ] `GET /api/v1/users/{userId}` returns HTTP 200 for existing user
- [ ] Unknown user id returns HTTP 404
- [ ] Valid JWT required

**Maps to:** FR-USER-02

---

### US-USER-04: Deactivate my account

**As an** authenticated user,  
**I want** to deactivate my profile,  
**so that** my account is marked inactive.

**Acceptance criteria:**

- [ ] `DELETE /api/v1/users/me` returns HTTP 204 (or documented 200 pattern)
- [ ] Subsequent `GET /api/v1/users/me` reflects inactive state or returns not found per implementation
- [ ] User cannot deactivate another user's profile via `/users/{userId}`

**Maps to:** FR-USER-05

---

### US-USER-05: Observe registration event in Kafka

**As a** developer,  
**I want** a `UserRegisteredEvent` published when a profile is created,  
**so that** I can verify async integration.

**Acceptance criteria:**

- [ ] After successful registration, a message appears on the configured Kafka topic
- [ ] Event payload includes `userId`, `username`, `email`, and `occurredAt`
- [ ] Event is visible in Kafka UI

**Maps to:** FR-USER-06, NFR-MSG-01

---

## Epic 3: Music Catalog

### US-MUSIC-01: Manage artists

**As a** catalog contributor,  
**I want** to create and manage artists,  
**so that** albums and tracks can be organized by performer.

**Acceptance criteria:**

- [ ] `POST /api/v1/artists` creates artist and returns HTTP 201
- [ ] Duplicate artist name returns HTTP 409
- [ ] `GET /api/v1/artists/{artistId}` returns artist details
- [ ] `GET /api/v1/artists` returns paginated list
- [ ] `PUT` and `DELETE` (soft delete) behave per functional music requirements
- [ ] All routes require valid JWT

**Maps to:** FR-MUSIC-01

---

### US-MUSIC-02: Manage albums for an artist

**As a** catalog contributor,  
**I want** to create albums linked to an artist,  
**so that** tracks can be grouped by release.

**Acceptance criteria:**

- [ ] `POST /api/v1/albums` with valid `artistId` returns HTTP 201
- [ ] Invalid `artistId` returns HTTP 404
- [ ] Album `type` accepts `SINGLE`, `EP`, or `ALBUM`
- [ ] `GET /api/v1/albums/artist/{artistId}` lists albums for that artist

**Maps to:** FR-MUSIC-02

---

### US-MUSIC-03: Manage tracks

**As a** catalog contributor,  
**I want** to create and list tracks with duration and associations,  
**so that** the catalog is playable-ready at the metadata layer.

**Acceptance criteria:**

- [ ] `POST /api/v1/tracks` creates track with title and `durationInSeconds`
- [ ] Tracks can be listed by album, artist, and genre
- [ ] `GET /api/v1/tracks` supports pagination (`page`, `size`)
- [ ] Default page size is 20; requests above max cap are rejected or capped

**Maps to:** FR-MUSIC-03, FR-MUSIC-05

---

### US-MUSIC-04: Manage genres

**As a** catalog contributor,  
**I want** to define genres and assign them to tracks,  
**so that** music can be classified consistently.

**Acceptance criteria:**

- [ ] `POST /api/v1/genres` creates genre with unique name
- [ ] Duplicate genre name returns HTTP 409
- [ ] `GET /api/v1/tracks/genre/{genreId}` returns related tracks

**Maps to:** FR-MUSIC-04

---

## Epic 4: Media Upload

### US-MEDIA-01: Upload artist image

**As a** catalog contributor,  
**I want** to upload an image for an artist,  
**so that** the artist profile displays artwork.

**Acceptance criteria:**

- [ ] `POST /api/v1/artists/{artistId}/image` with JPEG/PNG/WebP returns HTTP 200
- [ ] Artist `imageUrl` is updated and returned in subsequent GET
- [ ] Unsupported content type returns HTTP 400
- [ ] File exceeding size limit returns HTTP 400

**Maps to:** FR-MUSIC-07

---

### US-MEDIA-02: Upload album cover

**As a** catalog contributor,  
**I want** to upload album cover art,  
**so that** releases are visually identifiable.

**Acceptance criteria:**

- [ ] `POST /api/v1/albums/{albumId}/cover` stores file in album bucket
- [ ] Album `coverImageUrl` populated after upload
- [ ] Missing album returns HTTP 404

**Maps to:** FR-MUSIC-07

---

### US-MEDIA-03: Upload track audio

**As a** catalog contributor,  
**I want** to upload an audio file for a track,  
**so that** the track has a playable asset reference.

**Acceptance criteria:**

- [ ] `POST /api/v1/tracks/{trackId}/track` accepts MP3/WAV MIME types
- [ ] Track `audioUrl` is set after successful upload
- [ ] Storage failure returns HTTP 500 with safe error body (no stack trace)

**Maps to:** FR-MUSIC-08, FR-MUSIC-06

---

## Epic 5: Gateway & Cross-Service Flow

### US-GW-01: Single entry point for all services

**As a** API client developer,  
**I want** one gateway base URL,  
**so that** I do not hard-code individual service ports in client apps.

**Acceptance criteria:**

- [ ] Auth routes reachable at `http://localhost:8081/api/v1/auth/**`
- [ ] User routes reachable at `http://localhost:8081/api/v1/users/**`
- [ ] Music routes reachable at `http://localhost:8081/api/v1/artists|albums|tracks|genres/**`
- [ ] Direct calls to services on 8082–8084 still work for debugging but gateway is documented primary path

**Maps to:** FR-MUSIC-10, NFR-OPS-01

---

### US-GW-02: End-to-end registration flow

**As a** developer,  
**I want** registration to succeed across auth and user services,  
**so that** I can demo a complete onboarding path.

**Acceptance criteria:**

- [ ] Register → login → `GET /users/me` succeeds without manual DB seeding
- [ ] User id in profile matches JWT `sub` claim
- [ ] Kafka event emitted once per registration

**Maps to:** FR-AUTH-08, US-USER-05

---

## Epic 6: Local Infrastructure

### US-OPS-01: Start infrastructure with Docker Compose

**As a** developer,  
**I want** to start databases, cache, object storage, and Kafka with one command,  
**so that** I can run services locally without manual installs.

**Acceptance criteria:**

- [ ] `docker compose up` (or documented equivalent) starts PostgreSQL, Redis, MinIO, Kafka, and Kafka UI
- [ ] Databases `vibeus_auth_db`, `vibeus_user_db`, `vibeus_music_db` are creatable/connectable
- [ ] MinIO console/API reachable on documented ports
- [ ] Kafka UI shows broker and allows topic inspection

**Maps to:** NFR-OPS-01

---

### US-OPS-02: Verify service health

**As a** developer,  
**I want** health endpoints on each service,  
**so that** I can confirm they are running before testing APIs.

**Acceptance criteria:**

- [ ] Gateway, auth, user, and music services each expose actuator health endpoint
- [ ] Health returns UP when service and database connection are healthy

**Maps to:** NFR-REL-01

---

## Story Priority (MVP)

| Priority | Stories |
|----------|---------|
| P0 — Must complete | US-AUTH-01, US-AUTH-02, US-AUTH-03, US-USER-01, US-GW-02, US-OPS-01 |
| P1 — Core catalog | US-MUSIC-01 – 04, US-MEDIA-01 – 03, US-GW-01 |
| P2 — Full profile & events | US-USER-02 – 05, US-OPS-02 |

---

## Definition of Done (MVP)

A user story is **done** when:

1. All acceptance criteria pass via manual or automated test through the **gateway** (unless internal-only).
2. Error cases return documented HTTP status and response shape.
3. No secrets or credentials appear in committed configuration.
4. Behavior matches linked functional and non-functional requirements.

---

## Related Documents

| Document | Link |
|----------|------|
| Functional — auth | [functional-auth.md](functional-auth.md) |
| Functional — users | [functional-users.md](functional-users.md) |
| Functional — music | [functional-music.md](functional-music.md) |
| Non-functional | [non-functional.md](non-functional.md) |
| MVP scope lock | `mvp-scope.md` (upcoming) |

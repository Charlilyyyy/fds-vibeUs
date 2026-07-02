# Functional Requirements — User Profiles

## Overview

The user service owns **profile and presentation data** for accounts created through the auth service. It does not store passwords or issue tokens. Each profile is keyed by the same stable user identifier (`UUID`) assigned at registration.

Public profile endpoints are exposed through the API gateway under `/api/v1/users`. Internal profile creation is invoked only by the auth service during registration.

---

## FR-USER-01: Internal Profile Creation

**Description:** When auth completes registration, the user service creates a profile record linked to the new user id.

| Field | Requirement |
|-------|-------------|
| User id | Required; UUID from auth service; primary key |
| Email | Required; copied from registration for display and lookup |
| Username | Required; unique across profiles |
| First name / last name | Optional at creation |
| Bio | Optional |
| Profile image URL | Optional; may be null until user uploads elsewhere |
| Active flag | Default `true` |

**Behavior:**

1. Auth service sends `POST /internal/users` with creation payload (not routed through public gateway).
2. System validates input and rejects duplicate username or email with HTTP 409.
3. System persists profile in `vibeus_user_db`.
4. System returns HTTP 201 with profile response.
5. System publishes a `UserRegisteredEvent` to Kafka (see FR-USER-06).

**Authorization:** Internal endpoint only — network-restricted or secured by internal API convention; not callable by anonymous clients.

---

## FR-USER-02: Get Profile by Id

**Description:** An authenticated client can retrieve a user profile by id.

**Behavior:**

1. Client sends `GET /api/v1/users/{userId}` with valid JWT.
2. System returns HTTP 200 with profile data wrapped in standard `ApiResponse`.
3. If user id does not exist, returns HTTP 404.

**Authorization (MVP):** Any authenticated user may read any profile by id. Tighten to owner-only or public/private fields in a later iteration if needed.

---

## FR-USER-03: Get Current User Profile

**Description:** An authenticated client can retrieve their own profile without passing an id.

**Behavior:**

1. Client sends `GET /api/v1/users/me` with valid JWT.
2. System resolves current user id from token claims via `UserContext` (or equivalent).
3. System returns HTTP 200 with profile data.

---

## FR-USER-04: Update Profile

**Description:** A user can update editable profile fields.

| Updatable field | Validation |
|-----------------|------------|
| Username | Unique if changed; format rules as defined in DTO |
| First name / last name | Optional strings; max length |
| Bio | Optional; max length |
| Profile image URL | Optional; valid URL format if present |

**Behavior:**

1. Client sends `PUT /api/v1/users/me` (preferred) or `PUT /api/v1/users/{userId}` with update payload.
2. System ensures the authenticated user matches the target user id (403 if mismatch on `{userId}` routes).
3. System applies partial or full update per DTO design; returns HTTP 200 with updated profile.
4. Duplicate username returns HTTP 409.

**Not updatable via profile API:** Email and password (owned by auth service).

---

## FR-USER-05: Deactivate Profile

**Description:** A user can deactivate their account profile (soft delete).

**Behavior:**

1. Client sends `DELETE /api/v1/users/me` or `DELETE /api/v1/users/{userId}`.
2. System sets `active` to `false` (soft delete); does not hard-delete row in MVP.
3. Returns HTTP 204 (or HTTP 200 with message per API convention).

**Authorization:** Only the owning user may deactivate their profile.

**Note:** Coordinating credential disablement in auth service is a post-MVP cross-service concern; document as known gap for MVP.

---

## FR-USER-06: User Registered Event

**Description:** After profile creation, the user service publishes an asynchronous domain event.

**Event contract (minimum):**

| Field | Type | Description |
|-------|------|-------------|
| `userId` | UUID | Profile primary key |
| `username` | String | Chosen username |
| `email` | String | Registration email |
| `occurredAt` | Instant | Event timestamp |

**Behavior:**

1. Event published to a dedicated Kafka topic after successful DB commit.
2. Publishing failure should be logged; define retry or outbox pattern as implementation detail.
3. No consumer is required for MVP beyond verification in Kafka UI.

---

## FR-USER-07: Authenticated Request Context

**Description:** The user service extracts identity from the JWT on protected routes.

**Behavior:**

- `UserContext` (or equivalent) reads user id from gateway-forwarded token or `Authorization` header.
- Missing or invalid identity on protected routes yields HTTP 401.
- Internal `/internal/**` routes bypass user JWT and use separate protection.

---

## Data Ownership (User Database)

| Entity | Owned by | Notes |
|--------|----------|-------|
| User profile | User service | id, username, email, names, bio, image URL, active |
| Credentials | Auth service | Not stored here |

Database name: `vibeus_user_db`.

**Auditing:** Profiles extend a shared `BaseEntity` pattern (`createdAt`, `updatedAt`) consistent across services.

---

## API Summary (MVP)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/internal/users` | Internal | Create profile (auth → user) |
| GET | `/api/v1/users/me` | JWT | Current user profile |
| PUT | `/api/v1/users/me` | JWT | Update current user profile |
| DELETE | `/api/v1/users/me` | JWT | Deactivate current user |
| GET | `/api/v1/users/{userId}` | JWT | Get profile by id |
| PUT | `/api/v1/users/{userId}` | JWT | Update profile (owner only) |
| DELETE | `/api/v1/users/{userId}` | JWT | Deactivate profile (owner only) |

---

## Integration with Auth Service

```
Registration:
  Auth: save credentials
  Auth → User: POST /internal/users
  User: save profile + publish UserRegisteredEvent
  Auth → Client: 201 Created

Authenticated profile access:
  Client → Gateway (JWT) → User: /api/v1/users/**
```

---

## Related Documents

| Document | Link |
|----------|------|
| Authentication requirements | [functional-auth.md](functional-auth.md) |
| Music catalog requirements | `functional-music.md` (upcoming) |
| Feature prioritization | [features.md](../features.md) |
| Non-functional requirements | `non-functional.md` (upcoming) |

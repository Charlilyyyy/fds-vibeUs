# Functional Requirements — Authentication & Identity

## Overview

The authentication service owns **credentials and tokens**. It does not store user profile data (display name, bio, avatar). After successful registration, the auth service coordinates with the user service to provision a profile via an internal API.

All public auth endpoints are exposed through the API gateway under a versioned path prefix (e.g. `/api/v1/auth`).

---

## FR-AUTH-01: User Registration

**Description:** A new user can create an account with a unique email and password.

| Field | Requirement |
|-------|-------------|
| Email | Required; valid email format; unique across the system |
| Password | Required; minimum length (e.g. 8 characters); stored hashed, never plaintext |
| Username | Optional or required per final DTO design; unique if collected at registration |

**Behavior:**

1. Client sends `POST /api/v1/auth/register` with registration payload.
2. System validates input and rejects duplicate email with a clear error (HTTP 409).
3. System persists credentials in the auth database only.
4. System calls the user service (internal) to create a linked profile.
5. System returns HTTP 201 with a registration response (user identifier, message); tokens may be issued immediately or only after login — document the chosen flow consistently.

**Out of MVP scope:** Email verification, OAuth2/social login, CAPTCHA.

---

## FR-AUTH-02: User Login

**Description:** A registered user can authenticate and receive a JWT access token.

| Field | Requirement |
|-------|-------------|
| Email | Required |
| Password | Required |

**Behavior:**

1. Client sends `POST /api/v1/auth/login` with credentials.
2. System validates email and password against stored hash.
3. On success, returns HTTP 200 with access token and metadata (token type, expiry, user id).
4. On failure, returns HTTP 401 with a generic message (do not reveal whether email exists).

**Out of MVP scope:** Refresh tokens, remember-me, multi-factor authentication.

---

## FR-AUTH-03: JWT Access Token Issuance

**Description:** Successful login produces a signed JWT suitable for stateless authorization.

**Token must include (claims):**

| Claim | Purpose |
|-------|---------|
| Subject (`sub`) | Stable user identifier (UUID or numeric id) |
| Email | Optional; useful for debugging and downstream context |
| Roles / authorities | At minimum `ROLE_USER`; extensible for future admin |
| Issued at / expiration | Standard time bounds (e.g. 15–60 minutes for access token) |

**Behavior:**

- Token is signed with a shared secret or key pair configured per environment.
- Token is returned in the login response body (Bearer usage).
- Services and gateway must be able to validate signature and expiry without calling the database on every request.

---

## FR-AUTH-04: Token Validation at Gateway

**Description:** The API gateway validates JWTs on protected routes before forwarding requests.

**Behavior:**

1. Client sends `Authorization: Bearer <token>` on protected routes.
2. Gateway verifies signature, expiry, and required structure.
3. Valid token: request forwarded to downstream service with trusted headers or preserved Authorization header.
4. Missing, malformed, or expired token: HTTP 401; request is not forwarded.

**Public routes (no token required):**

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- Actuator health endpoints (as configured)

All other routes require a valid token unless explicitly documented as public.

---

## FR-AUTH-05: Internal Token Validation (Optional MVP Enhancement)

**Description:** Auth service may expose an internal endpoint for other services to validate tokens if not fully decoded at the gateway.

**Behavior:**

- Internal-only route (not exposed through public gateway or restricted by network).
- Accepts token; returns validity and principal claims.
- Used when a service cannot verify JWT locally.

**Note:** Prefer gateway-level validation for MVP; add internal validation only if needed.

---

## FR-AUTH-06: Password Security

**Description:** Passwords must be stored and compared securely.

| Requirement | Detail |
|-------------|--------|
| Hashing | BCrypt or equivalent adaptive hash |
| No plaintext storage | Passwords never logged or returned in API responses |
| Stateless sessions | No server-side HTTP session for API auth |

---

## FR-AUTH-07: Logout (Deferred)

**Description:** Explicit logout invalidates refresh tokens or server-side session state.

**MVP status:** **Deferred.** Stateless JWT access tokens expire naturally. Document refresh-token logout as a post-MVP requirement in [features](../features.md).

---

## FR-AUTH-08: Service-to-Service Registration Flow

**Description:** Registration spans auth and user services.

**Sequence:**

```
Client → Gateway → Auth (register)
                    → User (internal: create profile)
                    ← success / rollback on failure
         ← 201 Created
```

| Step | Responsibility |
|------|----------------|
| 1 | Auth validates and saves credentials |
| 2 | Auth invokes user service internal API with user id and minimal profile seed |
| 3 | User service creates profile and may publish domain event |
| 4 | Auth returns success to client |

**Failure handling:** If profile creation fails after credentials are saved, auth service must define compensating behavior (delete auth record or mark account incomplete) and return an appropriate error.

---

## Data Ownership (Auth Database)

| Entity / table | Owned by | Notes |
|----------------|----------|-------|
| User credentials | Auth service | Email, password hash, enabled flag |
| Refresh tokens | Auth service | Post-MVP |
| Profile fields | User service | Not in auth DB |

Database name: `vibeus_auth_db`.

---

## API Summary (MVP)

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/api/v1/auth/register` | Public | Create account |
| POST | `/api/v1/auth/login` | Public | Obtain JWT |

---

## Related Documents

| Document | Link |
|----------|------|
| Feature prioritization | [features.md](../features.md) |
| User profile requirements | `functional-users.md` (upcoming) |
| Non-functional requirements | `non-functional.md` (upcoming) |
| User stories | `user-stories.md` (upcoming) |
| MVP scope | `mvp-scope.md` (upcoming) |

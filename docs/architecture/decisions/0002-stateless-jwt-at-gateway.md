# 0002 — Stateless JWT enforced at the gateway

- Status: Accepted
- Date: 2026-07-19

## Context

Every protected request must be authenticated. We want to avoid server-side
sessions and avoid each service re-implementing token parsing.

## Decision

Use stateless JWT access tokens issued by auth-service (HS256). The gateway is
the single enforcement point: it validates the token, rejects non-access and
expired/tampered tokens, strips the `Authorization` header, and forwards
identity as trusted `X-User-Id`, `X-User-Email`, `X-User-Name` headers.

## Consequences

- **Pros**: no session store; downstream services stay simple and trust headers;
  one place to enforce auth.
- **Cons**: token revocation is non-trivial (mitigated later with refresh tokens
  and a revocation list); the JWT secret must be shared between auth and gateway.
- Internal endpoints (`/internal/**`) rely on network trust and are not exposed
  through the gateway.

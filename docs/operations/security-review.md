# Security Review

A pragmatic review of the MVP security posture and the hardening steps taken,
plus known gaps to address before production.

## In place

### Authentication & authorization
- Stateless JWT authentication; tokens signed with HS256.
- The gateway is the single enforcement point: it validates the access token,
  rejects non-access tokens and expired/tampered tokens, and strips the
  `Authorization` header before forwarding.
- Downstream services trust only gateway-injected `X-User-*` headers and never
  parse tokens themselves.
- Passwords hashed with BCrypt; plaintext passwords are never stored or logged.

### Input validation
- Request DTOs use Jakarta Bean Validation (`@NotBlank`, `@Email`, `@Size`,
  `@Positive`, `@NotEmpty`).
- Consistent error contract via `GlobalExceptionHandler`; internal exceptions are
  not leaked to clients (generic 500 message).
- File uploads validate content type and size before storage.

### Secrets management
- Secrets (JWT secret, DB credentials, MinIO keys, Kafka brokers) are read from
  environment variables with local-only defaults.
- `infrastructure/.env.example` documents required variables; real `.env` files
  are gitignored.

### Data isolation
- Database per service; services reference other aggregates by id only.

## Known gaps / TODO before production

- **Rate limiting**: add per-client rate limiting at the gateway (e.g. Redis-backed
  request limiter) to mitigate brute-force and abuse.
- **Token revocation & refresh**: add refresh tokens and a revocation list.
- **TLS**: terminate HTTPS at the gateway; enforce TLS between services.
- **CORS**: tighten `allowedOriginPatterns` from `*` to an explicit allowlist.
- **Secret rotation**: integrate a secrets manager; remove local default secrets.
- **AuthN on internal endpoints**: restrict `/internal/**` to the internal network
  or add mutual auth (currently network-trust only).
- **Dependency scanning**: add SCA (e.g. OWASP Dependency-Check) to CI.
- **Audit logging**: structured audit logs for auth and admin actions.

## Checklist for new endpoints

- [ ] Validate all inputs with Bean Validation.
- [ ] Confirm the route's public/protected classification at the gateway.
- [ ] Never log secrets or full tokens.
- [ ] Return the shared error contract; avoid leaking internals.
- [ ] Add tests covering the unauthorized/invalid-input paths.

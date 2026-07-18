# Lessons Learned

Reflections captured while building the VibeUs MVP.

## What worked well

- **Gateway as the single auth choke point** kept downstream services simple:
  they trust `X-User-*` headers instead of parsing tokens.
- **Database per service** forced clean boundaries and made data ownership
  obvious; referencing by id avoided hidden coupling.
- **Consistent contracts** (`ApiResponse`/`ErrorResponse`, pagination, a global
  exception handler) made the API predictable and controllers thin.
- **Env-var configuration with local defaults** made the services runnable out of
  the box while keeping secrets out of version control.
- **Testing with H2 + mocked collaborators** (Feign client, MinIO client, Kafka)
  gave fast, deterministic tests without external services.

## Gotchas and how we handled them

- **Test context startup and external systems**: full-context tests tried to hit
  MinIO/Kafka. We mocked the MinIO client, disabled listener auto-startup, and
  set short Kafka admin timeouts so tests stay fast and hermetic.
- **Distributed writes without 2PC**: auth creates the user profile via a sync
  call and rolls back its own record if the downstream call fails — a simple
  compensation instead of distributed transactions.
- **Eventual consistency**: publishing `user.registered` only after the profile
  is committed lets consumers assume the entity exists.
- **Upload lifecycle**: replacing media deletes the old object to avoid orphans;
  content type and size are validated before hitting storage.

## What we'd do next time

- Introduce refresh tokens and rate limiting earlier.
- Add correlation ids and tracing from the start for easier debugging.
- Add contract tests between auth and user services.
- Consider an aggregator build (or Gradle) to build all services with one command.

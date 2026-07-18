# Planned Services Roadmap

The MVP ships four services (gateway, auth, user, music). The following services
are planned for future iterations. Each has a placeholder directory documenting
its intended boundary.

| Service            | Port | Base path            | Database              | Status   |
|--------------------|------|----------------------|-----------------------|----------|
| gateway-service    | 8081 | (all)                | —                     | Built    |
| auth-service       | 8082 | `/api/v1/auth`       | `vibeus_auth_db`      | Built    |
| user-service       | 8083 | `/api/v1/users`      | `vibeus_user_db`      | Built    |
| music-service      | 8084 | `/api/v1/{catalog}`  | `vibeus_music_db`     | Built    |
| playlist-service   | 8085 | `/api/v1/playlists`  | `vibeus_playlist_db`  | Planned  |
| search-service     | 8086 | `/api/v1/search`     | (search engine)       | Planned  |
| streaming-service  | 8087 | `/api/v1/stream`     | —                     | Planned  |

## Guiding principles for new services

1. **Own your data** — each service has its own database/store; reference other
   aggregates by id.
2. **Sync for reads, async for reactions** — call other services synchronously
   only when the caller needs an immediate answer; otherwise consume events.
3. **Stateless auth** — trust the `X-User-*` headers injected by the gateway.
4. **Consistent contracts** — reuse the shared `ApiResponse`/`ErrorResponse`
   shapes and pagination conventions.

## Suggested build order

1. playlist-service (depends on music-service catalog).
2. search-service (consumes catalog events).
3. streaming-service (object storage + play analytics).

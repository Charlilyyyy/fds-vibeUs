# playlist-service (planned)

Planned service for user-owned playlists. Not yet implemented — this directory
is a placeholder that reserves the service boundary and documents intended
scope.

## Responsibilities (planned)

- Create, rename, and delete playlists owned by a user.
- Add/remove/reorder tracks within a playlist.
- Reference tracks by id from the music-service (no data duplication).
- Enforce ownership via the `X-User-Id` header forwarded by the gateway.

## Integration (planned)

- **Sync**: read track metadata from music-service when needed.
- **Async**: consume catalog events (e.g. track removed) to keep playlists
  consistent.

## Local defaults (planned)

- Port: `8085`
- Database: `vibeus_playlist_db`
- Base path: `/api/v1/playlists`

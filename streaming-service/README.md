# streaming-service (planned)

Planned service for audio streaming and play-count tracking. Not yet implemented
— this directory is a placeholder that reserves the service boundary and
documents intended scope.

## Responsibilities (planned)

- Stream track audio (range requests) from object storage.
- Record play events and increment play counts.
- Enforce access control via gateway-forwarded identity headers.

## Integration (planned)

- **Sync**: resolve track/audio locations from music-service.
- **Async**: emit play events for analytics and recommendations.

## Local defaults (planned)

- Port: `8087`
- Base path: `/api/v1/stream`

# Event Catalog

VibeUs uses Kafka for asynchronous, decoupled communication between services.
This catalog documents the event contracts, ownership, and flows currently in
place, plus planned async use cases.

## Conventions

- **Topic naming**: lowercase, dot-delimited, `<aggregate>.<past-tense-verb>`
  (e.g. `user.registered`).
- **Keying**: events are keyed by the aggregate id (e.g. `userId`) so all events
  for the same entity land on the same partition and preserve ordering.
- **Serialization**: JSON via Spring Kafka's `JsonSerializer` /
  `JsonDeserializer`. Type headers are added by the producer; consumers trust the
  `com.vibeus.*` packages.
- **Compatibility**: events are additive. Prefer adding optional fields over
  changing or removing existing ones.

## Events

### `user.registered`

| Property     | Value                                             |
|--------------|---------------------------------------------------|
| Topic        | `user.registered`                                 |
| Owner        | user-service                                      |
| Key          | `userId` (UUID as string)                         |
| Trigger      | A new user profile is created                     |
| Partitions   | 1 (local dev)                                     |

**Payload**

```json
{
  "userId": "6f1c7c1e-8b2a-4b3a-9c9e-1a2b3c4d5e6f",
  "username": "jazzfan",
  "email": "user@example.com",
  "occurredAt": "2026-07-19T00:00:00Z"
}
```

**Producers**

- `user-service` publishes on successful profile creation
  (`UserEventProducer`).

**Consumers**

- `user-service` (`UserRegisteredEventListener`) — currently a logging stub that
  represents the seam for downstream side effects (welcome notifications, search
  indexing, analytics).

## Flow

```
auth-service --(REST: create profile)--> user-service
user-service --(save profile)--> user DB
user-service --(publish user.registered)--> Kafka
Kafka --(consume)--> consumers (notifications / indexing / analytics)
```

Because publication happens after the profile is committed, downstream consumers
can assume the profile exists when they process the event.

## Verifying events end-to-end

1. Start the local infrastructure (Kafka + Kafka UI) from `infrastructure/`.
2. Register a user through the gateway (`POST /api/v1/auth/register`).
3. Open Kafka UI and inspect the `user.registered` topic to confirm a new
   message with the expected key and payload.
4. Check the user-service logs for the `Received UserRegisteredEvent` line.

## Planned async use cases

- **Notifications**: send a welcome email/notification on `user.registered`.
- **Search indexing**: index new users, artists, albums, and tracks for a future
  search-service.
- **Playlist sync**: propagate catalog changes to a future playlist-service.
- **Analytics**: stream play counts and engagement metrics.
- **Cache invalidation**: invalidate cached reads when catalog entities change.

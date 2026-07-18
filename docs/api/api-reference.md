# API Reference

All client traffic goes through the gateway at `http://localhost:8081`. The
gateway validates the JWT, forwards identity via `X-User-*` headers, and routes
to the owning service.

## Conventions

- **Base URL**: `http://localhost:8081`
- **Auth**: send `Authorization: Bearer <accessToken>` on protected endpoints.
- **Success envelope**:

```json
{ "status": 200, "message": "…", "data": { } }
```

- **Error envelope**:

```json
{ "timestamp": "2026-07-19T00:00:00Z", "status": 404, "error": "RESOURCE_NOT_FOUND", "message": "…" }
```

- **Pagination** (catalog list endpoints): `?page=0&size=20&sort=field,asc`.
  Paged responses return `content`, `page`, `size`, `totalElements`,
  `totalPages`, `last`.

## Public vs protected

| Access   | Endpoints                                                        |
|----------|-----------------------------------------------------------------|
| Public   | `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `GET` on catalog (`/artists`, `/genres`, `/albums`, `/tracks`), `GET /actuator/health` |
| Protected| Everything else (requires a valid access token)                 |

---

## Auth — `/api/v1/auth`

### Register

`POST /api/v1/auth/register` (public)

```json
{ "email": "user@example.com", "username": "jazzfan", "password": "password123" }
```

Returns `201` with `{ userId, email, username, message }`. Also creates the
user profile in user-service and publishes `user.registered`.

### Login

`POST /api/v1/auth/login` (public)

```json
{ "email": "user@example.com", "password": "password123" }
```

Returns `200` with `{ accessToken, tokenType, expiresAt, userId }`.

---

## Users — `/api/v1/users`

| Method | Path                     | Description                        |
|--------|--------------------------|------------------------------------|
| GET    | `/api/v1/users/me`       | Current user's profile             |
| PUT    | `/api/v1/users/me`       | Update current user's profile      |
| GET    | `/api/v1/users/{userId}` | Fetch a profile by id              |

Update body (all fields optional):

```json
{ "firstName": "Miles", "lastName": "Davis", "bio": "Trumpet", "profileImageUrl": "…" }
```

Internal (service-to-service, not exposed via gateway): `POST /internal/users`.

---

## Music catalog

### Artists — `/api/v1/artists`

| Method | Path                          | Access   |
|--------|-------------------------------|----------|
| POST   | `/api/v1/artists`             | protected|
| GET    | `/api/v1/artists/{id}`        | public   |
| GET    | `/api/v1/artists`             | public   |
| PUT    | `/api/v1/artists/{id}`        | protected|
| DELETE | `/api/v1/artists/{id}`        | protected|
| POST   | `/api/v1/artists/{id}/image`  | protected (multipart `file`) |

### Genres — `/api/v1/genres`

CRUD identical in shape to artists: `POST`, `GET /{id}`, `GET`, `PUT /{id}`,
`DELETE /{id}`.

### Albums — `/api/v1/albums`

| Method | Path                         | Notes                                |
|--------|------------------------------|--------------------------------------|
| POST   | `/api/v1/albums`             | requires `artistId`, `type`, `releaseDate` |
| GET    | `/api/v1/albums/{id}`        |                                      |
| GET    | `/api/v1/albums?artistId=…`  | filter by artist (optional)          |
| PUT    | `/api/v1/albums/{id}`        |                                      |
| DELETE | `/api/v1/albums/{id}`        |                                      |
| POST   | `/api/v1/albums/{id}/cover`  | multipart `file`                     |

`type` is one of `SINGLE`, `EP`, `ALBUM`.

### Tracks — `/api/v1/tracks`

| Method | Path                          | Notes                                   |
|--------|-------------------------------|-----------------------------------------|
| POST   | `/api/v1/tracks`              | requires `artistIds`, `genreIds`, audio |
| GET    | `/api/v1/tracks/{id}`         |                                         |
| GET    | `/api/v1/tracks?artistId=…` / `?albumId=…` / `?genreId=…` | filters |
| PUT    | `/api/v1/tracks/{id}`         |                                         |
| DELETE | `/api/v1/tracks/{id}`         |                                         |
| POST   | `/api/v1/tracks/{id}/audio`   | multipart `file`                        |
| POST   | `/api/v1/tracks/{id}/cover`   | multipart `file`                        |

Create body:

```json
{
  "title": "So What",
  "durationInSeconds": 545,
  "audioUrl": "https://…",
  "coverImageUrl": null,
  "albumId": "…",
  "artistIds": ["…"],
  "genreIds": ["…"]
}
```

### Uploads — `/api/v1/uploads`

Standalone media uploads (multipart `file`): `/artist-images`, `/album-covers`,
`/track-audio`. Returns `{ fileName, fileUrl }`.

Upload constraints: images `image/jpeg|png|webp` up to 5 MB; audio
`audio/mpeg|mp3|wav` up to 50 MB.

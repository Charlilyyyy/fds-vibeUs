# Functional Requirements — Music Catalog & Media

## Overview

The music service owns **catalog metadata** (artists, albums, tracks, genres) and **binary media assets** stored in S3-compatible object storage (MinIO). All public endpoints are exposed through the API gateway under versioned paths (e.g. `/api/v1/artists`, `/api/v1/albums`, `/api/v1/tracks`, `/api/v1/genres`).

Catalog operations require a valid JWT unless explicitly marked public in a later iteration (MVP: authenticated access for mutating and listing operations).

Database name: `vibeus_music_db`.

---

## Domain Model

### Relationships

```
Artist ──< Album ──< Track
  │                    │
  └──── track_artists ─┘
                       │
Genre ── track_genres ─┘
```

| Entity | Key fields | Relationships |
|--------|------------|---------------|
| **Artist** | name (unique), bio, imageUrl, verified, active | One artist → many albums |
| **Album** | title, type, releaseDate, coverImageUrl, active | Many-to-one artist; one album → many tracks |
| **Track** | title, durationInSeconds, audioUrl, coverImageUrl, playCount, active | Many-to-one album; many-to-many artists and genres |
| **Genre** | name (unique), description, active | Many-to-many with tracks |

### Album type enum

`SINGLE`, `EP`, `ALBUM`

---

## FR-MUSIC-01: Artist Management

**Description:** CRUD operations for artist catalog entries.

| Operation | Behavior |
|-----------|----------|
| Create | `POST /api/v1/artists` — name required, unique; returns 201 |
| Read by id | `GET /api/v1/artists/{artistId}` — returns 404 if missing |
| List | `GET /api/v1/artists` — paginated list; supports page/size query params |
| Update | `PUT /api/v1/artists/{artistId}` — partial or full update per DTO |
| Delete | `DELETE /api/v1/artists/{artistId}` — soft delete (`active = false`) preferred |

**Validation:**

- Duplicate artist name → HTTP 409
- Name required; bio max length (e.g. 3000 characters)

---

## FR-MUSIC-02: Album Management

**Description:** CRUD for albums linked to an artist.

| Field | Requirement |
|-------|-------------|
| Title | Required |
| Type | Required; enum `SINGLE`, `EP`, `ALBUM` |
| Release date | Required |
| Artist id | Required; must reference existing active artist |
| Cover image URL | Optional until upload |

| Operation | Behavior |
|-----------|----------|
| Create | `POST /api/v1/albums` |
| Read by id | `GET /api/v1/albums/{id}` |
| List | `GET /api/v1/albums` — paginated |
| List by artist | `GET /api/v1/albums/artist/{artistId}` |
| Update | `PUT /api/v1/albums/{id}` |
| Delete | `DELETE /api/v1/albums/{id}` — soft delete |

**Validation:**

- Invalid or missing artist → HTTP 404
- Invalid album type → HTTP 400

---

## FR-MUSIC-03: Track Management

**Description:** CRUD for tracks with duration, audio reference, and associations.

| Field | Requirement |
|-------|-------------|
| Title | Required |
| Duration (seconds) | Required; positive integer |
| Audio URL | Required after upload or at creation if URL provided |
| Album id | Optional or required per design; link when part of an album |
| Artists | One or more artist associations |
| Genres | Zero or more genre associations |
| Play count | Default 0; read-only via API in MVP |

| Operation | Behavior |
|-----------|----------|
| Create | `POST /api/v1/tracks` |
| Read by id | `GET /api/v1/tracks/{id}` |
| List | `GET /api/v1/tracks` — paginated |
| List by artist | `GET /api/v1/tracks/artist/{artistId}` |
| List by album | `GET /api/v1/tracks/album/{albumId}` |
| List by genre | `GET /api/v1/tracks/genre/{genreId}` |
| Update | `PUT /api/v1/tracks/{id}` |
| Delete | `DELETE /api/v1/tracks/{id}` — soft delete |

---

## FR-MUSIC-04: Genre Management

**Description:** CRUD for genre taxonomy used to classify tracks.

| Field | Requirement |
|-------|-------------|
| Name | Required; unique; max 100 characters |
| Description | Optional; max 1000 characters |

| Operation | Behavior |
|-----------|----------|
| Create | `POST /api/v1/genres` |
| Read by id | `GET /api/v1/genres/{id}` |
| List | `GET /api/v1/genres` — paginated |
| Update | `PUT /api/v1/genres/{id}` |
| Delete | `DELETE /api/v1/genres/{id}` — soft delete |

**Validation:** Duplicate genre name → HTTP 409

---

## FR-MUSIC-05: Pagination & List Responses

**Description:** All list endpoints return paginated results consistently.

| Parameter | Default | Rules |
|-----------|---------|-------|
| `page` | 0 | Zero-based index |
| `size` | 20 | Max cap (e.g. 100) enforced |

**Response:** Wrapped in standard `ApiResponse` with page metadata (total elements, total pages, current page) or Spring `Page` serialization — consistent across catalog resources.

---

## FR-MUSIC-06: Object Storage Integration

**Description:** Binary files are stored in MinIO buckets; metadata rows store returned URLs.

**Buckets (minimum):**

| Bucket purpose | Content |
|----------------|---------|
| Artists | Artist profile images |
| Albums | Album cover art |
| Tracks | Audio files and optional track cover images |

**Behavior:**

1. On service startup, ensure buckets exist (create if missing).
2. Upload generates a unique object key (e.g. UUID-based).
3. Stored URL is persisted on the corresponding entity field.
4. Delete/replace flows should handle orphaned objects as implementation detail (document chosen strategy).

---

## FR-MUSIC-07: Image Upload

**Description:** Upload images for artists, album covers, and track covers.

| Endpoint pattern | Target field |
|------------------|--------------|
| `POST /api/v1/artists/{artistId}/image` | `imageUrl` on Artist |
| `POST /api/v1/albums/{albumId}/cover` | `coverImageUrl` on Album |
| `POST /api/v1/tracks/{trackId}/cover` | `coverImageUrl` on Track |

**Allowed content types:** `image/jpeg`, `image/png`, `image/webp`

**Behavior:**

1. Client sends `multipart/form-data` with file part.
2. System validates file present, non-empty, content type, and max size.
3. System uploads to appropriate bucket and updates entity URL.
4. Returns HTTP 200 with upload response (URL, file name, content type, size).

**Errors:**

- Invalid type → HTTP 400
- Exceeds max size → HTTP 400 with clear message
- Entity not found → HTTP 404
- Storage failure → HTTP 500 with safe error body

---

## FR-MUSIC-08: Audio Upload

**Description:** Upload audio assets for tracks.

| Endpoint | Behavior |
|----------|----------|
| `POST /api/v1/tracks/{trackId}/track` | Upload audio file; set `audioUrl` on Track |

**Allowed content types:** `audio/mpeg`, `audio/mp3`, `audio/wav`, `audio/x-wav`

**Behavior:** Same validation and error pattern as image upload (FR-MUSIC-07).

**Optional:** Standalone upload endpoints under `/api/v1/uploads/**` that return a URL for use in track creation — support if implemented; otherwise upload is always bound to an existing track id.

---

## FR-MUSIC-09: Validation & Error Handling

**Description:** Catalog APIs use shared validation and exception patterns.

| Scenario | HTTP status |
|----------|-------------|
| Bean validation failure | 400 |
| Resource not found | 404 |
| Duplicate name (artist, genre) | 409 |
| Invalid reference (artist/album/genre id) | 404 |
| Unauthorized | 401 |
| Storage errors | 500 (no internal stack trace in body) |

All responses use shared `ApiResponse` / `ErrorResponse` shapes consistent with auth and user services.

---

## FR-MUSIC-10: Gateway & Authorization (MVP)

**Description:** Music routes are reached via gateway on port 8081; music service listens on 8084.

**MVP authorization:** Valid JWT required for catalog and upload endpoints. Role-based restrictions (e.g. admin-only create) may be added post-MVP; default authenticated user can manage catalog in local dev.

---

## API Summary (MVP)

### Artists

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/artists` | Create artist |
| GET | `/api/v1/artists/{artistId}` | Get artist |
| GET | `/api/v1/artists` | List artists |
| PUT | `/api/v1/artists/{artistId}` | Update artist |
| DELETE | `/api/v1/artists/{artistId}` | Deactivate artist |
| POST | `/api/v1/artists/{artistId}/image` | Upload artist image |

### Albums

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/albums` | Create album |
| GET | `/api/v1/albums/{id}` | Get album |
| GET | `/api/v1/albums` | List albums |
| GET | `/api/v1/albums/artist/{artistId}` | Albums by artist |
| PUT | `/api/v1/albums/{id}` | Update album |
| DELETE | `/api/v1/albums/{id}` | Deactivate album |
| POST | `/api/v1/albums/{albumId}/cover` | Upload cover |

### Tracks

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/tracks` | Create track |
| GET | `/api/v1/tracks/{id}` | Get track |
| GET | `/api/v1/tracks` | List tracks |
| GET | `/api/v1/tracks/artist/{artistId}` | Tracks by artist |
| GET | `/api/v1/tracks/album/{albumId}` | Tracks by album |
| GET | `/api/v1/tracks/genre/{genreId}` | Tracks by genre |
| PUT | `/api/v1/tracks/{id}` | Update track |
| DELETE | `/api/v1/tracks/{id}` | Deactivate track |
| POST | `/api/v1/tracks/{trackId}/track` | Upload audio |
| POST | `/api/v1/tracks/{trackId}/cover` | Upload track cover |

### Genres

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/genres` | Create genre |
| GET | `/api/v1/genres/{id}` | Get genre |
| GET | `/api/v1/genres` | List genres |
| PUT | `/api/v1/genres/{id}` | Update genre |
| DELETE | `/api/v1/genres/{id}` | Deactivate genre |

---

## Out of MVP Scope

- Playlist management
- Search and full-text indexing
- Streaming-range / dedicated streaming service
- Play-count increment on playback
- Royalty or rights metadata
- Public anonymous browse (all routes authenticated in MVP)

---

## Related Documents

| Document | Link |
|----------|------|
| Authentication requirements | [functional-auth.md](functional-auth.md) |
| User profile requirements | [functional-users.md](functional-users.md) |
| Feature prioritization | [features.md](../features.md) |
| Non-functional requirements | `non-functional.md` (upcoming) |

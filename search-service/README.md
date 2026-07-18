# search-service (planned)

Planned service for full-text search across the catalog. Not yet implemented —
this directory is a placeholder that reserves the service boundary and documents
intended scope.

## Responsibilities (planned)

- Index artists, albums, and tracks for fast search.
- Provide typeahead/autocomplete and ranked search results.
- Keep the index eventually consistent with the catalog.

## Integration (planned)

- **Async**: consume catalog events to update the search index.
- Backed by a search engine (e.g. OpenSearch/Elasticsearch) in a later iteration.

## Local defaults (planned)

- Port: `8086`
- Base path: `/api/v1/search`

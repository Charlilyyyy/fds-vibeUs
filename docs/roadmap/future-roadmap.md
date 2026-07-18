# Future Roadmap

Where VibeUs goes after the MVP. Grouped into product features and platform
maturity. See [planned-services.md](planned-services.md) for the service-level
breakdown.

## Product features

- **Playlists** (playlist-service): user-owned playlists referencing catalog
  tracks; add/remove/reorder.
- **Search** (search-service): full-text search and typeahead across the catalog,
  kept in sync via events.
- **Streaming** (streaming-service): audio streaming with range requests and
  play-count tracking.
- **Social**: follows, likes, and sharing.
- **Recommendations**: derived from play events and library signals.

## Platform maturity

### Security
- Refresh tokens + revocation list.
- Gateway rate limiting (Redis-backed).
- TLS everywhere; tighten CORS to an allowlist.
- Secrets manager + rotation; dependency scanning in CI.

### Reliability & performance
- Redis caching for hot reads (catalog).
- Idempotent consumers and a dead-letter topic for Kafka.
- Resilience patterns (timeouts, retries, circuit breakers) on sync calls.

### Observability
- Structured logging with correlation ids propagated through the gateway.
- Metrics (Micrometer → Prometheus) and dashboards (Grafana).
- Distributed tracing (OpenTelemetry).

### Delivery
- Container images published from CI.
- Kubernetes manifests/Helm charts; readiness/liveness probes.
- Environment promotion (dev → staging → prod) and DB migrations (Flyway/Liquibase).

## Rough sequencing

1. Harden security (refresh tokens, rate limiting).
2. Add caching + observability.
3. Build playlist-service, then search-service, then streaming-service.
4. Kubernetes deployment and CI-published images.

# Deployment Topology

## Overview

VibeUs MVP runs on a **developer machine** using:

1. **Docker Compose** — infrastructure containers (PostgreSQL, Redis, MinIO, Kafka, Kafka UI)
2. **Local JVM processes** — four Spring Boot services started via Maven (`mvn spring-boot:run` or JAR)

Application services bind to **localhost** ports and connect to infrastructure on published host ports. This avoids rebuilding service images during active development while keeping infra reproducible.

---

## Topology Diagram (Local MVP)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Developer host                                  │
│                                                                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐    │
│  │   gateway    │  │     auth     │  │     user     │  │    music     │    │
│  │    :8081     │  │    :8082     │  │    :8083     │  │    :8084     │    │
│  │  (JVM/JAR)   │  │  (JVM/JAR)   │  │  (JVM/JAR)   │  │  (JVM/JAR)   │    │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘    │
│         │                 │                 │                 │            │
│         │    HTTP routes  │────Feign───────►│                 │            │
│         └─────────────────┼─────────────────┼─────────────────┘            │
│                           │                 │                 │            │
│                           ▼                 ▼                 ▼            │
│  ┌────────────────────────────────────────────────────────────────────┐   │
│  │                    Docker Compose network                           │   │
│  │  ┌────────────┐ ┌───────┐ ┌───────┐ ┌────────────┐ ┌───────────┐ │   │
│  │  │ PostgreSQL │ │ Redis │ │ MinIO │ │   Kafka    │ │ Kafka UI  │ │   │
│  │  │   :5433    │ │ :6379 │ │ :9000 │ │   :9092    │ │   :8080   │ │   │
│  │  │  (host)    │ │       │ │ :9001 │ │            │ │           │ │   │
│  │  └────────────┘ └───────┘ └───────┘ └────────────┘ └───────────┘ │   │
│  └────────────────────────────────────────────────────────────────────┘   │
│                                                                              │
│  ┌──────────────┐                                                            │
│  │ API client   │ ──► http://localhost:8081/api/v1/...                       │
│  └──────────────┘                                                            │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Port Matrix

### Application services (host)

| Service | Port | Base URL (local) | Health |
|---------|------|------------------|--------|
| gateway-service | **8081** | `http://localhost:8081` | `/actuator/health` |
| auth-service | **8082** | `http://localhost:8082` | `/actuator/health` |
| user-service | **8083** | `http://localhost:8083` | `/actuator/health` |
| music-service | **8084** | `http://localhost:8084` | `/actuator/health` |

**Client entry point:** always `http://localhost:8081` (gateway).

### Infrastructure (Docker → host mapping)

| Container name | Image | Host port | Container port | Purpose |
|----------------|-------|-----------|----------------|---------|
| `vibeus-postgres` | `postgres:17` | **5433** | 5432 | Relational databases |
| `vibeus-redis` | `redis:8` | **6379** | 6379 | Future cache (idle in MVP) |
| `vibeus-minio` | `minio/minio` | **9000**, **9001** | 9000, 9001 | Object API + console |
| `vibeus-kafka` | `apache/kafka:latest` | **9092** | 9092 | Broker (host clients) |
| `vibeus-kafka-ui` | `provectuslabs/kafka-ui:latest` | **8080** | 8080 | Topic browser |

**Port conflict note:** Kafka UI uses host port `8080`. Do not run another app on `8080` locally.

---

## Repository Layout (Infrastructure)

```
infrastructure/
├── docker-compose.yml      # Postgres, Redis, MinIO, Kafka, Kafka UI
├── .env.example            # Documented defaults (no secrets in git)
└── README.md               # Start/stop commands (added during implementation)
```

Compose file lives at repo root under `infrastructure/` — not inside individual services.

---

## Docker Compose Services (Specification)

### PostgreSQL (`vibeus-postgres`)

| Setting | Value |
|---------|-------|
| User | `postgres` |
| Password | `postgres` (local dev only) |
| Host from JVM | `localhost` |
| Port from JVM | `5433` |

**Logical databases** (created on first connection or init script):

| Database | Consumer |
|----------|----------|
| `vibeus_auth_db` | auth-service |
| `vibeus_user_db` | user-service |
| `vibeus_music_db` | music-service |

JDBC URL pattern:

```
jdbc:postgresql://localhost:5433/vibeus_<service>_db
```

---

### Redis (`vibeus-redis`)

| Setting | Value |
|---------|-------|
| Host | `localhost` |
| Port | `6379` |

No application connection in MVP. Container started for future caching work.

---

### MinIO (`vibeus-minio`)

| Setting | Value |
|---------|-------|
| API endpoint (from JVM) | `http://localhost:9000` |
| Console | `http://localhost:9001` |
| Root user | `admin` |
| Root password | `admin123` (local dev only) |

**Buckets** (created by music-service on startup):

| Config key | Example bucket name |
|------------|---------------------|
| artists | `vibeus-artists` or `artists` |
| albums | `vibeus-albums` or `albums` |
| tracks | `vibeus-tracks` or `tracks` |

Public URL returned in API responses: `http://localhost:9000/<bucket>/<object-key>`.

---

### Kafka (`vibeus-kafka`)

KRaft single-node broker for local development.

| Listener | Address | Used by |
|----------|---------|---------|
| Host client | `localhost:9092` | Spring apps on host JVM |
| Internal | `vibeus-kafka:29092` | Kafka UI container |

**Spring `spring.kafka.bootstrap-servers`:** `localhost:9092`

Example internal compose environment (reference for `docker-compose.yml`):

```yaml
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://vibeus-kafka:29092
```

---

### Kafka UI (`vibeus-kafka-ui`)

| Setting | Value |
|---------|-------|
| URL | `http://localhost:8080` |
| Cluster bootstrap | `vibeus-kafka:29092` (internal) |
| Cluster display name | `vibeus` |

---

## Gateway Route Targets (Local)

Gateway forwards to **host** addresses because services run on the JVM, not inside compose:

| Route id (example) | Path predicate | Target URI |
|--------------------|----------------|------------|
| auth-service | `/api/v1/auth/**` | `http://localhost:8082` |
| user-service | `/api/v1/users/**` | `http://localhost:8083` |
| music-artists | `/api/v1/artists/**` | `http://localhost:8084` |
| music-albums | `/api/v1/albums/**` | `http://localhost:8084` |
| music-tracks | `/api/v1/tracks/**` | `http://localhost:8084` |
| music-genres | `/api/v1/genres/**` | `http://localhost:8084` |

**Excluded from gateway:** `http://localhost:8083/internal/**` (auth calls directly).

---

## Service-to-Service URLs (Local)

| Caller | Callee | URL |
|--------|--------|-----|
| auth-service | user-service | `http://localhost:8083/internal/users` |
| gateway-service | auth / user / music | See route table above |

No service registry (Eureka/Consul) in MVP — **static URLs** in configuration.

---

## Startup Order

| Step | Action | Verify |
|------|--------|--------|
| 1 | `docker compose -f infrastructure/docker-compose.yml up -d` | All infra containers healthy |
| 2 | Start **auth-service** :8082 | `/actuator/health` UP |
| 3 | Start **user-service** :8083 | `/actuator/health` UP |
| 4 | Start **music-service** :8084 | `/actuator/health` UP; MinIO buckets exist |
| 5 | Start **gateway-service** :8081 | `/actuator/health` UP |
| 6 | Smoke test | `POST /api/v1/auth/register` through gateway |

Auth before user is not strictly required for user to boot, but registration flow needs **both** before gateway testing. Gateway last so routes do not fail to connect during infra bring-up.

---

## Environment Variables (Key)

| Variable | Service(s) | Example (local) |
|----------|------------|-----------------|
| `JWT_SECRET` | auth, gateway | Long random string (shared) |
| `JWT_EXPIRATION_MS` | auth | `3600000` |
| `SPRING_DATASOURCE_URL` | auth, user, music | Per-service JDBC URL |
| `USER_SERVICE_URL` | auth | `http://localhost:8083` |
| `MINIO_ENDPOINT` | music | `http://localhost:9000` |
| `MINIO_ACCESS_KEY` | music | `admin` |
| `MINIO_SECRET_KEY` | music | `admin123` |
| `KAFKA_BOOTSTRAP_SERVERS` | user | `localhost:9092` |

Prefer env overrides over committing secrets; `.env.example` documents keys with placeholder values.

---

## Network Modes

### MVP (default): Hybrid

| Component | Runs where | Connects to |
|-----------|------------|-------------|
| Infrastructure | Docker | Published localhost ports |
| Spring services | Host JVM | `localhost:5433`, `9092`, `9000`, etc. |

### Alternative (post-MVP): Full containerization

Each service in Docker with compose `depends_on` and internal DNS names (`auth-service:8082`). Gateway targets container hostnames instead of `localhost`. Not required for MVP learning path.

---

## Data Persistence Volumes

| Compose volume | Contents |
|----------------|----------|
| `postgres_data` | All logical DBs on single Postgres instance |
| `minio_data` | Uploaded images and audio |

`docker compose down` retains volumes. `docker compose down -v` wipes local data.

---

## Actuator & Debug Endpoints

| Service | Exposed (MVP) | Notes |
|---------|---------------|-------|
| All services | `health`, `info` | Via `management.endpoints.web.exposure.include` |

Do not expose env or beans endpoints in shared environments.

---

## Local URLs Quick Reference

| What | URL |
|------|-----|
| API (gateway) | http://localhost:8081/api/v1 |
| Kafka UI | http://localhost:8080 |
| MinIO console | http://localhost:9001 |
| MinIO API | http://localhost:9000 |
| PostgreSQL | `localhost:5433` |

---

## Production Posture (Out of MVP)

| Concern | MVP | Production direction |
|---------|-----|----------------------|
| TLS | HTTP localhost | HTTPS termination at load balancer |
| Secrets | `.env` / compose | Vault, K8s secrets |
| Service discovery | Static localhost | K8s DNS or service mesh |
| Kafka | Single broker | Clustered, ACLs |
| Postgres | Single instance | Managed RDS / HA cluster |

---

## Implementation Checklist

- [ ] `infrastructure/docker-compose.yml` with `vibeus-*` container names
- [ ] Host port `5433` for Postgres (avoids conflict with local Postgres on 5432)
- [ ] Kafka dual listeners (`localhost:9092` + internal `29092`)
- [ ] Gateway routes point to `localhost:8082–8084`
- [ ] Auth Feign base URL `http://localhost:8083`
- [ ] Music MinIO endpoint `http://localhost:9000`
- [ ] User Kafka bootstrap `localhost:9092`
- [ ] Document startup order in infrastructure README

---

## Related Documents

| Document | Link |
|----------|------|
| Technology stack | [tech-stack.md](tech-stack.md) |
| Communication patterns | [communication-patterns.md](communication-patterns.md) |
| Service boundaries | [service-boundaries.md](service-boundaries.md) |
| Naming conventions | `conventions.md` (upcoming) |
| MVP scope | [mvp-scope.md](../requirements/mvp-scope.md) |

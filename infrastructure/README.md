# VibeUs Local Infrastructure

Docker Compose stack for local development: PostgreSQL, Redis, MinIO, Kafka, and Kafka UI.

Spring Boot services (`gateway-service`, `auth-service`, `user-service`, `music-service`) run on the host JVM and connect to these containers via published localhost ports.

---

## Prerequisites

- Docker Engine 24+ and Docker Compose v2
- Free host ports: `5433`, `6379`, `9000`, `9001`, `9092`, `8080`

---

## Quick start

```bash
cd infrastructure

# Optional: copy env overrides
cp .env.example .env

# Start all infrastructure containers
docker compose up -d

# Check status
docker compose ps
```

Stop (keep volumes):

```bash
docker compose down
```

Stop and wipe data:

```bash
docker compose down -v
```

---

## Containers and ports

| Container | Host port(s) | Purpose |
|-----------|--------------|---------|
| `vibeus-postgres` | `5433` | PostgreSQL 17 |
| `vibeus-redis` | `6379` | Redis 8 (provisioned; unused by app code in MVP) |
| `vibeus-minio` | `9000` (API), `9001` (console) | Object storage |
| `vibeus-kafka` | `9092` | Kafka broker (KRaft) |
| `vibeus-kafka-ui` | `8080` | Kafka topics UI |

---

## Databases

On **first** Postgres start (empty volume), `postgres/init-databases.sql` creates:

| Database | Used by |
|----------|---------|
| `vibeus_auth_db` | auth-service |
| `vibeus_user_db` | user-service |
| `vibeus_music_db` | music-service |

JDBC from host:

```
jdbc:postgresql://localhost:5433/vibeus_auth_db
jdbc:postgresql://localhost:5433/vibeus_user_db
jdbc:postgresql://localhost:5433/vibeus_music_db
```

Default credentials: `postgres` / `postgres` (local only).

If databases are missing because the volume was created before the init script existed:

```bash
docker compose exec postgres psql -U postgres -c "CREATE DATABASE vibeus_auth_db;"
docker compose exec postgres psql -U postgres -c "CREATE DATABASE vibeus_user_db;"
docker compose exec postgres psql -U postgres -c "CREATE DATABASE vibeus_music_db;"
```

Or reset volumes: `docker compose down -v && docker compose up -d`.

---

## Smoke checks

```bash
# Postgres
docker compose exec postgres pg_isready -U postgres

# Redis
docker compose exec redis redis-cli ping

# MinIO API
curl -sf http://localhost:9000/minio/health/live && echo OK

# Kafka UI
curl -sf -o /dev/null -w "%{http_code}\n" http://localhost:8080

# List databases
docker compose exec postgres psql -U postgres -c "\l"
```

Useful URLs:

| Resource | URL |
|----------|-----|
| Kafka UI | http://localhost:8080 |
| MinIO console | http://localhost:9001 |
| MinIO API | http://localhost:9000 |

MinIO console login (defaults): `admin` / `admin123`

---

## Application service ports

Start infrastructure first, then services (order recommended):

| Order | Service | Port | Health |
|-------|---------|------|--------|
| 1 | auth-service | 8082 | http://localhost:8082/actuator/health |
| 2 | user-service | 8083 | http://localhost:8083/actuator/health |
| 3 | music-service | 8084 | http://localhost:8084/actuator/health |
| 4 | gateway-service | 8081 | http://localhost:8081/actuator/health |

Client entry point: **http://localhost:8081**

Example (from each service directory):

```bash
./mvnw spring-boot:run
```

---

## Environment

Copy `.env.example` to `.env` for Compose credential overrides. Spring Boot services can also use the same keys when exported in your shell. See `.env.example` for JWT, MinIO, Kafka, and datasource notes.

Do not commit `.env` files with real secrets (root `.gitignore` already ignores them).

---

## Kafka notes

- Host applications use `localhost:9092`
- Kafka UI (container) uses the internal listener `vibeus-kafka:29092`
- Topic for registration events: `user.registered` (created when the producer first publishes or via Kafka UI)

---

## Troubleshooting

| Symptom | Likely fix |
|---------|------------|
| Port already in use | Stop the other process or change the published port in `docker-compose.yml` |
| Databases missing | Run the `CREATE DATABASE` commands above, or recreate volumes |
| Kafka UI cannot reach broker | Ensure `kafka` is up; UI uses `vibeus-kafka:29092` on the Compose network |
| Service cannot connect to Postgres | Confirm port `5433` (not `5432`) and that containers are `Up` |

Architecture detail: `docs/architecture/deployment-topology.md`

# Runbook

Operational guide for running VibeUs locally: start infrastructure, run
services, smoke-test, and troubleshoot common issues.

## Prerequisites

- Java 21 (`java -version`)
- Docker + Docker Compose
- Maven Wrapper (`./mvnw`, included per service)

## 1. Start infrastructure

```bash
cd infrastructure
cp .env.example .env        # adjust secrets if desired
docker compose up -d
docker compose ps           # all containers should be healthy
```

Infrastructure provides PostgreSQL (5433), Redis (6379), MinIO (9000/9001),
Kafka (9092), and Kafka UI (8080). Details: `infrastructure/README.md`.

## 2. Run services

Start in dependency order so downstream calls succeed:

```bash
cd auth-service   && ./mvnw spring-boot:run
cd user-service   && ./mvnw spring-boot:run
cd music-service  && ./mvnw spring-boot:run
cd gateway-service && ./mvnw spring-boot:run
```

Each service reads secrets from environment variables (see `.env.example`) and
falls back to local dev defaults. The JWT secret **must** match between
auth-service and gateway-service.

## 3. Smoke test through the gateway

```bash
# Register
curl -s -X POST http://localhost:8081/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","username":"jazzfan","password":"password123"}'

# Login (capture accessToken)
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"password123"}' | jq -r '.data.accessToken')

# Call a protected endpoint
curl -s http://localhost:8081/api/v1/users/me -H "Authorization: Bearer $TOKEN"
```

Verify the event: open Kafka UI (http://localhost:8080) and inspect the
`user.registered` topic.

## 4. Build & test

```bash
# Per service
cd <service> && ./mvnw verify
```

CI runs the same per-service build on push/PR (`.github/workflows/ci.yml`).

## 5. Containerized images

```bash
cd <service>
docker build -t vibeus/<service>:local .
```

Each image exposes an actuator health check.

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| `401 Unauthorized` on protected route | Missing/invalid/expired token, or gateway/auth JWT secrets differ | Re-login; ensure `JWT_SECRET` matches across auth and gateway |
| Service fails to connect to DB | Infra not up or wrong port | `docker compose ps`; DB is on host port **5433** |
| `Could not configure topics` / Kafka errors | Kafka not running | Start infra; check Kafka UI on 8080 |
| Upload returns `400 FILE_STORAGE_ERROR` | Unsupported type or too large | Use allowed types/sizes (images ≤5 MB, audio ≤50 MB) |
| Registration succeeds but no profile | user-service down when auth calls it | Start user-service before registering; auth rolls back on failure |
| Port already in use | Stale process | Stop the process or change the service port |

## Shutdown

```bash
cd infrastructure && docker compose down       # keep volumes
cd infrastructure && docker compose down -v     # wipe data
```

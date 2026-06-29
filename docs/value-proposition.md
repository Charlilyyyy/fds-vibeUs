# Core Value Proposition

## One-Line Summary

**VibeUs** gives backend developers a structured, runnable path from streaming-domain idea to a multi-service Java backend — with real infrastructure, clear boundaries, and documentation they can reuse in portfolios and interviews.

## The Gap We Fill

Most learning resources fall into one of two camps:

| Approach | Strength | Limitation |
|----------|----------|------------|
| **Single-app tutorials** | Easy to start | Never show gateways, per-service databases, or async events |
| **Production case studies** | Realistic scale | Too large to reproduce locally; opaque operational overhead |

VibeUs sits in the middle: **production-shaped architecture at laptop scale**. Builders get the mental model of a streaming backend without operating a global platform.

## Value for Builders

### 1. Cohesive domain, not disconnected exercises

Authentication, user profiles, music catalog, media storage, gateway routing, and Kafka events belong to one product story. Every service exists because the streaming domain needs it — not because a tutorial chapter needed another example.

### 2. Transferable stack and patterns

Technologies and patterns map directly to industry work:

- Spring Boot 3 and Java 21 for service implementation
- PostgreSQL with database-per-service isolation
- JWT-based stateless security and API gateway enforcement
- MinIO (S3-compatible) for binary assets
- Kafka for decoupled domain events
- Docker Compose for reproducible local infrastructure

Completing VibeUs means practicing skills that appear on job descriptions, not niche framework trivia.

### 3. Incremental clarity over big-bang complexity

The repo grows in deliberate steps — requirements, architecture, infrastructure, then individual services. Each increment is documented so builders always know *what exists*, *why it exists*, and *what comes next*.

### 4. Interview-ready narrative

Builders can articulate:

- Why auth and user profiles are separate services
- How the gateway validates tokens before forwarding requests
- When to use synchronous REST vs asynchronous messaging
- What belongs in MVP vs what is deferred (playlists, search, streaming)

That narrative is as valuable as the code itself.

### 5. Extensible foundation

MVP scope is intentionally bounded, but the architecture leaves room for playlist, search, and streaming services without rewriting the core. Builders who finish the base can continue learning on a familiar codebase.

## Value for the Hypothetical Product

From an end-user perspective (once a client app exists), VibeUs backends would deliver:

- **Trust** — secure registration, login, and profile management
- **Discovery** — browsable artists, albums, and tracks with consistent metadata
- **Rich media** — album art, artist images, and track assets stored reliably in object storage
- **Unified access** — one gateway URL for clients instead of juggling multiple service ports

These outcomes depend on the MVP capabilities defined in [features](features.md).

## Differentiators

What makes VibeUs distinct from generic “build a REST API” projects:

1. **Streaming-native domain** — relationships between artists, albums, tracks, and genres mirror real catalog models
2. **Security woven in** — not bolted on at the end; gateway and service-level auth are part of the core design
3. **Events as a first-class concern** — user registration and future catalog changes propagate through Kafka, teaching async integration early
4. **Documentation parity** — vision, users, requirements, and runbooks are peers to the code, not an afterthought

## What We Are Not Promising

Honest boundaries strengthen the proposition:

- Not a turnkey Spotify competitor or licensed music catalog
- Not a frontend or mobile player — API-first backend only
- Not production SRE coverage (K8s, multi-region, observability stacks) in the initial scope
- Not a certificate or course — self-directed building with written guidance

## Value Statement by Audience

| Audience | Core value |
|----------|------------|
| Backend developers | Hands-on microservices practice with industry-standard tools |
| Students / career switchers | Portfolio project with defensible architecture decisions |
| Mentors / instructors | Modular teaching spine with extension points for advanced topics |
| Future client developers | Clean REST APIs behind a single gateway (when services are complete) |

## Relationship to Other Documents

The [problem statement](problem-statement.md) frames the challenge. The [vision](vision.md) describes the destination. [Target users](target-users.md) define who benefits. [Features](features.md) list what will be built first. [Learning goals](learning-goals.md) spell out the skills builders will gain.

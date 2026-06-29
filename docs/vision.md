# Project Vision

## North Star

**VibeUs** is a hands-on backend platform that mirrors how real music streaming services are built — not as a single application, but as a set of cooperating services behind a unified API.

The long-term vision is a locally runnable system where a developer can register, authenticate, manage a personal profile, browse a music catalog (artists, albums, tracks), upload media assets, and observe cross-service workflows through events — all exposed through a single gateway entry point.

## Why This Matters

Consumer streaming products hide enormous engineering depth behind a simple play button. VibeUs makes that depth visible and approachable. Each service owns a clear slice of responsibility, so learners can reason about boundaries, trade-offs, and integration patterns the way backend teams do in production environments.

## Vision Pillars

### 1. Realistic architecture, approachable scale

Adopt microservice boundaries (gateway, authentication, users, music catalog) without pretending to operate at planetary scale. Optimize for clarity and teachability over premature optimization.

### 2. End-to-end backend completeness

Cover the full request path: public API → gateway security → domain services → persistence → object storage → asynchronous messaging. A working vertical slice beats a collection of disconnected tutorials.

### 3. Industry-aligned technology

Use the same categories of tools found in modern Java backends: Spring Boot, PostgreSQL, Redis, object storage, Kafka, and Docker. Skills transfer directly to professional work.

### 4. Incremental delivery

Grow the system in deliberate steps — from idea and requirements through infrastructure, individual services, gateway routing, and event-driven communication. Each increment should leave the repo in a coherent, documentable state.

### 5. Documentation as a first-class artifact

Every major decision (scope, architecture, APIs, runbooks) should be written down so the project can be understood, reproduced, and extended by someone who did not write the original code.

## What Success Looks Like

At the vision level, VibeUs succeeds when:

| Outcome | Description |
|--------|-------------|
| **Runnable MVP** | Auth, user profiles, music catalog, file uploads, and gateway routing work together on a developer machine |
| **Clear boundaries** | Each service has its own database and API; no shared tables across domains |
| **Secure by default** | Protected routes require valid tokens; public routes are explicitly defined |
| **Observable flows** | Registration and catalog changes can be traced through REST and message events |
| **Extensible foundation** | Playlists, search, and streaming can be added without rewriting the core |

## Guiding Principles

- **Prefer explicit over magical** — configuration, security rules, and service contracts should be easy to find and explain.
- **Fail clearly** — validation errors, auth failures, and missing resources return consistent, actionable API responses.
- **Defer complexity** — recommendation engines, global CDN, and Kubernetes orchestration are future concerns, not MVP blockers.
- **Learn by building** — reading about microservices is not enough; wiring them locally cements the mental model.

## Relationship to Other Documents

The [problem statement](problem-statement.md) explains *why* this project exists. This vision describes *where* it is headed. Upcoming documents define *who* it serves ([target users](target-users.md)), *what value* it delivers ([value proposition](value-proposition.md)), *which features* ship first ([features](features.md)), and *what skills* builders will practice ([learning goals](learning-goals.md)).

# Target Users

## Overview

VibeUs serves two overlapping audiences: **builders** who implement and run the backend, and **end users** who would interact with a finished streaming product. This document focuses on who the project is designed for, what each group needs, and how design choices should reflect those needs.

## Primary Audience: Backend Developers Learning Distributed Systems

### Profile

- Mid-level Java developers comfortable with Spring Boot basics who want to move beyond single-service tutorials
- Engineers preparing for roles that involve microservices, API gateways, JWT auth, or event-driven design
- Developers who learn best by building something cohesive rather than isolated code samples

### Goals

- Understand how to split a domain into services with separate databases
- Practice securing APIs with stateless authentication and gateway-level validation
- Integrate PostgreSQL, Redis, object storage, and Kafka in a realistic local setup
- Gain confidence reading and extending production-style Spring Boot codebases

### Pain Points

- Tutorial projects stop at CRUD in one app and never show service-to-service calls
- Infrastructure setup (Docker, brokers, buckets) feels overwhelming without a guided path
- Hard to see how auth, users, and catalog fit together in a real request flow

### What They Need from VibeUs

- Clear service boundaries and documented ports/routes
- Runnable docker-compose infrastructure with minimal manual steps
- Consistent API patterns (responses, errors, validation) across services
- Incremental milestones so the repo never feels like an unfinished monolith dump

---

## Secondary Audience: Students and Career Switchers

### Profile

- Computer science students completing a capstone or portfolio project
- Developers transitioning from frontend or scripting into backend Java roles
- Self-taught programmers who have completed introductory Spring courses

### Goals

- Build a portfolio piece that demonstrates architecture thinking, not just syntax
- Explain trade-offs in interviews (why separate databases, why Kafka, why a gateway)
- Follow a structured roadmap from idea to working system

### Pain Points

- Unclear where to start when the goal is “build something like Spotify”
- Fear of over-engineering or under-engineering without a defined MVP
- Limited time; need scope that fits evenings and weekends

### What They Need from VibeUs

- Written requirements and scope documents to avoid endless feature creep
- A must-have vs nice-to-have feature list they can cite in README and interviews
- Documentation that doubles as study notes for system design concepts

---

## Tertiary Audience: Technical Mentors and Instructors

### Profile

- Bootcamp instructors or senior engineers guiding juniors through a multi-service project
- Team leads evaluating a reference architecture for internal training

### Goals

- Use a single repo as a teaching spine for microservices workshops
- Point learners at concrete examples (JWT filter, Feign client, Kafka producer)

### What They Need from VibeUs

- Modular services that can be taught one at a time
- ADR-style or runbook documentation for classroom discussion
- Extension hooks (playlist, search, streaming scaffolds) for advanced assignments

---

## Hypothetical End Users (Product Perspective)

Although VibeUs is backend-focused and does not ship a consumer app in the MVP, the **imagined product users** inform API design:

| Persona | Description | Core needs |
|--------|-------------|------------|
| **Listener** | Casual music fan browsing and playing tracks | Fast catalog search, reliable playback URLs, personalized playlists (future) |
| **Curator** | User who builds and shares playlists | CRUD on playlists, metadata consistency across devices (future) |
| **Contributor** | Artist or label rep uploading catalog assets | Secure uploads, rich metadata (artist, album, track), image and audio storage |
| **Administrator** | Internal operator managing catalog quality | Audit trails, duplicate detection, moderation hooks (future) |

MVP backend work prioritizes **Listener** and **Contributor** flows at the API layer: authentication, profiles, catalog CRUD, and media upload. Playlist and advanced personalization personas are documented for later iterations.

---

## User Needs Summary

| Audience | Priority | Success signal |
|----------|----------|----------------|
| Backend developers | High | Can run full stack locally and trace a request through gateway → service → DB |
| Students / career switchers | High | Can explain architecture decisions and demo APIs in a portfolio |
| Mentors / instructors | Medium | Can assign incremental service ownership without repo chaos |
| Hypothetical listeners & contributors | Medium (design driver) | APIs support register/login, profile, catalog, and upload stories |

## Relationship to Other Documents

The [vision](vision.md) states where VibeUs is headed. The [value proposition](value-proposition.md) explains what unique benefit the project offers these users. [Features](features.md) translate user needs into prioritized capabilities. [Learning goals](learning-goals.md) map builder outcomes to concrete technical skills.

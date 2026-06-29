# Problem Statement

## Context

Music streaming has become the dominant way people discover and listen to music. Platforms like Spotify, Apple Music, and YouTube Music serve billions of streams daily by combining large catalogs, personalized recommendations, and seamless playback across devices. Behind these experiences lies complex distributed software: user identity, catalog management, media storage, playback orchestration, and real-time analytics.

## The Problem

Building a production-grade music streaming platform from scratch is difficult for several reasons:

1. **Scale and separation of concerns** — Authentication, user profiles, catalog metadata, binary media assets, playlists, search, and streaming each have different data models, consistency requirements, and scaling profiles. A monolithic design becomes hard to evolve; a microservice architecture requires deliberate boundaries and integration patterns.

2. **Media handling** — Tracks, album art, and artist images are not ordinary CRUD data. They require object storage, content-type validation, size limits, and URLs that downstream clients can use reliably.

3. **Security and identity** — Users must register, authenticate, and access only their own data. Services must trust identity claims without sharing databases, which pushes teams toward stateless tokens and gateway-level validation.

4. **Asynchronous workflows** — Events such as user registration, catalog updates, or playlist changes often need to propagate to other parts of the system without tight coupling. Message brokers and event contracts add design and operational complexity.

5. **Local development friction** — A realistic stack (relational databases per service, cache, object storage, message broker, API gateway) is heavy to run on a laptop without containerized infrastructure and clear documentation.

## What We Are Building Toward

**VibeUs** is a learning-oriented backend inspired by consumer music streaming products. It is not meant to compete with Spotify at global scale. Instead, it provides a structured path to implement the core backend concerns of a streaming service: secure auth, user profiles, a browsable music catalog, file uploads, an API gateway, and event-driven communication between services.

## Success Criteria (Problem Framing)

We will consider the problem well-scoped when:

- A developer can explain why each major capability (auth, users, catalog, storage, gateway, events) exists as its own concern.
- The MVP clearly separates **must-have** backend capabilities from **nice-to-have** features deferred to later iterations.
- The project remains runnable locally with documented infrastructure and service boundaries.

## Out of Scope (For Now)

The following are acknowledged industry requirements but are intentionally excluded from the initial problem scope:

- Client applications (mobile, web, desktop players)
- Large-scale recommendation engines and ML pipelines
- Global CDN delivery and sub-second streaming at millions of concurrent listeners
- Licensing, royalty, and rights-management workflows

This document establishes *why* the project exists. Companion documents cover vision, users, value proposition, feature prioritization, and learning goals.

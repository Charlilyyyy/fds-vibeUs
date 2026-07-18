# 0005 — MinIO for media storage

- Status: Accepted
- Date: 2026-07-19

## Context

The catalog needs to store binary assets (artist images, album covers, track
audio). Storing binaries in the database is a poor fit.

## Decision

Use MinIO (S3-compatible object storage) for media. music-service uploads to
per-type buckets (`vibeus-artists`, `vibeus-albums`, `vibeus-tracks`), validates
content type and size, and stores only the resulting URL in the database.

## Consequences

- **Pros**: object storage is the right tool for binaries; S3-compatible so it
  maps cleanly to cloud storage later; DB stays small.
- **Cons**: another system to run; must handle upload failures and orphaned
  objects (old files are deleted on replacement).
- Buckets are auto-created on startup; credentials come from environment
  variables.

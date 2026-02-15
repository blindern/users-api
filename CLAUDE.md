# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development

```bash
./gradlew runShadow  # Run locally (needs LDAP VPN access + overrides.properties)
./gradlew check      # Lint (ktlint) + test (Spek2)
```

JDK 25+ required. Tests use snapshot testing (snapshots in `src/test/resources/__snapshot__/`).

## Architecture

Kotlin REST API using **http4k** on Jetty (port 8000) that wraps an LDAP user/group directory.

**Request flow:** HTTP → GZip filter → logging filter → auth filter → route handler → DataProvider (Caffeine cache, 5min TTL) → `ldap/Ldap.kt` → LDAP server

- `AuthFilter.kt` — HMAC-SHA256 and Bearer token auth (both use same key)
- `ldap/` — LDAP operations, models (`User.kt`, `Group.kt`), recursive group membership expansion (`MemberExpander.kt`)
- `api/` — endpoint handlers, each injected with DataProvider and/or Ldap

Legacy endpoints (`/users`, `/group/{name}`, etc.) use a JSON envelope via `WrappedResponse` filter. v2 endpoints under `/v2/` skip the envelope.

## Configuration

Layered via konfig: system properties → env vars → `overrides.properties` (gitignored) → `defaults.properties`. Key settings: `users-api.ldap-admin-password`, `users-api.hmac-key`, `users-api.enforce-auth`.

## CI/CD

GitHub Actions on master: check → Docker build → Artifact Registry → deploy. All Actions pinned to commit SHAs.

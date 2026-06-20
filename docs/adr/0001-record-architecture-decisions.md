# ADR-0001: Record architecture decisions

- **Status:** Accepted
- **Date:** 2026-06-20

## Context

Vela is a long-lived, multi-app program. Decisions made early (language targets, DI, navigation,
persistence, licensing) have wide blast radius. We need a durable, low-ceremony record of *why*
each choice was made so future contributors don't re-litigate or accidentally violate constraints.

## Decision

We keep lightweight **Architecture Decision Records** in `docs/adr/`, one Markdown file per
decision, numbered sequentially. Each ADR states Status, Context, Decision, and Consequences.

## Baseline decisions captured at project start

1. **Targets:** Android + iOS + Desktop via Kotlin Multiplatform + Compose Multiplatform.
2. **License:** Apache-2.0, built clean-room (no GPL source copied — see clean-room-policy.md).
3. **Architecture:** Clean Architecture + MVI/UDF, fixed per-app module shape.
4. **Core libraries:** Koin (DI), Jetpack Navigation Compose (multiplatform), Room KMP (DB),
   DataStore (prefs), Coil 3 (images), kotlinx (coroutines/serialization/datetime).
5. **Build:** Gradle convention plugins (`build-logic`) + a single version catalog.
6. **First app:** Calculator (smallest full-stack proof across all three platforms).

## Consequences

- New significant choices get a new ADR. Superseded ADRs are marked, not deleted.
- The version catalog + convention plugins are the enforcement point for (4) and (5).

# Vela Architecture

## Goals

- **One look, many apps.** A shared design system and core infrastructure make every Vela app feel
  like one product.
- **Write once, run on Android / iOS / Desktop.** Maximize shared code; isolate platform specifics
  behind `expect/actual` seams.
- **Testable by construction.** Pure domain logic, deterministic state machines, injected dispatchers.
- **Add an app in an afternoon.** Convention plugins + a fixed module shape make new apps cheap.

## Module graph

```
                    ┌─────────────────────────┐
                    │   core/designsystem      │  brand tokens, VelaTheme, components
                    └────────────┬─────────────┘
                                 │
   core/common ── core/domain ── core/ui ── core/navigation
        │             │            │            │
   core/data ── core/database ── core/datastore ── core/permissions
        └──────────────┬───────────────────────────┘
                       │  (all the above = "core")
        ┌──────────────┴───────────────┐
        ▼                               ▼
  apps/<app>/domain  →  data  →  presentation  →  ui  →  {androidApp, iosApp, desktopApp}
```

Dependencies point **inward**: `ui → presentation → domain`; `data → domain`. The `domain` layer
depends on nothing but `core/common` and Kotlin stdlib.

## Layers (per app)

| Layer | Contains | Depends on | Platform code? |
|-------|----------|-----------|----------------|
| `domain` | entities, value types, repository **interfaces**, use cases | `core/domain`, `core/common` | none (pure Kotlin) |
| `data` | repository impls, Room DAOs/entities, DataStore, mappers | `domain`, `core/data`, `core/database` | `expect/actual` drivers |
| `presentation` | MVI `Store` (State + Intent + Effect), reducers | `domain` | none |
| `ui` | Compose screens & app-specific components | `presentation`, `core/designsystem`, `core/ui` | none |
| `androidApp` / `desktopApp` / `iosApp` | entry point, DI bootstrap, manifest/Info.plist | `ui` + platform `data` | platform-specific |

## Unidirectional data flow (MVI)

```
 UI ──Intent──▶ Store ──▶ reduce(State, Intent) ──▶ new State ──StateFlow──▶ UI
                  │
                  └── use case (suspend) ──▶ Effect (one-shot) ──Channel──▶ UI
```

- `State` is an immutable `data class` rendered by Compose.
- `Intent` is a sealed interface of user actions.
- `Effect` is a one-shot event (navigation, snackbar) delivered via a `Channel`.
- The `Store` is hosted in a multiplatform `androidx.lifecycle.ViewModel`.

See `core/common`'s `MviStore` base and the Calculator's `CalculatorStore` for the reference impl.

## Dependency injection (Koin)

Each layer exposes a Koin `Module`:

```kotlin
val calculatorModule = module {
    includes(calculatorDomainModule, calculatorDataModule)
    viewModel { CalculatorStore(get(), get()) }
}
```

Platform drivers (DB builder, settings path, file access) are provided by an `expect fun
platformModule(): Module` with an `actual` per target.

## Navigation

Type-safe Jetpack Navigation Compose (multiplatform). Destinations are `@Serializable` objects/
classes. Apps declare routes behind `core/navigation` contracts so screens stay decoupled from the
nav library. Deep links and predictive back are supported.

## Persistence

- **Structured data:** Room KMP. `@Database`/`@Dao`/`@Entity` live in common code; the
  `RoomDatabase.Builder` is created per platform via `expect/actual` (Android context, desktop file
  path, iOS documents dir).
- **Preferences:** DataStore (multiplatform), wrapped by typed `core/datastore` settings.

## Cross-platform seams

Anything platform-specific is an `expect`/`actual` pair in a `core/*` module:
`permissions`, `database` driver, `datastore` path, file access, notifications/alarms, media access.
Apps depend on the common interface only.

## Testing

- **Unit:** reducers + use cases (pure) with `kotlin-test` + Turbine for flows.
- **UI:** Compose UI tests in `commonTest` where possible.
- **Screenshot:** Roborazzi against the design-system catalog.
- **E2E:** Maestro flows per app.
- **Quality gates:** detekt, Spotless (license headers), Kover coverage floor.

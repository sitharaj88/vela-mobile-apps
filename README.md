<div align="center">

# ✦ Vela

**Private by design. Beautiful by default.**

A suite of minimalist, ad-free, privacy-first apps — rebuilt for Android, iOS, and Desktop
with Kotlin Multiplatform & Compose Multiplatform.

[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.0-blueviolet.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/compose--multiplatform-1.8.0-green.svg)](https://www.jetbrains.com/compose-multiplatform/)

</div>

---

## What is Vela?

Vela is a clean-room reimagining of the beloved [SimpleMobileTools](https://github.com/SimpleMobileTools)
suite (now continued by the community as [Fossify](https://github.com/FossifyOrg)) — rebuilt from
scratch as a cross-platform, Apache-2.0 licensed product with a single cohesive design language.

No ads. No trackers. No nonsense. Just fast, private, beautiful utilities.

> **Clean-room notice:** Vela copies **no** GPL-3.0 source from SimpleMobileTools/Fossify. Those
> projects are used only as a *functional reference*. See [docs/clean-room-policy.md](docs/clean-room-policy.md).

## Platforms

| Platform | Status | Entry point |
|----------|--------|-------------|
| Android  | ✅ primary | `apps/<app>/androidApp` |
| Desktop (JVM) | ✅ supported | `apps/<app>/desktopApp` |
| iOS      | ✅ supported | `apps/<app>/iosApp` (Xcode) |

## The suite

Apps ship in waves. Portability differs by how deeply an app touches platform system roles:

- **Tier A — fully cross-platform:** Calculator · Notes · Draw · Clock · Calendar
- **Tier B — mobile-rich:** Gallery · File Manager · Music Player · Voice Recorder · Flashlight
- **Tier C — Android system roles:** Phone · Messages · Contacts · Camera · Keyboard · Launcher

**First reference app:** Calculator (validates the whole stack on all three platforms).

## Architecture

```
core/*                     shared design system + infrastructure (used by every app)
apps/<app>/domain          pure-Kotlin entities + use cases (no framework)
apps/<app>/data            repositories, Room, mappers
apps/<app>/presentation    MVI stores (StateFlow + Effects) on KMP ViewModel
apps/<app>/ui              Compose screens
apps/<app>/{androidApp, iosApp, desktopApp}   thin platform entry points
```

- **Pattern:** Clean Architecture + unidirectional data flow (MVI).
- **DI:** Koin · **Navigation:** Jetpack Navigation Compose (multiplatform) · **DB:** Room KMP ·
  **Prefs:** DataStore · **Images:** Coil 3.
- **Design system:** `core/designsystem` (Material 3 Expressive base, brand tokens, per-app accents).

See [docs/architecture.md](docs/architecture.md) and [docs/brand.md](docs/brand.md).

## Getting started

```bash
# 1. Materialize the Gradle wrapper (first time only — requires a local Gradle ≥ 8.13)
gradle wrapper --gradle-version 8.13

# 2. Build everything + run quality gates
./gradlew build detekt koverVerify

# 3. Run the design system catalog (Storybook)
./gradlew :core:designsystem-catalog:run            # Desktop

# 4. Run the Calculator
./gradlew :apps:calculator:desktopApp:run           # Desktop
./gradlew :apps:calculator:androidApp:installDebug  # Android (device/emulator)
# iOS: open apps/calculator/iosApp in Xcode and run.
```

**Prerequisites:** JDK 17, Android SDK (API 35), and — for iOS — macOS with Xcode.

## License

Apache License 2.0 — see [LICENSE](LICENSE) and [NOTICE](NOTICE).

# Vela App-Authoring Guide

How to add a new app to the Vela monorepo so it compiles and matches the four shipped apps
(Calculator, Notes, Clock, Draw). **Read `apps/notes/**` as the canonical reference** (Room + list +
editor); `apps/clock/**` for in-memory + tabs; `apps/draw/**` for canvas/gestures.

## Module shape

Every app lives under `apps/<app>/` with this fixed shape:

```
apps/<app>/domain         pure Kotlin: models, repository interfaces, use cases
apps/<app>/data           (only if it persists / touches platform) Room, expect/actual, DI
apps/<app>/presentation   MVI stores (State + Intent + Effect) over MviStore
apps/<app>/ui             Compose screens, DI module, InitKoin, iOS controller
apps/<app>/androidApp     Android Application + MainActivity + manifest + res
apps/<app>/desktopApp     desktop main()
apps/<app>/iosApp         (optional) Swift wrapper — boilerplate, copy from apps/notes/iosApp
```

Package root: `com.vela.apps.<app>`. Namespaces are derived automatically from the Gradle path by
the convention plugin — **do not set `namespace` in library modules**.

## CRITICAL build.gradle.kts rules (these caused real failures)

- Module build files apply base plugins by **versionless `id("...")`** (they are already on the
  classpath via build-logic). **Never use `alias(...)` for kotlin/android/compose plugins in a
  module** — it throws "already on the classpath with an unknown version".
- Convention plugins (`vela.kmp.library`, `vela.compose`, `vela.android.application`) are
  **configure-only**; the module must apply the real plugins itself.

### KMP library module (domain / presentation; data without compose)
```kotlin
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("vela.kmp.library")
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.apps.<app>.domain)   // as needed
            api(projects.core.common)         // presentation needs this for MviStore
            implementation(libs.koin.core)    // if it declares a Koin module
        }
    }
}
```

### Compose UI module (`ui`)
```kotlin
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("vela.kmp.library")
    id("vela.compose")
}
kotlin {
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework { baseName = "<App>UI"; isStatic = true }
    }
    sourceSets {
        commonMain.dependencies {
            api(projects.apps.<app>.presentation)
            implementation(projects.apps.<app>.data)        // if present
            implementation(projects.core.designsystem)
            implementation(projects.core.common)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            // add libs.androidx.navigation.compose + libs.kotlinx.serialization.json
            //   (+ plugin id("org.jetbrains.kotlin.plugin.serialization")) only if you use NavHost
        }
    }
}
```

### Android app module (`androidApp`)
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("vela.android.application")
}
android {
    namespace = "com.vela.apps.<app>.android"
    defaultConfig { applicationId = "com.vela.<app>" }
}
dependencies {
    implementation(projects.apps.<app>.ui)
    implementation(compose.runtime); implementation(compose.foundation); implementation(compose.material3)
    implementation(libs.androidx.activity.compose); implementation(libs.androidx.core.ktx)
    implementation(libs.koin.android); implementation(libs.koin.compose)
}
```
Also needs: `proguard-rules.pro` (keep `org.koin.**`), `src/main/AndroidManifest.xml`,
`res/values/strings.xml` (app_name), `res/values/themes.xml` (`Theme.Vela` parent
`@android:style/Theme.Material.NoActionBar`), `res/drawable/ic_vela.xml` (copy from another app,
recolor to the accent), `VelaXxxApp : Application` (calls `initXxxKoin { }`), `MainActivity`.

### Desktop app module (`desktopApp`) — plain JVM
```kotlin
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}
dependencies {
    implementation(projects.apps.<app>.ui)
    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.koin.core)
}
compose.desktop { application { mainClass = "com.vela.apps.<app>.desktop.MainKt"
    nativeDistributions { targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
        packageName = "Vela<App>"; packageVersion = "1.0.0" } } }
```

## Architecture / code conventions

- **MVI store**: extend `com.vela.core.common.MviStore<State, Intent, Effect>`. State is an immutable
  data class; Intent/Effect are sealed interfaces. Use `setState { copy(...) }`, `emitEffect(...)`,
  `currentState`, and `viewModelScope` (from `androidx.lifecycle.viewModelScope`). If an app has no
  one-shot events, use `Nothing` for Effect.
- **DI**: presentation provides use cases via `factoryOf(::UseCase)`. **ViewModels are bound in the
  `ui` module** with `org.koin.core.module.dsl.viewModelOf` (NOT `org.koin.compose.viewmodel.dsl`,
  which is deprecated). Parameterized stores: `viewModel { (id: Long) -> Store(id, get()) }`.
  Retrieve in Compose with `org.koin.compose.viewmodel.koinViewModel` (use
  `koinViewModel { parametersOf(x) }` for params).
- **Koin start**: `ui/di/InitKoin.kt` exposes `fun initXxxKoin(appDeclaration: KoinAppDeclaration = {})`
  and `fun doInitXxxKoin()` (no-arg, for Swift). Android passes `androidContext(this)` ONLY if the
  app uses Room/Context.
- **DispatcherProvider** / `platformIoDispatcher()` live in `core/common`.
- **Design system**: wrap screens in `VelaScaffold(title=...)`; use `VelaButton`, `VelaCard`,
  `VelaEmptyState`, `LocalVelaTokens.current.spacing.*`. Theme via
  `VelaTheme(accent = VelaAccent.<Accent>, themeMode, dynamicColor = true)`. Never hardcode colors —
  read `MaterialTheme.colorScheme`.
- **State collection**: `state by store.state.collectAsStateWithLifecycle()`
  (`androidx.lifecycle.compose`).

## Room (when persisting) — copy apps/notes/data exactly

- `data/build.gradle.kts` adds plugins `id("androidx.room")` + `id("com.google.devtools.ksp")`,
  deps `libs.androidx.room.runtime` + `libs.androidx.sqlite.bundled`, a `room { schemaDirectory(...) }`
  block, and per-target KSP: `add("kspAndroid"/"kspDesktop"/"kspIosX64"/"kspIosArm64"/"kspIosSimulatorArm64", libs.androidx.room.compiler)`.
- `@Database(... exportSchema = false)` + `@ConstructedBy(XxxDatabaseConstructor::class)`, and an
  `expect object XxxDatabaseConstructor : RoomDatabaseConstructor<XxxDatabase>`.
- Platform DB builders via `expect fun xxxPlatformDatabaseModule(): Module` with android/desktop/ios
  actuals (Context / `user.home/.vela` / `NSDocumentDirectory`). Build with
  `.setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(platformIoDispatcher()).build()`.
- Entity stores time as epoch millis (`Long`); map to/from `kotlinx.datetime.Instant` in the repo.

## expect/actual platform capabilities (torch, audio, files)

- Put the platform interface in `commonMain` (e.g. `interface Torch { ... }`) and provide it through
  an `expect fun xxxPlatformModule(): Module` whose actuals construct the platform implementation.
- Implement **Android and Desktop fully** (these are CI-verified on this machine). For **iOS**, write
  a best-effort actual; if an API is uncertain, provide a compiling stub clearly marked `// TODO(ios)`
  rather than guessing — iOS only compiles on macOS.
- Desktop should degrade gracefully (e.g. torch `isAvailable() = false`).

## Quality gates (detekt runs on every module)

- **No `String.format` in commonMain** — it's JVM-only. Pad manually (`toString().padStart(2,'0')`).
- Max line length **120** — wrap long calls/initializers.
- Top-level `const` may be SCREAMING_SNAKE_CASE. `MagicNumber` and `MatchingDeclarationName` are
  disabled, so grouping small declarations per file is fine.
- Inherently complex functions (parsers, scanners) may carry a justified
  `@Suppress("CyclomaticComplexMethod", ...)`.
- Every `.kt`/`.kts`/`.xml`/`.swift` file starts with the Apache header:
  ```
  /*
   * Copyright 2026 The Vela Authors
   * SPDX-License-Identifier: Apache-2.0
   */
  ```
  (XML/Swift use the appropriate comment syntax.)

## Hard constraints for parallel authoring

- **Only create files under your `apps/<app>/` subtree.** Do NOT edit `settings.gradle.kts`,
  `gradle/libs.versions.toml`, anything under `core/`, `build-logic/`, or other apps — the integrator
  handles shared files.
- **Do NOT run Gradle, builds, or git.** The integrator compiles everything centrally.
- All listed catalog aliases (`libs.koin.core`, `libs.androidx.room.runtime`, etc.) already exist —
  do not add new dependencies to the version catalog. If you genuinely need one, note it in your
  final summary instead of editing the catalog.

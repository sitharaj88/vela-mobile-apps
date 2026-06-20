/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
@file:Suppress("UnstableApiUsage")

rootProject.name = "vela"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        // JetBrains Compose / multiplatform AndroidX artifacts
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// Modules are added to this file phase-by-phase as they are actually scaffolded, so the build
// stays green at every step. Future core modules (ui, database, datastore, navigation,
// permissions, testing) and additional apps are added when their phase lands.

// ---- Core (shared by all apps) ----
include(":core:common")
include(":core:designsystem")
include(":core:designsystem-catalog")

// ---- Apps ----
// Calculator (first reference app)
include(":apps:calculator:domain")
include(":apps:calculator:data")
include(":apps:calculator:presentation")
include(":apps:calculator:ui")
include(":apps:calculator:androidApp")
include(":apps:calculator:desktopApp")
// NOTE: iOS entry point (:apps:calculator:iosApp) is an Xcode project, not a Gradle module.

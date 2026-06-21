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

// Notes (second app)
include(":apps:notes:domain")
include(":apps:notes:data")
include(":apps:notes:presentation")
include(":apps:notes:ui")
include(":apps:notes:androidApp")
include(":apps:notes:desktopApp")

// Clock (third app) — now persists alarms via Room
include(":apps:clock:domain")
include(":apps:clock:data")
include(":apps:clock:presentation")
include(":apps:clock:ui")
include(":apps:clock:androidApp")
include(":apps:clock:desktopApp")

// Draw (fourth app) — Compose canvas; data module added for cross-platform PNG export
include(":apps:draw:domain")
include(":apps:draw:data")
include(":apps:draw:presentation")
include(":apps:draw:ui")
include(":apps:draw:androidApp")
include(":apps:draw:desktopApp")

// Flashlight (Tier B — expect/actual torch capability)
include(":apps:flashlight:domain")
include(":apps:flashlight:data")
include(":apps:flashlight:presentation")
include(":apps:flashlight:ui")
include(":apps:flashlight:androidApp")
include(":apps:flashlight:desktopApp")

// Voice Recorder (Tier B — Room recordings + audio capture)
include(":apps:voicerecorder:domain")
include(":apps:voicerecorder:data")
include(":apps:voicerecorder:presentation")
include(":apps:voicerecorder:ui")
include(":apps:voicerecorder:androidApp")
include(":apps:voicerecorder:desktopApp")

// File Manager (Tier B — expect/actual filesystem)
include(":apps:filemanager:domain")
include(":apps:filemanager:data")
include(":apps:filemanager:presentation")
include(":apps:filemanager:ui")
include(":apps:filemanager:androidApp")
include(":apps:filemanager:desktopApp")

// Calendar (Room-backed events + month grid)
include(":apps:calendar:domain")
include(":apps:calendar:data")
include(":apps:calendar:presentation")
include(":apps:calendar:ui")
include(":apps:calendar:androidApp")
include(":apps:calendar:desktopApp")

// Gallery (Tier B — expect/actual media access + Coil grid)
include(":apps:gallery:domain")
include(":apps:gallery:data")
include(":apps:gallery:presentation")
include(":apps:gallery:ui")
include(":apps:gallery:androidApp")
include(":apps:gallery:desktopApp")

// Music Player (Tier B — expect/actual media library + audio playback)
include(":apps:musicplayer:domain")
include(":apps:musicplayer:data")
include(":apps:musicplayer:presentation")
include(":apps:musicplayer:ui")
include(":apps:musicplayer:androidApp")
include(":apps:musicplayer:desktopApp")

// Contacts (Android-primary — ContactsContract read)
include(":apps:contacts:domain")
include(":apps:contacts:data")
include(":apps:contacts:presentation")
include(":apps:contacts:ui")
include(":apps:contacts:androidApp")
include(":apps:contacts:desktopApp")

// App Launcher (Android-primary — PackageManager)
include(":apps:applauncher:domain")
include(":apps:applauncher:data")
include(":apps:applauncher:presentation")
include(":apps:applauncher:ui")
include(":apps:applauncher:androidApp")
include(":apps:applauncher:desktopApp")

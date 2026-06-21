/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.domain

/**
 * Platform gateway to the set of launchable apps.
 *
 * - **Android** enumerates launchable activities via `PackageManager`.
 * - **Desktop** enumerates real installed applications per-OS (Start Menu shortcuts on Windows,
 *   `.app` bundles on macOS, `.desktop` entries on Linux) and launches them.
 * - **iOS** cannot enumerate or launch other apps (sandbox), so it reports an empty list and
 *   treats [launch] as a no-op; the UI then shows an "unsupported" state.
 */
interface InstalledApps {

    /** Whether this platform can actually enumerate and launch apps. */
    val isSupported: Boolean

    /** All launchable apps, de-duplicated. Sorting is left to the caller. Empty when unsupported. */
    suspend fun list(): List<AppEntry>

    /** Launches the app identified by [id]; no-op when unsupported or the id is unknown. */
    fun launch(id: String)
}

/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.data

import com.vela.apps.applauncher.domain.AppEntry
import com.vela.apps.applauncher.domain.InstalledApps

/**
 * Fallback [InstalledApps] for platforms that cannot enumerate or launch installed apps (iOS).
 * It reports no apps and ignores launch requests so the UI shows the unsupported state.
 */
class UnsupportedInstalledApps : InstalledApps {
    override val isSupported: Boolean = false
    override suspend fun list(): List<AppEntry> = emptyList()
    override fun launch(id: String) = Unit
}

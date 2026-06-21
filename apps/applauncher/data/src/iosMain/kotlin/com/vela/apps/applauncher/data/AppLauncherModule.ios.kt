/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.data

import com.vela.apps.applauncher.domain.InstalledApps
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS sandboxes app discovery and launching, so v1 binds the unsupported implementation.
 * TODO(ios): a future version could expose a curated set via `LSApplicationWorkspace` (private API)
 *  or URL schemes — out of scope for the cross-platform v1.
 */
actual fun appLauncherPlatformModule(): Module = module {
    single<InstalledApps> { UnsupportedInstalledApps() }
}

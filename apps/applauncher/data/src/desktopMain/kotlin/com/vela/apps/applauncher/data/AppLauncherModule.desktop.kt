/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.data

import com.vela.apps.applauncher.domain.InstalledApps
import org.koin.core.module.Module
import org.koin.dsl.module

/** Desktop binds the real [InstalledApps] that enumerates and launches installed OS applications. */
actual fun appLauncherPlatformModule(): Module = module {
    single<InstalledApps> { DesktopInstalledApps() }
}

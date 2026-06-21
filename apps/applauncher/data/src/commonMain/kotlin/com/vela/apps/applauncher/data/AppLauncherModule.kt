/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.data

import org.koin.core.module.Module

/**
 * Each platform binds a [com.vela.apps.applauncher.domain.InstalledApps] here. Only Android can
 * enumerate launchable apps; Desktop and iOS bind an unsupported implementation that lists nothing.
 */
expect fun appLauncherPlatformModule(): Module

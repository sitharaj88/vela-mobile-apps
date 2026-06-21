/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.ui.di

import com.vela.apps.applauncher.data.di.appLauncherDataModule
import com.vela.apps.applauncher.presentation.AppListStore
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Single entry point platforms use to wire the whole App Launcher app. */
val appLauncherModule = module {
    includes(appLauncherDataModule)
    viewModelOf(::AppListStore)
}

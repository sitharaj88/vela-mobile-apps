/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.data.di

import com.vela.apps.applauncher.data.RoomFavoritesRepository
import com.vela.apps.applauncher.data.appLauncherPlatformModule
import com.vela.apps.applauncher.data.local.AppLauncherDatabase
import com.vela.apps.applauncher.data.local.appLauncherPlatformDatabaseModule
import com.vela.apps.applauncher.data.local.buildAppLauncherDatabase
import com.vela.apps.applauncher.domain.FavoritesRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Wires the whole data layer: the platform [com.vela.apps.applauncher.domain.InstalledApps] plus
 * the Room favorites database, DAO, and repository binding.
 */
val appLauncherDataModule = module {
    includes(appLauncherPlatformModule())
    includes(appLauncherPlatformDatabaseModule())
    single { buildAppLauncherDatabase(get()) }
    single { get<AppLauncherDatabase>().favoriteDao() }
    singleOf(::RoomFavoritesRepository) bind FavoritesRepository::class
}

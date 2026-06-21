/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.data

import com.vela.apps.gallery.data.local.GalleryDatabase
import com.vela.apps.gallery.data.local.buildGalleryDatabase
import com.vela.apps.gallery.data.local.galleryPlatformDatabaseModule
import com.vela.apps.gallery.domain.FavoritesRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Each platform binds a [com.vela.apps.gallery.domain.MediaSource]: Android queries `MediaStore`,
 * desktop scans `~/Pictures` and `~/Videos`, iOS reads the photo library (best-effort).
 */
expect fun galleryPlatformModule(): Module

/**
 * Full data wiring: the platform media source plus the Room-backed favorites store. The `ui` DI
 * module includes this single entry point.
 */
fun galleryDataModule(): Module = module {
    includes(galleryPlatformModule())
    includes(galleryPlatformDatabaseModule())
    single { buildGalleryDatabase(get()) }
    single { get<GalleryDatabase>().favoriteDao() }
    singleOf(::RoomFavoritesRepository) bind FavoritesRepository::class
}

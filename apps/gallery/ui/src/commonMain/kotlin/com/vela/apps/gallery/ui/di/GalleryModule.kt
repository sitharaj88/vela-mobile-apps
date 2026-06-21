/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.ui.di

import com.vela.apps.gallery.data.galleryDataModule
import com.vela.apps.gallery.presentation.GalleryStore
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Single entry point platforms use to wire the whole Gallery app. */
val galleryModule = module {
    includes(galleryDataModule())
    viewModelOf(::GalleryStore)
}

/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.ui.di

import com.vela.apps.musicplayer.data.musicPlatformModule
import com.vela.apps.musicplayer.presentation.LibraryStore
import com.vela.apps.musicplayer.presentation.PlayerStore
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Single entry point platforms use to wire the whole Music Player app. */
val musicModule = module {
    includes(musicPlatformModule())
    viewModelOf(::LibraryStore)
    viewModelOf(::PlayerStore)
}

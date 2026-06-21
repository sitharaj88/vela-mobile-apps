/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.data

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import com.vela.apps.musicplayer.domain.AudioPlayer
import com.vela.apps.musicplayer.domain.MusicLibrary

/** Android wires MediaStore querying and an [android.media.MediaPlayer]-backed player. */
actual fun musicPlatformModule(): Module = module {
    single<MusicLibrary> { AndroidMusicLibrary(androidContext()) }
    single<AudioPlayer> { AndroidAudioPlayer(androidContext()) }
}

/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.data

import com.vela.apps.musicplayer.domain.AudioPlayer
import com.vela.apps.musicplayer.domain.MusicLibrary
import org.koin.core.module.Module
import org.koin.dsl.module

/** Desktop scans `user.home/Music` and plays WAV through `javax.sound.sampled.Clip`. */
actual fun musicPlatformModule(): Module = module {
    single<MusicLibrary> { DesktopMusicLibrary() }
    single<AudioPlayer> { DesktopAudioPlayer() }
}

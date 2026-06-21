/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.recorder

import com.vela.apps.voicerecorder.data.player.AndroidAudioPlayer
import com.vela.apps.voicerecorder.domain.player.AudioPlayer
import com.vela.apps.voicerecorder.domain.recorder.AudioRecorder
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun recorderPlatformModule(): Module = module {
    single<AudioRecorder> { AndroidAudioRecorder(androidContext().applicationContext) }
    single<AudioPlayer> { AndroidAudioPlayer() }
}

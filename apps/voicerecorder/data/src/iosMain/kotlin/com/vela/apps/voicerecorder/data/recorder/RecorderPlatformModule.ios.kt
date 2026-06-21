/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.recorder

import com.vela.apps.voicerecorder.data.player.IosAudioPlayer
import com.vela.apps.voicerecorder.domain.player.AudioPlayer
import com.vela.apps.voicerecorder.domain.recorder.AudioRecorder
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun recorderPlatformModule(): Module = module {
    single<AudioRecorder> { IosAudioRecorder() }
    single<AudioPlayer> { IosAudioPlayer() }
}

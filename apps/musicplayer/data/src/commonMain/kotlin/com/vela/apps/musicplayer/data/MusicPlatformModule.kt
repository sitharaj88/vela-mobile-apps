/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.data

import org.koin.core.module.Module

/**
 * Binds [com.vela.apps.musicplayer.domain.MusicLibrary] and
 * [com.vela.apps.musicplayer.domain.AudioPlayer] per platform. The actuals differ only in how they
 * enumerate audio and how they drive playback.
 */
expect fun musicPlatformModule(): Module

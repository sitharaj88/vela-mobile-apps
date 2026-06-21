/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.recorder

import org.koin.core.module.Module

/**
 * Provides the platform [com.vela.apps.voicerecorder.domain.recorder.AudioRecorder] (and, on
 * Android, the Room database builder which needs a Context). Each platform supplies an actual.
 */
expect fun recorderPlatformModule(): Module

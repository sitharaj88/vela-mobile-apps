/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.di

import com.vela.apps.voicerecorder.data.RoomRecordingRepository
import com.vela.apps.voicerecorder.data.local.RecordingsDatabase
import com.vela.apps.voicerecorder.data.local.buildRecordingsDatabase
import com.vela.apps.voicerecorder.data.local.recordingsPlatformDatabaseModule
import com.vela.apps.voicerecorder.data.recorder.recorderPlatformModule
import com.vela.apps.voicerecorder.domain.repository.RecordingRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/** Wires the Recorder Room database, DAO, repository binding, and the platform AudioRecorder. */
val recorderDataModule = module {
    includes(recordingsPlatformDatabaseModule(), recorderPlatformModule())
    single { buildRecordingsDatabase(get()) }
    single { get<RecordingsDatabase>().recordingDao() }
    singleOf(::RoomRecordingRepository) bind RecordingRepository::class
}

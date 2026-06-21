/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.recorder

import com.vela.apps.voicerecorder.domain.recorder.AudioRecorder
import com.vela.apps.voicerecorder.domain.recorder.RecorderState
import com.vela.apps.voicerecorder.domain.recorder.RecordingResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import platform.AVFAudio.AVAudioRecorder
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.AVFAudio.AVEncoderAudioQualityKey
import platform.AVFAudio.AVFormatIDKey
import platform.AVFAudio.AVNumberOfChannelsKey
import platform.AVFAudio.AVSampleRateKey
import platform.CoreAudioTypes.kAudioFormatMPEG4AAC
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970

/**
 * Best-effort iOS capture via [AVAudioRecorder]. Microphone usage requires an
 * `NSMicrophoneUsageDescription` Info.plist entry (present) and a granted permission; without it
 * recording fails gracefully (returns null). Bits that can only be verified on a device are marked
 * `// TODO(ios)`.
 */
@OptIn(ExperimentalForeignApi::class)
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class IosAudioRecorder : AudioRecorder {

    override val isSupported: Boolean = true

    private val _state = MutableStateFlow(RecorderState())
    override val state: StateFlow<RecorderState> = _state.asStateFlow()

    private var recorder: AVAudioRecorder? = null
    private var outputUrl: NSURL? = null
    private var accumulatedSeconds: Double = 0.0
    private var spanStartSeconds: Double = 0.0

    override suspend fun start() {
        val dir = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        ) ?: return
        val recordingsDir = dir.URLByAppendingPathComponent("recordings", isDirectory = true)
        if (recordingsDir != null) {
            NSFileManager.defaultManager.createDirectoryAtURL(
                url = recordingsDir,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
        }
        val baseDir = recordingsDir ?: dir
        val fileName = "${(NSDate().timeIntervalSince1970 * MILLIS_PER_SECOND).toLong()}.m4a"
        val url = baseDir.URLByAppendingPathComponent(fileName) ?: return
        outputUrl = url

        // TODO(ios): verify AVAudioSession activation + permission flow on a real device.
        val session = AVAudioSession.sharedInstance()
        session.setCategory(AVAudioSessionCategoryPlayAndRecord, error = null)
        session.setActive(true, error = null)

        val settings = mapOf<Any?, Any?>(
            AVFormatIDKey to kAudioFormatMPEG4AAC,
            AVSampleRateKey to NSNumber(double = SAMPLE_RATE),
            AVNumberOfChannelsKey to NSNumber(int = 1),
            AVEncoderAudioQualityKey to NSNumber(int = AV_AUDIO_QUALITY_HIGH),
        )
        val rec = AVAudioRecorder(uRL = url, settings = settings, error = null)
        if (rec.record()) {
            recorder = rec
            accumulatedSeconds = 0.0
            spanStartSeconds = NSDate().timeIntervalSince1970
            _state.value = RecorderState(isRecording = true, isPaused = false)
        } else {
            recorder = null
            outputUrl = null
            _state.value = RecorderState()
        }
    }

    override suspend fun pause() {
        val rec = recorder ?: return
        if (_state.value.isPaused) return
        rec.pause()
        accumulatedSeconds += (NSDate().timeIntervalSince1970 - spanStartSeconds).coerceAtLeast(0.0)
        _state.update {
            it.copy(isPaused = true, amplitude = 0f, elapsedMs = (accumulatedSeconds * MILLIS_PER_SECOND).toLong())
        }
    }

    override suspend fun resume() {
        val rec = recorder ?: return
        if (!_state.value.isPaused) return
        rec.record()
        spanStartSeconds = NSDate().timeIntervalSince1970
        _state.update { it.copy(isPaused = false) }
    }

    @Suppress("ReturnCount")
    override suspend fun stop(): RecordingResult? {
        val rec = recorder ?: return null
        val url = outputUrl
        val wasPaused = _state.value.isPaused
        val seconds = accumulatedSeconds +
            if (wasPaused) 0.0 else (NSDate().timeIntervalSince1970 - spanStartSeconds)
        val durationMs = (seconds * MILLIS_PER_SECOND).toLong().coerceAtLeast(0)
        recorder = null
        outputUrl = null
        _state.value = RecorderState()
        rec.stop()
        val path = url?.path ?: return null
        val size = fileSize(path)
        return RecordingResult(filePath = path, durationMs = durationMs, sizeBytes = size)
    }

    private fun fileSize(path: String): Long {
        val attrs = NSFileManager.defaultManager.attributesOfItemAtPath(path, error = null)
        // TODO(ios): NSFileSize key resolves to an NSNumber; cast may need refining on-device.
        val size = attrs?.get("NSFileSize") as? NSNumber
        return size?.longLongValue ?: 0L
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1000.0
        const val SAMPLE_RATE = 44100.0
        const val AV_AUDIO_QUALITY_HIGH = 0x60
    }
}

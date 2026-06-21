/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.voicerecorder.data.recorder

import com.vela.apps.voicerecorder.domain.recorder.AudioRecorder
import com.vela.apps.voicerecorder.domain.recorder.RecorderState
import com.vela.apps.voicerecorder.domain.recorder.RecordingResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.ByteArrayInputStream
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.TargetDataLine
import kotlin.concurrent.thread

/**
 * Real desktop capture with `javax.sound.sampled`: a [TargetDataLine] streams 16-bit PCM mono into a
 * background thread that (a) accumulates raw bytes for the final WAV and (b) computes a peak level
 * for the meter. On stop the buffer is written to a genuine `.wav` file via [AudioSystem.write].
 *
 * Pause/resume simply gate whether captured frames are accumulated; the line keeps draining so the
 * buffer never overflows.
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class DesktopAudioRecorder : AudioRecorder {

    private val format = AudioFormat(SAMPLE_RATE, BITS_PER_SAMPLE, CHANNELS, true, false)

    override val isSupported: Boolean =
        AudioSystem.isLineSupported(DataLine.Info(TargetDataLine::class.java, format))

    private val _state = MutableStateFlow(RecorderState())
    override val state: StateFlow<RecorderState> = _state.asStateFlow()

    private var line: TargetDataLine? = null
    private var captureThread: Thread? = null
    private val capturing = AtomicBoolean(false)
    private val paused = AtomicBoolean(false)
    private var captured: java.io.ByteArrayOutputStream? = null
    private var outputFile: File? = null
    private var accumulatedMs: Long = 0
    private var spanStartMs: Long = 0

    override suspend fun start() {
        if (!isSupported) return
        val dir = File(System.getProperty("user.home"), ".vela/recordings").apply { mkdirs() }
        val file = File(dir, "${System.currentTimeMillis()}.wav")
        outputFile = file

        val info = DataLine.Info(TargetDataLine::class.java, format)
        val target = AudioSystem.getLine(info) as TargetDataLine
        target.open(format)
        target.start()
        line = target

        val buffer = java.io.ByteArrayOutputStream()
        captured = buffer
        capturing.set(true)
        paused.set(false)
        accumulatedMs = 0
        spanStartMs = System.currentTimeMillis()
        _state.value = RecorderState(isRecording = true, isPaused = false)

        captureThread = thread(name = "vela-recorder", isDaemon = true) { captureLoop(target, buffer) }
    }

    private fun captureLoop(target: TargetDataLine, buffer: java.io.ByteArrayOutputStream) {
        val chunk = ByteArray(CHUNK_BYTES)
        while (capturing.get()) {
            val read = target.read(chunk, 0, chunk.size)
            if (read <= 0) continue
            if (paused.get()) continue
            buffer.write(chunk, 0, read)
            val elapsed = accumulatedMs + (System.currentTimeMillis() - spanStartMs)
            _state.update {
                it.copy(elapsedMs = elapsed.coerceAtLeast(0), amplitude = peakLevel(chunk, read))
            }
        }
    }

    override suspend fun pause() {
        if (!_state.value.isRecording || _state.value.isPaused) return
        paused.set(true)
        accumulatedMs += (System.currentTimeMillis() - spanStartMs).coerceAtLeast(0)
        _state.update { it.copy(isPaused = true, amplitude = 0f, elapsedMs = accumulatedMs) }
    }

    override suspend fun resume() {
        if (!_state.value.isPaused) return
        spanStartMs = System.currentTimeMillis()
        paused.set(false)
        _state.update { it.copy(isPaused = false) }
    }

    override suspend fun stop(): RecordingResult? {
        if (!_state.value.isRecording) return null
        val wasPaused = _state.value.isPaused
        val durationMs = (accumulatedMs +
            if (wasPaused) 0 else (System.currentTimeMillis() - spanStartMs)).coerceAtLeast(0)
        capturing.set(false)
        captureThread?.join(JOIN_TIMEOUT_MS)
        captureThread = null
        runCatching { line?.stop(); line?.close() }
        line = null
        _state.value = RecorderState()

        val bytes = captured?.toByteArray() ?: return null
        val file = outputFile ?: return null
        captured = null
        outputFile = null
        if (bytes.isEmpty()) return null

        return try {
            val frameLength = (bytes.size / format.frameSize).toLong()
            val ais = AudioInputStream(ByteArrayInputStream(bytes), format, frameLength)
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file)
            ais.close()
            RecordingResult(file.absolutePath, durationMs, file.length())
        } catch (error: Exception) {
            runCatching { file.delete() }
            null
        }
    }

    /** Peak of the 16-bit little-endian samples in [data], normalized 0f..1f. */
    private fun peakLevel(data: ByteArray, length: Int): Float {
        var peak = 0
        var i = 0
        while (i + 1 < length) {
            val sample = (data[i].toInt() and 0xFF) or (data[i + 1].toInt() shl BITS_PER_BYTE)
            val abs = if (sample > 0) sample else -sample
            if (abs > peak) peak = abs
            i += 2
        }
        return (peak / MAX_SAMPLE).coerceIn(0f, 1f)
    }

    private companion object {
        const val SAMPLE_RATE = 44_100f
        const val BITS_PER_SAMPLE = 16
        const val CHANNELS = 1
        const val CHUNK_BYTES = 4096
        const val JOIN_TIMEOUT_MS = 1000L
        const val MAX_SAMPLE = 32_768f
        const val BITS_PER_BYTE = 8
    }
}

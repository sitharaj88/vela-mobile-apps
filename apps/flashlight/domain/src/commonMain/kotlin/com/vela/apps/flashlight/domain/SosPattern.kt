/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.flashlight.domain

/**
 * One step of a blink pattern: keep the light [on] for [durationMillis] before advancing.
 *
 * Patterns are pure data so they can be unit-tested and replayed identically on every platform
 * (hardware torch or on-screen flash).
 */
data class BlinkStep(val on: Boolean, val durationMillis: Long)

/**
 * Pure generator for the international Morse "SOS" distress signal (`... --- ...`).
 *
 * Timing follows standard Morse units where a dash is three dots long, the gap between symbols is
 * one dot, the gap between letters is three dots, and the trailing gap between whole words is seven
 * dots — so the sequence loops cleanly. [unitMillis] scales the whole pattern (smaller = faster).
 */
object SosPattern {

    /** A single "dot" — the base time unit every other duration is a multiple of. */
    const val DEFAULT_UNIT_MILLIS: Long = 200L

    private const val DASH_UNITS = 3
    private const val LETTER_GAP_UNITS = 3
    private const val WORD_GAP_UNITS = 7

    // S = dot dot dot, O = dash dash dash, S = dot dot dot.
    private val letters: List<List<Int>> = listOf(
        listOf(1, 1, 1),
        listOf(DASH_UNITS, DASH_UNITS, DASH_UNITS),
        listOf(1, 1, 1),
    )

    /**
     * Build the full on/off [BlinkStep] sequence for one SOS cycle, scaled by [unitMillis]. The
     * trailing word gap is included so repeating the list yields correctly spaced repeats.
     */
    fun steps(unitMillis: Long = DEFAULT_UNIT_MILLIS): List<BlinkStep> {
        val unit = unitMillis.coerceAtLeast(1L)
        val out = mutableListOf<BlinkStep>()
        letters.forEachIndexed { letterIndex, symbols ->
            symbols.forEachIndexed { symbolIndex, lengthUnits ->
                out += BlinkStep(on = true, durationMillis = lengthUnits * unit)
                val isLastSymbol = symbolIndex == symbols.lastIndex
                if (!isLastSymbol) {
                    out += BlinkStep(on = false, durationMillis = unit)
                }
            }
            val isLastLetter = letterIndex == letters.lastIndex
            val gapUnits = if (isLastLetter) WORD_GAP_UNITS else LETTER_GAP_UNITS
            out += BlinkStep(on = false, durationMillis = gapUnits * unit)
        }
        return out
    }
}

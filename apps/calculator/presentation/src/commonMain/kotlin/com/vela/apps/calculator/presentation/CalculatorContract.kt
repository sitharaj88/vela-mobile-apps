/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.presentation

import com.vela.apps.calculator.domain.model.AngleMode
import com.vela.apps.calculator.domain.model.HistoryEntry

/** Immutable UI state rendered by the calculator screen. */
data class CalculatorState(
    val expression: String = "",
    val preview: String = "",
    val isError: Boolean = false,
    val history: List<HistoryEntry> = emptyList(),
    val isScientific: Boolean = false,
    val angleMode: AngleMode = AngleMode.Degrees,
) {
    /** Big primary line — shows a friendly zero when nothing has been entered. */
    val display: String get() = expression.ifEmpty { "0" }
}

/** Every user action on the calculator. */
sealed interface CalculatorIntent {
    /** Append a token (digit, operator, parenthesis, or decimal point). */
    data class Append(val token: String) : CalculatorIntent
    data object DeleteLast : CalculatorIntent
    data object Clear : CalculatorIntent
    data object Evaluate : CalculatorIntent
    data class RecallHistory(val entry: HistoryEntry) : CalculatorIntent
    data object ClearHistory : CalculatorIntent
    data object ToggleScientific : CalculatorIntent
    data object ToggleAngleMode : CalculatorIntent
}

/** One-shot events (consumed once by the UI). */
sealed interface CalculatorEffect {
    data class ShowError(val message: String) : CalculatorEffect
}

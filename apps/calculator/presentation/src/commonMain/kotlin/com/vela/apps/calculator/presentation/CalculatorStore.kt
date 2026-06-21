/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.presentation

import androidx.lifecycle.viewModelScope
import com.vela.apps.calculator.domain.model.AngleMode
import com.vela.apps.calculator.domain.model.CalculationError
import com.vela.apps.calculator.domain.model.CalculationResult
import com.vela.apps.calculator.domain.repository.HistoryRepository
import com.vela.apps.calculator.domain.usecase.EvaluateExpressionUseCase
import com.vela.core.common.MviStore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * MVI store for the calculator. Holds the current expression, computes a live preview as the user
 * types, and commits results to history on evaluate.
 */
class CalculatorStore(
    private val evaluate: EvaluateExpressionUseCase,
    private val history: HistoryRepository,
) : MviStore<CalculatorState, CalculatorIntent, CalculatorEffect>(CalculatorState()) {

    init {
        viewModelScope.launch {
            history.observeHistory().collectLatest { entries ->
                setState { copy(history = entries) }
            }
        }
    }

    override fun onIntent(intent: CalculatorIntent) {
        when (intent) {
            is CalculatorIntent.Append -> append(intent.token)
            CalculatorIntent.DeleteLast -> updateExpression(currentState.expression.dropLast(1))
            CalculatorIntent.Clear -> setState { copy(expression = "", preview = "", isError = false) }
            CalculatorIntent.Evaluate -> evaluateNow()
            is CalculatorIntent.RecallHistory -> updateExpression(intent.entry.result)
            CalculatorIntent.ClearHistory -> viewModelScope.launch { history.clear() }
            CalculatorIntent.ToggleScientific ->
                setState { copy(isScientific = !isScientific) }
            CalculatorIntent.ToggleAngleMode -> toggleAngleMode()
        }
    }

    private fun toggleAngleMode() {
        val next = if (currentState.angleMode == AngleMode.Degrees) {
            AngleMode.Radians
        } else {
            AngleMode.Degrees
        }
        // Recompute the live preview so a trig expression updates immediately on the unit switch.
        setState { copy(angleMode = next, preview = computePreview(expression, next)) }
    }

    private fun append(token: String) {
        // Starting fresh after an error or a result: a digit/'(' replaces, an operator continues.
        val base = if (currentState.isError) "" else currentState.expression
        updateExpression(base + token)
    }

    private fun updateExpression(expression: String) {
        setState {
            copy(
                expression = expression,
                preview = computePreview(expression, angleMode),
                isError = false,
            )
        }
    }

    private fun computePreview(expression: String, angleMode: AngleMode): String =
        when (val result = evaluate(expression, angleMode)) {
            is CalculationResult.Success -> result.formatted
            is CalculationResult.Failure -> "" // don't surface errors during typing
        }

    private fun evaluateNow() {
        val expression = currentState.expression
        when (val result = evaluate(expression, currentState.angleMode)) {
            is CalculationResult.Success -> {
                setState { copy(expression = result.formatted, preview = "", isError = false) }
                viewModelScope.launch { history.add(expression, result.formatted) }
            }
            is CalculationResult.Failure -> {
                setState { copy(isError = true) }
                emitEffect(CalculatorEffect.ShowError(messageFor(result.reason)))
            }
        }
    }

    private fun messageFor(error: CalculationError): String = when (error) {
        CalculationError.EmptyExpression -> "Enter an expression"
        CalculationError.MalformedExpression -> "Invalid expression"
        CalculationError.DivisionByZero -> "Can't divide by zero"
        CalculationError.Overflow -> "Number too large"
    }
}

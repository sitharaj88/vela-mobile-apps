/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.domain.model

/** Outcome of evaluating an expression. */
sealed interface CalculationResult {
    data class Success(val value: Double, val formatted: String) : CalculationResult
    data class Failure(val reason: CalculationError) : CalculationResult
}

enum class CalculationError {
    EmptyExpression,
    MalformedExpression,
    DivisionByZero,
    Overflow,
}

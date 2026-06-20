/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.domain.usecase

import com.vela.apps.calculator.domain.engine.ExpressionEvaluator
import com.vela.apps.calculator.domain.model.CalculationResult

/** Use case the presentation layer calls to evaluate the current expression. */
class EvaluateExpressionUseCase(
    private val evaluator: ExpressionEvaluator = ExpressionEvaluator(),
) {
    operator fun invoke(expression: String): CalculationResult = evaluator.evaluate(expression)
}

/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.domain

import com.vela.apps.calculator.domain.engine.ExpressionEvaluator
import com.vela.apps.calculator.domain.model.CalculationError
import com.vela.apps.calculator.domain.model.CalculationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExpressionEvaluatorTest {

    private val evaluator = ExpressionEvaluator()

    private fun eval(expr: String) = evaluator.evaluate(expr)

    private fun assertResult(expr: String, expected: Double) {
        val result = eval(expr)
        assertTrue(result is CalculationResult.Success, "Expected success for '$expr' but got $result")
        assertEquals(expected, result.value, 1e-9, "Wrong value for '$expr'")
    }

    private fun assertError(expr: String, expected: CalculationError) {
        val result = eval(expr)
        assertTrue(result is CalculationResult.Failure, "Expected failure for '$expr' but got $result")
        assertEquals(expected, result.reason)
    }

    @Test
    fun addition_and_subtraction() {
        assertResult("2 + 3", 5.0)
        assertResult("10 - 4 - 1", 5.0)
    }

    @Test
    fun precedence_is_respected() {
        assertResult("2 + 3 * 4", 14.0)
        assertResult("2 * 3 + 4", 10.0)
    }

    @Test
    fun parentheses_override_precedence() {
        assertResult("(2 + 3) * 4", 20.0)
        assertResult("2 * (3 + (4 - 1))", 12.0)
    }

    @Test
    fun unary_minus() {
        assertResult("-5 + 3", -2.0)
        assertResult("3 * -2", -6.0)
        assertResult("-(2 + 3)", -5.0)
    }

    @Test
    fun exponent_is_right_associative() {
        assertResult("2 ^ 3 ^ 2", 512.0) // 2^(3^2), not (2^3)^2
    }

    @Test
    fun decimals_and_modulo() {
        assertResult("0.1 + 0.2", 0.3)
        assertResult("10 % 3", 1.0)
    }

    @Test
    fun division_by_zero_is_an_error() {
        assertError("5 / 0", CalculationError.DivisionByZero)
    }

    @Test
    fun empty_and_malformed_are_errors() {
        assertError("", CalculationError.EmptyExpression)
        assertError("2 +", CalculationError.MalformedExpression)
        assertError("(2 + 3", CalculationError.MalformedExpression)
        assertError("2 ** 3", CalculationError.MalformedExpression)
    }

    @Test
    fun formatting_trims_trailing_zeros() {
        val result = eval("4 / 2")
        assertTrue(result is CalculationResult.Success)
        assertEquals("2", result.formatted)
    }
}

/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.domain

import com.vela.apps.calculator.domain.engine.ExpressionEvaluator
import com.vela.apps.calculator.domain.model.AngleMode
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

    private fun assertResult(expr: String, angleMode: AngleMode, expected: Double) {
        val result = evaluator.evaluate(expr, angleMode)
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

    // ---- Scientific functions ----

    @Test
    fun trig_in_degrees() {
        assertResult("sin(0)", AngleMode.Degrees, 0.0)
        assertResult("sin(90)", AngleMode.Degrees, 1.0)
        assertResult("cos(0)", AngleMode.Degrees, 1.0)
        assertResult("cos(180)", AngleMode.Degrees, -1.0)
        assertResult("tan(45)", AngleMode.Degrees, 1.0)
    }

    @Test
    fun trig_in_radians() {
        assertResult("sin(0)", AngleMode.Radians, 0.0)
        assertResult("cos(0)", AngleMode.Radians, 1.0)
        assertResult("sin(pi / 2)", AngleMode.Radians, 1.0)
        assertResult("tan(0)", AngleMode.Radians, 0.0)
    }

    @Test
    fun inverse_trig_respects_angle_mode() {
        assertResult("asin(1)", AngleMode.Degrees, 90.0)
        assertResult("acos(1)", AngleMode.Degrees, 0.0)
        assertResult("atan(1)", AngleMode.Degrees, 45.0)
        assertResult("atan(1)", AngleMode.Radians, kotlin.math.PI / 4)
    }

    @Test
    fun logarithms() {
        assertResult("log(1000)", 3.0)
        assertResult("log(1)", 0.0)
        assertResult("ln(1)", 0.0)
        assertResult("ln(e)", 1.0)
    }

    @Test
    fun sqrt_abs_and_exp() {
        assertResult("sqrt(16)", 4.0)
        assertResult("sqrt(2)", kotlin.math.sqrt(2.0))
        assertResult("abs(-7)", 7.0)
        assertResult("exp(0)", 1.0)
        assertResult("exp(1)", kotlin.math.E)
    }

    @Test
    fun factorial_is_a_postfix_operator() {
        assertResult("5!", 120.0)
        assertResult("0!", 1.0)
        assertResult("3! + 1", 7.0)
        assertResult("(2 + 1)!", 6.0)
    }

    @Test
    fun percentage_postfix() {
        assertResult("50%", 0.5)
        assertResult("200 * 10%", 20.0)
    }

    @Test
    fun modulo_still_works_as_binary_operator() {
        assertResult("10 % 3", 1.0)
        assertResult("10 % 4 + 1", 3.0)
    }

    @Test
    fun constants_pi_and_e() {
        assertResult("pi", kotlin.math.PI)
        assertResult("e", kotlin.math.E)
        assertResult("2 * pi", 2 * kotlin.math.PI)
    }

    @Test
    fun nested_functions() {
        assertResult("sqrt(sin(0) + 1)", AngleMode.Degrees, 1.0)
        assertResult("sqrt(abs(-9))", 3.0)
        assertResult("log(exp(0) * 10)", 1.0)
    }

    @Test
    fun precedence_with_functions() {
        assertResult("sqrt(4) + 1", 3.0)
        assertResult("2 * sqrt(9)", 6.0)
        assertResult("sqrt(9) ^ 2", 9.0)
        assertResult("2 + abs(-3) * 2", 8.0)
    }

    @Test
    fun sqrt_of_negative_is_malformed() {
        assertError("sqrt(-4)", CalculationError.MalformedExpression)
    }

    @Test
    fun unknown_function_is_malformed() {
        assertError("foo(2)", CalculationError.MalformedExpression)
        assertError("sine(0)", CalculationError.MalformedExpression)
    }

    @Test
    fun factorial_of_negative_or_fraction_is_malformed() {
        assertError("(-1)!", CalculationError.MalformedExpression)
        assertError("2.5!", CalculationError.MalformedExpression)
    }
}

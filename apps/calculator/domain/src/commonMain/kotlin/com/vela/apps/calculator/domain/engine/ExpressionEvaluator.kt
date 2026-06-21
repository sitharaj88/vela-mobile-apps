/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.domain.engine

import com.vela.apps.calculator.domain.model.AngleMode
import com.vela.apps.calculator.domain.model.CalculationError
import com.vela.apps.calculator.domain.model.CalculationResult
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Evaluates infix scientific expressions using the shunting-yard algorithm.
 *
 * Supports: + - * / % ^, parentheses, unary minus, decimals; the functions sin, cos, tan, asin,
 * acos, atan, log (base 10), ln, sqrt, abs, exp; the constants pi (π) and e; postfix factorial (!);
 * and a postfix percentage (%) operator. Trigonometry honours the supplied [AngleMode]. Original
 * clean-room implementation — no third-party or GPL code involved.
 */
class ExpressionEvaluator {

    fun evaluate(input: String, angleMode: AngleMode = AngleMode.Radians): CalculationResult {
        val expression = input.trim()
        if (expression.isEmpty()) return CalculationResult.Failure(CalculationError.EmptyExpression)

        return try {
            val tokens = tokenize(expression)
            val rpn = toReversePolish(tokens)
            val value = evaluateRpn(rpn, angleMode)
            when {
                value.isNaN() -> CalculationResult.Failure(CalculationError.MalformedExpression)
                value.isInfinite() -> CalculationResult.Failure(CalculationError.DivisionByZero)
                else -> CalculationResult.Success(value, formatNumber(value))
            }
        } catch (_: DivisionByZeroException) {
            CalculationResult.Failure(CalculationError.DivisionByZero)
        } catch (_: MalformedExpressionException) {
            CalculationResult.Failure(CalculationError.MalformedExpression)
        }
    }

    // ---- Tokenizer ----

    private sealed interface Token {
        data class Num(val value: Double) : Token
        data class Op(val symbol: Char, val unary: Boolean = false) : Token
        data class Func(val name: String) : Token
        data object Factorial : Token
        data object Percent : Token
        data object LParen : Token
        data object RParen : Token
    }

    // A hand-written scanner is inherently branchy and validates many error cases; the complexity
    // here is essential, not accidental.
    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth", "ThrowsCount", "LongMethod")
    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var prevWasValueOrClose = false // distinguishes binary vs unary minus and implicit needs

        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isWhitespace() -> i++

                c.isDigit() || c == '.' -> {
                    val start = i
                    var dots = if (c == '.') 1 else 0
                    i++
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) {
                        if (expr[i] == '.' && ++dots > 1) throw MalformedExpressionException()
                        i++
                    }
                    val text = expr.substring(start, i)
                    tokens += Token.Num(text.toDoubleOrNull() ?: throw MalformedExpressionException())
                    prevWasValueOrClose = true
                }

                c == 'π' -> { // dedicated symbol — handled before the letter scanner
                    tokens += Token.Num(PI)
                    prevWasValueOrClose = true
                    i++
                }

                c.isLetter() -> {
                    val start = i
                    while (i < expr.length && expr[i].isLetter() && expr[i] != 'π') i++
                    val name = expr.substring(start, i).lowercase()
                    when (val constant = CONSTANTS[name]) {
                        null -> {
                            if (name !in FUNCTIONS) throw MalformedExpressionException()
                            tokens += Token.Func(name)
                            prevWasValueOrClose = false
                        }
                        else -> {
                            tokens += Token.Num(constant)
                            prevWasValueOrClose = true
                        }
                    }
                }

                c == '!' -> {
                    if (!prevWasValueOrClose) throw MalformedExpressionException()
                    tokens += Token.Factorial
                    prevWasValueOrClose = true
                    i++
                }

                c == '%' -> {
                    if (!prevWasValueOrClose) throw MalformedExpressionException()
                    // '%' is postfix percentage at the end of an operand (end of input, or before a
                    // closing paren / binary operator); it is binary modulo when another operand
                    // follows, preserving the classic "10 % 3" behaviour.
                    if (startsNewOperand(expr, i + 1)) {
                        tokens += Token.Op('%')
                        prevWasValueOrClose = false
                    } else {
                        tokens += Token.Percent
                        prevWasValueOrClose = true
                    }
                    i++
                }

                c == '(' -> {
                    tokens += Token.LParen
                    prevWasValueOrClose = false
                    i++
                }

                c == ')' -> {
                    tokens += Token.RParen
                    prevWasValueOrClose = true
                    i++
                }

                c in OPERATORS -> {
                    val unary = c == '-' && !prevWasValueOrClose
                    tokens += Token.Op(c, unary)
                    prevWasValueOrClose = false
                    i++
                }

                else -> throw MalformedExpressionException()
            }
        }
        if (tokens.isEmpty()) throw MalformedExpressionException()
        return tokens
    }

    /** True if the first non-space char at [from] begins a new operand (number, constant, function,
     *  unary sign, or an opening paren) — used to disambiguate '%' modulo from percentage. */
    private fun startsNewOperand(expr: String, from: Int): Boolean {
        var j = from
        while (j < expr.length && expr[j].isWhitespace()) j++
        if (j >= expr.length) return false
        val c = expr[j]
        return c.isDigit() || c == '.' || c.isLetter() || c == 'π' || c == '(' || c == '-' || c == '+'
    }

    // ---- Shunting-yard: infix -> RPN ----

    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
    private fun toReversePolish(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Num -> output += token
                // Postfix operators emit immediately — they bind to the value already in output.
                Token.Factorial, Token.Percent -> output += token
                is Token.Func -> stack.addLast(token)
                is Token.Op -> {
                    while (stack.isNotEmpty() && shouldPopForOperator(token, stack.last())) {
                        output += stack.removeLast()
                    }
                    stack.addLast(token)
                }
                Token.LParen -> stack.addLast(token)
                Token.RParen -> {
                    while (stack.isNotEmpty() && stack.last() != Token.LParen) {
                        output += stack.removeLast()
                    }
                    if (stack.isEmpty()) throw MalformedExpressionException() // mismatched parens
                    stack.removeLast() // pop the LParen
                    // A function name directly preceding the group applies to it.
                    if (stack.isNotEmpty() && stack.last() is Token.Func) output += stack.removeLast()
                }
            }
        }
        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top == Token.LParen || top == Token.RParen) throw MalformedExpressionException()
            output += top
        }
        return output
    }

    private fun shouldPopForOperator(incoming: Token.Op, top: Token): Boolean = when (top) {
        is Token.Func -> true // functions bind tighter than any binary/unary operator
        is Token.Op -> shouldPopOperator(incoming, top)
        else -> false // LParen
    }

    private fun shouldPopOperator(incoming: Token.Op, top: Token.Op): Boolean {
        if (incoming.unary) return false // unary (right-assoc, highest) never pops
        val incomingPrec = precedence(incoming)
        val topPrec = precedence(top)
        val rightAssociative = incoming.symbol == '^'
        return if (rightAssociative) topPrec > incomingPrec else topPrec >= incomingPrec
    }

    private fun precedence(op: Token.Op): Int = when {
        op.unary -> 4
        op.symbol == '^' -> 3
        op.symbol == '*' || op.symbol == '/' || op.symbol == '%' -> 2
        else -> 1 // + and -
    }

    // ---- RPN evaluation ----

    @Suppress("ThrowsCount", "CyclomaticComplexMethod")
    private fun evaluateRpn(rpn: List<Token>, angleMode: AngleMode): Double {
        val stack = ArrayDeque<Double>()
        for (token in rpn) {
            when (token) {
                is Token.Num -> stack.addLast(token.value)
                is Token.Func -> {
                    val arg = stack.removeLastOrNull() ?: throw MalformedExpressionException()
                    stack.addLast(applyFunction(token.name, arg, angleMode))
                }
                Token.Factorial -> {
                    val arg = stack.removeLastOrNull() ?: throw MalformedExpressionException()
                    stack.addLast(factorial(arg))
                }
                Token.Percent -> {
                    val arg = stack.removeLastOrNull() ?: throw MalformedExpressionException()
                    stack.addLast(arg / PERCENT_DIVISOR)
                }
                is Token.Op -> {
                    if (token.unary) {
                        val operand = stack.removeLastOrNull() ?: throw MalformedExpressionException()
                        stack.addLast(-operand)
                    } else {
                        val b = stack.removeLastOrNull() ?: throw MalformedExpressionException()
                        val a = stack.removeLastOrNull() ?: throw MalformedExpressionException()
                        stack.addLast(applyBinary(token.symbol, a, b))
                    }
                }
                else -> throw MalformedExpressionException()
            }
        }
        return stack.singleOrNull() ?: throw MalformedExpressionException()
    }

    private fun applyBinary(op: Char, a: Double, b: Double): Double = when (op) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> if (b == 0.0) throw DivisionByZeroException() else a / b
        '%' -> if (b == 0.0) throw DivisionByZeroException() else a % b
        '^' -> a.pow(b)
        else -> throw MalformedExpressionException()
    }

    private fun applyFunction(name: String, arg: Double, angleMode: AngleMode): Double = when (name) {
        "sin" -> sin(toRadians(arg, angleMode))
        "cos" -> cos(toRadians(arg, angleMode))
        "tan" -> tan(toRadians(arg, angleMode))
        "asin" -> fromRadians(asin(arg), angleMode)
        "acos" -> fromRadians(acos(arg), angleMode)
        "atan" -> fromRadians(atan(arg), angleMode)
        "log" -> log10(arg)
        "ln" -> ln(arg)
        "sqrt" -> sqrt(arg)
        "abs" -> abs(arg)
        "exp" -> exp(arg)
        else -> throw MalformedExpressionException()
    }

    private fun toRadians(value: Double, angleMode: AngleMode): Double =
        if (angleMode == AngleMode.Degrees) value * PI / STRAIGHT_ANGLE else value

    private fun fromRadians(value: Double, angleMode: AngleMode): Double =
        if (angleMode == AngleMode.Degrees) value * STRAIGHT_ANGLE / PI else value

    private fun factorial(value: Double): Double {
        // Only defined for non-negative whole numbers; anything else yields NaN -> a clean error.
        if (value < 0.0 || value != round(value) || value > MAX_FACTORIAL) return Double.NaN
        var result = 1.0
        var n = 2
        while (n <= value.toInt()) {
            result *= n
            n++
        }
        return result
    }

    private class DivisionByZeroException : Exception()
    private class MalformedExpressionException : Exception()

    private companion object {
        val OPERATORS = setOf('+', '-', '*', '/', '%', '^')
        val FUNCTIONS = setOf(
            "sin", "cos", "tan", "asin", "acos", "atan", "log", "ln", "sqrt", "abs", "exp",
        )
        val CONSTANTS = mapOf("pi" to PI, "e" to E)
        const val STRAIGHT_ANGLE = 180.0
        const val PERCENT_DIVISOR = 100.0
        const val MAX_FACTORIAL = 170.0 // 171! overflows Double to +Infinity
    }
}

/**
 * Formats a [Double] for display: integers show without a trailing ".0", others trim trailing
 * zeros. Kept top-level so the presentation layer can reuse identical formatting.
 */
fun formatNumber(value: Double): String {
    if (value == value.toLong().toDouble() && kotlin.math.abs(value) < 1e15) {
        return value.toLong().toString()
    }
    val text = value.toString()
    return if (text.contains('.')) text.trimEnd('0').trimEnd('.') else text
}

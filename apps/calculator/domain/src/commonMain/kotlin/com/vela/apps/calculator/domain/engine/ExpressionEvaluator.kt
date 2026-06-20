/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.domain.engine

import com.vela.apps.calculator.domain.model.CalculationError
import com.vela.apps.calculator.domain.model.CalculationResult
import kotlin.math.pow

/**
 * Evaluates infix arithmetic expressions using the shunting-yard algorithm.
 *
 * Supports: + - * / % ^, parentheses, unary minus, and decimals. Original clean-room
 * implementation — no third-party or GPL code involved.
 */
class ExpressionEvaluator {

    fun evaluate(input: String): CalculationResult {
        val expression = input.trim()
        if (expression.isEmpty()) return CalculationResult.Failure(CalculationError.EmptyExpression)

        return try {
            val tokens = tokenize(expression)
            val rpn = toReversePolish(tokens)
            val value = evaluateRpn(rpn)
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
        data object LParen : Token
        data object RParen : Token
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        var prevWasValueOrClose = false // distinguishes binary vs unary minus

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

    // ---- Shunting-yard: infix -> RPN ----

    private fun toReversePolish(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Num -> output += token
                is Token.Op -> {
                    while (stack.isNotEmpty()) {
                        val top = stack.last()
                        if (top is Token.Op && shouldPopOperator(token, top)) {
                            output += stack.removeLast()
                        } else {
                            break
                        }
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

    private fun evaluateRpn(rpn: List<Token>): Double {
        val stack = ArrayDeque<Double>()
        for (token in rpn) {
            when (token) {
                is Token.Num -> stack.addLast(token.value)
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

    private class DivisionByZeroException : Exception()
    private class MalformedExpressionException : Exception()

    private companion object {
        val OPERATORS = setOf('+', '-', '*', '/', '%', '^')
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

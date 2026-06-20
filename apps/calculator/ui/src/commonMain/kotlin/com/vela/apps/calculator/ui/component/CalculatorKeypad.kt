/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vela.apps.calculator.presentation.CalculatorIntent
import com.vela.core.designsystem.theme.LocalVelaTokens

private enum class KeyStyle { Number, Operator, Function, Accent }

private data class CalcKey(
    val label: String,
    val style: KeyStyle,
    val intent: CalculatorIntent,
    val isBackspace: Boolean = false,
)

private fun digit(d: String) = CalcKey(d, KeyStyle.Number, CalculatorIntent.Append(d))
private fun op(label: String, token: String) = CalcKey(label, KeyStyle.Operator, CalculatorIntent.Append(token))

private val keypad: List<List<CalcKey>> = listOf(
    listOf(
        CalcKey("C", KeyStyle.Function, CalculatorIntent.Clear),
        op("(", "("), op(")", ")"), op("÷", "/"),
    ),
    listOf(digit("7"), digit("8"), digit("9"), op("×", "*")),
    listOf(digit("4"), digit("5"), digit("6"), op("−", "-")),
    listOf(digit("1"), digit("2"), digit("3"), op("+", "+")),
    listOf(
        CalcKey("⌫", KeyStyle.Function, CalculatorIntent.DeleteLast, isBackspace = true),
        digit("0"), digit("."),
        CalcKey("=", KeyStyle.Accent, CalculatorIntent.Evaluate),
    ),
)

/** The full calculator keypad. Sends [CalculatorIntent]s up; holds no state of its own. */
@Composable
fun CalculatorKeypad(
    onIntent: (CalculatorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalVelaTokens.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
    ) {
        keypad.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
            ) {
                row.forEach { key -> KeyButton(key, onIntent, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun KeyButton(
    key: CalcKey,
    onIntent: (CalculatorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container: Color = when (key.style) {
        KeyStyle.Number -> scheme.surfaceVariant
        KeyStyle.Operator -> scheme.secondaryContainer
        KeyStyle.Function -> scheme.tertiaryContainer
        KeyStyle.Accent -> scheme.primary
    }
    val content: Color = when (key.style) {
        KeyStyle.Number -> scheme.onSurfaceVariant
        KeyStyle.Operator -> scheme.onSecondaryContainer
        KeyStyle.Function -> scheme.onTertiaryContainer
        KeyStyle.Accent -> scheme.onPrimary
    }
    Surface(
        onClick = { onIntent(key.intent) },
        modifier = modifier.aspectRatio(1f),
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = content,
    ) {
        Column(
            modifier = Modifier.padding(tokensPadding()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (key.isBackspace) {
                Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Text(text = key.label, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun tokensPadding() = LocalVelaTokens.current.spacing.sm

/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vela.apps.calculator.presentation.CalculatorIntent
import com.vela.core.designsystem.theme.LocalVelaTokens

private data class SciKey(val label: String, val token: String)

private val scientificRows: List<List<SciKey>> = listOf(
    listOf(SciKey("sin", "sin("), SciKey("cos", "cos("), SciKey("tan", "tan("), SciKey("π", "π")),
    listOf(SciKey("ln", "ln("), SciKey("log", "log("), SciKey("√", "sqrt("), SciKey("e", "e")),
    listOf(SciKey("xʸ", "^"), SciKey("x!", "!"), SciKey("%", "%"), SciKey("abs", "abs(")),
    listOf(SciKey("asin", "asin("), SciKey("acos", "acos("), SciKey("atan", "atan("), SciKey("eˣ", "exp(")),
)

/** Function-key panel shown above the standard keypad while scientific mode is active. */
@Composable
fun ScientificKeypad(
    onIntent: (CalculatorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalVelaTokens.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
    ) {
        scientificRows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
            ) {
                row.forEach { key ->
                    SciKeyButton(key, onIntent, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SciKeyButton(
    key: SciKey,
    onIntent: (CalculatorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = { onIntent(CalculatorIntent.Append(key.token)) },
        modifier = modifier.heightIn(min = 48.dp),
        shape = MaterialTheme.shapes.large,
        color = scheme.surfaceVariant,
        contentColor = scheme.onSurfaceVariant,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = key.label,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

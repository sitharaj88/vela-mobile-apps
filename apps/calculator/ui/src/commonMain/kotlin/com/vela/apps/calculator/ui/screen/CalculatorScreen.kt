/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.vela.apps.calculator.domain.model.AngleMode
import com.vela.apps.calculator.presentation.CalculatorEffect
import com.vela.apps.calculator.presentation.CalculatorIntent
import com.vela.apps.calculator.presentation.CalculatorState
import com.vela.apps.calculator.presentation.CalculatorStore
import com.vela.apps.calculator.ui.component.CalculatorKeypad
import com.vela.apps.calculator.ui.component.ScientificKeypad
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.LocalVelaTokens
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CalculatorScreen(
    onOpenHistory: () -> Unit,
    store: CalculatorStore = koinViewModel(),
) {
    val state by store.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val tokens = LocalVelaTokens.current

    LaunchedEffect(store) {
        store.effects.collect { effect ->
            when (effect) {
                is CalculatorEffect.ShowError -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    VelaScaffold(
        title = "Calculator",
        snackbarHostState = snackbarHostState,
        actions = {
            IconButton(onClick = { store.onIntent(CalculatorIntent.ToggleScientific) }) {
                Icon(
                    Icons.Filled.Functions,
                    contentDescription = if (state.isScientific) "Standard mode" else "Scientific mode",
                    tint = if (state.isScientific) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            IconButton(onClick = onOpenHistory) {
                Icon(Icons.Filled.History, contentDescription = "History")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(tokens.spacing.lg),
            verticalArrangement = Arrangement.Bottom,
        ) {
            // Display
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = tokens.spacing.xl),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = state.display,
                    style = MaterialTheme.typography.displayMedium,
                    color = if (state.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                )
                if (state.preview.isNotEmpty()) {
                    Text(
                        text = "= ${state.preview}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (state.isScientific) {
                AngleModeToggle(
                    state = state,
                    onToggle = { store.onIntent(CalculatorIntent.ToggleAngleMode) },
                    modifier = Modifier.padding(bottom = tokens.spacing.md),
                )
                ScientificKeypad(
                    onIntent = store::onIntent,
                    modifier = Modifier.padding(bottom = tokens.spacing.md),
                )
            }
            CalculatorKeypad(onIntent = store::onIntent)
        }
    }
}

@Composable
private fun AngleModeToggle(
    state: CalculatorState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = true,
        onClick = onToggle,
        label = {
            Text(text = if (state.angleMode == AngleMode.Degrees) "DEG" else "RAD")
        },
        modifier = modifier,
    )
}

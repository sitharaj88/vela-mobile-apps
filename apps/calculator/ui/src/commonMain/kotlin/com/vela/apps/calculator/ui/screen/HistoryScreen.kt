/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.calculator.presentation.CalculatorIntent
import com.vela.apps.calculator.presentation.CalculatorStore
import com.vela.core.designsystem.component.VelaCard
import com.vela.core.designsystem.component.VelaEmptyState
import com.vela.core.designsystem.theme.LocalVelaTokens
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    store: CalculatorStore = koinViewModel(),
) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current

    com.vela.core.designsystem.component.VelaScaffold(
        title = "History",
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = onBack,
        actions = {
            if (state.history.isNotEmpty()) {
                IconButton(onClick = { store.onIntent(CalculatorIntent.ClearHistory) }) {
                    Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear history")
                }
            }
        },
    ) { padding ->
        if (state.history.isEmpty()) {
            VelaEmptyState(
                icon = Icons.Filled.Calculate,
                title = "No calculations yet",
                description = "Your past results will show up here.",
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(tokens.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
            ) {
                items(state.history, key = { it.id }) { entry ->
                    VelaCard(
                        modifier = Modifier.fillMaxWidth().clickable {
                            store.onIntent(CalculatorIntent.RecallHistory(entry))
                            onBack()
                        },
                    ) {
                        Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = entry.expression,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.End,
                            )
                            Text(
                                text = "= ${entry.result}",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                }
            }
        }
    }
}

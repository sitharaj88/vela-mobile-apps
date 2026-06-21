/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.ui.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.clock.presentation.clock.ClockStore
import com.vela.core.designsystem.theme.LocalVelaTokens

@Composable
fun ClockTab(store: ClockStore) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current
    Column(
        modifier = Modifier.fillMaxSize().padding(tokens.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(state.time, style = MaterialTheme.typography.displayLarge)
        Text(
            state.date,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = tokens.spacing.sm),
        )
    }
}

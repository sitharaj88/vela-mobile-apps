/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vela.core.designsystem.theme.LocalVelaTokens

/** Tonal surface card with Vela's standard inner padding. */
@Composable
fun VelaCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val tokens = LocalVelaTokens.current
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(),
        elevation = CardDefaults.cardElevation(defaultElevation = tokens.elevation.level1),
    ) {
        Column(Modifier.padding(tokens.spacing.lg)) {
            content()
        }
    }
}

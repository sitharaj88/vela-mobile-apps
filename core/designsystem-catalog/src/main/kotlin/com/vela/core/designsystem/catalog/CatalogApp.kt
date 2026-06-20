/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.core.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vela.core.designsystem.component.VelaButton
import com.vela.core.designsystem.component.VelaButtonVariant
import com.vela.core.designsystem.component.VelaCard
import com.vela.core.designsystem.component.VelaEmptyState
import com.vela.core.designsystem.theme.LocalVelaTokens
import com.vela.core.designsystem.theme.ThemeMode
import com.vela.core.designsystem.theme.VelaAccent
import com.vela.core.designsystem.theme.VelaTheme

@Composable
fun CatalogApp() {
    var accent by remember { mutableStateOf(VelaAccent.Indigo) }
    var themeMode by remember { mutableStateOf(ThemeMode.Light) }

    VelaTheme(accent = accent, themeMode = themeMode) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val tokens = LocalVelaTokens.current
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(tokens.spacing.xl),
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.xl),
            ) {
                Text("✦ Vela Design System", style = MaterialTheme.typography.headlineMedium)

                Section("Accent") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
                        VelaAccent.entries.forEach { value ->
                            FilterChip(
                                selected = accent == value,
                                onClick = { accent = value },
                                label = { Text(value.name) },
                            )
                        }
                    }
                }

                Section("Theme mode") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
                        ThemeMode.entries.forEach { value ->
                            FilterChip(
                                selected = themeMode == value,
                                onClick = { themeMode = value },
                                label = { Text(value.name) },
                            )
                        }
                    }
                }

                Section("Buttons") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
                        VelaButton("Primary", {}, variant = VelaButtonVariant.Primary)
                        VelaButton("Tonal", {}, variant = VelaButtonVariant.Tonal)
                        VelaButton("Outlined", {}, variant = VelaButtonVariant.Outlined)
                        VelaButton("Text", {}, variant = VelaButtonVariant.Text)
                    }
                }

                Section("Color roles") {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
                        val scheme = MaterialTheme.colorScheme
                        Swatch("Primary", scheme.primary)
                        Swatch("Secondary", scheme.secondary)
                        Swatch("Tertiary", scheme.tertiary)
                        Swatch("Surface", scheme.surface)
                        Swatch("Variant", scheme.surfaceVariant)
                        Swatch("Error", scheme.error)
                    }
                }

                Section("Card") {
                    VelaCard(Modifier.fillMaxWidth()) {
                        Text("Card title", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Cards use shared tonal surfaces and standard inner padding.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Section("Typography") {
                    Column(verticalArrangement = Arrangement.spacedBy(tokens.spacing.xs)) {
                        Text("Display medium", style = MaterialTheme.typography.displayMedium)
                        Text("Headline small", style = MaterialTheme.typography.headlineSmall)
                        Text("Title medium", style = MaterialTheme.typography.titleMedium)
                        Text("Body large", style = MaterialTheme.typography.bodyLarge)
                        Text("Label small", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Section("Empty state") {
                    Box(Modifier.fillMaxWidth().height(220.dp)) {
                        VelaEmptyState(
                            icon = Icons.Filled.Inbox,
                            title = "Nothing here yet",
                            description = "Empty states keep the suite friendly and consistent.",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    val tokens = LocalVelaTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(tokens.spacing.md)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        content()
    }
}

@Composable
private fun Swatch(label: String, color: Color) {
    val tokens = LocalVelaTokens.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(64.dp)
                .background(color, RoundedCornerShape(tokens.spacing.md)),
        )
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = tokens.spacing.xs))
    }
}

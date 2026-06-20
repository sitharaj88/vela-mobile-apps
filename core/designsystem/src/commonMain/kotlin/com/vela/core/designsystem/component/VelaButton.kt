/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class VelaButtonVariant { Primary, Tonal, Outlined, Text }

/**
 * The one button every Vela screen uses. Variants map to Material 3 button styles so behavior,
 * accessibility, and theming stay consistent across the suite.
 */
@Composable
fun VelaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: VelaButtonVariant = VelaButtonVariant.Primary,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val content: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text)
        }
    }
    when (variant) {
        VelaButtonVariant.Primary -> Button(onClick, modifier, enabled = enabled) { content() }
        VelaButtonVariant.Tonal -> FilledTonalButton(onClick, modifier, enabled = enabled) { content() }
        VelaButtonVariant.Outlined -> OutlinedButton(onClick, modifier, enabled = enabled) { content() }
        VelaButtonVariant.Text -> TextButton(onClick, modifier, enabled = enabled) { content() }
    }
}

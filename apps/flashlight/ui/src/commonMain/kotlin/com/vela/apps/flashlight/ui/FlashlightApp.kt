/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.flashlight.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.flashlight.presentation.FlashlightIntent
import com.vela.apps.flashlight.presentation.FlashlightMode
import com.vela.apps.flashlight.presentation.FlashlightState
import com.vela.apps.flashlight.presentation.FlashlightStore
import com.vela.apps.flashlight.presentation.ScreenTint
import com.vela.core.designsystem.component.VelaCard
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.LocalVelaTokens
import com.vela.core.designsystem.theme.ThemeMode
import com.vela.core.designsystem.theme.VelaAccent
import com.vela.core.designsystem.theme.VelaTheme
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/** Root composable for the Flashlight app. */
@Composable
fun FlashlightApp(themeMode: ThemeMode = ThemeMode.System) {
    VelaTheme(accent = VelaAccent.Sunset, themeMode = themeMode, dynamicColor = true) {
        val store: FlashlightStore = koinViewModel()
        val state by store.state.collectAsStateWithLifecycle()
        FlashlightScreen(state = state, onIntent = store::onIntent)
    }
}

@Composable
private fun FlashlightScreen(
    state: FlashlightState,
    onIntent: (FlashlightIntent) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        VelaScaffold(title = "Vela Flashlight") { padding ->
            FlashlightControls(
                state = state,
                onIntent = onIntent,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
        // Full-screen light surface — the headline feature that works on every platform incl. desktop.
        ScreenLightOverlay(state = state, onIntent = onIntent)
    }
}

@Composable
private fun FlashlightControls(
    state: FlashlightState,
    onIntent: (FlashlightIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalVelaTokens.current
    Column(
        modifier = modifier.padding(tokens.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.lg),
    ) {
        PowerButton(
            isOn = state.mode != FlashlightMode.Off,
            onClick = { onIntent(FlashlightIntent.TogglePower) },
        )

        ModeSelector(state = state, onIntent = onIntent)

        if (state.mode == FlashlightMode.Strobe) {
            StrobeControls(state = state, onIntent = onIntent)
        }

        if (state.mode == FlashlightMode.ScreenLight) {
            ScreenLightControls(state = state, onIntent = onIntent)
        }

        if (!state.torchAvailable) {
            Text(
                text = "No hardware flash on this device — Screen Light, Strobe and SOS use the screen.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PowerButton(
    isOn: Boolean,
    onClick: () -> Unit,
) {
    val tokens = LocalVelaTokens.current
    val container = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val content = if (isOn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val label = if (isOn) "Turn off" else "Turn on"
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = container,
        contentColor = content,
        tonalElevation = tokens.elevation.level2,
        modifier = Modifier.size(180.dp).semantics { contentDescription = label },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.PowerSettingsNew,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
            )
        }
    }
}

private data class ModeOption(val mode: FlashlightMode, val label: String, val icon: ImageVector)

@Composable
private fun ModeSelector(
    state: FlashlightState,
    onIntent: (FlashlightIntent) -> Unit,
) {
    val options = buildList {
        if (state.torchAvailable) add(ModeOption(FlashlightMode.Torch, "Torch", Icons.Filled.FlashlightOn))
        add(ModeOption(FlashlightMode.ScreenLight, "Screen", Icons.Filled.LightMode))
        add(ModeOption(FlashlightMode.Strobe, "Strobe", Icons.Filled.Bolt))
        add(ModeOption(FlashlightMode.Sos, "SOS", Icons.Filled.Sos))
    }
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = state.mode == option.mode,
                onClick = { onIntent(FlashlightIntent.SelectMode(option.mode)) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                icon = { Icon(option.icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                label = { Text(option.label, style = MaterialTheme.typography.labelMedium) },
            )
        }
    }
}

@Composable
private fun StrobeControls(
    state: FlashlightState,
    onIntent: (FlashlightIntent) -> Unit,
) {
    val tokens = LocalVelaTokens.current
    VelaCard(modifier = Modifier.fillMaxWidth()) {
        LabeledRow(label = "Strobe rate", value = "${formatOneDecimal(state.strobeHz)} Hz")
        Spacer(Modifier.height(tokens.spacing.sm))
        Slider(
            value = state.strobeHz,
            onValueChange = { onIntent(FlashlightIntent.SetStrobeHz(it)) },
            valueRange = FlashlightState.MIN_STROBE_HZ..FlashlightState.MAX_STROBE_HZ,
            modifier = Modifier.semantics { contentDescription = "Strobe frequency in hertz" },
        )
    }
}

@Composable
private fun ScreenLightControls(
    state: FlashlightState,
    onIntent: (FlashlightIntent) -> Unit,
) {
    val tokens = LocalVelaTokens.current
    VelaCard(modifier = Modifier.fillMaxWidth()) {
        Text("Tint", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(tokens.spacing.sm))
        Row(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md)) {
            ScreenTint.entries.forEach { tint ->
                TintSwatch(
                    tint = tint,
                    selected = state.screenTint == tint,
                    onClick = { onIntent(FlashlightIntent.SetTint(tint)) },
                )
            }
        }
        Spacer(Modifier.height(tokens.spacing.lg))
        LabeledRow(label = "Brightness", value = "${(state.brightness * PERCENT).roundToInt()}%")
        Slider(
            value = state.brightness,
            onValueChange = { onIntent(FlashlightIntent.SetBrightness(it)) },
            modifier = Modifier.semantics { contentDescription = "Screen light brightness" },
        )
    }
}

@Composable
private fun TintSwatch(
    tint: ScreenTint,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = tintColor(tint, brightness = 1f),
        border = BorderStroke(if (selected) 3.dp else 1.dp, border),
        modifier = Modifier.size(44.dp).semantics { contentDescription = "${tint.name} tint" },
    ) {}
}

@Composable
private fun LabeledRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall)
        Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
    }
}

/** Full-screen bright surface for Screen-Light, Strobe(screen) and SOS(screen) modes. */
@Composable
private fun ScreenLightOverlay(
    state: FlashlightState,
    onIntent: (FlashlightIntent) -> Unit,
) {
    AnimatedVisibility(
        visible = state.screenLightActive,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        val lit = state.emitterOn
        val fill = if (lit) tintColor(state.screenTint, state.brightness) else Color.Black
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(fill)
                .clickable { onIntent(FlashlightIntent.TogglePower) },
        ) {
            IconButton(
                onClick = { onIntent(FlashlightIntent.TogglePower) },
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close screen light",
                    tint = closeIconTint(state),
                )
            }
        }
    }
}

private fun closeIconTint(state: FlashlightState): Color =
    if (state.emitterOn && state.brightness > HALF) Color.Black else Color.White

/** Map a [ScreenTint] + brightness to a concrete color (pure, platform-independent). */
private fun tintColor(tint: ScreenTint, brightness: Float): Color {
    val base = when (tint) {
        ScreenTint.White -> Color(0xFFFFFFFF)
        ScreenTint.Warm -> Color(0xFFFFE9C7)
        ScreenTint.Cool -> Color(0xFFCDE7FF)
        ScreenTint.Red -> Color(0xFFFF3B30)
    }
    val factor = brightness.coerceIn(MIN_VISIBLE_BRIGHTNESS, 1f)
    return Color(
        red = base.red * factor,
        green = base.green * factor,
        blue = base.blue * factor,
        alpha = 1f,
    )
}

/** Format a float with a single decimal place without JVM-only String.format. */
private fun formatOneDecimal(value: Float): String {
    val tenths = (value * TEN).roundToInt()
    val whole = tenths / TEN
    val frac = tenths % TEN
    return "$whole.$frac"
}

private const val PERCENT = 100f
private const val TEN = 10
private const val HALF = 0.5f
private const val MIN_VISIBLE_BRIGHTNESS = 0.05f

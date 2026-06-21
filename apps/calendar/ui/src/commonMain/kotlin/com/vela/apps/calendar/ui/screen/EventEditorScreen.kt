/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.model.Recurrence
import com.vela.apps.calendar.presentation.editor.EventEditorEffect
import com.vela.apps.calendar.presentation.editor.EventEditorIntent
import com.vela.apps.calendar.presentation.editor.EventEditorState
import com.vela.apps.calendar.presentation.editor.EventEditorStore
import com.vela.apps.calendar.presentation.editor.reminderPresets
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.LocalVelaTokens
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun EventEditorScreen(
    eventId: Long,
    epochDay: Long,
    onBack: () -> Unit,
    store: EventEditorStore = koinViewModel { parametersOf(eventId, epochDay) },
) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current

    LaunchedEffect(store) {
        store.effects.collect { effect ->
            when (effect) {
                EventEditorEffect.NavigateBack -> onBack()
            }
        }
    }

    VelaScaffold(
        title = if (state.isNew) "New event" else "Edit event",
        navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
        onNavigationClick = { store.onIntent(EventEditorIntent.Close) },
        actions = {
            if (!state.isNew) {
                IconButton(onClick = { store.onIntent(EventEditorIntent.Delete) }) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete event")
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(padding).padding(horizontal = tokens.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
        ) {
            item { TitleField(state.title) { store.onIntent(EventEditorIntent.TitleChanged(it)) } }
            item { LocationField(state.location) { store.onIntent(EventEditorIntent.LocationChanged(it)) } }
            item { AllDayRow(state.allDay) { store.onIntent(EventEditorIntent.AllDayChanged(it)) } }
            item { ScheduleSection(state, store) }
            item { SectionLabel("Color") }
            item { ColorPicker(state.colorIndex) { store.onIntent(EventEditorIntent.ColorChanged(it)) } }
            item { SectionLabel("Repeat") }
            item { RecurrencePicker(state.recurrence) { store.onIntent(EventEditorIntent.RecurrenceChanged(it)) } }
            item { SectionLabel("Reminder") }
            item { ReminderPicker(state.reminderMinutes) { store.onIntent(EventEditorIntent.ReminderChanged(it)) } }
            item { NotesField(state.description) { store.onIntent(EventEditorIntent.DescriptionChanged(it)) } }
        }
    }
}

@Composable
private fun TitleField(value: String, onChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Title", style = MaterialTheme.typography.titleLarge) },
        textStyle = MaterialTheme.typography.titleLarge,
        singleLine = true,
        colors = transparentFieldColors(),
    )
}

@Composable
private fun LocationField(value: String, onChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Location") },
        singleLine = true,
        colors = transparentFieldColors(),
    )
}

@Composable
private fun NotesField(value: String, onChange: (String) -> Unit) {
    val tokens = LocalVelaTokens.current
    TextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth().padding(top = tokens.spacing.sm),
        placeholder = { Text("Notes") },
        colors = transparentFieldColors(),
    )
}

@Composable
private fun AllDayRow(allDay: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("All day", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = allDay, onCheckedChange = onChange)
    }
}

@Composable
private fun ScheduleSection(state: EventEditorState, store: EventEditorStore) {
    val tokens = LocalVelaTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(tokens.spacing.xs)) {
        DateTimeRow(
            label = "Starts",
            date = state.startDate,
            time = state.startTime,
            showTime = !state.allDay,
            onDateDelta = { store.onIntent(EventEditorIntent.StartDateChanged(state.startDate.shift(it))) },
            onTimeDelta = { store.onIntent(EventEditorIntent.StartTimeChanged(state.startTime.shift(it))) },
        )
        DateTimeRow(
            label = "Ends",
            date = state.endDate,
            time = state.endTime,
            showTime = !state.allDay,
            onDateDelta = { store.onIntent(EventEditorIntent.EndDateChanged(state.endDate.shift(it))) },
            onTimeDelta = { store.onIntent(EventEditorIntent.EndTimeChanged(state.endTime.shift(it))) },
        )
    }
}

@Composable
private fun DateTimeRow(
    label: String,
    date: LocalDate,
    time: LocalTime,
    showTime: Boolean,
    onDateDelta: (Int) -> Unit,
    onTimeDelta: (Int) -> Unit,
) {
    val tokens = LocalVelaTokens.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Stepper(text = formatDateShort(date), onDown = { onDateDelta(-1) }, onUp = { onDateDelta(1) })
        if (showTime) {
            Stepper(
                text = formatTime(time),
                onDown = { onTimeDelta(-MINUTE_STEP) },
                onUp = { onTimeDelta(MINUTE_STEP) },
            )
        }
    }
}

@Composable
private fun Stepper(text: String, onDown: () -> Unit, onUp: () -> Unit) {
    val tokens = LocalVelaTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        AssistChip(onClick = onDown, label = { Text("–") })
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = tokens.spacing.sm),
        )
        AssistChip(onClick = onUp, label = { Text("+") })
    }
}

@Composable
private fun SectionLabel(text: String) {
    val tokens = LocalVelaTokens.current
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = tokens.spacing.sm),
    )
}

@Composable
private fun ColorPicker(selected: Int, onSelect: (Int) -> Unit) {
    val tokens = LocalVelaTokens.current
    Row(horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm)) {
        repeat(Event.COLOR_COUNT) { index ->
            val isSelected = index == selected
            val border = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent
            Box(
                modifier = Modifier.size(COLOR_SWATCH.dp).clip(CircleShape)
                    .background(eventLabelColor(index))
                    .border(SWATCH_BORDER.dp, border, CircleShape)
                    .clickable { onSelect(index) },
            )
        }
    }
}

@Composable
private fun RecurrencePicker(selected: Recurrence, onSelect: (Recurrence) -> Unit) {
    val tokens = LocalVelaTokens.current
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
    ) {
        Recurrence.entries.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(recurrenceChipLabel(option)) },
            )
        }
    }
}

@Composable
private fun ReminderPicker(selected: Int?, onSelect: (Int?) -> Unit) {
    val tokens = LocalVelaTokens.current
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
    ) {
        reminderPresets.forEach { minutes ->
            FilterChip(
                selected = minutes == selected,
                onClick = { onSelect(minutes) },
                label = { Text(reminderLabel(minutes)) },
            )
        }
    }
}

@Composable
private fun transparentFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
)

private fun recurrenceChipLabel(recurrence: Recurrence): String =
    if (recurrence == Recurrence.NONE) "None" else recurrenceLabel(recurrence)

private fun LocalDate.shift(days: Int): LocalDate = plus(days, DateTimeUnit.DAY)

private fun LocalTime.shift(minutes: Int): LocalTime {
    val total = (hour * MINUTES_PER_HOUR + minute + minutes).mod(MINUTES_PER_DAY)
    return LocalTime(total / MINUTES_PER_HOUR, total % MINUTES_PER_HOUR)
}

private const val MINUTE_STEP = 15
private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 1440
private const val COLOR_SWATCH = 32
private const val SWATCH_BORDER = 2

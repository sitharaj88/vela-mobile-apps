/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.presentation.month.CalendarState
import com.vela.apps.calendar.presentation.month.localDateOf
import com.vela.apps.calendar.presentation.month.weekDays
import com.vela.apps.calendar.presentation.month.weekdayLabels
import com.vela.core.designsystem.component.VelaEmptyState
import com.vela.core.designsystem.theme.LocalVelaTokens
import kotlinx.datetime.LocalDate

// ---------------------------------------------------------------------------------------------------
// Month
// ---------------------------------------------------------------------------------------------------

@Composable
fun MonthView(state: CalendarState, onSelectDate: (LocalDate) -> Unit, onOpenEvent: (Long) -> Unit) {
    WeekdayHeader()
    MonthGrid(
        weeks = state.weeks,
        visibleMonth = state.anchor.monthNumber,
        selected = state.selectedDate,
        datesWithEvents = state.datesWithEvents,
        onSelect = onSelectDate,
    )
    OccurrenceList(
        occurrences = state.selectedDayOccurrences,
        emptyTitle = "No events on this day",
        onOpen = onOpenEvent,
    )
}

@Composable
private fun WeekdayHeader() {
    Row(Modifier.fillMaxWidth()) {
        weekdayLabels().forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MonthGrid(
    weeks: List<List<LocalDate>>,
    visibleMonth: Int,
    selected: LocalDate,
    datesWithEvents: Set<Long>,
    onSelect: (LocalDate) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        weeks.forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    DayCell(
                        date = date,
                        inMonth = date.monthNumber == visibleMonth,
                        selected = date == selected,
                        hasEvents = datesWithEvents.contains(date.toEpochDays().toLong()),
                        onClick = { onSelect(date) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    inMonth: Boolean,
    selected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalVelaTokens.current
    val textColor = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        inMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }
    Box(
        modifier = modifier.aspectRatio(1f).padding(tokens.spacing.xs).clip(CircleShape)
            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium, color = textColor)
            Box(
                modifier = Modifier.padding(top = tokens.spacing.xs).size(tokens.spacing.xs).clip(CircleShape)
                    .background(
                        if (hasEvents && !selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    ),
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------------
// Week
// ---------------------------------------------------------------------------------------------------

@Composable
fun WeekView(state: CalendarState, onSelectDate: (LocalDate) -> Unit, onOpenEvent: (Long) -> Unit) {
    val tokens = LocalVelaTokens.current
    val days = weekDays(state.anchor)
    Row(Modifier.fillMaxWidth().padding(vertical = tokens.spacing.sm)) {
        days.forEach { date ->
            WeekDayChip(
                date = date,
                selected = date == state.selectedDate,
                hasEvents = state.datesWithEvents.contains(date.toEpochDays().toLong()),
                onClick = { onSelectDate(date) },
                modifier = Modifier.weight(1f),
            )
        }
    }
    OccurrenceList(
        occurrences = state.selectedDayOccurrences,
        emptyTitle = "No events on this day",
        onOpen = onOpenEvent,
    )
}

@Composable
private fun WeekDayChip(
    date: LocalDate,
    selected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalVelaTokens.current
    val container = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val onContainer =
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Column(
        modifier = modifier.padding(tokens.spacing.xs).clip(MaterialTheme.shapes.medium)
            .background(container).clickable(onClick = onClick).padding(vertical = tokens.spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = weekdayLabels()[date.dayOfWeek.ordinal],
            style = MaterialTheme.typography.labelSmall,
            color = onContainer.copy(alpha = WEEKDAY_LABEL_ALPHA),
        )
        Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, color = onContainer)
        Box(
            modifier = Modifier.padding(top = tokens.spacing.xs).size(tokens.spacing.xs).clip(CircleShape)
                .background(if (hasEvents && !selected) MaterialTheme.colorScheme.primary else Color.Transparent),
        )
    }
}

// ---------------------------------------------------------------------------------------------------
// Day
// ---------------------------------------------------------------------------------------------------

@Composable
fun DayView(state: CalendarState, onOpenEvent: (Long) -> Unit) {
    OccurrenceList(
        occurrences = state.occurrences,
        emptyTitle = "Nothing scheduled",
        onOpen = onOpenEvent,
    )
}

// ---------------------------------------------------------------------------------------------------
// Agenda
// ---------------------------------------------------------------------------------------------------

@Composable
fun AgendaView(state: CalendarState, onOpenEvent: (Long) -> Unit) {
    val tokens = LocalVelaTokens.current
    if (state.occurrences.isEmpty()) {
        VelaEmptyState(icon = Icons.Filled.EventBusy, title = "No upcoming events")
        return
    }
    val grouped = state.occurrences.groupBy { localDateOf(it.startMillis) }.toSortedMap()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = tokens.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
    ) {
        grouped.forEach { (date, dayOccurrences) ->
            item(key = "header-${date.toEpochDays()}") {
                Text(
                    text = formatDateFull(date),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = tokens.spacing.sm),
                )
            }
            items(dayOccurrences, key = { it.key }) { occurrence ->
                OccurrenceCard(occurrence) { onOpenEvent(occurrence.event.id) }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------------
// Search
// ---------------------------------------------------------------------------------------------------

@Composable
fun SearchResults(events: List<Event>, onOpen: (Long) -> Unit) {
    val tokens = LocalVelaTokens.current
    if (events.isEmpty()) {
        VelaEmptyState(icon = Icons.Filled.SearchOff, title = "No matching events")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = tokens.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
    ) {
        items(events, key = { it.id }) { event ->
            EventResultCard(event) { onOpen(event.id) }
        }
    }
}

private const val WEEKDAY_LABEL_ALPHA = 0.7f

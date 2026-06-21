/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vela.apps.calendar.presentation.month.CalendarEffect
import com.vela.apps.calendar.presentation.month.CalendarIntent
import com.vela.apps.calendar.presentation.month.CalendarState
import com.vela.apps.calendar.presentation.month.CalendarStore
import com.vela.apps.calendar.presentation.month.CalendarView
import com.vela.core.designsystem.component.VelaScaffold
import com.vela.core.designsystem.theme.LocalVelaTokens
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CalendarScreen(
    onOpenEditor: (Long, Long) -> Unit,
    store: CalendarStore = koinViewModel(),
) {
    val state by store.state.collectAsStateWithLifecycle()
    val tokens = LocalVelaTokens.current

    LaunchedEffect(store) {
        store.effects.collect { effect ->
            when (effect) {
                is CalendarEffect.OpenEditor -> onOpenEditor(effect.eventId, effect.epochDay)
            }
        }
    }

    VelaScaffold(
        title = "Vela Calendar",
        actions = {
            IconButton(onClick = { store.onIntent(CalendarIntent.GoToToday) }) {
                Icon(Icons.Filled.Today, contentDescription = "Go to today")
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { store.onIntent(CalendarIntent.AddEvent(state.selectedDate)) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add event")
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = tokens.spacing.lg)) {
            SearchField(
                query = state.searchQuery,
                onQueryChange = { store.onIntent(CalendarIntent.Search(it)) },
                onClear = { store.onIntent(CalendarIntent.ClearSearch) },
            )
            if (state.isSearching) {
                SearchResults(
                    events = state.searchResults,
                    onOpen = { store.onIntent(CalendarIntent.OpenEvent(it)) },
                )
            } else {
                CalendarBody(state = state, store = store)
            }
        }
    }
}

@Composable
private fun CalendarBody(state: CalendarState, store: CalendarStore) {
    ViewSelector(
        selected = state.view,
        onSelect = { store.onIntent(CalendarIntent.SelectView(it)) },
    )
    PeriodHeader(
        title = state.periodTitle,
        onPrev = { store.onIntent(CalendarIntent.Prev) },
        onNext = { store.onIntent(CalendarIntent.Next) },
    )
    when (state.view) {
        CalendarView.MONTH -> MonthView(
            state = state,
            onSelectDate = { store.onIntent(CalendarIntent.SelectDate(it)) },
            onOpenEvent = { store.onIntent(CalendarIntent.OpenEvent(it)) },
        )
        CalendarView.WEEK -> WeekView(
            state = state,
            onSelectDate = { store.onIntent(CalendarIntent.SelectDate(it)) },
            onOpenEvent = { store.onIntent(CalendarIntent.OpenEvent(it)) },
        )
        CalendarView.DAY -> DayView(
            state = state,
            onOpenEvent = { store.onIntent(CalendarIntent.OpenEvent(it)) },
        )
        CalendarView.AGENDA -> AgendaView(
            state = state,
            onOpenEvent = { store.onIntent(CalendarIntent.OpenEvent(it)) },
        )
    }
}

@Composable
private fun ViewSelector(selected: CalendarView, onSelect: (CalendarView) -> Unit) {
    val tokens = LocalVelaTokens.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = tokens.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
    ) {
        CalendarView.entries.forEach { view ->
            FilterChip(
                selected = view == selected,
                onClick = { onSelect(view) },
                label = { Text(view.label()) },
            )
        }
    }
}

@Composable
private fun PeriodHeader(title: String, onPrev: () -> Unit, onNext: () -> Unit) {
    val tokens = LocalVelaTokens.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = tokens.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrev) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next")
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, onClear: () -> Unit) {
    val tokens = LocalVelaTokens.current
    val colors = TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
    )
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(top = tokens.spacing.sm),
        placeholder = { Text("Search events") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        colors = colors,
    )
}

private fun CalendarView.label(): String = when (this) {
    CalendarView.MONTH -> "Month"
    CalendarView.WEEK -> "Week"
    CalendarView.DAY -> "Day"
    CalendarView.AGENDA -> "Agenda"
}

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vela.apps.calendar.domain.model.Event
import com.vela.apps.calendar.domain.model.EventOccurrence
import com.vela.apps.calendar.domain.model.Recurrence
import com.vela.apps.calendar.presentation.month.localDateOf
import com.vela.core.designsystem.component.VelaCard
import com.vela.core.designsystem.theme.LocalVelaTokens

/** Vertical list of occurrences with an empty state, used by Month/Week/Day day-lists. */
@Composable
fun OccurrenceList(occurrences: List<EventOccurrence>, emptyTitle: String, onOpen: (Long) -> Unit) {
    val tokens = LocalVelaTokens.current
    if (occurrences.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top = tokens.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Filled.EventNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = emptyTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = tokens.spacing.sm),
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(top = tokens.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
        ) {
            items(occurrences, key = { it.key }) { occurrence ->
                OccurrenceCard(occurrence) { onOpen(occurrence.event.id) }
            }
        }
    }
}

/** A single occurrence card with a color stripe, time label, title, location, and recurrence badge. */
@Composable
fun OccurrenceCard(occurrence: EventOccurrence, onClick: () -> Unit) {
    val tokens = LocalVelaTokens.current
    val event = occurrence.event
    VelaCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.Top) {
            ColorStripe(event.colorIndex)
            Column(Modifier.padding(start = tokens.spacing.md).weight(1f)) {
                Text(
                    text = occurrenceTimeLabel(occurrence),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = event.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                EventMeta(event)
            }
        }
    }
}

/** A search-result card (no per-occurrence time; shows the master event's date + meta). */
@Composable
fun EventResultCard(event: Event, onClick: () -> Unit) {
    val tokens = LocalVelaTokens.current
    VelaCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.Top) {
            ColorStripe(event.colorIndex)
            Column(Modifier.padding(start = tokens.spacing.md).weight(1f)) {
                Text(
                    text = formatDateShort(localDateOf(event.startMillis)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = event.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                EventMeta(event)
            }
        }
    }
}

@Composable
private fun EventMeta(event: Event) {
    val tokens = LocalVelaTokens.current
    if (event.location.isNotBlank()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = tokens.spacing.xs),
        ) {
            Icon(
                Icons.Filled.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(META_ICON.dp).height(META_ICON.dp),
            )
            Text(
                text = event.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = tokens.spacing.xs),
            )
        }
    }
    if (event.recurrence != Recurrence.NONE) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = tokens.spacing.xs),
        ) {
            Icon(
                Icons.Filled.Repeat,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(META_ICON.dp).height(META_ICON.dp),
            )
            Text(
                text = recurrenceLabel(event.recurrence),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = tokens.spacing.xs),
            )
        }
    }
}

@Composable
private fun ColorStripe(colorIndex: Int) {
    Box(
        modifier = Modifier.width(STRIPE_WIDTH.dp).height(STRIPE_HEIGHT.dp)
            .clip(RoundedCornerShape(STRIPE_RADIUS.dp))
            .background(eventLabelColor(colorIndex)),
    )
}

/** Title-cased recurrence label, e.g. `Weekly`. */
fun recurrenceLabel(recurrence: Recurrence): String =
    recurrence.name.lowercase().replaceFirstChar { it.titlecase() }

private const val META_ICON = 14
private const val STRIPE_WIDTH = 4
private const val STRIPE_HEIGHT = 40
private const val STRIPE_RADIUS = 2

/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.domain.model

/**
 * A single concrete occurrence of an [event] within a visible range. A non-recurring event yields
 * exactly one occurrence equal to its own start/end; a recurring event yields one per repeat that
 * falls inside the queried window.
 *
 * [startMillis]/[endMillis] are this occurrence's instant span (which differs from the master event's
 * for repeats after the first). [occurrenceStartMillis] equals [startMillis] and uniquely keys the
 * occurrence together with the event id, so list rendering stays stable.
 */
data class EventOccurrence(
    val event: Event,
    val startMillis: Long,
    val endMillis: Long,
) {
    val occurrenceStartMillis: Long get() = startMillis

    /** Stable composite key for list rendering (one master event can appear many times). */
    val key: String get() = "${event.id}@$startMillis"
}

/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calendar.domain.model

/** How an event repeats. Stored on the event and expanded into occurrences within a visible range. */
enum class Recurrence {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
}

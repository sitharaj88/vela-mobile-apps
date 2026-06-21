/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.domain

/**
 * A contact with its display name and reachable channels.
 *
 * @param id stable identifier. For system contacts this is the platform contact id; for locally
 *   created contacts (Desktop) it is the Room row id rendered as a string.
 * @param photoUri optional platform URI/path to a contact photo, when the platform exposes one.
 * @param isFavorite whether the user starred this contact (stored locally on read-only platforms).
 */
data class Contact(
    val id: String,
    val displayName: String,
    val phoneNumbers: List<String> = emptyList(),
    val emails: List<String> = emptyList(),
    val organization: String? = null,
    val photoUri: String? = null,
    val isFavorite: Boolean = false,
) {
    /** Upper-case monogram (1-2 letters) derived from the display name; "#" when empty. */
    val monogram: String get() = monogramOf(displayName)
}

/** First sortable letter of [name] in upper case, or '#' for names that don't start with a letter. */
fun sectionKeyOf(name: String): Char {
    val first = name.trim().firstOrNull()?.uppercaseChar() ?: '#'
    return if (first.isLetter()) first else '#'
}

/** Up to two initials for an avatar monogram (first letter of first and last word). */
fun monogramOf(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        words.isEmpty() -> "#"
        words.size == 1 -> words[0].take(1).uppercase()
        else -> (words.first().take(1) + words.last().take(1)).uppercase()
    }
}

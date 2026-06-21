/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.domain

/** One alphabetical bucket of contacts under a section [key] (e.g. "A", "B", "#"). */
data class ContactSection(
    val key: String,
    val contacts: List<Contact>,
)

/**
 * True when [contact] matches the search [query]: a blank query matches everything, otherwise the
 * query (case-insensitive, trimmed) must appear in the name, organization, a phone number, or an
 * email. Phone matching ignores formatting characters so "555 123" matches "+1 (555) 123-4567".
 */
fun contactMatches(contact: Contact, query: String): Boolean {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return true
    val needle = trimmed.lowercase()
    if (contact.displayName.lowercase().contains(needle)) return true
    if (contact.organization?.lowercase()?.contains(needle) == true) return true
    if (contact.emails.any { it.lowercase().contains(needle) }) return true
    val phoneNeedle = needle.filter { it.isDigit() }
    if (phoneNeedle.isNotEmpty() &&
        contact.phoneNumbers.any { it.filter(Char::isDigit).contains(phoneNeedle) }
    ) {
        return true
    }
    return contact.phoneNumbers.any { it.lowercase().contains(needle) }
}

/** Filters [contacts] by [contactMatches] against [query]. */
fun searchContacts(contacts: List<Contact>, query: String): List<Contact> =
    contacts.filter { contactMatches(it, query) }

/**
 * Groups [contacts] into alphabetical sections keyed by the first letter of the display name,
 * with non-letter names collected under "#". Sections and the contacts within them are sorted by
 * display name (case-insensitive); the "#" section sorts last.
 */
fun groupIntoSections(contacts: List<Contact>): List<ContactSection> =
    contacts
        .groupBy { sectionKeyOf(it.displayName) }
        .map { (key, items) ->
            ContactSection(
                key = key.toString(),
                contacts = items.sortedBy { it.displayName.lowercase() },
            )
        }
        .sortedWith(compareBy({ it.key == "#" }, { it.key }))

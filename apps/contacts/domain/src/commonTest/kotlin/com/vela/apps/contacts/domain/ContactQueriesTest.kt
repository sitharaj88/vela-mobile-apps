/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ContactQueriesTest {

    private val ada = Contact(
        id = "1",
        displayName = "Ada Lovelace",
        phoneNumbers = listOf("+1 (555) 123-4567"),
        emails = listOf("ada@analytical.org"),
        organization = "Analytical Engines",
    )
    private val alan = Contact(id = "2", displayName = "Alan Turing", emails = listOf("alan@bletchley.uk"))
    private val numeric = Contact(id = "3", displayName = "123 Diner")

    @Test
    fun blank_query_matches_everything() {
        assertTrue(contactMatches(ada, ""))
        assertTrue(contactMatches(ada, "   "))
    }

    @Test
    fun matches_name_case_insensitively() {
        assertTrue(contactMatches(ada, "lovel"))
        assertTrue(contactMatches(ada, "ADA"))
        assertFalse(contactMatches(ada, "turing"))
    }

    @Test
    fun matches_organization_and_email() {
        assertTrue(contactMatches(ada, "analytical"))
        assertTrue(contactMatches(ada, "ada@"))
    }

    @Test
    fun matches_phone_ignoring_formatting() {
        assertTrue(contactMatches(ada, "555 123"))
        assertTrue(contactMatches(ada, "5551234567"))
        assertFalse(contactMatches(ada, "999"))
    }

    @Test
    fun search_filters_list() {
        val result = searchContacts(listOf(ada, alan, numeric), "ala")
        assertEquals(listOf(alan), result)
    }

    @Test
    fun groups_into_sorted_sections_with_hash_last() {
        val sections = groupIntoSections(listOf(numeric, alan, ada))
        assertEquals(listOf("A", "#"), sections.map { it.key })
        // Within "A": Ada before Alan (alphabetical, case-insensitive).
        assertEquals(listOf("Ada Lovelace", "Alan Turing"), sections.first().contacts.map { it.displayName })
        assertEquals(listOf("123 Diner"), sections.last().contacts.map { it.displayName })
    }

    @Test
    fun monogram_uses_first_and_last_initials() {
        assertEquals("AL", monogramOf("Ada Lovelace"))
        assertEquals("A", monogramOf("Ada"))
        assertEquals("#", monogramOf("   "))
    }
}

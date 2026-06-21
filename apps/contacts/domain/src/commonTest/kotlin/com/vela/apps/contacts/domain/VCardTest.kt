/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VCardTest {

    @Test
    fun parses_fn_tel_email_org() {
        val card = """
            BEGIN:VCARD
            VERSION:3.0
            FN:Grace Hopper
            ORG:US Navy;Research
            TEL;TYPE=CELL:+1-555-0100
            EMAIL;TYPE=WORK:grace@navy.mil
            END:VCARD
        """.trimIndent()

        val contacts = VCard.parse(card)
        assertEquals(1, contacts.size)
        val c = contacts.single()
        assertEquals("Grace Hopper", c.displayName)
        assertEquals(listOf("+1-555-0100"), c.phoneNumbers)
        assertEquals(listOf("grace@navy.mil"), c.emails)
        assertEquals("US Navy", c.organization)
    }

    @Test
    fun parses_multiple_cards() {
        val text = """
            BEGIN:VCARD
            FN:First Person
            END:VCARD
            BEGIN:VCARD
            FN:Second Person
            TEL:123
            END:VCARD
        """.trimIndent()

        val contacts = VCard.parse(text)
        assertEquals(listOf("First Person", "Second Person"), contacts.map { it.displayName })
    }

    @Test
    fun falls_back_to_structured_name_when_no_fn() {
        val card = """
            BEGIN:VCARD
            N:Lovelace;Ada;;;
            END:VCARD
        """.trimIndent()

        assertEquals("Ada Lovelace", VCard.parse(card).single().displayName)
    }

    @Test
    fun skips_cards_without_a_name() {
        val card = """
            BEGIN:VCARD
            TEL:555
            END:VCARD
        """.trimIndent()

        assertTrue(VCard.parse(card).isEmpty())
    }

    @Test
    fun handles_line_folding_and_group_prefixes() {
        // RFC 6350 unfolding removes CRLF + one leading space WITHOUT inserting a space,
        // so a mid-token fold rejoins into "LongName". Group prefix "item1." is stripped.
        val card = "BEGIN:VCARD\r\nFN:Long\r\n Name\r\nitem1.TEL:42\r\nEND:VCARD"
        val c = VCard.parse(card).single()
        assertEquals("LongName", c.displayName)
        assertEquals(listOf("42"), c.phoneNumbers)
    }

    @Test
    fun write_then_parse_round_trips() {
        val original = Contact(
            id = "x",
            displayName = "Round Trip",
            phoneNumbers = listOf("111", "222"),
            emails = listOf("rt@example.com"),
            organization = "Acme",
        )
        val parsed = VCard.parse(VCard.write(original)).single()
        assertEquals(original.displayName, parsed.displayName)
        assertEquals(original.phoneNumbers, parsed.phoneNumbers)
        assertEquals(original.emails, parsed.emails)
        assertEquals(original.organization, parsed.organization)
    }
}

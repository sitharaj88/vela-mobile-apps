/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.domain

/**
 * Minimal vCard (RFC 6350) reader/writer covering the fields Vela models: FN (formatted name),
 * N (structured name fallback), TEL, EMAIL, and ORG. Property parameters (e.g. `TEL;TYPE=CELL`)
 * and group prefixes (e.g. `item1.TEL`) are tolerated and ignored. Good enough for desktop import.
 */
object VCard {

    private const val BEGIN = "BEGIN:VCARD"
    private const val END = "END:VCARD"

    /** Parses zero or more vCards from [text], skipping cards without any usable name. */
    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth")
    fun parse(text: String): List<Contact> {
        val contacts = mutableListOf<Contact>()
        var current: Builder? = null
        for (raw in unfold(text)) {
            val line = raw.trim()
            when {
                line.equals(BEGIN, ignoreCase = true) -> current = Builder()
                line.equals(END, ignoreCase = true) -> {
                    current?.build()?.let { contacts += it }
                    current = null
                }
                current != null -> current.consume(line)
            }
        }
        return contacts
    }

    /** Serializes a single [contact] to a vCard 3.0 string. */
    fun write(contact: Contact): String = buildString {
        append(BEGIN).append('\n')
        append("VERSION:3.0").append('\n')
        append("FN:").append(contact.displayName).append('\n')
        contact.organization?.takeIf { it.isNotBlank() }?.let { append("ORG:").append(it).append('\n') }
        contact.phoneNumbers.forEach { append("TEL:").append(it).append('\n') }
        contact.emails.forEach { append("EMAIL:").append(it).append('\n') }
        append(END).append('\n')
    }

    /** RFC 6350 line unfolding: a line starting with a space/tab continues the previous one. */
    private fun unfold(text: String): List<String> {
        val out = mutableListOf<String>()
        for (line in text.replace("\r\n", "\n").replace("\r", "\n").split("\n")) {
            if ((line.startsWith(" ") || line.startsWith("\t")) && out.isNotEmpty()) {
                out[out.lastIndex] = out.last() + line.substring(1)
            } else {
                out += line
            }
        }
        return out
    }

    private class Builder {
        private var fn: String? = null
        private var structuredName: String? = null
        private var org: String? = null
        private val phones = mutableListOf<String>()
        private val emails = mutableListOf<String>()

        fun consume(line: String) {
            val colon = line.indexOf(':')
            if (colon <= 0) return
            val name = propertyName(line.substring(0, colon))
            val value = line.substring(colon + 1).trim()
            if (value.isEmpty()) return
            when (name) {
                "FN" -> fn = value
                "N" -> structuredName = structuredNameToDisplay(value)
                "ORG" -> org = value.split(';').firstOrNull { it.isNotBlank() }?.trim()
                "TEL" -> if (value !in phones) phones += value
                "EMAIL" -> if (value !in emails) emails += value
            }
        }

        fun build(): Contact? {
            val display = fn?.takeIf { it.isNotBlank() } ?: structuredName?.takeIf { it.isNotBlank() }
            ?: return null
            return Contact(
                id = "",
                displayName = display,
                phoneNumbers = phones.toList(),
                emails = emails.toList(),
                organization = org,
            )
        }

        /** Drops any group prefix ("item1.TEL" -> "TEL") and parameters ("TEL;TYPE=CELL" -> "TEL"). */
        private fun propertyName(key: String): String =
            key.substringAfter('.').substringBefore(';').trim().uppercase()

        /** "Last;First;Middle;..." -> "First Middle Last". */
        private fun structuredNameToDisplay(value: String): String {
            val parts = value.split(';')
            val last = parts.getOrNull(0).orEmpty().trim()
            val first = parts.getOrNull(1).orEmpty().trim()
            val middle = parts.getOrNull(2).orEmpty().trim()
            return listOf(first, middle, last).filter { it.isNotEmpty() }.joinToString(" ")
        }
    }
}

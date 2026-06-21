/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileSortingTest {

    private fun node(
        name: String,
        isDir: Boolean = false,
        size: Long = 0L,
        modified: Long = 0L,
    ) = FileNode(
        path = "/root/$name",
        name = name,
        isDirectory = isDir,
        sizeBytes = size,
        lastModified = modified,
        extension = if (isDir) "" else name.substringAfterLast('.', ""),
    )

    @Test
    fun hidden_filter_drops_dot_files_unless_enabled() {
        val entries = listOf(node("visible.txt"), node(".secret"), node(".config", isDir = true))

        assertEquals(1, FileSorting.filterHidden(entries, showHidden = false).size)
        assertEquals(3, FileSorting.filterHidden(entries, showHidden = true).size)
    }

    @Test
    fun query_match_is_case_insensitive_and_blank_keeps_all() {
        val entries = listOf(node("Report.PDF"), node("notes.txt"), node("image.png"))

        assertEquals(listOf("Report.PDF"), FileSorting.matchingQuery(entries, "report").map { it.name })
        assertEquals(3, FileSorting.matchingQuery(entries, "   ").size)
    }

    @Test
    fun sort_always_places_folders_before_files() {
        val entries = listOf(node("aaa.txt"), node("zzz_folder", isDir = true), node("bbb.txt"))

        val sorted = FileSorting.sorted(entries, SortOrder(SortField.NAME, ascending = true))

        assertTrue(sorted.first().isDirectory)
        assertEquals(listOf("zzz_folder", "aaa.txt", "bbb.txt"), sorted.map { it.name })
    }

    @Test
    fun sort_by_size_descending_orders_largest_file_first() {
        val entries = listOf(node("small.txt", size = 10), node("big.bin", size = 9_000), node("mid.dat", size = 500))

        val sorted = FileSorting.sorted(entries, SortOrder(SortField.SIZE, ascending = false))

        assertEquals(listOf("big.bin", "mid.dat", "small.txt"), sorted.map { it.name })
    }

    @Test
    fun sort_by_date_ascending_orders_oldest_first() {
        val entries = listOf(
            node("new.txt", modified = 300),
            node("old.txt", modified = 100),
            node("mid.txt", modified = 200),
        )

        val sorted = FileSorting.sorted(entries, SortOrder(SortField.DATE, ascending = true))

        assertEquals(listOf("old.txt", "mid.txt", "new.txt"), sorted.map { it.name })
    }

    @Test
    fun pipeline_filters_then_sorts() {
        val entries = listOf(
            node(".hidden_report.txt"),
            node("Report_final.txt"),
            node("report_draft", isDir = true),
            node("unrelated.png"),
        )

        val result = FileSorting.pipeline(
            entries = entries,
            query = "report",
            showHidden = false,
            order = SortOrder(SortField.NAME, ascending = true),
        )

        assertEquals(listOf("report_draft", "Report_final.txt"), result.map { it.name })
        assertFalse(result.any { it.isHidden })
    }

    @Test
    fun file_node_marks_dot_prefixed_names_hidden() {
        assertTrue(node(".env").isHidden)
        assertFalse(node("env").isHidden)
    }
}

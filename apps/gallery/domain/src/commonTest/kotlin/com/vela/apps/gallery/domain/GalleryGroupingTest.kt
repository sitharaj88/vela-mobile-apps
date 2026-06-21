/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GalleryGroupingTest {

    private fun item(
        id: String,
        name: String,
        folder: String,
        date: Long,
        kind: MediaKind = MediaKind.Image,
    ) = MediaItem(
        id = id,
        uri = "file:///$folder/$name",
        name = name,
        kind = kind,
        sizeBytes = 0,
        dateModifiedMs = date,
        folder = folder,
    )

    private val sample = listOf(
        item("1", "beach.jpg", "Camera", date = 30),
        item("2", "cat.png", "Camera", date = 10),
        item("3", "Screenshot.png", "Screenshots", date = 20),
        item("4", "clip.mp4", "Camera", date = 40, kind = MediaKind.Video),
    )

    @Test
    fun sort_date_desc_orders_newest_first() {
        val sorted = GalleryGrouping.sort(sample, MediaSort.DateDesc)
        assertEquals(listOf("4", "1", "3", "2"), sorted.map { it.id })
    }

    @Test
    fun sort_date_asc_orders_oldest_first() {
        val sorted = GalleryGrouping.sort(sample, MediaSort.DateAsc)
        assertEquals(listOf("2", "3", "1", "4"), sorted.map { it.id })
    }

    @Test
    fun sort_name_is_case_insensitive() {
        val sorted = GalleryGrouping.sort(sample, MediaSort.NameAsc)
        assertEquals(listOf("beach.jpg", "cat.png", "clip.mp4", "Screenshot.png"), sorted.map { it.name })
    }

    @Test
    fun filter_matches_name_substring_ignoring_case() {
        val filtered = GalleryGrouping.filter(sample, "CAT")
        assertEquals(listOf("2"), filtered.map { it.id })
    }

    @Test
    fun filter_blank_returns_all() {
        assertEquals(sample.size, GalleryGrouping.filter(sample, "   ").size)
    }

    @Test
    fun albums_group_by_folder_with_count_and_newest_cover() {
        val albums = GalleryGrouping.albums(sample)
        assertEquals(listOf("Camera", "Screenshots"), albums.map { it.name })
        val camera = albums.first { it.name == "Camera" }
        assertEquals(3, camera.itemCount)
        assertEquals("file:///Camera/clip.mp4", camera.coverUri)
        assertEquals(40, camera.newestDateMs)
    }

    @Test
    fun items_in_album_filters_by_folder() {
        val camera = GalleryGrouping.itemsInAlbum(sample, "Camera")
        assertTrue(camera.all { it.folder == "Camera" })
        assertEquals(3, camera.size)
    }

    @Test
    fun items_in_all_media_album_returns_everything() {
        assertEquals(sample.size, GalleryGrouping.itemsInAlbum(sample, ALL_MEDIA_ALBUM).size)
    }
}

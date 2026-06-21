/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MusicLibraryGroupingTest {

    private fun track(
        id: String,
        title: String,
        artist: String = "Artist",
        album: String = "Album",
    ) = Track(id = id, uri = "file://$id", title = title, artist = artist, album = album, durationMs = 1000)

    @Test
    fun sortedByTitle_orders_case_insensitively_then_by_artist() {
        val tracks = listOf(
            track("1", "banana", artist = "Zed"),
            track("2", "Apple"),
            track("3", "apple", artist = "Al"),
        )
        val titles = tracks.sortedByTitle().map { it.title }
        // "Apple" and "apple" tie on title; ordered by artist (Al before Artist).
        assertEquals(listOf("apple", "Apple", "banana"), titles)
    }

    @Test
    fun groupIntoAlbums_groups_by_album_and_sorts_by_name() {
        val tracks = listOf(
            track("1", "T1", album = "Zebra"),
            track("2", "T2", album = "Alpha"),
            track("3", "T3", album = "Alpha"),
        )
        val albums = tracks.groupIntoAlbums()
        assertEquals(listOf("Alpha", "Zebra"), albums.map { it.name })
        assertEquals(2, albums.first().trackCount)
    }

    @Test
    fun groupIntoAlbums_marks_various_artists_when_artists_differ() {
        val tracks = listOf(
            track("1", "T1", artist = "A", album = "Mix"),
            track("2", "T2", artist = "B", album = "Mix"),
        )
        assertEquals("Various artists", tracks.groupIntoAlbums().single().artist)
    }

    @Test
    fun groupIntoAlbums_folds_blank_album_into_unknown() {
        val albums = listOf(track("1", "T1", album = "")).groupIntoAlbums()
        assertEquals("Unknown album", albums.single().name)
    }

    @Test
    fun groupIntoArtists_counts_distinct_albums() {
        val tracks = listOf(
            track("1", "T1", artist = "Solo", album = "One"),
            track("2", "T2", artist = "Solo", album = "Two"),
            track("3", "T3", artist = "Solo", album = "Two"),
        )
        val artist = tracks.groupIntoArtists().single()
        assertEquals(3, artist.trackCount)
        assertEquals(2, artist.albumCount)
    }

    @Test
    fun groupIntoArtists_sorts_by_name_and_keeps_track_title_order() {
        val tracks = listOf(
            track("1", "Zulu", artist = "Beta"),
            track("2", "Alpha", artist = "Beta"),
            track("3", "Mike", artist = "Alpha"),
        )
        val artists = tracks.groupIntoArtists()
        assertEquals(listOf("Alpha", "Beta"), artists.map { it.name })
        val beta = artists.first { it.name == "Beta" }
        assertEquals(listOf("Alpha", "Zulu"), beta.tracks.map { it.title })
    }

    @Test
    fun empty_input_produces_empty_groupings() {
        assertTrue(emptyList<Track>().groupIntoAlbums().isEmpty())
        assertTrue(emptyList<Track>().groupIntoArtists().isEmpty())
    }
}

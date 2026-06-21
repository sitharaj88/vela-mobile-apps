/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.domain

/** A single audio track surfaced by a [MusicLibrary]. */
data class Track(
    val id: String,
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
)

/** A group of [tracks] that share the same album name, with the contributing artist(s). */
data class Album(
    val name: String,
    val artist: String,
    val tracks: List<Track>,
) {
    val trackCount: Int get() = tracks.size
}

/** A group of [tracks] performed by the same artist. */
data class Artist(
    val name: String,
    val tracks: List<Track>,
) {
    val trackCount: Int get() = tracks.size
    val albumCount: Int get() = tracks.map { it.album }.distinct().size
}

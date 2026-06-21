/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.domain

/** Reads the device/host audio library. Each platform supplies its own implementation. */
interface MusicLibrary {
    /** All music tracks known to the platform, already sorted by title (case-insensitive). */
    suspend fun queryTracks(): List<Track>

    /** Tracks grouped into [Album]s. Default groups the result of [queryTracks]. */
    suspend fun queryAlbums(): List<Album> = queryTracks().groupIntoAlbums()

    /** Tracks grouped into [Artist]s. Default groups the result of [queryTracks]. */
    suspend fun queryArtists(): List<Artist> = queryTracks().groupIntoArtists()
}

private const val UNKNOWN_ALBUM = "Unknown album"
private const val UNKNOWN_ARTIST = "Unknown artist"

/** Sorts tracks by title, then artist, both case-insensitively. Pure and total. */
fun List<Track>.sortedByTitle(): List<Track> =
    sortedWith(compareBy({ it.title.lowercase() }, { it.artist.lowercase() }))

/**
 * Groups tracks by album name (blank album folds into [UNKNOWN_ALBUM]). Albums are sorted by name;
 * the tracks inside each album keep title order.
 */
fun List<Track>.groupIntoAlbums(): List<Album> =
    groupBy { it.album.ifBlank { UNKNOWN_ALBUM } }
        .map { (name, tracks) ->
            Album(
                name = name,
                artist = tracks.map { it.artist }.distinct().singleOrNull() ?: "Various artists",
                tracks = tracks.sortedByTitle(),
            )
        }
        .sortedBy { it.name.lowercase() }

/**
 * Groups tracks by artist name (blank artist folds into [UNKNOWN_ARTIST]). Artists are sorted by
 * name; the tracks inside each artist keep title order.
 */
fun List<Track>.groupIntoArtists(): List<Artist> =
    groupBy { it.artist.ifBlank { UNKNOWN_ARTIST } }
        .map { (name, tracks) -> Artist(name = name, tracks = tracks.sortedByTitle()) }
        .sortedBy { it.name.lowercase() }

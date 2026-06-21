/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.musicplayer.data

import com.vela.apps.musicplayer.domain.MusicLibrary
import com.vela.apps.musicplayer.domain.Track
import java.io.File

/**
 * Recursively scans `user.home/Music` for common audio files. Metadata is inferred from the path:
 * the file name is the title, its parent folder the album, and the grandparent folder the artist
 * (the conventional `Music/<Artist>/<Album>/<track>` layout). Missing levels degrade to "Unknown".
 */
internal class DesktopMusicLibrary : MusicLibrary {

    override suspend fun queryTracks(): List<Track> {
        val musicDir = File(System.getProperty("user.home"), "Music")
        if (!musicDir.isDirectory) return emptyList()
        return musicDir.walkTopDown()
            .filter { it.isFile && it.extension.lowercase() in AUDIO_EXTENSIONS }
            .map { file -> file.toTrack(musicDir) }
            .sortedBy { it.title.lowercase() }
            .toList()
    }

    private fun File.toTrack(root: File): Track {
        val parent = parentFile
        val grandparent = parent?.parentFile
        val album = parent?.takeIf { it != root }?.name ?: "Unknown album"
        val artist = grandparent?.takeIf { it != root && it.startsWith(root) }?.name ?: "Unknown artist"
        return Track(
            id = absolutePath,
            uri = toURI().toString(),
            title = nameWithoutExtension,
            artist = artist,
            album = album,
            durationMs = 0L,
        )
    }

    private fun File.startsWith(other: File): Boolean =
        absolutePath.startsWith(other.absolutePath)

    private companion object {
        val AUDIO_EXTENSIONS = setOf("wav", "mp3", "flac", "ogg", "aac", "m4a", "aiff", "au")
    }
}

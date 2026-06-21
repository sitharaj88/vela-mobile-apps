/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.data

import com.vela.apps.gallery.domain.MediaItem
import com.vela.apps.gallery.domain.MediaKind
import com.vela.apps.gallery.domain.MediaSource
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

/**
 * Desktop recursively scans the user's `Pictures` and `Videos` directories (bounded depth) for
 * image/video files by extension and groups them by their parent folder name.
 */
actual fun galleryPlatformModule(): Module = module {
    single<MediaSource> { DesktopMediaSource() }
}

private class DesktopMediaSource : MediaSource {

    private val home = File(System.getProperty("user.home"))
    private val roots = listOf(File(home, "Pictures"), File(home, "Videos"))

    @Suppress("TooGenericExceptionCaught") // Filesystem walks can throw on permissions/symlinks.
    override suspend fun allItems(): List<MediaItem> =
        roots.filter { it.isDirectory }.flatMap { root ->
            try {
                root.walkTopDown()
                    .maxDepth(MAX_DEPTH)
                    .filter { it.isFile && it.extension.lowercase() in MEDIA_EXTENSIONS }
                    .map { it.toMediaItem(root) }
                    .toList()
            } catch (error: Exception) {
                emptyList()
            }
        }

    private fun File.toMediaItem(root: File): MediaItem {
        val ext = extension.lowercase()
        val parent = parentFile
        // Files directly under the root get the root's name; nested files keep their folder name.
        val folder = if (parent == null || parent == root) root.name else parent.name
        return MediaItem(
            id = absolutePath,
            uri = toURI().toString(),
            name = name,
            kind = if (ext in VIDEO_EXTENSIONS) MediaKind.Video else MediaKind.Image,
            sizeBytes = length(),
            dateModifiedMs = lastModified(),
            folder = folder,
        )
    }

    private companion object {
        const val MAX_DEPTH = 6
        val VIDEO_EXTENSIONS = setOf("mp4", "mkv", "webm", "mov", "avi", "m4v")
        val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic")
        val MEDIA_EXTENSIONS = IMAGE_EXTENSIONS + VIDEO_EXTENSIONS
    }
}

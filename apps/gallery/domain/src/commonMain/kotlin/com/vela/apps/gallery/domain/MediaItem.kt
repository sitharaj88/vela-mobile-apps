/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.domain

/** Whether a [MediaItem] is a still image or a video clip. */
enum class MediaKind { Image, Video }

/**
 * A single browsable photo or video.
 *
 * @property id stable identity, unique across the whole library.
 * @property uri platform-resolvable location (a MediaStore content uri on Android, a `file://`
 *   url on desktop/iOS) that Coil can load directly.
 * @property name display file name.
 * @property kind image or video.
 * @property sizeBytes file size in bytes (0 when unknown).
 * @property dateModifiedMs last-modified time as epoch millis.
 * @property folder the parent album/folder name this item belongs to.
 */
data class MediaItem(
    val id: String,
    val uri: String,
    val name: String,
    val kind: MediaKind,
    val sizeBytes: Long,
    val dateModifiedMs: Long,
    val folder: String,
) {
    val isVideo: Boolean get() = kind == MediaKind.Video
}

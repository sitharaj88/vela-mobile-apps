/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.domain

/** How a media list is ordered. Each value carries both a field and a direction. */
enum class MediaSort {
    DateDesc,
    DateAsc,
    NameAsc,
    NameDesc,
}

/**
 * Pure, platform-agnostic grouping/sorting/filtering used by the presentation layer. Keeping this
 * here (instead of in the store) makes the logic unit-testable without coroutines or Compose.
 */
object GalleryGrouping {

    /** Orders [items] by the chosen [sort]. Name comparisons are case-insensitive. */
    fun sort(items: List<MediaItem>, sort: MediaSort): List<MediaItem> = when (sort) {
        MediaSort.DateDesc -> items.sortedByDescending { it.dateModifiedMs }
        MediaSort.DateAsc -> items.sortedBy { it.dateModifiedMs }
        MediaSort.NameAsc -> items.sortedBy { it.name.lowercase() }
        MediaSort.NameDesc -> items.sortedByDescending { it.name.lowercase() }
    }

    /** Case-insensitive substring filter over file name; blank query returns [items] unchanged. */
    fun filter(items: List<MediaItem>, query: String): List<MediaItem> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return items
        return items.filter { it.name.contains(trimmed, ignoreCase = true) }
    }

    /**
     * Collapses [items] into albums keyed by [MediaItem.folder]. Each album's cover and date come
     * from its newest item; albums are returned newest-first.
     */
    fun albums(items: List<MediaItem>): List<Album> =
        items.groupBy { it.folder }
            .map { (folder, group) ->
                val newest = group.maxByOrNull { it.dateModifiedMs }
                Album(
                    name = folder,
                    itemCount = group.size,
                    coverUri = newest?.uri,
                    newestDateMs = newest?.dateModifiedMs ?: 0L,
                )
            }
            .sortedByDescending { it.newestDateMs }

    /** Items belonging to [album], or the full list for the synthetic [ALL_MEDIA_ALBUM]. */
    fun itemsInAlbum(items: List<MediaItem>, album: String): List<MediaItem> =
        if (album == ALL_MEDIA_ALBUM) items else items.filter { it.folder == album }
}

/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.domain

/**
 * A folder/bucket of media, summarized for the albums grid.
 *
 * @property name folder name (also the key used to fetch its items).
 * @property itemCount number of media items in the folder.
 * @property coverUri uri of the most-recent item, used as the album thumbnail.
 * @property newestDateMs the most-recent item's date, used for sorting albums.
 */
data class Album(
    val name: String,
    val itemCount: Int,
    val coverUri: String?,
    val newestDateMs: Long,
)

/** Identifier of the synthetic album that aggregates every item in the library. */
const val ALL_MEDIA_ALBUM = "All media"

/** Identifier of the synthetic album that lists favorited items. */
const val FAVORITES_ALBUM = "Favorites"

/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.domain

/**
 * Enumerates the device's photos and videos. Each platform supplies its own implementation
 * (Android MediaStore, desktop filesystem scan, iOS Photos).
 *
 * Implementations return a flat list from [allItems]; albums and per-album listings are derived
 * with the pure helpers in [GalleryGrouping] so the grouping logic stays testable and shared.
 */
interface MediaSource {

    /** Every image and video in the library, unsorted (callers sort/group as needed). */
    suspend fun allItems(): List<MediaItem>
}

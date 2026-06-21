/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.data

import com.vela.apps.gallery.domain.MediaItem
import com.vela.apps.gallery.domain.MediaSource
import org.koin.core.module.Module
import org.koin.dsl.module

/** iOS binds a best-effort Photos source. */
actual fun galleryPlatformModule(): Module = module {
    single<MediaSource> { IosMediaSource() }
}

private class IosMediaSource : MediaSource {
    // TODO(ios): enumerate PHAsset via PHFetchResult, request a content/asset URL per asset with
    //  PHImageManager / requestContentEditingInputForAsset, group by PHAssetCollection (album), and
    //  map to MediaItem(kind = ...). Photos cinterop only compiles on macOS, so we return empty for
    //  now to keep the iOS framework building. The grouping helpers in GalleryGrouping work as-is
    //  once real items are produced here.
    override suspend fun allItems(): List<MediaItem> = emptyList()
}

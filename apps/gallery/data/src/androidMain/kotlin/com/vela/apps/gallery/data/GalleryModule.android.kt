/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.gallery.data

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.vela.apps.gallery.domain.MediaItem
import com.vela.apps.gallery.domain.MediaKind
import com.vela.apps.gallery.domain.MediaSource
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android reads images and videos from `MediaStore` via the app [ContentResolver]. */
actual fun galleryPlatformModule(): Module = module {
    single<MediaSource> { AndroidMediaSource(androidContext()) }
}

private class AndroidMediaSource(context: Context) : MediaSource {

    private val resolver: ContentResolver = context.contentResolver

    override suspend fun allItems(): List<MediaItem> = queryImages() + queryVideos()

    private fun queryImages(): List<MediaItem> =
        query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaKind.Image)

    private fun queryVideos(): List<MediaItem> =
        query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, MediaKind.Video)

    @Suppress("TooGenericExceptionCaught") // MediaStore queries throw varied runtime errors.
    private fun query(collection: Uri, kind: MediaKind): List<MediaItem> {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
        )
        val sort = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
        val items = mutableListOf<MediaItem>()
        try {
            resolver.query(collection, projection, null, null, sort)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val bucketCol =
                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(collection, id)
                    items += MediaItem(
                        id = "$id-${if (kind == MediaKind.Video) "v" else "i"}",
                        uri = uri.toString(),
                        name = cursor.getString(nameCol).orEmpty(),
                        kind = kind,
                        sizeBytes = cursor.getLong(sizeCol),
                        dateModifiedMs = cursor.getLong(dateCol) * MILLIS_PER_SECOND,
                        folder = cursor.getString(bucketCol) ?: UNKNOWN_FOLDER,
                    )
                }
            }
        } catch (error: Exception) {
            return emptyList()
        }
        return items
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1000L
        const val UNKNOWN_FOLDER = "Other"
    }
}

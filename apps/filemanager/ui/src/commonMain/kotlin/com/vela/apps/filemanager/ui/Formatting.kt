/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.ui.graphics.vector.ImageVector
import com.vela.apps.filemanager.domain.FileNode
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private const val UNIT_STEP = 1024.0
private val UNITS = listOf("B", "KB", "MB", "GB", "TB")

/**
 * KMP-safe human-readable byte size (no `String.format`). One decimal place above bytes, computed
 * with manual rounding + string padding.
 */
fun humanReadableSize(bytes: Long): String {
    if (bytes < UNIT_STEP) return "$bytes B"
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= UNIT_STEP && unitIndex < UNITS.lastIndex) {
        value /= UNIT_STEP
        unitIndex++
    }
    val scaled = (value * 10).toLong() // one decimal place
    val whole = scaled / 10
    val decimal = scaled % 10
    return "$whole.$decimal ${UNITS[unitIndex]}"
}

/** Local date + time as `yyyy-MM-dd HH:mm`, built with kotlinx-datetime and manual padding. */
fun formatModified(epochMillis: Long): String {
    if (epochMillis <= 0L) return "—"
    val dateTime = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    val month = dateTime.monthNumber.toString().padStart(2, '0')
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')
    return "${dateTime.year}-$month-$day $hour:$minute"
}

/** Row subtitle: directories show date only; files show size + date. */
fun subtitleFor(node: FileNode): String {
    val modified = formatModified(node.lastModified)
    return if (node.isDirectory) modified else "${humanReadableSize(node.sizeBytes)} · $modified"
}

private val IMAGE_EXT = setOf("png", "jpg", "jpeg", "gif", "bmp", "webp", "heic", "svg")
private val VIDEO_EXT = setOf("mp4", "mkv", "mov", "avi", "webm", "m4v")
private val AUDIO_EXT = setOf("mp3", "wav", "flac", "aac", "ogg", "m4a")
private val ARCHIVE_EXT = setOf("zip", "rar", "7z", "tar", "gz", "bz2")
private val CODE_EXT = setOf("kt", "java", "js", "ts", "py", "c", "cpp", "rs", "go", "swift", "json", "xml", "html")
private val DOC_EXT = setOf("txt", "md", "doc", "docx", "rtf", "odt")

/** Maps a node to a Material type icon based on its extension. */
fun iconFor(node: FileNode): ImageVector {
    if (node.isDirectory) return Icons.Filled.Folder
    return when (node.extension) {
        in IMAGE_EXT -> Icons.Filled.Image
        in VIDEO_EXT -> Icons.Filled.VideoFile
        in AUDIO_EXT -> Icons.Filled.AudioFile
        in ARCHIVE_EXT -> Icons.Filled.Archive
        in CODE_EXT -> Icons.Filled.Code
        in DOC_EXT -> Icons.Filled.Description
        "pdf" -> Icons.Filled.PictureAsPdf
        else -> Icons.Filled.InsertDriveFile
    }
}

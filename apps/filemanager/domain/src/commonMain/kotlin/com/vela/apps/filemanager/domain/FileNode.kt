/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.domain

/** A single entry (file or folder) in a directory listing. */
data class FileNode(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val extension: String,
) {
    /** True when [name] starts with a dot — used by the show-hidden toggle. */
    val isHidden: Boolean get() = name.startsWith(".")
}

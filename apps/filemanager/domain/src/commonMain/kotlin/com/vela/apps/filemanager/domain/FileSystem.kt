/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.domain

/**
 * A read/write view over an app-accessible portion of the platform file system.
 *
 * Implementations are platform-specific (java.nio on Desktop/Android, NSFileManager on iOS) but
 * share this contract so domain + presentation stay pure. All calls are blocking; callers move them
 * off the main thread.
 */
interface FileSystem {

    /** The starting directories the browser may open (storage roots, user home, sandbox, ...). */
    fun roots(): List<FileNode>

    /** Lists the immediate children of [path]; never recursive. Empty when unreadable. */
    fun list(path: String): List<FileNode>

    /** The parent directory of [path], or null when [path] has no accessible parent. */
    fun parentOf(path: String): String?

    /** Creates a directory named [name] under [parentPath]; returns the new node or null on failure. */
    fun createFolder(parentPath: String, name: String): FileNode?

    /** Renames the entry at [path] to [newName] (same parent); returns the new node or null. */
    fun rename(path: String, newName: String): FileNode?

    /** Deletes the entry at [path] (recursively for directories); returns true on success. */
    fun delete(path: String): Boolean

    /** Copies the entry at [sourcePath] into directory [targetDir]; returns the new node or null. */
    fun copy(sourcePath: String, targetDir: String): FileNode?

    /** Moves the entry at [sourcePath] into directory [targetDir]; returns the new node or null. */
    fun move(sourcePath: String, targetDir: String): FileNode?

    /** Recursively searches under [path] for entries whose name contains [query] (case-insensitive). */
    fun search(path: String, query: String): List<FileNode>
}

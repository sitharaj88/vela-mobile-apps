/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.data

import com.vela.apps.filemanager.domain.FileNode
import com.vela.apps.filemanager.domain.FileSystem
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSDate
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileModificationDate
import platform.Foundation.NSFileSize
import platform.Foundation.NSFileType
import platform.Foundation.NSFileTypeDirectory
import platform.Foundation.NSNumber
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970

/** iOS root is the app-sandbox Documents directory (no permission required). */
@OptIn(ExperimentalForeignApi::class)
actual fun fileSystemPlatformModule(): Module = module {
    single<FileSystem> { IosFileSystem() }
}

@OptIn(ExperimentalForeignApi::class)
private class IosFileSystem : FileSystem {

    private val manager = NSFileManager.defaultManager

    private val rootPath: String = manager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )?.path.orEmpty()

    override fun roots(): List<FileNode> = if (rootPath.isEmpty()) emptyList() else listOf(toNode(rootPath))

    @Suppress("UNCHECKED_CAST")
    override fun list(path: String): List<FileNode> {
        val names = manager.contentsOfDirectoryAtPath(path, error = null) as? List<String> ?: return emptyList()
        return names.map { toNode("$path/$it") }
    }

    override fun parentOf(path: String): String? {
        if (path == rootPath) return null
        val parent = path.substringBeforeLast('/', missingDelimiterValue = "")
        return parent.ifEmpty { null }
    }

    override fun createFolder(parentPath: String, name: String): FileNode? {
        val target = "$parentPath/$name"
        val ok = manager.createDirectoryAtPath(
            target,
            withIntermediateDirectories = false,
            attributes = null,
            error = null,
        )
        return if (ok) toNode(target) else null
    }

    override fun rename(path: String, newName: String): FileNode? {
        val parent = path.substringBeforeLast('/', missingDelimiterValue = "")
        val target = "$parent/$newName"
        val ok = manager.moveItemAtPath(path, toPath = target, error = null)
        return if (ok) toNode(target) else null
    }

    override fun delete(path: String): Boolean = manager.removeItemAtPath(path, error = null)

    override fun copy(sourcePath: String, targetDir: String): FileNode? {
        val target = "$targetDir/${sourcePath.substringAfterLast('/')}"
        val ok = manager.copyItemAtPath(sourcePath, toPath = target, error = null)
        return if (ok) toNode(target) else null
    }

    override fun move(sourcePath: String, targetDir: String): FileNode? {
        val target = "$targetDir/${sourcePath.substringAfterLast('/')}"
        val ok = manager.moveItemAtPath(sourcePath, toPath = target, error = null)
        return if (ok) toNode(target) else null
    }

    override fun search(path: String, query: String): List<FileNode> {
        val needle = query.trim()
        if (needle.isEmpty()) return emptyList()
        val matches = mutableListOf<FileNode>()
        searchInto(path, needle, matches)
        return matches
    }

    private fun searchInto(path: String, needle: String, sink: MutableList<FileNode>) {
        list(path).forEach { node ->
            if (node.name.contains(needle, ignoreCase = true)) sink += node
            if (node.isDirectory) searchInto(node.path, needle, sink)
        }
    }

    private fun toNode(path: String): FileNode {
        val isDir = isDirectory(path)
        val name = path.substringAfterLast('/')
        // TODO(ios): verify attribute keys (NSFileSize / NSFileModificationDate) on device.
        val attrs = manager.attributesOfItemAtPath(path, error = null)
        val sizeBytes = (attrs?.get(NSFileSize) as? NSNumber)?.longLongValue ?: 0L
        val modifiedSeconds = (attrs?.get(NSFileModificationDate) as? NSDate)?.timeIntervalSince1970 ?: 0.0
        return FileNode(
            path = path,
            name = name,
            isDirectory = isDir,
            sizeBytes = if (isDir) 0L else sizeBytes,
            lastModified = (modifiedSeconds * MILLIS_PER_SECOND).toLong(),
            extension = if (isDir) "" else name.substringAfterLast('.', "").lowercase(),
        )
    }

    private fun isDirectory(path: String): Boolean {
        // TODO(ios): fileExistsAtPath(path, isDirectory:) out-param needs a memScoped BooleanVar on
        //  device; this attribute-based check is a compiling best-effort.
        val type = manager.attributesOfItemAtPath(path, error = null)?.get(NSFileType)
        return type == NSFileTypeDirectory
    }

    private companion object {
        const val MILLIS_PER_SECOND = 1000.0
    }
}

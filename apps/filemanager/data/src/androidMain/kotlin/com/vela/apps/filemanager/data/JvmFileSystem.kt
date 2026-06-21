/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.data

import com.vela.apps.filemanager.domain.FileNode
import com.vela.apps.filemanager.domain.FileSystem
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.SimpleFileVisitor
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.copyTo
import kotlin.io.path.name

/**
 * Full java.nio-backed [FileSystem]. Shared verbatim by the Android and Desktop actuals (KMP has no
 * shared JVM source set in this module, so the file is duplicated; logic is identical).
 */
internal class JvmFileSystem(private val rootPaths: List<String>) : FileSystem {

    override fun roots(): List<FileNode> = rootPaths.map { Paths.get(it) }.filter { Files.exists(it) }.map(::toNode)

    override fun list(path: String): List<FileNode> {
        val dir = Paths.get(path)
        if (!Files.isDirectory(dir)) return emptyList()
        return try {
            Files.newDirectoryStream(dir).use { stream -> stream.map(::toNode) }
        } catch (_: IOException) {
            emptyList()
        }
    }

    override fun parentOf(path: String): String? = Paths.get(path).parent?.toString()

    override fun createFolder(parentPath: String, name: String): FileNode? = runCatchingNull {
        val target = Paths.get(parentPath).resolve(name)
        toNode(Files.createDirectory(target))
    }

    override fun rename(path: String, newName: String): FileNode? = runCatchingNull {
        val source = Paths.get(path)
        val target = source.resolveSibling(newName)
        toNode(Files.move(source, target))
    }

    override fun delete(path: String): Boolean = runCatching {
        val target = Paths.get(path)
        if (Files.isDirectory(target)) deleteRecursively(target) else Files.deleteIfExists(target)
        true
    }.getOrDefault(false)

    override fun copy(sourcePath: String, targetDir: String): FileNode? = runCatchingNull {
        val source = Paths.get(sourcePath)
        val target = uniqueTarget(Paths.get(targetDir).resolve(source.name))
        if (Files.isDirectory(source)) copyRecursively(source, target) else Files.copy(source, target)
        toNode(target)
    }

    override fun move(sourcePath: String, targetDir: String): FileNode? = runCatchingNull {
        val source = Paths.get(sourcePath)
        val target = uniqueTarget(Paths.get(targetDir).resolve(source.name))
        toNode(Files.move(source, target, StandardCopyOption.ATOMIC_MOVE))
    }

    @Suppress("ReturnCount")
    override fun search(path: String, query: String): List<FileNode> {
        val needle = query.trim()
        if (needle.isEmpty()) return emptyList()
        val start = Paths.get(path)
        if (!Files.isDirectory(start)) return emptyList()
        val matches = mutableListOf<FileNode>()
        runCatching {
            Files.walkFileTree(
                start,
                object : SimpleFileVisitor<Path>() {
                    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                        if (file.name.contains(needle, ignoreCase = true)) matches += toNode(file)
                        return FileVisitResult.CONTINUE
                    }

                    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                        if (dir != start && dir.name.contains(needle, ignoreCase = true)) matches += toNode(dir)
                        return FileVisitResult.CONTINUE
                    }

                    override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult =
                        FileVisitResult.CONTINUE
                },
            )
        }
        return matches
    }

    private fun toNode(p: Path): FileNode {
        val isDir = Files.isDirectory(p)
        val name = p.fileName?.toString() ?: p.toString()
        return FileNode(
            path = p.toAbsolutePath().toString(),
            name = name,
            isDirectory = isDir,
            sizeBytes = if (isDir) 0L else runCatching { Files.size(p) }.getOrDefault(0L),
            lastModified = runCatching { Files.getLastModifiedTime(p).toMillis() }.getOrDefault(0L),
            extension = if (isDir) "" else name.substringAfterLast('.', "").lowercase(),
        )
    }

    private fun uniqueTarget(desired: Path): Path {
        if (!Files.exists(desired)) return desired
        val parent = desired.parent
        val full = desired.name
        val base = full.substringBeforeLast('.', full)
        val ext = full.substringAfterLast('.', "").let { if (it.isEmpty()) "" else ".$it" }
        var index = 1
        var candidate = parent.resolve("$base ($index)$ext")
        while (Files.exists(candidate)) {
            index++
            candidate = parent.resolve("$base ($index)$ext")
        }
        return candidate
    }

    private fun copyRecursively(source: Path, target: Path) {
        Files.walkFileTree(
            source,
            object : SimpleFileVisitor<Path>() {
                override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.createDirectories(target.resolve(source.relativize(dir).toString()))
                    return FileVisitResult.CONTINUE
                }

                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    file.copyTo(target.resolve(source.relativize(file).toString()), overwrite = true)
                    return FileVisitResult.CONTINUE
                }
            },
        )
    }

    private fun deleteRecursively(dir: Path) {
        Files.walkFileTree(
            dir,
            object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.deleteIfExists(file)
                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(d: Path, exc: IOException?): FileVisitResult {
                    Files.deleteIfExists(d)
                    return FileVisitResult.CONTINUE
                }
            },
        )
    }

    private inline fun <T> runCatchingNull(block: () -> T): T? = runCatching(block).getOrNull()
}

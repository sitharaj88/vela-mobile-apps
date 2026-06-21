/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.data

import com.vela.apps.applauncher.domain.AppEntry
import com.vela.apps.applauncher.domain.InstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File

/**
 * Real desktop [InstalledApps]. Enumerates genuinely installed applications and launches them,
 * with a strategy per operating system:
 *
 * - **Windows**: scans Start Menu shortcut folders (all-users + per-user) for `.lnk`/`.url` files.
 *   The label is the file name without extension. Launch opens the shortcut via
 *   [Desktop.open], falling back to `cmd /c start`.
 * - **macOS**: lists `*.app` bundles in `/Applications`, `/System/Applications`, and
 *   `~/Applications`. Launch uses `open <path>`.
 * - **Linux**: parses freedesktop `*.desktop` entries in the standard XDG application directories,
 *   reading `Name=` and `Exec=`. Launch runs the cleaned `Exec` command.
 *
 * The [AppEntry.id] carries everything [launch] needs (the shortcut/bundle path, or the Linux Exec
 * command), so launching never needs to re-scan the disk.
 */
internal class DesktopInstalledApps : InstalledApps {

    private val os: DesktopOs = detectOs()

    override val isSupported: Boolean get() = os != DesktopOs.UNKNOWN

    override suspend fun list(): List<AppEntry> = withContext(Dispatchers.IO) {
        when (os) {
            DesktopOs.WINDOWS -> WindowsApps.list()
            DesktopOs.MAC -> MacApps.list()
            DesktopOs.LINUX -> LinuxApps.list()
            DesktopOs.UNKNOWN -> emptyList()
        }.distinctBy { it.id }
    }

    override fun launch(id: String) {
        runCatching {
            when (os) {
                DesktopOs.WINDOWS -> WindowsApps.launch(id)
                DesktopOs.MAC -> MacApps.launch(id)
                DesktopOs.LINUX -> LinuxApps.launch(id)
                DesktopOs.UNKNOWN -> Unit
            }
        }
    }
}

internal enum class DesktopOs { WINDOWS, MAC, LINUX, UNKNOWN }

internal fun detectOs(): DesktopOs {
    val name = System.getProperty("os.name").orEmpty().lowercase()
    return when {
        name.contains("win") -> DesktopOs.WINDOWS
        name.contains("mac") || name.contains("darwin") -> DesktopOs.MAC
        name.contains("nix") || name.contains("nux") || name.contains("aix") -> DesktopOs.LINUX
        else -> DesktopOs.UNKNOWN
    }
}

/** Recursively collects files under [root] (up to [maxDepth]) matching [accept]. */
internal fun scanFiles(root: File, maxDepth: Int, accept: (File) -> Boolean): List<File> {
    if (!root.isDirectory) return emptyList()
    return root.walkTopDown()
        .maxDepth(maxDepth)
        .filter { it.isFile && accept(it) }
        .toList()
}

// ---------------------------------------------------------------------------------------------
// Windows
// ---------------------------------------------------------------------------------------------

private object WindowsApps {

    private const val SCAN_DEPTH = 6

    fun list(): List<AppEntry> {
        val roots = buildList {
            System.getenv("ProgramData")?.let {
                add(File(it, "Microsoft/Windows/Start Menu/Programs"))
            }
            System.getenv("APPDATA")?.let {
                add(File(it, "Microsoft/Windows/Start Menu/Programs"))
            }
        }
        return roots
            .flatMap { root -> scanFiles(root, SCAN_DEPTH) { it.isShortcut() } }
            .map { AppEntry(id = it.absolutePath, label = it.nameWithoutExtension) }
    }

    fun launch(path: String) {
        val file = File(path)
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(file)
        } else {
            ProcessBuilder("cmd", "/c", "start", "", path).start()
        }
    }

    private fun File.isShortcut(): Boolean {
        val ext = extension.lowercase()
        return ext == "lnk" || ext == "url"
    }
}

// ---------------------------------------------------------------------------------------------
// macOS
// ---------------------------------------------------------------------------------------------

private object MacApps {

    private const val SCAN_DEPTH = 4

    fun list(): List<AppEntry> {
        val roots = listOf(
            File("/Applications"),
            File("/System/Applications"),
            File(System.getProperty("user.home"), "Applications"),
        )
        return roots
            .filter { it.isDirectory }
            .flatMap { root ->
                root.walkTopDown()
                    .maxDepth(SCAN_DEPTH)
                    .filter { it.isDirectory && it.extension.equals("app", ignoreCase = true) }
                    .toList()
            }
            .map { AppEntry(id = it.absolutePath, label = it.nameWithoutExtension) }
    }

    fun launch(path: String) {
        ProcessBuilder("open", path).start()
    }
}

// ---------------------------------------------------------------------------------------------
// Linux
// ---------------------------------------------------------------------------------------------

private object LinuxApps {

    private const val SCAN_DEPTH = 4
    private const val EXEC_PREFIX = "exec="
    private const val NAME_PREFIX = "name="
    private const val NO_DISPLAY_PREFIX = "nodisplay="
    private const val TYPE_PREFIX = "type="

    fun list(): List<AppEntry> {
        val dataHome = System.getenv("XDG_DATA_HOME")
            ?: "${System.getProperty("user.home")}/.local/share"
        val roots = listOf(
            File("/usr/share/applications"),
            File("/usr/local/share/applications"),
            File("/var/lib/flatpak/exports/share/applications"),
            File(dataHome, "applications"),
        )
        return roots
            .flatMap { root -> scanFiles(root, SCAN_DEPTH) { it.extension == "desktop" } }
            .mapNotNull { parse(it) }
    }

    fun launch(execCommand: String) {
        val args = tokenize(stripFieldCodes(execCommand))
        if (args.isNotEmpty()) ProcessBuilder(args).start()
    }

    @Suppress("ReturnCount")
    private fun parse(file: File): AppEntry? {
        var name: String? = null
        var exec: String? = null
        var isApplication = true
        var hidden = false
        file.useLines { lines ->
            for (raw in lines) {
                val line = raw.trim()
                val lower = line.lowercase()
                when {
                    name == null && lower.startsWith(NAME_PREFIX) ->
                        name = line.substringAfter('=').trim()
                    exec == null && lower.startsWith(EXEC_PREFIX) ->
                        exec = line.substringAfter('=').trim()
                    lower.startsWith(TYPE_PREFIX) ->
                        isApplication = line.substringAfter('=').trim().equals("Application", true)
                    lower.startsWith(NO_DISPLAY_PREFIX) ->
                        hidden = line.substringAfter('=').trim().equals("true", true)
                }
            }
        }
        val label = name ?: return null
        val command = exec ?: return null
        if (!isApplication || hidden) return null
        return AppEntry(id = command, label = label)
    }

    /** Removes freedesktop field codes (%u, %f, %i, ...) that are not valid as process args. */
    private fun stripFieldCodes(exec: String): String =
        exec.replace(Regex("%[a-zA-Z]"), "").trim()

    /** Splits a command honoring simple double-quoted segments. */
    private fun tokenize(command: String): List<String> {
        val tokens = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (ch in command) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch.isWhitespace() && !inQuotes -> {
                    if (current.isNotEmpty()) {
                        tokens.add(current.toString())
                        current.clear()
                    }
                }
                else -> current.append(ch)
            }
        }
        if (current.isNotEmpty()) tokens.add(current.toString())
        return tokens
    }
}

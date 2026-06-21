/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.data

import com.vela.apps.filemanager.domain.FileSystem
import org.koin.core.module.Module
import org.koin.dsl.module
import java.nio.file.FileSystems

/** Desktop roots: every default filesystem root directory plus the user's home. */
actual fun fileSystemPlatformModule(): Module = module {
    single<FileSystem> {
        val roots = buildList {
            add(System.getProperty("user.home"))
            FileSystems.getDefault().rootDirectories.forEach { add(it.toString()) }
        }.distinct()
        JvmFileSystem(roots)
    }
}

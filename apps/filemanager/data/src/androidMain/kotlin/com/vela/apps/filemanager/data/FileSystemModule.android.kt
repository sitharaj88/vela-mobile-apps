/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.data

import android.content.Context
import android.os.Environment
import com.vela.apps.filemanager.domain.FileSystem
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android roots: shared external storage (best-effort) plus the always-readable app files dir.
 *
 * Reading shared external storage works without a runtime permission for the app's own areas; broad
 * device-wide browsing would need MANAGE_EXTERNAL_STORAGE + a SAF picker — tracked as a follow-up.
 */
actual fun fileSystemPlatformModule(): Module = module {
    single<FileSystem> {
        val context = androidContext()
        JvmFileSystem(androidRoots(context))
    }
}

private fun androidRoots(context: Context): List<String> = buildList {
    @Suppress("DEPRECATION")
    val external = Environment.getExternalStorageDirectory()
    if (external != null && external.canRead()) add(external.absolutePath)
    context.getExternalFilesDir(null)?.let { add(it.absolutePath) }
    add(context.filesDir.absolutePath)
}.distinct()

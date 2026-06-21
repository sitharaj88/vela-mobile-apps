/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.vela.apps.applauncher.domain.AppEntry
import com.vela.apps.applauncher.domain.InstalledApps
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/** Android binds the real [InstalledApps] backed by [PackageManager]. */
actual fun appLauncherPlatformModule(): Module = module {
    single<InstalledApps> { AndroidInstalledApps(androidContext()) }
}

/**
 * Enumerates launchable activities via the standard MAIN/LAUNCHER query and maps each to an
 * [AppEntry] keyed by package name. Launching resolves the package's launch intent and starts it
 * as a new task.
 */
private class AndroidInstalledApps(context: Context) : InstalledApps {

    private val appContext: Context = context.applicationContext
    private val packageManager: PackageManager get() = appContext.packageManager

    override val isSupported: Boolean = true

    override suspend fun list(): List<AppEntry> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        packageManager.queryIntentActivities(intent, 0)
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val label = resolveInfo.loadLabel(packageManager).toString()
                AppEntry(id = activityInfo.packageName, label = label)
            }
            .distinctBy { it.id }
    }

    override fun launch(id: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(id) ?: return
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(launchIntent)
    }
}

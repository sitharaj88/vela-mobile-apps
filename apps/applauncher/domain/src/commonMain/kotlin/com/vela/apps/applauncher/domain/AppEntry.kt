/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.applauncher.domain

/**
 * A single launchable application.
 *
 * [id] is the stable, platform-specific launch key — on Android it is the package name; on Desktop
 * it is the absolute path to the shortcut/bundle (or the `.desktop` Exec command on Linux). It is
 * what gets handed back to [InstalledApps.launch].
 *
 * [label] is the human-readable name shown in the grid.
 */
data class AppEntry(
    val id: String,
    val label: String,
)

/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.clock.ui.platform

import org.koin.core.module.Module

/**
 * Provides the platform [com.vela.apps.clock.domain.AlarmScheduler] implementation:
 * - Android: `AlarmManager` + a notification when it fires.
 * - Desktop: an in-process coroutine that waits until the alarm time and shows a tray notification.
 * - iOS: best-effort `UNUserNotificationCenter` (see `// TODO(ios)` markers).
 */
expect fun clockPlatformModule(): Module

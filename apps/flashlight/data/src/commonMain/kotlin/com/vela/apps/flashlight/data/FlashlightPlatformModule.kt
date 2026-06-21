/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.flashlight.data

import org.koin.core.module.Module

/**
 * Provides the platform [com.vela.apps.flashlight.domain.Torch] binding. Each platform's actual
 * constructs an implementation backed by its native flash API (or a no-op where none exists).
 */
expect fun flashlightPlatformModule(): Module

/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.filemanager.data

import org.koin.core.module.Module

/**
 * Each platform provides a [com.vela.apps.filemanager.domain.FileSystem] bound here. The actuals
 * differ only in how they resolve the app-accessible root and enumerate directory contents.
 */
expect fun fileSystemPlatformModule(): Module

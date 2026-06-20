/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    // Versionless ids: these plugins are already on the classpath via build-logic.
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("vela.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.kermit)
        }
    }
}

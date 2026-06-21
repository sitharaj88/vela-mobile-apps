/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("vela.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.apps.voicerecorder.domain)
            api(projects.core.common)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
        }
    }
}

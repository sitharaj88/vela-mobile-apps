/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("vela.kmp.library")
    id("vela.compose")
}

kotlin {
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "ClockUI"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.apps.clock.presentation)
            implementation(projects.apps.clock.data)
            implementation(projects.core.designsystem)
            implementation(projects.core.common)

            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.core.ktx)
        }
    }
}

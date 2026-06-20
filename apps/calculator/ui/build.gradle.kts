/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("vela.kmp.library")
    id("vela.compose")
}

kotlin {
    // Umbrella framework consumed by the iOS Xcode project.
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "CalculatorUI"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.apps.calculator.presentation)
            implementation(projects.apps.calculator.data)
            implementation(projects.core.designsystem)
            implementation(projects.core.common)

            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
        }
    }
}

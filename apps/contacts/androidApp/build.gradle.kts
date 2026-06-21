/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("vela.android.application")
}

android {
    namespace = "com.vela.apps.contacts.android"
    defaultConfig {
        applicationId = "com.vela.contacts"
    }
}

dependencies {
    implementation(projects.apps.contacts.ui)

    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
}

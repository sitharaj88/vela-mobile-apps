/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
plugins {
    `kotlin-dsl`
}

group = "com.vela.buildlogic"

// Pin a Java 17 toolchain so the compiled convention plugins are always v61 bytecode — loadable by
// any Gradle daemon running on JDK 17+ (avoids UnsupportedClassVersionError when a daemon's JDK
// differs from the JDK that built build-logic).
kotlin {
    jvmToolchain(17)
}

dependencies {
    // Plugin artifacts so precompiled convention scripts can apply them by id.
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.android.gradle.plugin)
    implementation(libs.compose.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
}

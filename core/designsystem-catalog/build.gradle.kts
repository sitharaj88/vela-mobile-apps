/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    // Applied by raw id (no version): these plugins are already on the classpath via the root
    // project's `apply false` declarations, so re-declaring a version would conflict.
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.kotlinx.coroutines.swing)
}

compose.desktop {
    application {
        mainClass = "com.vela.core.designsystem.catalog.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "VelaCatalog"
            packageVersion = "1.0.0"
        }
    }
}

/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 *
 * Convention (configure-only): adds Compose Multiplatform dependencies to a KMP module.
 * The consuming module must apply the base plugins first, e.g.:
 *
 *     plugins {
 *         alias(libs.plugins.kotlinMultiplatform)
 *         alias(libs.plugins.androidLibrary)
 *         alias(libs.plugins.composeMultiplatform)
 *         alias(libs.plugins.composeCompiler)
 *         id("vela.kmp.library")
 *         id("vela.compose")
 *     }
 */
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
private val compose = ComposePlugin.Dependencies(project)

extensions.configure<KotlinMultiplatformExtension> {
    sourceSets.getByName("commonMain").dependencies {
        implementation(compose.runtime)
        implementation(compose.foundation)
        implementation(compose.material3)
        implementation(compose.materialIconsExtended)
        implementation(compose.components.resources)
        implementation(compose.components.uiToolingPreview)
        implementation(libs.findLibrary("androidx-lifecycle-viewmodel").get())
        implementation(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
    }
}

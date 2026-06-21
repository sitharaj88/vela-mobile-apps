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
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
private val compose = ComposePlugin.Dependencies(project)

// KMP 2.2 + Compose 1.8 bug: Strong Skipping generates $artificial stability stubs that are
// never defined in Kotlin/Native static frameworks, causing linker errors on iOS.
extensions.configure<ComposeCompilerGradlePluginExtension> {
    featureFlags.add(ComposeFeatureFlag.StrongSkipping.disabled())
    stabilityConfigurationFiles.add(
        rootProject.layout.projectDirectory.file("config/compose_stability.conf")
    )
}

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

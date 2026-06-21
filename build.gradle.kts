/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    // Declared here (apply false) so plugin versions resolve once for all subprojects.
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
}

// ---- Repo-wide quality gates ----
allprojects {
    apply(plugin = rootProject.libs.plugins.spotless.get().pluginId)
    apply(plugin = rootProject.libs.plugins.kover.get().pluginId)
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

    spotless {
        kotlin {
            target("src/**/*.kt")
            ktlint("1.3.1").editorConfigOverride(mapOf("ktlint_standard_no-wildcard-imports" to "disabled"))
            licenseHeaderFile(rootProject.file("config/spotless/license-header.txt"))
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint("1.3.1")
        }
    }

    // Detekt runs in every module. We point its source at all KMP source sets (non-existent dirs
    // are ignored) so common/android/desktop/ios/test code is all analyzed by `./gradlew detekt`.
    configure<DetektExtension> {
        buildUponDefaultConfig = true
        parallel = true
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        source.setFrom(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/desktopMain/kotlin",
            "src/iosMain/kotlin",
            "src/commonTest/kotlin",
            "src/main/kotlin",
        )
    }
}

dependencies {
    // Aggregate coverage across the pure-Kotlin layers that carry real logic.
    kover(projects.core.common)
    kover(projects.apps.calculator.domain)
    kover(projects.apps.calculator.data)
    kover(projects.apps.calculator.presentation)
}

// Kover 0.8 DSL. Floor starts intentionally low and is raised as the suite matures; `build` does
// not depend on `koverVerify`, so a fresh checkout still builds even before coverage climbs.
kover {
    reports {
        verify {
            rule {
                minBound(20)
            }
        }
    }
}

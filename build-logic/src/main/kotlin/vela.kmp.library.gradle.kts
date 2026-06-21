/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 *
 * Convention (configure-only) for a Kotlin Multiplatform *library* targeting Android, iOS, and
 * Desktop (JVM). The consuming module must apply the base plugins first:
 *
 *     plugins {
 *         alias(libs.plugins.kotlinMultiplatform)
 *         alias(libs.plugins.androidLibrary)
 *         id("vela.kmp.library")
 *     }
 *
 * Applying the base plugins in the module (rather than here) is what makes the `kotlin { }` and
 * `android { }` script accessors available in the module's own build file.
 */
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
private fun v(alias: String) = libs.findVersion(alias).get().requiredVersion

// ":core:designsystem" -> "com.vela.core.designsystem"; ":apps:calculator:domain" -> "com.vela.apps.calculator.domain"
private val derivedNamespace =
    "com.vela." + path.removePrefix(":").replace(":", ".").replace("-", "")

extensions.configure<KotlinMultiplatformExtension> {
    // expect/actual classes (used by Room's @ConstructedBy + our platform seams) are stable in
    // practice but still flagged Beta; opt in across the suite so builds stay warning-clean.
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    }
    jvm("desktop")
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    applyDefaultHierarchyTemplate()

    sourceSets.getByName("commonMain").dependencies {
        implementation(libs.findLibrary("kotlinx-coroutines-core").get())
    }
    sourceSets.getByName("commonTest").dependencies {
        implementation(libs.findLibrary("kotlin-test").get())
        implementation(libs.findLibrary("kotlinx-coroutines-test").get())
        implementation(libs.findLibrary("turbine").get())
    }
}

extensions.configure<LibraryExtension> {
    namespace = derivedNamespace
    compileSdk = v("androidCompileSdk").toInt()
    defaultConfig {
        minSdk = v("androidMinSdk").toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

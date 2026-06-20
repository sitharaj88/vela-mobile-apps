/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 *
 * Convention (configure-only) for an Android *application* entry point. The consuming module must
 * apply the base plugins first:
 *
 *     plugins {
 *         alias(libs.plugins.androidApplication)
 *         alias(libs.plugins.kotlinAndroid)
 *         alias(libs.plugins.composeMultiplatform)
 *         alias(libs.plugins.composeCompiler)
 *         id("vela.android.application")
 *     }
 */
import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

private val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
private fun v(alias: String) = libs.findVersion(alias).get().requiredVersion

extensions.configure<ApplicationExtension> {
    compileSdk = v("androidCompileSdk").toInt()
    defaultConfig {
        minSdk = v("androidMinSdk").toInt()
        targetSdk = v("androidTargetSdk").toInt()
        versionCode = 1
        versionName = "1.0.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
}

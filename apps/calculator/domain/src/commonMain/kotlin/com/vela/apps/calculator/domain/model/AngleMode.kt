/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.calculator.domain.model

/**
 * Unit the trigonometric functions interpret their argument in. [Degrees] is the default for a
 * pocket-calculator feel; [Radians] matches the raw [kotlin.math] functions.
 */
enum class AngleMode {
    Degrees,
    Radians,
}

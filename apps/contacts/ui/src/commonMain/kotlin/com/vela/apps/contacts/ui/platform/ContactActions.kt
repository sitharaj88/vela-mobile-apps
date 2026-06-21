/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.ui.platform

import androidx.compose.runtime.Composable

/** Platform actions for the detail screen — dialing and emailing. */
interface ContactActions {
    /** Whether this platform can launch a dialer / mail client. */
    val canDialAndEmail: Boolean

    /** Opens the platform dialer pre-filled with [number] (no auto-call). */
    fun dial(number: String)

    /** Opens the platform mail composer addressed to [address]. */
    fun email(address: String)
}

/** Remembers a platform [ContactActions] bound to the current composition (Android needs Context). */
@Composable
expect fun rememberContactActions(): ContactActions

/**
 * Lets the user pick a .vcf file and returns its text content, or `null` if cancelled/unavailable.
 * Implemented on Desktop (Swing file chooser); a no-op elsewhere.
 */
@Composable
expect fun rememberVCardPicker(): VCardPicker

/** Opens a file picker for a vCard and delivers its contents to [onPicked]. */
fun interface VCardPicker {
    fun pick(onPicked: (String) -> Unit)
}

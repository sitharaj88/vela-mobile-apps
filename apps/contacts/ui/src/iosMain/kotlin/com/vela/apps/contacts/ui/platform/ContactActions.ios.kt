/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun rememberContactActions(): ContactActions = remember { IosContactActions() }

private class IosContactActions : ContactActions {
    override val canDialAndEmail: Boolean = true

    override fun dial(number: String) {
        val sanitized = number.filter { it.isDigit() || it == '+' }
        NSURL.URLWithString("tel:$sanitized")?.let { UIApplication.sharedApplication.openURL(it) }
    }

    override fun email(address: String) {
        NSURL.URLWithString("mailto:$address")?.let { UIApplication.sharedApplication.openURL(it) }
    }
}

// TODO(ios): present a document picker (UIDocumentPickerViewController) to read a .vcf file.
@Composable
actual fun rememberVCardPicker(): VCardPicker = VCardPicker { /* not supported on iOS in v1 */ }

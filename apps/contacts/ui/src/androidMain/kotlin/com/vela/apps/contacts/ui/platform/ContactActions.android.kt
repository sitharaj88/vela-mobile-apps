/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.ui.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberContactActions(): ContactActions {
    val context = LocalContext.current
    return remember(context) { AndroidContactActions(context) }
}

private class AndroidContactActions(private val context: Context) : ContactActions {
    override val canDialAndEmail: Boolean = true

    override fun dial(number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }

    override fun email(address: String) {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}

@Composable
actual fun rememberVCardPicker(): VCardPicker = VCardPicker { /* import is desktop-only in v1 */ }

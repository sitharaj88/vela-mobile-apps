/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.Desktop
import java.io.File
import java.net.URI
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberContactActions(): ContactActions = remember { DesktopContactActions() }

private class DesktopContactActions : ContactActions {
    // Desktop can open a mail client via the OS, but has no dialer; expose email only via mailto.
    override val canDialAndEmail: Boolean = false

    override fun dial(number: String) = Unit

    override fun email(address: String) {
        val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else return
        if (desktop.isSupported(Desktop.Action.MAIL)) {
            runCatching { desktop.mail(URI("mailto:$address")) }
        }
    }
}

@Composable
actual fun rememberVCardPicker(): VCardPicker = remember {
    VCardPicker { onPicked ->
        val chooser = JFileChooser().apply {
            dialogTitle = "Import vCard"
            fileFilter = FileNameExtensionFilter("vCard files (*.vcf)", "vcf")
        }
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            val file: File = chooser.selectedFile
            runCatching { file.readText() }.getOrNull()?.let(onPicked)
        }
    }
}

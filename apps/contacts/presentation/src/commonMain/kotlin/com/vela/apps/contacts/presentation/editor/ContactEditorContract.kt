/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.presentation.editor

data class ContactEditorState(
    val id: String = "",
    val name: String = "",
    val phones: String = "",
    val emails: String = "",
    val organization: String = "",
    val isLoading: Boolean = true,
) {
    val isNew: Boolean get() = id.isEmpty()
    val canSave: Boolean get() = name.isNotBlank()
}

sealed interface ContactEditorIntent {
    data class NameChanged(val value: String) : ContactEditorIntent
    data class PhonesChanged(val value: String) : ContactEditorIntent
    data class EmailsChanged(val value: String) : ContactEditorIntent
    data class OrganizationChanged(val value: String) : ContactEditorIntent
    data object Save : ContactEditorIntent
}

sealed interface ContactEditorEffect {
    data object Saved : ContactEditorEffect
}

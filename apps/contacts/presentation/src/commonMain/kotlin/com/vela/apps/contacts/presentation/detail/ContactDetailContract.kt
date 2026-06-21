/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.presentation.detail

import com.vela.apps.contacts.domain.Contact

data class ContactDetailState(
    val contact: Contact? = null,
    val isLoading: Boolean = true,
    val canEdit: Boolean = false,
) {
    val isMissing: Boolean get() = !isLoading && contact == null
}

sealed interface ContactDetailIntent {
    data class SetFavorite(val favorite: Boolean) : ContactDetailIntent
    data object Delete : ContactDetailIntent
}

sealed interface ContactDetailEffect {
    data object Deleted : ContactDetailEffect
}

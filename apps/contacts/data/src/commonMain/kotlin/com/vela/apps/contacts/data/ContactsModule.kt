/*
 * Copyright 2026 The Vela Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package com.vela.apps.contacts.data

import org.koin.core.module.Module

/**
 * Each platform binds a [com.vela.apps.contacts.domain.ContactsRepository] here:
 * - **Android** reads `ContactsContract` with a local Room favorites overlay (read-only contacts).
 * - **Desktop** uses a Room-backed editable store with vCard import.
 * - **iOS** reads `CNContactStore` best-effort with a local favorites overlay.
 */
expect fun contactsPlatformModule(): Module

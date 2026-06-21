// Copyright 2026 The Vela Authors
// SPDX-License-Identifier: Apache-2.0

import SwiftUI
import NotesUI

@main
struct iOSApp: App {
    init() {
        InitKoinKt.doInitNotesKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
        }
    }
}

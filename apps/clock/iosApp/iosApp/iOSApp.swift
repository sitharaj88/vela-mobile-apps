// Copyright 2026 The Vela Authors
// SPDX-License-Identifier: Apache-2.0

import SwiftUI
import ClockUI

@main
struct iOSApp: App {
    init() {
        // Start Koin once before any Compose content is created.
        InitKoinKt.doInitClockKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
        }
    }
}

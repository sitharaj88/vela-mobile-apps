// Copyright 2026 The Vela Authors
// SPDX-License-Identifier: Apache-2.0

import SwiftUI
import RecorderUI

@main
struct iOSApp: App {
    init() {
        InitKoinKt.doInitRecorderKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .ignoresSafeArea(.all)
        }
    }
}

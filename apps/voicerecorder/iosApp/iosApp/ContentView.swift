// Copyright 2026 The Vela Authors
// SPDX-License-Identifier: Apache-2.0

import SwiftUI
import RecorderUI

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        RecorderViewControllerKt.recorderViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}

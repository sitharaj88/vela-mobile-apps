// Copyright 2026 The Vela Authors
// SPDX-License-Identifier: Apache-2.0

import SwiftUI
import NotesUI

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        NotesViewControllerKt.notesViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.keyboard)
    }
}

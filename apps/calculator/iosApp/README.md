# Vela Calculator — iOS app

The iOS entry point is an Xcode project that embeds the Compose Multiplatform UI from
`:apps:calculator:ui` (framework `CalculatorUI`).

The Swift sources (`iosApp/iOSApp.swift`, `iosApp/ContentView.swift`, `iosApp/Info.plist`) are
checked in. The Xcode project file (`iosApp.xcodeproj`) is generated locally so it isn't committed
with machine-specific paths.

## One-time setup

1. **Create the Xcode project** (`File ▸ New ▸ Project ▸ iOS App`, SwiftUI, name `iosApp`) inside
   this folder, then replace its generated Swift files with the ones here.

2. **Link the Kotlin framework.** Add a "Run Script" build phase (before "Compile Sources"):

   ```bash
   cd "$SRCROOT/../../../.."   # repo root
   ./gradlew :apps:calculator:ui:embedAndSignAppleFrameworkForXcode
   ```

   Set **Framework Search Paths** to the build output, and add `CalculatorUI` to
   *Frameworks, Libraries, and Embedded Content*.

3. **Build & run** on a simulator or device from Xcode.

## How it connects

- `iOSApp.swift` calls `InitKoinKt.doInitCalculatorKoin()` once at launch.
- `ContentView.swift` wraps `CalculatorViewControllerKt.calculatorViewController()` (defined in
  `ui/src/iosMain/.../CalculatorViewController.kt`) in a `UIViewControllerRepresentable`.

> **Note:** Kotlin/Native cannot cross-compile Apple targets from Windows or Linux — the iOS
> framework only builds on **macOS**. The `ios` CI job runs on a macOS runner and compiles
> `:apps:calculator:ui:compileKotlinIosSimulatorArm64` to guard the Kotlin side on every PR.
>
> Tip: many teams use a checked-in `project.yml` (XcodeGen) to make the Xcode project reproducible
> in CI.

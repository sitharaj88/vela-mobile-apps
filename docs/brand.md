# Vela Brand & Design System

> **Vela** — named for the constellation *"the sails."* Calm, premium, private.
> Tagline: **"Private by design. Beautiful by default."**

## Personality

| Trait | Expression |
|-------|------------|
| Calm | generous whitespace, soft motion, no visual noise |
| Premium | precise typography, considered elevation, tactile feedback |
| Private | no ads/trackers ever; privacy surfaced as a feature, not a checkbox |
| Cohesive | one system, per-app accent — every app is unmistakably "Vela" |

## Logo / motif

A four-point star / sail glyph (`✦`). Works as a monochrome app-mask and as a gradient mark.
Each app reuses the glyph tinted with its **accent**.

## Color system

Built on **Material 3 (Expressive)** tonal palettes, exposed through `VelaTheme`:

- **Schemes:** Light, Dark, **AMOLED (true black)**, and Android 12+ **dynamic color**.
- **Brand seed:** a deep indigo-violet (`#5B5BD6`) → generates primary/secondary/tertiary roles.
- **Per-app accent:** each app picks an accent (e.g. Calculator = teal, Notes = amber). The accent
  re-tints the primary role while keeping neutrals shared, so apps differ without feeling unrelated.
- **Never hardcode.** UI reads colors from `MaterialTheme.colorScheme` and brand extras from
  `LocalVelaTokens`.

## Typography

A variable sans for UI + a tabular/mono face for numerals (calculator, clock). Full M3 type scale
(display → label), shipped as Compose Resources fonts. Dynamic-type aware.

## Shape, spacing, elevation, motion

- **Shape:** rounded scale (4 / 8 / 16 / 28 dp) — friendly but crisp.
- **Spacing:** 4dp grid; tokens `xs..xxl` in `LocalVelaTokens`.
- **Elevation:** subtle; prefer tonal surfaces over shadows.
- **Motion:** expressive M3 spec — emphasized easing, container transforms, shared-element
  transitions between list↔detail, predictive back, and tasteful haptics on key actions.

## Tokens, not values

```kotlin
VelaTheme(accent = VelaAccent.Teal, themeMode = ThemeMode.System) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Text("12", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(LocalVelaTokens.current.spacing.lg))
    }
}
```

## Component library (in `core/designsystem`)

Buttons & FABs · cards · list items · dialogs · bottom sheets · collapsing top bars · search field ·
segmented controls · sliders · color picker · empty/error/skeleton states · snackbars · settings
rows · about/donate screens.

Every component is developed and visually regression-tested in the **catalog** app
(`core/designsystem-catalog`) on Android, iOS, and Desktop.

## Accessibility (non-negotiable)

Dynamic type · WCAG AA contrast · TalkBack/VoiceOver labels · ≥48dp touch targets · full RTL ·
respects reduced-motion.

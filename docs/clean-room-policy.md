# Clean-Room Policy

Vela is licensed under **Apache-2.0**. The SimpleMobileTools suite and its community fork
**Fossify** are licensed under **GPL-3.0**. GPL-3.0 is a strong copyleft license: copying or
adapting its source code would force Vela to be GPL-3.0 too. That is incompatible with our goal
of an Apache-2.0 product.

Therefore Vela is built **clean-room**.

## Rules

1. **No copying.** Do not copy, paste, translate, or line-by-line adapt any source code, resource,
   string table, vector asset, or layout from SimpleMobileTools or Fossify into Vela.
2. **Reference behavior, not code.** You may study the *running apps* to understand features and
   UX (what the app does, what settings exist, expected results) and write that down as a
   specification. Implement from the spec, in your own code.
3. **Own assets only.** All icons, illustrations, fonts, colors, and copy in Vela are original or
   come from permissively licensed (Apache/MIT/CC0/SIL-OFL) third-party sources, tracked in NOTICE.
4. **Document third-party deps.** Every external dependency and its license is recorded in the
   version catalog and surfaced in each app's "Open-source licenses" screen.
5. **When in doubt, re-derive.** If you cannot implement a feature without looking at GPL code,
   stop and design it from first principles instead.

## Why this is safe

Functionality and ideas are not copyrightable — only the specific expression (the code/assets) is.
Reimplementing a calculator, a notes app, or a file manager from a behavioral specification is
legitimate and routine. We just must not reuse the GPL *expression*.

## Practical workflow per app

1. Write `docs/specs/<app>.md` describing features/screens/behaviors (from using the app + its docs).
2. Implement against the spec in Vela's architecture.
3. Add tests asserting the spec'd behavior.
4. Never reference the upstream repo in commit messages as a source of copied code.

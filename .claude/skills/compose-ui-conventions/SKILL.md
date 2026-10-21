---
name: compose-ui-conventions
description: Use when writing or editing Jetpack Compose screens or components in the GabAI Android app.
---

# Compose conventions

- Theme tokens only (see `gabai-design-system`): `MaterialTheme.*` and the project's Spacing object.
- Every composable takes `modifier: Modifier = Modifier` as its first optional parameter. Hoist state; keep composables stateless where possible.
- Add a `@Preview` (light and dark) for every new screen-level composable.
- Icons: vector drawables via `painterResource`. `contentDescription` is required unless the icon is purely decorative (then `null`).
- Animations: follow `motion-and-reduced-motion`, using `rememberReducedMotion()`.
- Minimum touch target 48dp; support 200% font scale without clipping.
- Package layout: screens under `presentation/ui/<feature>/`, shared pieces in `presentation/ui/common/`, navigation in `presentation/navigation/`.
- Build check: `./gradlew.bat assembleDebug --console=plain -q`. The JDK is deliberately pinned to Android Studio's JBR in `gradle.properties`; do not change it.

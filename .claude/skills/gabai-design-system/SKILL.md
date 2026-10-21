---
name: gabai-design-system
description: Use when building or restyling any UI in the GabAI Android app. Ensures colors, icons, type and spacing come from the existing theme so the app stays consistent with gabai-web and gabai-console.
---

# GabAI design system (Android)

Never invent colors, spacing, radii or fonts. Read the source of truth first.

## Sources of truth
- `app/src/main/java/com/gabai/presentation/theme/`: `Color.kt`, `Shape.kt`, `Spacing.kt`, `Type.kt`, `Theme.kt`. Font is Nunito (`res/font/nunito.ttf`).
- Sibling surfaces: `gabai-web/app/globals.css` and `gabai-console/src/app/globals.css`. The web palette was originally sourced from `Color.kt` but the web has since moved its primary CTA to teal while Android still uses coral; palettes can drift. When changing a brand token, tell the user the other surfaces may need the same change rather than editing them silently.

## Rules
- Use `MaterialTheme.colorScheme`, `typography` and `shapes`, plus the Spacing object. No hard-coded `Color(0x..)`, magic `dp` or `sp` outside the theme files.
- Icons: Lucide, imported as vector drawables in `res/drawable` and used via `painterResource`. Outline style, tinted from the theme. Do not add new `Icons.Filled.*` usages, and do not add an icon library dependency.
- Crisis/safety colors (`CrisisRed`) keep their meaning; do not reuse them for decoration.
- Tone: calm, soft, generous spacing. Crisis and escalation UI stays prominent.

## Before finishing
- Check light AND dark theme.
- Contrast: body text >= 4.5:1, UI components >= 3:1.
- Touch targets >= 48dp; layout survives 200% font scale.
- Follow the `motion-and-reduced-motion` skill for any animation.

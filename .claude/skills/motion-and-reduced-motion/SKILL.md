---
name: motion-and-reduced-motion
description: Use whenever adding or changing any animation, transition, or looping visual in the GabAI Android app. Guarantees every animation respects the system "remove animations" setting.
---

# Motion rules (non-negotiable)

Every animation must have a reduced-motion path. No exceptions.

## Detecting the setting
- Android's "Remove animations" sets `Settings.Global.ANIMATOR_DURATION_SCALE` to 0.
- Use the `rememberReducedMotion()` helper in `presentation/util/` (create it there if it does not exist yet). Do not re-implement the check inline.

## Compose
- Compose's own animation APIs follow the system animator scale, but decorative/looping animations (`rememberInfiniteTransition`, pulses, breathing visuals) and any non-Compose animation library must be gated explicitly: render a static state when reduced.
- When reduced, prefer `snap()` over `tween()/spring()`, and skip `AnimatedVisibility` slide/scale (a plain fade or no transition is fine).
- Never auto-advance content on a timer when reduced.

## Scope and tone
- This is a mental-health app: calm, slow easing, no flashing (never >3 flashes/sec).
- Crisis, escalation, call and safety screens must not animate beyond minimal state feedback.
- Typical durations: 150-300ms for UI feedback; slower only for deliberately calming visuals.

## Verify before finishing
- Developer options > "Animator duration scale" = off, then walk the changed screen.
- Check light and dark themes.

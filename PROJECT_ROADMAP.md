# GabAI — Project Plan & Roadmap

_Last updated: 2026-07-27_

## Vision

GabAI is a Filipino-market mental wellness companion app: a chat-based first line of support, with a clear, safe path to escalate to a human when someone is in crisis. It is explicitly **not** a replacement for professional care — it's a bridge to it.

## How to use this file

This is a living checklist, not a one-time spec. Check items off (`- [x]`) as you finish them, add new ones as scope changes, and update the "Last updated" date whenever you make a real pass through it. There's no issue tracker or CI wired up yet (see Phase 4), so this file **is** the tracker for now.

---

## Current Status Snapshot

**Working today:**
- Registration flow (nickname + PH province/city)
- Dashboard with quick actions
- Real Gemini-powered chat (`ChatViewModel` → `GeminiChatRepositoryImpl`), replacing the old hardcoded keyword bot
- Crisis detection: instant local keyword net for SEVERE (works offline) + Gemini-based structured classification for MODERATE/subtler SEVERE
- Escalation flow: "Request Human Support" → DataStore-logged event → confirmation screen
- Dashboard Emergency card is clickable and routes into the same escalation flow

**Not built yet / placeholders:**
- Consultation booking screen (static placeholder text)
- Settings screen (static placeholder text)
- Daily Wellness tracking (dashboard card is a no-op)
- History / past sessions (dashboard card is a no-op)
- Any backend, user accounts, or real staff-side inbox for escalation requests
- Any automated tests
- Version control (project is **not** currently a git repository)

**Known debt (see also `PROJECT_OVERVIEW.md`):**
- `GEMINI_API_KEY` ships embedded in `BuildConfig` — fine for prototyping, not for production (extractable from the APK)
- Hotline numbers in `SafetyResources.kt` are unverified placeholders (`TODO` marked)
- `estimatedResponseMinutes = 15` in `EscalationRepositoryImpl` is a hardcoded placeholder — no real staffing/backend exists to honor it
- No DI framework, no Room, single Gradle module — deliberate for now, but will need revisiting once the backend/accounts phase starts
- Compose BOM version mismatch between `libs.versions.toml` and `app/build.gradle.kts` (cosmetic, not urgent)

---

## Phase 0 — Foundation ✅ done
- [x] Compose + Material3 project scaffold
- [x] Registration screen with PH province/city data
- [x] Dashboard with navigation to core features
- [x] Navigation graph (single-activity, Navigation Compose)

## Phase 1 — Real AI Chat + Crisis Safety ✅ done
- [x] Gemini SDK integration (`gemini-1.5-flash`, structured JSON responses)
- [x] Local keyword-based SEVERE fail-safe (network-independent)
- [x] Gemini-based crisis-level classification for subtler cases
- [x] Crisis banner that escalates-only (no more silent auto-clear bug)
- [x] Escalation screen + "Request Human Support" flow
- [x] DataStore-backed escalation event log
- [x] Safety-first UI: hotline card, persistent disclaimer, clickable Emergency card
- [ ] **Verify hotline numbers with an authoritative source before shipping** (currently placeholders)
- [ ] Manual device/emulator test pass of the full flow (build succeeds, but runtime click-through hasn't been done yet — no device was available in the last session)
- [ ] Add a real `GEMINI_API_KEY` to `local.properties` and confirm live Gemini responses

## Phase 2 — Consultation Booking
- [ ] Define what "consultation" means concretely: booking with a real professional, a directory of providers, or a simple contact-request form?
- [ ] Design `ConsultationScreen` (replacing the current placeholder)
- [ ] Decide if this needs its own backend/scheduling system or can reuse the escalation DataStore pattern for now
- [ ] Confirmation + reminder UX

## Phase 3 — Daily Wellness & History
- [ ] Define what "Daily Wellness" tracks (mood check-ins? journaling? guided exercises?)
- [ ] Local persistence for wellness entries (likely Room, given this needs querying/trends over time — this is the point where introducing Room becomes justified)
- [ ] History screen: surface past chat sessions and/or wellness entries
- [ ] Decide retention/privacy rules for stored conversation data (relevant given the PH Data Privacy Act note already on the registration screen)

## Phase 4 — Accounts, Backend & Production Hardening
- [ ] Stand up a minimal backend (even a lightweight serverless function) to proxy Gemini calls — removes the client-embedded API key risk
- [ ] Real user accounts / auth (currently registration just collects a nickname locally, nothing persists across installs)
- [ ] Real staff-side inbox to receive and act on escalation requests (today's "staff will reach out" confirmation has no one actually reading the queue)
- [ ] Settings screen: notification prefs, data export/delete (Data Privacy Act compliance), account management
- [ ] Initialize git version control and push to a remote — right now there's no history, no branches, no way to diff or roll back changes
- [ ] Basic CI (build + lint) once git/remote exists
- [ ] Unit tests for `GeminiChatRepositoryImpl` fallback logic and `LocalCrisisKeywordDetector`; instrumentation test for the escalation flow
- [ ] Security review pass (API key handling, data-at-rest for chat/escalation logs, PH Data Privacy Act compliance check)

---

## Suggested near-term order

1. Verify hotline numbers + do a real device test pass (finishes Phase 1 properly)
2. Initialize git (low effort, high value — you're about to keep iterating on this without any safety net)
3. Pick Consultation vs. Wellness/History as the next feature based on what matters most to you, then flesh out its concrete requirements before implementation (both are currently vague placeholders)
4. Defer backend/accounts/production hardening until the core feature set (chat, escalation, consultation, wellness) is what you actually want — building auth/backend too early usually means rebuilding it once requirements are clearer

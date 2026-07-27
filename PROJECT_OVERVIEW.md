# GabAI — Project Overview

> **Note:** this is an early-stage scaffold, not a mature app. Only the UI layer is really implemented; `data/` and `domain/` are empty stub files, and the "AI" chatbot is currently hardcoded keyword-matching, not a real LLM integration.

## 1. What it does

- **Package:** `com.gabai` · **App name:** GabAI
- A Filipino-market mental wellness app. Flow so far:
  - **Registration** — nickname + Philippine province/city dropdowns, with a Data Privacy Act compliance note.
  - **Dashboard** — "Talk to GabAI" (chat), "Request Consultation," "Daily Wellness" and "History" (not wired yet), plus a persistent Emergency Support card (911).
  - **Chatbot** — chat UI whose "AI" is a local `generateAIResponse()` function that pattern-matches keywords (sad, anxious, panic, etc.) and returns randomized canned replies. It detects crisis keywords (e.g. "suicide") and surfaces an emergency warning that can route to an escalation screen.
  - **Escalation / Consultation / Settings screens** — placeholders only ("to be implemented").

## 2. Tech stack

- **Language:** Kotlin only, UI in **Jetpack Compose (Material 3)**, no XML layouts.
- **compileSdk/targetSdk:** 36, **minSdk:** 24
- **Gradle:** 8.13, **AGP:** 8.13.2, **Kotlin:** 2.0.21 (using the new Compose Compiler plugin)
- **Compose BOM:** hardcoded to `2024.02.00` in `app/build.gradle.kts`, even though `libs.versions.toml` declares a newer `2024.09.00` — inconsistency worth cleaning up.
- Java/Kotlin compat is declared twice in `app/build.gradle.kts` (an 11 block, then a 17 block that overrides it) — looks like a leftover.

## 3. Architecture

- Folder names suggest **Clean Architecture / MVVM** (`presentation` / `data` / `domain`), but only `presentation` has real code.
- No ViewModels — screen state is local `remember`/`mutableStateOf`.
- No DI framework (no Hilt/Koin/Dagger).
- No Room DB — `DatabaseHelper.kt` is an empty stub.
- No Firebase (no `google-services.json`).
- No AI/LLM SDK — despite the name, no Gemini/OpenAI dependency anywhere.
- **Navigation:** Navigation Compose, single-Activity, routes centralized in a `Routes` object in `NavGraph.kt`.
- **Async:** Kotlin Coroutines, used lightly (simulated "typing" delay in chat).

## 4. Dependencies

_(from `app/build.gradle.kts`)_

- **UI:** Compose BOM 2024.02.00, compose-ui, material3, navigation-compose 2.7.6, activity-compose 1.8.2
- **Core:** core-ktx 1.12.0, lifecycle-runtime-ktx 2.7.0, lifecycle-viewmodel-compose 2.7.0
- **Storage:** datastore-preferences 1.0.0 (declared, unused)
- **Networking:** Retrofit 2.9.0, converter-gson 2.9.0, OkHttp logging-interceptor 4.12.0 (all declared, unused — `ApiService.kt` is empty)
- **Coroutines:** kotlinx-coroutines-android 1.7.3
- **Testing:** JUnit 4.13.2, androidx test/espresso, compose-ui-test — all still default template tests, nothing custom written
- **Not present at all:** DI, Room, image loading (Coil/Glide), any AI/ML SDK

A `libs.versions.toml` version catalog exists with newer versions but the app module mostly ignores it and hardcodes versions directly — out of sync.

## 5. Folder structure

```
GabAI/
├── build.gradle.kts, settings.gradle.kts   (single module: :app)
├── gradle/libs.versions.toml
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/                             (strings, colors, themes, mipmaps)
        └── java/com/gabai/
            ├── MainActivity.kt              — hosts NavGraph
            ├── ui/theme/                     — UNUSED leftover (default AS Compose template)
            ├── presentation/                 — the real, active UI layer
            │   ├── theme/                    — actual GabAI brand theme (PrimaryBlue #2563EB, etc.)
            │   ├── navigation/NavGraph.kt     — Routes + NavHost
            │   └── ui/
            │       ├── auth/RegistrationScreen.kt
            │       ├── dashboard/DashboardScreen.kt
            │       └── chatbot/ChatbotScreen.kt
            ├── data/                         — STUB ONLY (empty files)
            │   ├── local/database/DatabaseHelper.kt
            │   ├── local/repository/UserRepository.kt
            │   └── remote/{api/ApiService.kt, models/User.kt}
            └── domain/                       — STUB ONLY (empty files)
                ├── models/ChatMessage.kt      (ChatbotScreen.kt uses its own local one instead)
                └── usecases/ValidationUseCase.kt
```

No CI config, no README, no `google-services.json`, single Gradle module.

## Flags worth addressing

1. Duplicate theme packages — delete unused `com.gabai.ui.theme`.
2. `data/`/`domain/` are placeholders despite Retrofit/OkHttp/DataStore being wired into Gradle.
3. No real AI integration yet — chatbot is hardcoded keyword matching.
4. Version catalog vs. hardcoded dependency versions are out of sync.
5. Duplicate `compileOptions`/`kotlinOptions` blocks in `app/build.gradle.kts`.
6. Default template tests untouched — no real test coverage.

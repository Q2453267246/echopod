# EchoPod Agent Guide (AGENTS.md)

This file is the canonical engineering guide for AI coding agents (Codex, Claude Code, Gemini, etc.) working on **EchoPod**, an Android podcast application built with Kotlin and Jetpack Compose.

---

## 1. Project Overview & Tech Stack

- **App Name**: EchoPod (`com.echopod`)
- **Language & UI**: Kotlin (`2.2.21`), Jetpack Compose (BOM `2025.05.01`), Material 3
- **Build**: Gradle Kotlin DSL, AGP `8.13.2`, JDK `17+`, `minSdk = 26`, `compileSdk = 35`, `targetSdk = 35`
- **Design Assets**: `EchoPod-设计稿/` (PNG mockups in `PNG/00-设计语言`, `PNG/01-手机`, `PNG/02-折叠屏`, and HTML source specs in `源文件（Claude设计稿HTML）/`)
- **Implementation Plans**: `docs/` (e.g., `docs/coding-steps-navigation.md`)

### Common Commands

```sh
# Build debug APK
./gradlew :app:assembleDebug

# Run local JVM unit tests
./gradlew :app:testDebugUnitTest

# Run instrumented / Compose UI tests (requires connected device or emulator)
./gradlew :app:connectedDebugAndroidTest

# Lint check
./gradlew :app:lintDebug
```

---

## 2. Architecture Rules: Clean Architecture + MVVM

Follow strict unidirectional data flow (UDF) and Clean Architecture boundaries:

```text
presentation (Compose UI + ViewModel + UiState)
      │
      ▼
domain (Pure Kotlin Models + Repository Interfaces + UseCases)
      ▲
      │
data (Repository Implementations + DataSource / Local / Remote)
```

### Layer Constraints

1. **`domain` layer**:
   - **Pure Kotlin only**. Never import `android.*`, `androidx.*`, or Compose APIs.
   - Contains domain models, repository interfaces, and single-responsibility UseCases.
2. **`data` layer**:
   - Implements `domain` repository interfaces.
   - Handles persistence (DataStore / Room), network / RSS parsing, and DTO-to-Domain mapping.
3. **`presentation` layer (MVVM)**:
   - Each feature exposes an immutable `UiState` data class via `StateFlow` from a `ViewModel`.
   - Composables are stateless where possible: accept `state` and event lambdas (`onEvent`), never call repositories or UseCases directly.
   - Preserve UI state across configuration changes and fold/unfold transitions using `ViewModel` + `SavedStateHandle`.
4. **`core` packages**:
   - `core/designsystem`: Theme tokens, typography, shapes, icons, and reusable Quiet Paper UI components.
   - `core/adaptive`: Window size class and foldable posture helpers.

---

## 3. Design System: Quiet Paper (V1.0)

Reference: `EchoPod-设计稿/PNG/00-设计语言/01-设计语言-QuietPaper.png`

- **Principles**: Content-first, low-saturation warm neutral palette with a single brand accent (Mist Teal); cover art is the primary source of color.
- **Colors**:
  - **Light**: Background `#F7F5F0`, Card `#FFFFFF`, Container `#EFECE5`, Primary Text `#1C1B19`, Secondary Text `#6B675F`, Outline `#E3DFD6`, Brand `#2E6B66`, Brand Container `#D6E7E3`, OnBrandContainer `#123A37`, Update Dot `#E4893F`.
  - **Dark**: Background `#131412`, Card `#22231F`, Container `#2A2B27`, Primary Text `#ECEAE4`, Secondary Text `#A3A098`, Outline `#33342F`, Brand `#86C6BD`, Brand Container `#1F3B38`, OnBrandContainer `#BFE3DD`, Update Dot `#F0A060`.
- **Touch Targets**: Every interactive element must have a minimum touch target of `48dp × 48dp`.
- **Icons**: `24dp` outline icons, `1.8dp` stroke width, rounded caps.
- **Motion**: `200ms` for buttons/selection states, `300ms` for page transitions/bottom sheets.

---

## 4. Foldable & Adaptive Layout Rules

Reference: `EchoPod-设计稿/PNG/02-折叠屏/01-折叠屏适配规则.png`

- **Always adapt by window width (`currentWindowAdaptiveInfo()`), never by device model.**
- **Breakpoints**:
  - **Compact (`< 600dp`)**: Bottom `NavigationBar` (`80dp` height + system bar insets) + single-column content; mini player sits above bottom bar.
  - **Medium (`600–839dp`)**: Left `NavigationRail` (`88dp` width) with top `56dp` Import (`+`) FAB + adapted grid content.
  - **Expanded (`>= 840dp`)**: Left `NavigationRail` (keep Rail, do **not** switch to Permanent Drawer) + `ListDetailPaneScaffold` two-pane layout.
- **Foldable Postures (`FoldingFeature`)**:
  - Tabletop mode (horizontal hinge): media cover/title on top half, playback controls on bottom half; never place interactive controls across the hinge.
  - Book mode (vertical hinge): align two-pane divider with the hinge.
- **Continuity**: Never interrupt playback, lose selected tab, or reset scroll position on fold/unfold or rotation. Do not lock screen orientation in `AndroidManifest.xml`.

---

## 5. Engineering & Verification Guidelines

- Keep changes focused and minimal; avoid speculative abstractions.
- Always support both Light and Dark themes and Edge-to-Edge `WindowInsets` (`enableEdgeToEdge()`).
- Write JVM unit tests for every UseCase and ViewModel, and Compose UI tests for adaptive layout breakpoints (`Compact`, `Medium`, `Expanded`).
- Before finishing any code change, run `./gradlew :app:assembleDebug` and `./gradlew :app:testDebugUnitTest` and report the results.

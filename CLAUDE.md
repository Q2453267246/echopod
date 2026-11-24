# Claude Code Instructions — EchoPod

@AGENTS.md

## Claude-Specific Workflow Notes

- **Source of Truth**: Follow all architecture, Quiet Paper design tokens, foldable breakpoints, and build commands defined in `@AGENTS.md`.
- **Design Verification**: Before implementing UI screens or components, inspect the corresponding PNG mockup in `EchoPod-设计稿/PNG/` and HTML token specs in `EchoPod-设计稿/源文件（Claude设计稿HTML）/`.
- **Architecture Enforcement**:
  - Keep `domain/` free of Android/Compose imports.
  - Use immutable `UiState` + `StateFlow` in `ViewModel` for every screen.
- **Testing & Safety**:
  - Run `./gradlew :app:assembleDebug` and `./gradlew :app:testDebugUnitTest` after code edits.
  - Do not modify files inside `EchoPod-设计稿/`.
  - Prefer small, reviewable commits and avoid introducing unused third-party libraries.

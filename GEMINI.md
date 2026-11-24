# Gemini CLI / Antigravity Instructions — EchoPod

@./AGENTS.md

## Gemini-Specific Workflow Notes

- **Canonical Context**: Always adhere to the architecture, Quiet Paper design system, foldable rules, and verification commands in `@./AGENTS.md`.
- **Design Reference**: Cross-check visual details (colors, spacing, pill dimensions, dark mode tokens) against `EchoPod-设计稿/PNG/` and `EchoPod-设计稿/源文件（Claude设计稿HTML）/` before writing Compose UI.
- **Clean Architecture Guardrails**:
  - `domain/` must remain pure Kotlin with zero `android.*` or `androidx.*` imports.
  - `presentation/` must communicate with `domain/` exclusively via `ViewModel` and `UseCase`.
- **Verification**:
  - After modifying Kotlin or Gradle files, verify with `./gradlew :app:assembleDebug` and `./gradlew :app:testDebugUnitTest`.
  - Never edit or overwrite original assets under `EchoPod-设计稿/`.

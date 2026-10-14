---
type: project
created: 2026-05-25
updated: 2026-07-12
---

# Project Conventions

## Git Workflow
- Do not create branches; work and commit directly on `main`.

## Supported AI platforms (AG Kit)
- AG Kit **only supports Gemini CLI and Google Antigravity**.
- Do not claim compatibility with Claude Code, Cursor, Copilot, Windsurf, or other assistants unless the user explicitly expands scope.
- Copy on the website, docs, FAQ, README, and marketing should describe AG Kit as a toolkit for Gemini CLI / Antigravity-style agent setups.

## Documentation & Plans
- Store all plans and future markdown files in folder called `Docs`.

## Android Versioning & Commits
- Always bump version in `android/app/version.properties` with every code commit based on SemVer:
  - `fix(...)`: increment PATCH (e.g. `2.0.0` -> `2.0.1`) and increment `versionCode` (+1).
  - `feat(...)`: increment MINOR (e.g. `2.0.0` -> `2.1.0`) and increment `versionCode` (+1).
  - `BREAKING CHANGE`: increment MAJOR (e.g. `2.0.0` -> `3.0.0`) and increment `versionCode` (+1).
- Never commit code changes without bundling or following up with the corresponding version bump in `android/app/version.properties`.

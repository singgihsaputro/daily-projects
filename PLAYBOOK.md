# Playbook

The rules the nightly agent follows. Edit this file to change what it builds —
the routine reads it fresh every run, so changes take effect the next night.

## Track rotation

Deterministic, from the day of the year (`date +%j`):

**One platform per day. Never two.** The cycle runs
Android → iOS → Web → Backend → Kotlin Multiplatform, then back to Android.

Index the table with `(%j + 3) mod 5`. The `+ 3` is not decoration: it keeps the
sequence continuous with the projects already in `projects/`, so adding the KMP
track did not reshuffle or repeat a day.

| `(%j + 3) mod 5` | Track | Stack |
|---|---|---|
| 0 | **Android** | Kotlin, Jetpack Compose, Material 3, ViewModel + StateFlow, Gradle KTS |
| 1 | **iOS** | Swift 6, SwiftUI, `@Observable`, Swift Package Manager |
| 2 | **Web** | **Vue 3** — rotate: Vue 3 + Vite + Tailwind v4, Nuxt 4, Vue 3 + Pinia. Composition API and `<script setup>` throughout. No React. |
| 3 | **Backend** | rotate: Go (net/http), TypeScript (Hono), Python (FastAPI), Rust (Axum) |
| 4 | **Kotlin Multiplatform** | KMP + Compose Multiplatform, `shared/` module with `commonMain`, Android and iOS targets, `expect`/`actual` where a platform differs |

`%j` is the **UTC** day of the year. The nightly fire and its backups all fall on
the same UTC day, so they resolve to the same track and the same slot — which is
what stops a day ever producing two projects.

## What counts as a project

One evening's worth of work. A single screen or a handful of endpoints. If it
needs more than ~300 lines it is too big — cut scope.

Daily-task ideas (pick one **not already in `projects/`**):

reminder · checklist · image compressor · unit converter · pomodoro timer ·
expense splitter · QR generator · habit tracker · password generator ·
markdown scratchpad · tip calculator · countdown · water intake log ·
receipt scanner (mock OCR) · currency converter · packing list ·
grocery list · shift planner · reading tracker · sleep log

## Hard rules

1. **Mock data only.** No API keys, no live third-party calls, no secrets.
   Network-shaped features read from a local fixture file (`mock/*.json`)
   behind an interface, so swapping in a real client later is one file.
2. **It must run from a clean checkout** with the commands in the README, on a
   machine that has only the language toolchain installed.
3. **No placeholder code.** No `TODO`, no stubbed function bodies, no lorem ipsum.
4. **Verify what can be verified.**
   - Backend and web: actually start it, hit it, capture a real screenshot or
     terminal output.
   - **Android: the runner has a JDK and the Android SDK — run
     `./gradlew assembleDebug` and make it pass.** A project that does not
     compile does not ship.
   - **Kotlin Multiplatform: build what this runner can.** `./gradlew
     :shared:assemble` and the Android target must pass. The iOS target needs
     Xcode and will not build here — say so in the README rather than implying
     the whole thing was verified.
   - iOS: the runner is Linux, so there is no Xcode and no build. Write clean,
     complete source and say so in the README.
   - Neither mobile track has an emulator, so ship a hand-drawn SVG **mockup**
     for the preview — label it "UI mockup" and never call it a screenshot.
5. **One project per night, on one platform.** If a folder for today's date
   already exists, stop immediately and build nothing.

## Every project ships

```
README.md          what it does, why, the stack, and honest limitations
                   + "How to run" with exact copy-pasteable commands
                   + the preview image embedded at the top
preview.svg        real screenshot (web/backend) or labelled UI mockup (mobile)
mock/*.json        the fixture data
<source>           the actual project
```

The README opens with one sentence on what the thing does, then the preview,
then How to run. Not a feature tour.

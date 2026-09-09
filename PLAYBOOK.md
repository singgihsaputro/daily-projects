# Playbook

The rules the nightly agent follows. Edit this file to change what it builds —
the routine reads it fresh every run, so changes take effect the next night.

## Track rotation

Deterministic, from the day of the year (`date +%j`):

| `%j mod 4` | Track | Stack |
|---|---|---|
| 0 | **Android** | Kotlin, Jetpack Compose, Material 3, ViewModel + StateFlow, Gradle KTS |
| 1 | **iOS** | Swift 6, SwiftUI, `@Observable`, Swift Package Manager |
| 2 | **Backend** | rotate: Go (net/http), TypeScript (Hono), Python (FastAPI), Rust (Axum) |
| 3 | **Web** | rotate: React 19 + Vite + Tailwind v4, SvelteKit 5, Next.js 16 |

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
4. **Verify what can be verified.** Backend and web: actually start it, hit it,
   capture a real screenshot or terminal output. Android and iOS: the sandbox has
   no emulator or Xcode, so ship a hand-drawn SVG **mockup** — label it
   "UI mockup" and never call it a screenshot.
5. **One project per night.** If today's slot already exists, stop.

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

# Playbook

The rules the nightly agent follows. Edit this file to change what it builds —
the routine reads it fresh every run, so changes take effect the next night.

## Track rotation

Deterministic, from the day of the year (`date +%j`):

**One platform per day. Never two.** An eight-day cycle:

Android → iOS → Web → Backend → KMP mobile → **KMP full-stack** →
**Android library** → **Freelance web app** → back to Android.

Index the table with `(%j + 4) mod 8`. The offset is chosen so the first
full-stack day falls on 2026-09-22 and the cycle runs cleanly from there.

Widening five tracks to eight does **not** preserve how past days map — re-deriving
a track for 2026-09-14 under this formula gives a different answer than the one
that actually built it. That is harmless, because nothing ever re-derives a past
day: the only check is whether a folder for *today's* date already exists. Do not
try to "fix" old folders to match.

| `(%j + 4) mod 8` | Track | Stack | Budget |
|---|---|---|---|
| 0 | **Android** | Kotlin, Jetpack Compose, Material 3, ViewModel + StateFlow, Gradle KTS | ~300 lines |
| 1 | **iOS** | Swift 6, SwiftUI, `@Observable`, Swift Package Manager | ~300 lines |
| 2 | **Web** | **Vue 3** — rotate: Vue 3 + Vite + Tailwind v4, Nuxt 4, Vue 3 + Pinia. Composition API and `<script setup>`. No React on this track. | ~300 lines |
| 3 | **Backend** | rotate: Go (net/http), TypeScript (Hono), Python (FastAPI), Rust (Axum) | ~300 lines |
| 4 | **KMP mobile** | KMP + Compose Multiplatform, `shared/` with `commonMain`, Android and iOS targets, `expect`/`actual` where a platform differs | ~300 lines |
| 5 | **KMP full-stack** | One repository, four targets — see below | ~700 lines |
| 6 | **Android library** | A publishable library, not an app — see below | ~400 lines |
| 7 | **Freelance web app** | A slice of something a client would pay for — see below | ~600 lines |

### 5 · KMP full-stack, one repository

Kotlin end to end, four targets sharing one domain:

```
shared/      commonMain — models, use cases, repository interfaces. The point.
server/      Ktor, serving the API the clients call
androidApp/  Compose
iosApp/      SwiftUI (written, not built — no Xcode on the runner)
webApp/      Compose for Web, or Kotlin/JS
```

The value is that a model defined once in `commonMain` is used by all four. Say
plainly in the README which targets were actually built.

Keep the domain trivial so the wiring is the work: a todo list, a note store, a
counter synced across clients. Ideas: shared shopping list · habit sync ·
bookmark store · short-link service · reading queue.

### 6 · Android library, for developers

A library another developer would depend on — published as a Gradle module with a
clear public API, not an app. It needs a README showing the dependency
coordinates, a usage snippet, and a `sample/` app that actually exercises it.

Ideas: on-device debug overlay (FPS, memory, last network calls) · logging
wrapper with redaction for PII · Compose skeleton/shimmer loaders · a
feature-flag debug menu · retry-with-backoff helper for Kotlin Flows ·
Indonesian formatters (Rupiah, phone numbers, NIK validation) · permission
request helper · crash breadcrumb recorder · a collection of Compose modifiers ·
a network inspector that logs to a shareable file.

### 7 · Freelance web app

The kind of thing a small business actually pays for. **React 19 + Vite +
Tailwind v4** is allowed here (and preferred — it is what freelance clients ask
for); rotate with Next.js 16 and Vue 3 + Tailwind.

Scope to one useful screen or flow, finished, not a whole ERP. "The stock-in
screen of a warehouse system" beats "an ERP" every time.

Ideas: mart POS — cart, checkout, printable receipt · inventory with low-stock
alerts · invoice generator with PDF export · restaurant order board ·
attendance and leave tracker · simple CRM pipeline · booking and reservation
calendar · expense claim approval · school fee admin · purchase-order approval.

`%j` is the **UTC** day of the year. The nightly fire and its backups all fall on
the same UTC day, so they resolve to the same track and the same slot — which is
what stops a day ever producing two projects.

## What counts as a project

One evening's worth of work. Stay inside the budget in the rotation table — most
tracks are a single screen or a handful of endpoints at ~300 lines; the
full-stack, library and freelance tracks get more room because they have more
surface to cover, not because they are allowed to sprawl. Over budget means cut
scope, never ship half a thing.

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
   - **KMP full-stack: build the server and every target that is not iOS.**
     `./gradlew build` must pass for `shared`, `server` and the Android and web
     targets. The iOS target will not build here; name that in the README rather
     than implying the whole repository was verified.
   - **Android library: `./gradlew build` must pass, and the `sample/` app must
     compile against the library** — a library nothing consumes is untested.
   - **Freelance web app: install, build, run it and capture a real screenshot.**
     This track has no excuse for a mockup.
   - Neither mobile track has an emulator, so ship a hand-drawn SVG **mockup**
     for the preview — label it "UI mockup" and never call it a screenshot.
5. **One project per night, on one platform.** If a folder for today's date
   already exists, stop immediately and build nothing.

## Folder naming

`projects/<YYYY-MM-DD>-<track>-<short-name>/`, where `<track>` is one of
`android`, `ios`, `web`, `backend`, `kmp`, `kmpfullstack`, `androidlib`,
`freelance`. The standalone repository drops the date prefix, so today's
freelance project becomes `freelance-mart-pos`.

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

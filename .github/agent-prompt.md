Build tonight's mini project in this repository.

1. Read `PLAYBOOK.md` first. It is the spec — follow it exactly. Where it
   conflicts with anything below, PLAYBOOK.md wins.

2. Work out today's slot:
   - `today=$(date -u +%F)`, `doy=$((10#$(date -u +%j)))`
   - track = `doy % 4` → 0 Android, 1 iOS, 2 Web, 3 Backend
     (so the cycle reads Android → iOS → Web → Backend, one platform per day)
   - If any directory in `projects/` already starts with today's date, stop and
     do nothing at all.

3. List every existing folder in `projects/` and pick an idea from the
   playbook's list that has NOT been used before. Rotate the sub-stack for
   backend and web so consecutive days are not all the same language.

4. Create `projects/<YYYY-MM-DD>-<track>-<short-name>/` and build it there. One
   evening of work: a single screen or a handful of endpoints, under roughly 300
   lines. Mock data only — no API keys, no live third-party calls, no secrets.
   Anything network-shaped reads from `mock/*.json` behind an interface, so a
   real client could be swapped in later by changing one file.

5. Verify what can be verified:
   - Backend and web: install, build, and actually run it. Exercise it. Fix
     whatever breaks. Capture real evidence — a headless browser screenshot if a
     browser is available, otherwise real captured terminal output.
   - Android and iOS: this runner has no emulator, no simulator and no Xcode. Do
     not pretend otherwise and do not fake a screenshot. Write clean, complete,
     compilable source, and hand-draw `preview.svg` as a UI mockup. Label it
     "UI mockup" in the README, never "screenshot".

6. Write `README.md` in the project folder: one sentence on what it does, then
   the preview image, then "How to run" with exact copy-pasteable commands that
   work from a clean checkout on a machine with only the language toolchain
   installed, then the stack, then honest limitations — state plainly when a
   mobile project was written but not built.

7. No placeholder code. No TODO comments, no stubbed function bodies, no lorem
   ipsum. If you cannot finish something properly, build something smaller that
   is complete.

8. Publish the project as its own public repository:
   `scripts/publish.sh projects/<folder> "<one-line description>"`
   That script is deliberately a no-op when repo creation is not permitted. If it
   skips, that is fine — do not work around it, just say so in your report.

Do not run `git commit` or `git push` yourself; the workflow does that after you
finish. End with a short report: the track and idea you picked, whether the
project was actually run and verified or only written, and whether the
standalone repository was created.

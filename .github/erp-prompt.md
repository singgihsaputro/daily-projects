Deliver tonight's ERP feature. The current directory is the ERP repository.

1. Read `CLAUDE.md` first — it is the spec and every rule in it applies. Then read
   `ROADMAP.md`, the top of `CHANGELOG.md`, and enough of the code to know how the
   existing modules are built, so tonight's work matches them.

2. Tonight's night date is in the `NIGHT` environment variable (`echo "$NIGHT"`).

3. Take the **first unchecked line** in `ROADMAP.md` and deliver it completely:
   migration, shared schemas, API endpoints with validation, tests, the screen,
   seed data, and a screenshot. If the repository is empty apart from the docs,
   tonight also builds the skeleton described in `CLAUDE.md` — the root scripts
   must exist and pass by the end.

   - If the feature depends on something that is missing, build that as part of
     tonight and say so in the CHANGELOG entry.
   - If the feature is too big for one night, deliver a complete, useful slice of
     it, keep the line's ID for that slice, and add a new unchecked line right
     after it for the remainder. A finished smaller feature beats a half-built
     large one.
   - At least one feature a night. Take the next line too only if the first is
     completely done and verified and the second is small; never leave a second
     feature half-built.

4. Run `npm run verify` and make it pass. Then start the app with the seed data
   and exercise tonight's feature end to end through the API (curl) and the
   screen. Fix whatever breaks. The workflow runs `npm ci && npm run verify`
   again after you finish and throws the whole night away if it fails, so do not
   finish with it red.

5. Record what shipped, exactly as `CLAUDE.md` says: tick the line in
   `ROADMAP.md` as `- [x] F0NN Title — $NIGHT` (the real date), add the
   `CHANGELOG.md` entry, and update the Modules list in `README.md`.

Never rewrite or remove existing features, never delete or weaken a test to get
to green, and never edit a migration that has already shipped.

Do not run `git commit` or `git push`; the workflow does that after checking your
work. End with a short report: the roadmap ID and title you delivered, what a user
can now do, the test count, and whether a screenshot was captured.

# daily-projects

A scheduled cloud agent that builds software every night at 23:00 WIB.

**Now: one ERP, one feature a night.** The agent is building an ERP for
Indonesian retail and distribution SMEs in its own repository,
[`singgihsaputro/erp`](https://github.com/singgihsaputro/erp). Each night it
takes the next line of that repository's `ROADMAP.md`, builds it with tests and
a screen, and the workflow publishes it only if a clean install passes
typecheck, tests and build.

- **The workflow:** [`.github/workflows/erp-nightly.yml`](.github/workflows/erp-nightly.yml)
- **What the agent is told each night:** [`.github/erp-prompt.md`](.github/erp-prompt.md)
- **The roadmap and conventions it starts from:** [`erp-seed/`](erp-seed/) — copied
  into the ERP repository on the first night. After that, edit `ROADMAP.md` and
  `CLAUDE.md` in the ERP repository itself; the agent reads them fresh every night.

**Before: a different mini project every night** (Android, iOS, web, backend and
more, in rotation). That workflow, [`nightly.yml`](.github/workflows/nightly.yml),
is paused; its schedule is commented out, and the 50 projects it built stay in
[`projects/`](projects/), each also in its own repository. Its rules are in
[PLAYBOOK.md](PLAYBOOK.md).

## Schedule

`0 16 * * *` UTC (23:00 WIB), with backups at 01:05 and 04:05 WIB because
GitHub's scheduler starts runs late or drops them. A night runs from 19:00 to
19:00 WIB; every fire inside it works out the same night and does nothing if
that night's feature has already shipped, so the backups only matter when the
first attempt was dropped or failed. A night whose every attempt fails shows up
as a failed run, and GitHub emails you about it.

To get an extra feature on a given day, run it by hand: **Actions** → *Nightly
ERP feature* → *Run workflow*, and tick *Deliver another feature*.

## Setup

Secrets (Settings → Secrets and variables → Actions):

| Secret | Required | What it does |
|---|---|---|
| `CLAUDE_CODE_OAUTH_TOKEN` | one of these two | Subscription token from `claude setup-token`. No per-run billing. |
| `ANTHROPIC_API_KEY` | one of these two | API key instead. Bills per run. |
| `PUBLISH_TOKEN` | **yes** | PAT with `repo` scope. Creates the ERP repository on the first night and pushes each night's feature to it. It is never given to the agent. |

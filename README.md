# daily-projects

A small app a night, built by a scheduled cloud agent at 23:00 WIB and published
as its own public repository. Android, iOS, backend and web in rotation.

Every project is deliberately tiny — one evening, one useful thing, real running
code with mock data instead of API keys.

- **What gets built and the rules it follows:** [PLAYBOOK.md](PLAYBOOK.md)
- **The archive:** [`projects/`](projects/) — every project also lives in its own repo
- **How publishing works:** [`scripts/publish.sh`](scripts/publish.sh)

Nothing here runs on my laptop — it runs on a GitHub Actions runner at
`0 16 * * *` UTC, so the schedule holds whether the machine is on or not.

## Setup

The workflow needs one secret before it can run
(Settings → Secrets and variables → Actions):

| Secret | Required | What it does |
|---|---|---|
| `CLAUDE_CODE_OAUTH_TOKEN` | one of these two | Subscription token from `claude setup-token`. No per-run billing. |
| `ANTHROPIC_API_KEY` | one of these two | API key instead. Bills per run. |
| `PUBLISH_TOKEN` | optional | PAT with `repo` scope so each project also becomes its own repository. Without it, projects still land in `projects/` here. |

Then run it once by hand from the **Actions** tab → *Nightly mini project* →
*Run workflow*, to confirm it works before trusting the schedule.

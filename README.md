# daily-projects

A small app a night, built by a scheduled cloud agent at 23:00 WIB and published
as its own public repository. Android, iOS, backend and web in rotation.

Every project is deliberately tiny — one evening, one useful thing, real running
code with mock data instead of API keys.

- **What gets built and the rules it follows:** [PLAYBOOK.md](PLAYBOOK.md)
- **The archive:** [`projects/`](projects/) — every project also lives in its own repo
- **How publishing works:** [`scripts/publish.sh`](scripts/publish.sh)

Nothing here runs on my laptop. The agent runs in Anthropic's cloud with its own
checkout, so the schedule holds whether the machine is on or not.

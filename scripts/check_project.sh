#!/usr/bin/env bash
# Refuse to publish an incomplete project.
#
#   scripts/check_project.sh projects/2026-09-23-androidlib-retry-backoff
#
# The playbook requires a README with how to run it, and no placeholder code.
# Nothing enforced that, so a night that ran out of room shipped a repository
# with no README and a generic description derived from the missing file.
set -uo pipefail

dir=${1:?usage: check_project.sh <project-dir>}
fail=0

note() { echo "::error::$(basename "$dir"): $1"; fail=1; }

[ -d "$dir" ] || { echo "no such directory: $dir" >&2; exit 1; }

readme="$dir/README.md"
if [ ! -f "$readme" ]; then
  note "no README.md. Every project ships one — see PLAYBOOK.md."
else
  lines=$(grep -cvE '^\s*$' "$readme")
  [ "$lines" -ge 8 ] || note "README.md is only $lines lines; it needs what it does, a preview, and how to run it."
  grep -qiE '^#+ *how to run|^#+ *running|^#+ *usage' "$readme" \
    || note "README.md has no 'How to run' section."
fi

# Placeholder code is explicitly banned by the playbook.
placeholders=$(grep -rIlE 'TODO|FIXME|lorem ipsum' "$dir" \
  --exclude-dir=.git --exclude-dir=build --exclude-dir=node_modules 2>/dev/null || true)
if [ -n "$placeholders" ]; then
  note "placeholder markers found in: $(echo "$placeholders" | tr '\n' ' ')"
fi

if [ "$fail" -eq 0 ]; then
  echo "$(basename "$dir") looks complete."
else
  echo "::notice::The project is committed and safe. Fix it and the next run publishes it — publish.sh walks every folder."
fi
exit "$fail"

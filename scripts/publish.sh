#!/usr/bin/env bash
# Publish one project folder as its own public GitHub repo.
#
#   scripts/publish.sh projects/2026-09-10-android-reminder "One-line description"
#
# Degrades safely: if repo creation is not permitted (no gh auth, missing scope),
# it says so and exits 0 — the project is already committed here, nothing is lost.
set -euo pipefail

dir=${1:?usage: publish.sh <project-dir> [description]}
desc=${2:-"A small daily project"}
name=$(basename "$dir")

[ -d "$dir" ] || { echo "no such directory: $dir" >&2; exit 1; }

if ! gh auth status >/dev/null 2>&1; then
  echo "gh is not authenticated — skipping publish, project stays in this repo."
  exit 0
fi

owner=$(gh api user --jq .login)

if gh repo view "$owner/$name" >/dev/null 2>&1; then
  echo "$owner/$name already exists — skipping."
  exit 0
fi

work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
cp -R "$dir/." "$work/"

git -C "$work" init -q -b main
git -C "$work" add -A
git -C "$work" -c user.email="singgih.rochmad@gmail.com" \
                -c user.name="Singgih Rochmad Saputro" \
                commit -q -m "$name

$desc"

if ! gh repo create "$name" --public --source="$work" --remote=origin --push \
     --description "$desc" 2>&1; then
  echo "could not create $owner/$name (permissions?) — project stays in this repo."
  exit 0
fi

echo "published https://github.com/$owner/$name"

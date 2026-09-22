#!/usr/bin/env bash
# Publish one project folder as its own public GitHub repo.
#
#   scripts/publish.sh projects/2026-09-10-android-reminder "One-line description"
#
# ...publishes to the repository "android-reminder".
#
# Exception: kids activity books all collect in ONE repository, a book per folder,
# because a shelf of books reads better than thirty repositories of one book each.
#
# Degrades safely: if repo creation is not permitted (no gh auth, missing scope),
# it says so and exits 0 — the project is already committed here, nothing is lost.
set -euo pipefail

dir=${1:?usage: publish.sh <project-dir> [description]}
desc=${2:-"A small daily project"}
# The archive folder keeps its date prefix (that is how the agent knows a slot
# is already filled); the published repository does not need it.
name=$(basename "$dir")
name=${name#[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]-}

[ -d "$dir" ] || { echo "no such directory: $dir" >&2; exit 1; }

if ! gh auth status >/dev/null 2>&1; then
  echo "gh is not authenticated — skipping publish, project stays in this repo."
  exit 0
fi

owner=$(gh api user --jq .login)

# ---- kids activity books: append to one shelf repo, never a repo per book ----
if [[ "$(basename "$dir")" == *-kidsbook-* ]]; then
  shelf=kids-activity-books
  book=$(basename "$dir")            # keep the date here; it orders the shelf

  if ! gh repo view "$owner/$shelf" >/dev/null 2>&1; then
    gh repo create "$shelf" --public \
      --description "Printable activity books for ages 1-5, one folder per book" \
      >/dev/null || { echo "could not create $shelf — book stays in this repo."; exit 0; }
  fi

  work=$(mktemp -d); trap 'rm -rf "$work"' EXIT
  if ! git clone -q "https://github.com/$owner/$shelf.git" "$work" 2>/dev/null; then
    echo "could not clone $shelf — book stays in this repo."; exit 0
  fi
  git -C "$work" checkout -q -B main

  if [ -d "$work/books/$book" ]; then
    echo "$shelf already has books/$book — skipping."; exit 0
  fi

  mkdir -p "$work/books/$book"
  cp -R "$dir/." "$work/books/$book/"

  # a shelf needs an index, or it is just a pile
  {
    echo "# Activity books for ages 1–5"
    echo
    echo "Printable A4 books, thick lines and big shapes, no reading required."
    echo "Each folder holds its generator script, the PDF, and page previews."
    echo
    echo "| Book | Pages | PDF |"
    echo "|---|---|---|"
    for b in $(ls "$work/books" | sort -r); do
      pages=$(pdfinfo "$work/books/$b/book.pdf" 2>/dev/null | awk '/^Pages/{print $2}')
      echo "| [$b](books/$b) | ${pages:-—} | [book.pdf](books/$b/book.pdf) |"
    done
  } > "$work/README.md"

  git -C "$work" add -A
  git -C "$work" -c user.email="singgih.rochmad@gmail.com" \
                 -c user.name="Singgih Rochmad Saputro" \
                 commit -q -m "Add $book

$desc"
  if ! git -C "$work" push -q origin main 2>/dev/null; then
    echo "could not push to $shelf — book stays in this repo."; exit 0
  fi
  echo "published https://github.com/$owner/$shelf/tree/main/books/$book"
  exit 0
fi

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

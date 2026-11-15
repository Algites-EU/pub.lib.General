#!/usr/bin/env bash
# Record file renames/moves introduced by pub.lib.General-key-20261105.tgz.
# Run from anywhere within the pub.lib.General Git worktree.
# Works both BEFORE and AFTER unpacking the modified .tgz archive.
set -euo pipefail

if ! repo_root="$(git rev-parse --show-toplevel 2>/dev/null)"; then
  echo 'ERROR: Run this script inside the pub.lib.General Git repository.' >&2
  exit 1
fi
cd "$repo_root"
base='data/dataobject/intf/src/product/java/eu/algites/lib/data/dataobject'
if [[ ! -d "$base" ]]; then
  echo "ERROR: Expected pub.lib.General source directory is missing: $base" >&2
  exit 1
fi

move_annotation() {
  local old="$1" new="$2" backup_dir='' restored_old=0
  if ! git ls-files --error-unmatch -- "$old" >/dev/null 2>&1; then
    if git ls-files --error-unmatch -- "$new" >/dev/null 2>&1; then
      echo "Already tracked under new name: $new"
      return 0
    fi
    echo "ERROR: Neither expected tracked source nor tracked destination: $old" >&2
    return 1
  fi

  # If the archive was unpacked with a --delete-like synchronization, the
  # original tracked file may be missing in the worktree. Restore from the
  # index solely to make the Git rename possible; its contents will be replaced
  # by the already-unpacked destination below.
  if [[ ! -f "$old" ]]; then
    mkdir -p "$(dirname "$old")"
    git show ":$old" > "$old"
    restored_old=1
  fi

  # The destination may already contain updated code from the new archive.
  # Preserve its bytes, permissions and mtime; git mv must not overwrite them.
  if [[ -e "$new" ]]; then
    backup_dir="$(mktemp -d "$(dirname "$new")/.git-mv-preserve.XXXXXXXX")"
    mv -- "$new" "$backup_dir/$(basename "$new")"
  fi

  if ! git mv -- "$old" "$new"; then
    if [[ -n "$backup_dir" ]]; then
      mv -- "$backup_dir/$(basename "$new")" "$new"
      rmdir -- "$backup_dir"
    fi
    if (( restored_old )); then rm -f -- "$old"; fi
    echo "ERROR: git mv failed for $old" >&2
    return 1
  fi

  if [[ -n "$backup_dir" ]]; then
    mv -- "$backup_dir/$(basename "$new")" "$new"
    rmdir -- "$backup_dir"
  fi
  # Stage the new content too, so the rename reflects the delivered archive.
  git add -- "$new"
  echo "git mv: $old -> $new"
}

move_annotation \
  "$base/xmldefs/AIaXmlKeyDefinition.java" \
  "$base/AIaDataObjectListKeyDefinition.java"
move_annotation \
  "$base/xmldefs/AIaXmlKeyDefinitions.java" \
  "$base/AIaDataObjectListKeyDefinitions.java"

echo
echo 'Staged changes (rename detection enabled):'
git diff --cached --name-status --find-renames -- "$base"

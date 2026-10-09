#!/usr/bin/env bash
set -euo pipefail

# Applies only after the former 1.1 artifact-layout migration, before overlaying the corrected ZIP.
# Unrelated staged or unstaged changes are allowed; no files are deleted.
cd "$(git rev-parse --show-toplevel)"

if [[ -d version/common/intf && -d version/common/impl ]]; then
    echo "Version artifacts already use version/common/{intf,impl}; no Git moves needed."
    exit 0
fi
if [[ ! -d version/commonintf || ! -d version/commonimpl ]]; then
    echo "Expected version/commonintf and version/commonimpl. Aborting without moving files." >&2
    exit 1
fi
if [[ -e version/common/intf || -e version/common/impl ]]; then
    echo "One destination already exists; resolve the partial migration before retrying." >&2
    exit 1
fi
mkdir -p version/common

git mv version/commonintf version/common/intf
git mv version/commonimpl version/common/impl

echo "Version common artifacts moved. Overlay the corrected ZIP and run git add -A."

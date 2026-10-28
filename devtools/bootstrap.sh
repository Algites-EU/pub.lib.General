#!/usr/bin/env bash
set -euo pipefail
locRepo="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd -P)"
exec bash "$locRepo/devops/bootstrap/bootstrap.sh" "$@"

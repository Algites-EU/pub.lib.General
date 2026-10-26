#!/usr/bin/env bash
set -euo pipefail

# Standalone deployment of ONLY naming.convention.{algites,standardprefix,standardpostfix}.
# Required contracts must already be available from Maven/Python repositories.
locRepository="$(cd "$(dirname "$0")/.." && pwd -P)"
locAction="${1:---upload}"
case "$locAction" in
    --plan-only|--prepare-only|--upload) ;;
    --help|-h)
        echo 'Usage: bash devtools/bootstrap-profiles.sh [--plan-only|--prepare-only|--upload]'
        exit 0 ;;
    *) echo "Unknown action: $locAction" >&2; exit 2 ;;
esac
[[ "$#" -le 1 ]] || { echo 'Only one action is accepted' >&2; exit 2; }
locPython="${ALGITES_PYTHON_EXECUTABLE:-python3}"
locBuildRoot="$locRepository/build/run/naming-profiles-bootstrap"
locVersion="${MODUSTRO_GENERAL_BOOTSTRAP_VERSION:-$("$locPython" - "$locRepository/modustro-source-repository.yml" <<'PY'
import sys, yaml
version = yaml.safe_load(open(sys.argv[1], encoding='utf-8'))['Version']
print(f"{version['ReleaseLineVersion']}.{version['Revision']}" +
      ('-SNAPSHOT' if str(version.get('QualifierKind','release')).lower() == 'snapshot' else ''))
PY
)}"
locJavaRepo="${MODUSTRO_GENERAL_BOOTSTRAP_CLOUDSMITH_REPO:-algites/java-snapshots-pub}"
locPythonRepo="${MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_CLOUDSMITH_REPO:-algites/python-snapshots-pub}"
locDependencyMode="${MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE:-remote}"
case "$locDependencyMode" in remote|local|source) ;; *) echo "Invalid dependency mode $locDependencyMode" >&2; exit 2 ;; esac
locRefresh="${MODUSTRO_BOOTSTRAP_REFRESH_DEPENDENCIES:-}"
if [[ -z "$locRefresh" ]]; then
    if [[ "$locDependencyMode" == remote ]]; then locRefresh=1; else locRefresh=0; fi
fi
locHelper="$locRepository/devtools/bootstrap/prepare.py"
locArgs=(--repository "$locRepository" --output "$locBuildRoot" --version "$locVersion" --profiles-only)
if [[ -n "${MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_VERSION:-}" ]]; then
    locArgs+=(--python-version "$MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_VERSION")
fi
mkdir -p "$locRepository/build/run"
rm -rf -- "$locBuildRoot"
mkdir -p "$locBuildRoot"
"$locPython" "$locHelper" plan "${locArgs[@]}"
if [[ "$locAction" == --plan-only ]]; then
    echo "Standalone profile plan: $locBuildRoot"
    exit 0
fi
locGradle=(bash "$locRepository/gradlew")
if [[ -n "${MODUSTRO_GRADLE_EXECUTABLE:-}" ]]; then locGradle=("$MODUSTRO_GRADLE_EXECUTABLE"); fi
locGradleArgs=(--no-daemon --project-dir "$locBuildRoot/gradle")
if [[ "$locRefresh" == 1 ]]; then locGradleArgs+=(--refresh-dependencies); fi
MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE="$locDependencyMode" \
MODUSTRO_GENERAL_BOOTSTRAP_OUTPUT="$locBuildRoot" \
MODUSTRO_GENERAL_BOOTSTRAP_VERSION="$locVersion" \
    "${locGradle[@]}" "${locGradleArgs[@]}" publishAllPublicationsToBootstrapRepository
"$locPython" "$locHelper" verify-maven "${locArgs[@]}"
"$locPython" "$locHelper" package-python "${locArgs[@]}" --python-executable "$locPython"
[[ "$(wc -l < "$locBuildRoot/packages.tsv")" == 3 ]] || { echo 'Expected three Maven profiles' >&2; exit 1; }
[[ "$(wc -l < "$locBuildRoot/python-packages.tsv")" == 3 ]] || { echo 'Expected three Python profiles' >&2; exit 1; }
echo "Three Java and three Python naming profiles prepared: $locBuildRoot"
if [[ "$locAction" == --prepare-only || "${MODUSTRO_BOOTSTRAP_PREPARE_ONLY:-0}" == 1 ]]; then
    exit 0
fi
command -v algites-cloudsmith >/dev/null || { echo 'algites-cloudsmith is required to upload' >&2; exit 2; }
algites-cloudsmith --session -- bash -c '
    set -euo pipefail
    while IFS=$'"'"'\t'"'"' read -r jar pom module sources; do
        cloudsmith push maven "$1" "$jar" --pom-file "$pom" --extra-files "$module" --sources-file "$sources"
    done < "$2"
    while IFS=$'"'"'\t'"'"' read -r wheel sdist; do
        cloudsmith push python "$3" "$wheel"
        cloudsmith push python "$3" "$sdist"
    done < "$4"
' _ "$locJavaRepo" "$locBuildRoot/packages.tsv" "$locPythonRepo" "$locBuildRoot/python-packages.tsv"

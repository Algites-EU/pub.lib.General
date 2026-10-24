#!/usr/bin/env bash
set -euo pipefail

# Standalone pub.lib.General Maven + Python bootstrap. No Modustro Builder.
# Like the other Algites/Modustro bootstraps, one invocation builds and uploads
# unless MODUSTRO_BOOTSTRAP_PREPARE_ONLY=1 explicitly disables publication.
locRepo="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd -P)"
locHelper="$locRepo/devtools/bootstrap/prepare.py"
locBuildRoot="$locRepo/build/run/general-bootstrap"
locJavaRepo="${MODUSTRO_GENERAL_BOOTSTRAP_CLOUDSMITH_REPO:-${MODUSTRO_GENERAL_BOOTSTRAP_JAVA_REPO:-algites/java-snapshots-pub}}"
locPythonRepo="${MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_CLOUDSMITH_REPO:-${MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_REPO:-algites/python-snapshots-pub}}"
locPython="${ALGITES_PYTHON_EXECUTABLE:-python3}"

locMode="${1:---upload}"
case "$locMode" in
    --plan-only|--prepare-only|--upload) ;;
    --help|-h)
        printf '%s\n' \
            'Usage: bash devtools/bootstrap.sh [--plan-only|--prepare-only|--upload]' \
            'Default: build, verify and upload in one invocation.' \
            'Set MODUSTRO_BOOTSTRAP_PREPARE_ONLY=1 to build and verify without upload.'
        exit 0 ;;
    *) echo "Unknown bootstrap option: $locMode" >&2; exit 2 ;;
esac
if [[ $# -gt 1 ]]; then
    echo 'Only one optional bootstrap action is permitted' >&2
    exit 2
fi

locPrepareOnly="${MODUSTRO_BOOTSTRAP_PREPARE_ONLY:-0}"
case "$locPrepareOnly" in
    0|1) ;;
    *) echo "Invalid MODUSTRO_BOOTSTRAP_PREPARE_ONLY='$locPrepareOnly' (0|1)" >&2; exit 2 ;;
esac
if [[ "$locMode" == --prepare-only ]]; then
    locPrepareOnly=1
fi

# Dependency resolution follows the common bootstrap precedence policy.
# All pub.lib.General project dependencies are always built from this checkout;
# source mode therefore has no additional sibling-source preparation step.
locDependencyMode="${MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE:-remote}"
case "$locDependencyMode" in
    remote|local|source) ;;
    *) echo "Invalid MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE='$locDependencyMode' (remote|local|source)" >&2; exit 2 ;;
esac
export MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE="$locDependencyMode"
locRefreshDependencies="${MODUSTRO_BOOTSTRAP_REFRESH_DEPENDENCIES:-}"
if [[ -z "$locRefreshDependencies" ]]; then
    if [[ "$locDependencyMode" == remote ]]; then
        locRefreshDependencies=1
    else
        locRefreshDependencies=0
    fi
fi
case "$locRefreshDependencies" in
    0|1) ;;
    *) echo "Invalid MODUSTRO_BOOTSTRAP_REFRESH_DEPENDENCIES='$locRefreshDependencies' (0|1)" >&2; exit 2 ;;
esac
export MODUSTRO_BOOTSTRAP_REFRESH_DEPENDENCIES="$locRefreshDependencies"
echo "Bootstrap dependency mode: $locDependencyMode; refresh published dependencies: $locRefreshDependencies"
if [[ "$locMode" == --plan-only ]]; then
    echo 'Bootstrap action: plan only (no build, no upload)'
elif [[ "$locPrepareOnly" == 1 ]]; then
    echo 'Bootstrap action: build and prepare only (no upload)'
else
    echo 'Bootstrap action: build, verify and upload to Cloudsmith'
fi

if ! command -v "$locPython" >/dev/null 2>&1; then
    echo "Missing Python interpreter: $locPython" >&2; exit 2
fi
if ! "$locPython" -c 'import yaml' >/dev/null 2>&1; then
    echo 'PyYAML is required to interpret modustro-artifact.yml (python -m pip install pyyaml)' >&2; exit 2
fi
locDetectedVersion="$("$locPython" - "$locRepo/modustro-source-repository.yml" <<'PY_VERSION'
import sys
import yaml
release = yaml.safe_load(open(sys.argv[1], encoding='utf-8'))['Version']
base = str(release['ReleaseLineVersion']) + '.' + str(release['Revision'])
kind = str(release.get('QualifierKind', 'release')).lower()
if kind not in ('snapshot', 'release'):
    raise SystemExit('Unsupported repository version qualifier: ' + kind)
print(base + ('-SNAPSHOT' if kind == 'snapshot' else ''))
PY_VERSION
)"
locVersion="${MODUSTRO_GENERAL_BOOTSTRAP_VERSION:-$locDetectedVersion}"
if [[ "$locMode" != --plan-only ]] && ! "$locPython" -c 'import setuptools, wheel' >/dev/null 2>&1; then
    echo 'Python build backends are required (python -m pip install setuptools wheel)' >&2; exit 2
fi

# Never mix artifacts from different source snapshots or versions.
mkdir -p "$locRepo/build/run"
if [[ -e "$locBuildRoot" && ! -d "$locBuildRoot" ]]; then
    echo "Build output path is not a directory: $locBuildRoot" >&2; exit 2
fi
rm -rf -- "$locBuildRoot"
mkdir -p "$locBuildRoot"
locArgs=(--repository "$locRepo" --output "$locBuildRoot" --version "$locVersion")
if [[ -n "${MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_VERSION:-}" ]]; then
    locArgs+=(--python-version "$MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_VERSION")
fi
export PYTHONDONTWRITEBYTECODE=1
export PYTEST_ADDOPTS="${PYTEST_ADDOPTS:+${PYTEST_ADDOPTS} }-p no:cacheprovider"
"$locPython" "$locHelper" plan "${locArgs[@]}"
if [[ "$locMode" == --plan-only ]]; then
    echo "Bootstrap plan generated in $locBuildRoot; no build and no upload performed."
    exit 0
fi

locGradle=(bash "$locRepo/gradlew")
if [[ -n "${MODUSTRO_GRADLE_EXECUTABLE:-}" ]]; then
    locGradle=("${MODUSTRO_GRADLE_EXECUTABLE}")
fi
locGradleArgs=(--no-daemon --project-dir "$locBuildRoot/gradle")
if [[ "$locRefreshDependencies" == 1 ]]; then
    locGradleArgs+=(--refresh-dependencies)
fi
# The standalone build publishes only to local staging; never to Maven Local or
# Cloudsmith as a Gradle side effect.
MODUSTRO_GENERAL_BOOTSTRAP_OUTPUT="$locBuildRoot" MODUSTRO_GENERAL_BOOTSTRAP_VERSION="$locVersion" \
    "${locGradle[@]}" "${locGradleArgs[@]}" publishAllPublicationsToBootstrapRepository

"$locPython" "$locHelper" verify-maven "${locArgs[@]}"
"$locPython" "$locHelper" package-python "${locArgs[@]}" --python-executable "$locPython"
[[ "$(wc -l < "$locBuildRoot/packages.tsv")" == 31 ]] || { echo 'Unexpected Maven manifest size' >&2; exit 1; }
[[ "$(wc -l < "$locBuildRoot/python-packages.tsv")" == 9 ]] || { echo 'Unexpected Python manifest size' >&2; exit 1; }

# Same manifest-output override as the other Modustro bootstraps; do not move
# the real staging area or rebuild for a later --upload invocation.
locManifestOutput="${MODUSTRO_BOOTSTRAP_OUTPUT_DIR:-$locBuildRoot}"
mkdir -p "$locManifestOutput"
if [[ "$locManifestOutput/packages.tsv" != "$locBuildRoot/packages.tsv" ]]; then
    cp "$locBuildRoot/packages.tsv" "$locManifestOutput/packages.tsv"
fi
if [[ "$locManifestOutput/python-packages.tsv" != "$locBuildRoot/python-packages.tsv" ]]; then
    cp "$locBuildRoot/python-packages.tsv" "$locManifestOutput/python-packages.tsv"
fi
if [[ "$locManifestOutput/python-release.txt" != "$locBuildRoot/python-release.txt" ]]; then
    cp "$locBuildRoot/python-release.txt" "$locManifestOutput/python-release.txt"
fi

echo 'Prepared 31 Maven artifacts and 9 Python wheels with their sdists.'
echo "Java manifest: $locManifestOutput/packages.tsv"
echo "Python manifest: $locManifestOutput/python-packages.tsv"
if [[ "$locPrepareOnly" == 1 ]]; then
    echo 'Preparation complete. No Cloudsmith upload performed.'
    exit 0
fi

command -v algites-cloudsmith >/dev/null || { echo 'algites-cloudsmith is required for upload' >&2; exit 2; }
# Resolve the credential once for the whole upload batch.
algites-cloudsmith --session -- bash -c '
    set -euo pipefail
    locJavaRepo="$1"; locJavaManifest="$2"; locPythonRepo="$3"; locPythonManifest="$4"
    while IFS=$'"'"'\t'"'"' read -r locJar locPom locModule locSources; do
        [[ -s "$locJar" && -s "$locPom" && -s "$locModule" && -s "$locSources" ]] || {
            echo "Maven artifact is missing: $locJar" >&2; exit 1;
        }
        cloudsmith push maven "$locJavaRepo" "$locJar" \
            --pom-file "$locPom" --extra-files "$locModule" --sources-file "$locSources"
    done < "$locJavaManifest"
    while IFS=$'"'"'\t'"'"' read -r locWheel locSdist; do
        [[ -s "$locWheel" && -s "$locSdist" ]] || {
            echo "Python artifact is missing: $locWheel" >&2; exit 1;
        }
        cloudsmith push python "$locPythonRepo" "$locWheel"
        cloudsmith push python "$locPythonRepo" "$locSdist"
    done < "$locPythonManifest"
' _ "$locJavaRepo" "$locBuildRoot/packages.tsv" "$locPythonRepo" "$locBuildRoot/python-packages.tsv"

echo 'Cloudsmith upload finished.'

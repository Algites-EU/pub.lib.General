#!/usr/bin/env bash
set -euo pipefail

# Standalone pub.lib.General Maven + Python bootstrap. No Modustro Builder.
# All generated build descriptions, packages and logs live under build/run.
locRepo="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd -P)"
locHelper="$locRepo/devtools/bootstrap/prepare.py"
locOutput="$locRepo/build/run/general-bootstrap"
locJavaRepo="${MODUSTRO_GENERAL_BOOTSTRAP_JAVA_REPO:-algites/java-snapshots-pub}"
locPythonRepo="${MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_REPO:-algites/python-snapshots-pub}"
locPython="${ALGITES_PYTHON_EXECUTABLE:-python3}"
locMode="${1:---prepare-only}"

case "$locMode" in
    --plan-only|--prepare-only|--upload) ;;
    *) echo "Usage: bash devtools/bootstrap.sh [--plan-only|--prepare-only|--upload]" >&2; exit 2 ;;
esac
if [[ $# -gt 1 ]]; then echo 'Only one bootstrap mode is permitted' >&2; exit 2; fi
if [[ "${MODUSTRO_BOOTSTRAP_PREPARE_ONLY:-0}" == 1 && "$locMode" == --upload ]]; then
    echo 'MODUSTRO_BOOTSTRAP_PREPARE_ONLY=1 forbids uploading' >&2
    exit 2
fi
if ! command -v "$locPython" >/dev/null 2>&1; then
    echo "Missing Python interpreter: $locPython" >&2; exit 2
fi
if ! "$locPython" -c 'import yaml' >/dev/null 2>&1; then
    echo 'PyYAML is required to interpret modustro-artifact.yml (python -m pip install pyyaml)' >&2; exit 2
fi
locDetectedVersion="$($locPython - "$locRepo/modustro-source-repository.yml" <<'PY_VERSION'
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

# Avoid ever mixing artifacts from different source snapshots or release versions.
mkdir -p "$locRepo/build/run"
if [[ -e "$locOutput" && ! -d "$locOutput" ]]; then
    echo "Build output path is not a directory: $locOutput" >&2; exit 2
fi
rm -rf -- "$locOutput"
mkdir -p "$locOutput"
locArgs=(--repository "$locRepo" --output "$locOutput" --version "$locVersion")
if [[ -n "${MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_VERSION:-}" ]]; then
    locArgs+=(--python-version "$MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_VERSION")
fi
export PYTHONDONTWRITEBYTECODE=1
"$locPython" "$locHelper" plan "${locArgs[@]}"
if [[ "$locMode" == --plan-only ]]; then
    echo "Bootstrap plan generated in $locOutput; no build and no upload performed."
    exit 0
fi

locGradle=(bash "$locRepo/gradlew")
if [[ -n "${MODUSTRO_GRADLE_EXECUTABLE:-}" ]]; then
    locGradle=("${MODUSTRO_GRADLE_EXECUTABLE}")
fi
locGradleArgs=(--no-daemon --project-dir "$locOutput/gradle")
if [[ "${MODUSTRO_BOOTSTRAP_REFRESH_DEPENDENCIES:-0}" == 1 ]]; then
    locGradleArgs+=(--refresh-dependencies)
fi
# The build only publishes to a local *staging* Maven repository; never to
# Maven Local or Cloudsmith as a side effect of a Gradle task.
MODUSTRO_GENERAL_BOOTSTRAP_OUTPUT="$locOutput" MODUSTRO_GENERAL_BOOTSTRAP_VERSION="$locVersion" \
    "${locGradle[@]}" "${locGradleArgs[@]}" publishAllPublicationsToBootstrapRepository

"$locPython" "$locHelper" verify-maven "${locArgs[@]}"
"$locPython" "$locHelper" package-python "${locArgs[@]}" --python-executable "$locPython"
[[ "$(wc -l < "$locOutput/packages.tsv")" == 31 ]] || { echo 'Unexpected Maven manifest size' >&2; exit 1; }
[[ "$(wc -l < "$locOutput/python-packages.tsv")" == 9 ]] || { echo 'Unexpected Python manifest size' >&2; exit 1; }

echo "Prepared 31 Maven artifacts and 9 Python wheels with their sdists."
echo "Java manifest: $locOutput/packages.tsv"
echo "Python manifest: $locOutput/python-packages.tsv"
if [[ "$locMode" == --prepare-only || "${MODUSTRO_BOOTSTRAP_PREPARE_ONLY:-0}" == 1 ]]; then
    echo 'Preparation complete. No Cloudsmith upload performed.'
    exit 0
fi

command -v algites-cloudsmith >/dev/null || { echo 'algites-cloudsmith is required for upload' >&2; exit 2; }
# Resolve secret only once. The whole publication shares one authenticated
# session (never run algites-cloudsmith once per artifact).
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
' _ "$locJavaRepo" "$locOutput/packages.tsv" "$locPythonRepo" "$locOutput/python-packages.tsv"

echo 'Cloudsmith upload finished.'

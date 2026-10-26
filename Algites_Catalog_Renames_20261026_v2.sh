#!/usr/bin/env bash
set -euo pipefail

# Preserve Git rename history for manually implemented naming catalogs.
# Invoke separately for the pub.lib.General checkout, before or after unpacking
# the updated files. The source paths may already have disappeared after overlay.
#
# Usage: bash Algites_git_mv_catalog_classes_20261026.sh PATH_TO_pub.lib.General --dry-run
#        bash Algites_git_mv_catalog_classes_20261026.sh PATH_TO_pub.lib.General --apply

repo="${1:-.}"
mode="${2:---dry-run}"
case "$mode" in --dry-run|--apply) ;; *) echo 'Expected --dry-run or --apply' >&2; exit 2;; esac
repo="$(cd "$repo" && pwd -P)"
root="$(git -C "$repo" rev-parse --show-toplevel 2>/dev/null)" || {
    echo "Not a Git working tree: $repo" >&2; exit 2;
}
[[ "$root" == "$repo" ]] || { echo "Supply Git repository root: $root" >&2; exit 2; }

move_file() {
    local old="$1" new="$2" buffer=''
    if ! git -C "$root" ls-files --error-unmatch -- "$old" >/dev/null 2>&1; then
        if git -C "$root" ls-files --error-unmatch -- "$new" >/dev/null 2>&1; then
            printf 'Already moved in Git: %s\n' "$new"
        else
            printf 'Not tracked yet (stage new file using git add): %s\n' "$new"
        fi
        return 0
    fi
    printf 'git mv %q %q\n' "$old" "$new"
    [[ "$mode" == --apply ]] || return 0

    if [[ -f "$root/$new" ]]; then
        buffer="$(mktemp)"
        cp -p -- "$root/$new" "$buffer"
        rm -- "$root/$new"
    fi
    mkdir -p -- "$(dirname "$root/$old")" "$(dirname "$root/$new")"
    if [[ ! -f "$root/$old" ]]; then
        git -C "$root" show ":$old" > "$root/$old"
    fi
    if ! git -C "$root" mv -- "$old" "$new"; then
        if [[ -n "$buffer" ]]; then cp -p -- "$buffer" "$root/$new"; rm -f -- "$buffer"; fi
        return 1
    fi
    if [[ -n "$buffer" ]]; then
        cp -p -- "$buffer" "$root/$new"
        rm -f -- "$buffer"
    fi
    git -C "$root" add -- "$new"
}

for kind in algites impl; do
    location="naming/convention/$kind/src/product"
    if [[ "$kind" == algites ]]; then pkg='algites'; else pkg='standard'; fi
    for lang in java python; do
        location_root="$location/$lang/eu/algites/lib/naming/convention/impl/$pkg"
        if [[ "$lang" == java ]]; then
            move_file "$location_root/AIcNameConventionCatalog_1.java" "$location_root/AIcNameConventionCatalog.java"
        else
            move_file "$location_root/aic_name_convention_catalog_1.py" "$location_root/aic_name_convention_catalog.py"
        fi
    done
done
for lang in java python; do
    location="naming/convention/algites/src/product/$lang/eu/algites/lib/naming/convention/impl/algites"
    if [[ "$lang" == java ]]; then
        move_file "$location/AIcAlgitesNamingProfiles_1.java" "$location/AIcAlgitesNamingProfiles.java"
    else
        move_file "$location/aic_algites_naming_profiles_1.py" "$location/aic_algites_naming_profiles.py"
    fi
done
move_file "naming/convention/algites/src/develop/java/eu/algites/lib/naming/convention/impl/algites/AItcNameConventionCatalog_1Test.java" \
          "naming/convention/algites/src/develop/java/eu/algites/lib/naming/convention/impl/algites/AItcNameConventionCatalogTest.java"
move_file "naming/convention/algites/src/develop/python/test_name_convention_catalog_1.py" \
          "naming/convention/algites/src/develop/python/test_name_convention_catalog.py"
if [[ "$mode" == --apply ]]; then
    echo 'Renames staged. Stage remaining updates in pub.lib.General and pub.tool.General with git add.'
    git -C "$root" status --short -- naming/convention
fi

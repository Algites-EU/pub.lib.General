# pub.lib.General 1.1-SNAPSHOT artifact migration

## Tracked move procedure

The full ZIP contains all new artifact descriptors and sources. To let Git
retain renames when migrating a checkout that still has the old 1.0 layout,
extract `devtools/migrate-1.1-artifact-layout-git-mv.sh` and run it **before**
overwriting the checkout with the new ZIP. The script requires a clean
working tree. After the move, overlay the contents of the full ZIP and stage
everything with `git add -A`. The script only handles Git moves, not the new
files or descriptor updates.

```bash
bash devtools/migrate-1.1-artifact-layout-git-mv.sh
# Extract/overlay the complete 1.1 ZIP into this checkout.
git add -A
git status --short
```

## Moved and split artifacts

| Before | After |
|---|---|
| `naming/convention/coreintf` | `naming/convention/intf` |
| `naming/convention/coreimpl` | `naming/convention/impl` |
| `naming/conversion/coreintf` | `naming/conversion/intf` |
| `naming/conversion/coreimpl` | `naming/conversion/impl` |
| `naming/validation/coreintf` | `naming/validation/intf` |
| `naming/validation/coreimpl` | `naming/validation/impl` |
| `version/core` | `version/commonintf + version/commonimpl` |
| `documentation (single artifact)` | `documentation/intf + documentation/impl` |

All Java/Python packages and pre-existing type names are unchanged.
The old artifact coordinates disappear; update dependent repositories
to the new coordinates explicitly. `common` and the version `scheme/*`
artifact paths are unchanged.

## Publication/bootstrap ordering

1. Build and publish the complete `pub.lib.General` 1.1-SNAPSHOT set.
2. Update `pub.tool.General` generation and naming dependencies to 1.1.
3. Rebuild and publish codegen tooling, then migrate consumers.
4. Avoid mixing old artifact coordinates with the new version.

Changing a version does not in itself resolve bootstrap dependency cycles;
dependencies must follow the actual new artifact graph.

## Intentional version module limitation

The version `commonintf` artifact contains some `AIc*`, `AInBuiltin*`
and `AIs*` types because their current Java APIs form a dependency cycle
if naïvely split by filename prefix alone. The partition is acyclic and
does not alter public signatures. A future redesign of the version API
could reduce the foundational implementation footprint.

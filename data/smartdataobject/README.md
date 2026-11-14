# SmartDataObject runtime and format implementations

The SmartDataObject implementation is being separated into independent artifacts:

- [`intf`](intf) — common public SmartDataObject contracts, with no format-specific SDO implementation.
- [`impl/common`](impl/common) — the **new** SDO base class, independent metadata and binding registries, implementation/field annotations, factory and format-binding service SPI.
- [`impl/format/json`](impl/format/json), [`xml`](impl/format/xml), [`yaml`](impl/format/yaml) — independent serialization modules and registered format providers.
- [`impl`](impl) — the **legacy** runtime retained separately during codegen migration. See its [README](impl/README.md). This module is not a dependency of the new common runtime.

New format implementations are discovered through Java `ServiceLoader` files in their artifacts' `META-INF/services` directories. Python implementations use package entry points.

The original SDO code generators and existing generated sources still reference the legacy runtime. Do not remove the legacy module or overwrite `src/product/*.gen` by hand. Switch those dependencies only when generator output is migrated and verified.

When applying the archive to an existing Git checkout, run `devops/migrations/git-mv-sdo-modules.sh` **before extracting the new tree**. The old root SDO README was relocated to the deprecated `impl/README.md`; the new root README documents the modularized layout. Format module Maven dependencies use the new `.impl.common` coordinates, while existing generated source dependencies on `.impl` are kept until the corresponding generators have been migrated. See `impl/common/SDO-BINDING-REFERENCE.md`.

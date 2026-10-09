# Algites General Libraries

Reusable general-purpose libraries shared across the Algites ecosystem.

> Public Algites project.

---

## 📦 Overview

This repository contains reusable public libraries that provide common functionality used by multiple Algites artifacts and repositories.

It currently contains:
- general-purpose utility code,
- reusable naming convention, conversion, and validation infrastructure,
- documentation model utilities,
- a technology-neutral version model,
- concrete version-scheme implementations,
- explicit conversions between version schemes.

The repository is intended for shared functionality that does not belong to a more specialized Algites library or framework repository.

---

## 🧱 Modules & Structure

The repository is organized as a set of independently buildable Algites artifacts:

```text
.
├── README.md
├── common/
│   ├── README.md
│   └── src/
├── naming/
│   ├── convention/{coreintf,coreimpl}/
│   ├── conversion/{coreintf,coreimpl}/
│   └── validation/{coreintf,coreimpl}/
├── documentation/
│   ├── README.md
│   └── src/
└── version/
    ├── core/
    │   ├── README.md
    │   └── src/
    └── scheme/
        ├── algites/
        │   └── v1/
        │       ├── README.md
        │       └── src/
        ├── maven/
        │   ├── README.md
        │   └── src/
        ├── gradle/
        │   ├── README.md
        │   └── src/
        ├── pep440/
        │   ├── README.md
        │   └── src/
        └── conversion/
            ├── common/
            │   ├── README.md
            │   └── src/
            ├── maven2gradle/
            │   ├── README.md
            │   └── src/
            ├── algites2maven/
            │   └── v1/
            │       ├── README.md
            │       └── src/
            ├── algites2gradle/
            │   └── v1/
            │       ├── README.md
            │       └── src/
            └── algites2pep440/
                └── v1/
                    ├── README.md
                    └── src/
```

The main modules are:

- `common` — general-purpose support utilities shared across Algites artifacts and repositories.
- `naming/convention` — naming conventions, naming policies, and versioned-name policy contracts/defaults.
- `naming/conversion` — deterministic token-based conversion between naming conventions.
- `naming/validation` — configurable validation against naming and version policies, including the strict Algites profile.
- `documentation/intf` and `documentation/impl` — renderer-neutral documentation contracts and implementations.
- `version/commonintf` and `version/commonimpl` — version contracts and shared implementations (existing Java packages retained).
- `version/scheme/algites/v1` — version 1 of the Algites version scheme.
- `version/scheme/maven` — Maven version and version-range semantics.
- `version/scheme/gradle` — Gradle dependency-version and rich-version-constraint semantics.
- `version/scheme/pep440` — PEP 440 version and specifier semantics.
- `version/scheme/conversion/common` — common contracts and diagnostics for version-scheme conversions.
- `version/scheme/conversion/maven2gradle` — Maven-to-Gradle version requirement conversion.
- `version/scheme/conversion/algites2maven/v1` — Algites v1 to Maven conversion.
- `version/scheme/conversion/algites2gradle/v1` — Algites v1 to Gradle conversion.
- `version/scheme/conversion/algites2pep440/v1` — Algites v1 to PEP 440 conversion.

The version-scheme modules are intentionally independent. Maven, Gradle, and PEP 440 implementations do not depend on Algites version semantics. Cross-scheme knowledge is isolated in explicit conversion artifacts.

Algites version semantics are explicitly versioned below `version/scheme/algites`. This allows a future incompatible scheme, for example `v2`, to coexist with `v1` without changing the meaning of existing builds or published metadata.

---

## 🚀 Build

### Gradle

The repository uses the shared Algites Gradle build infrastructure:

```bash
./gradlew clean modustroBuild
```

For a conventional Gradle lifecycle build, the following is also available where appropriate:

```bash
./gradlew build
```

### Maven

Published Java artifacts are Maven-compatible and can be consumed from Maven builds. The repository itself is built through the Algites Gradle build infrastructure rather than by Maven.

---

## 🔄 Continuous Integration (Algites CI)

This repository uses the **Algites unified GitHub Actions CI pipeline**; build, test, documentation, and publication rules are centralized in the Algites governance infrastructure.

For exact usage and naming of the branches to utilize fully the defined possibilities, see:

https://github.com/Algites-EU/pub.gov.Algites.specs/blob/main/ci/Algites-Github-CI-Policy.md

---

## 📥 Usage

Each module is published as an independent Algites artifact. Consumers should depend only on the modules they actually require.

Most legacy/common artifacts inherit the repository default GroupId `eu.algites.lib.common`. Functional artifact families may override that default. The naming family uses:

```text
eu.algites.lib.naming
```

Example Maven dependency for the higher-level version implementation artifact:

```xml
<dependency>
  <groupId>eu.algites.lib.common</groupId>
  <artifactId>pub.lib.General_version.commonimpl</artifactId>
  <version>...</version>
</dependency>
```

For version handling, use:

- `version/commonintf` for the generic version model and scheme SPI, or `version/commonimpl` when its extra helpers and parsers are needed;
- `version/scheme/maven`, `version/scheme/gradle`, or `version/scheme/pep440` for a concrete external version scheme;
- `version/scheme/algites/v1` for Algites v1 version semantics;
- a module below `version/scheme/conversion` when explicit conversion between schemes is required.

Conversion is modeled as a first-class operation rather than being hidden in build scripts. Conversion results can therefore distinguish exact, normalized, lossy, and unsupported mappings and expose diagnostics to callers.

See the `README.md` in each module for its responsibilities and detailed usage.

---

## 🛠 Development

Typical workflow:

```bash
git clone https://github.com/Algites-EU/pub.lib.General.git
cd pub.lib.General
./gradlew clean modustroBuild
```

Individual artifacts follow the standard Algites source layout below `src/product/<source-kind>` and `src/develop/<source-kind>`, including the standard `.gen` and `.extgen` generated-source suffix semantics where applicable.

Python artifacts use PEP 420 namespace packages where namespaces are shared; no `__init__.py` is required in the new naming packages. Each main public `AI*` Python type lives in its own deterministic snake_case module, matching the Java one-public-type-per-file organization.

Algites data-object naming is semantic rather than implementation-specific: handwritten Java `record` types and Python `@dataclass` types use `AIcd...`; generated data objects use `AIcgd...`. The `d` marker denotes a data object, while `g` additionally denotes generated source. Public/protected source API is documented with Javadoc or Python docstrings; undocumented source API is not considered acceptable Algites source.

When extending the version subsystem:

- keep low-level version APIs and their necessary primitives in `version/commonintf`, and other generic implementations in `version/commonimpl`;
- keep scheme-specific behavior in the corresponding module below `version/scheme`;
- keep knowledge of two schemes in an explicit module below `version/scheme/conversion` rather than introducing cross-scheme dependencies into the scheme modules themselves;
- add tests and update the module-local `README.md` together with behavioral changes.

---

## 🤝 Contributing

Contributions are welcome.

Please:
- open an issue to discuss changes,
- follow the Algites coding and naming standards,
- preserve module boundaries and avoid unnecessary cross-module dependencies,
- ensure CI passes before submitting a PR.

---

## 📜 License

This project is licensed under the terms of the license specified in the `LICENSE` file. Materialized third-party and component license texts are available under `LICENSES/` where applicable.

---

## 🌍 About Algites

Algites develops platforms, tools, and applications based on strong governance, modeling, and automation principles.

See:
- https://github.com/Algites-EU/pub.gov.Algites

---

**© Algites**


## Modustro repository initialization

Repository and artifact metadata use `modustro-source-repository.yml`,
`modustro-artifact-set.yml` and `modustro-artifact.yml`. Root Settings load the
compiled `modustro.builder.gradleinit` artifact once; project inclusion,
resource endpoints and metadata are resolved by that plugin. Artifact source
sets, dependencies and output production are configured by the shared Modustro
conventions. Source generation is inferred from `src/product/yamldefs`,
`src/product/jsondefs` and `src/product/xmldefs`, without per-file generation
sections in descriptors. Test-only canonical fixtures belong below `src/develop`.

## New general-purpose text and structured data libraries

- [`text/format`](text/format/README.md): reusable text-format descriptors, reference policies, static profile catalog, and formatting helpers.
- [`data/smartdataobject`](data/smartdataobject/README.md): general mutable SmartDataObject runtime, read-only and mutable contracts, raw/effective defaults, presence, validation and identity-aware diagnostic/transport graph rendering.

These modules use `intf` and `impl` artifact names because they expose a public contract directly; there is no extra `core`/`facade` boundary. Internals of `data/smartdataobject/impl` are intentionally non-public so the graph engine may later move into a separate general object-graph artifact. Code generation for concrete `AIig` / `AIigd` / `AIcgd` classes belongs to `pub.tool.General`, not to this repository.

## General DataObject and SmartDataObject (1.1-SNAPSHOT)

`data/dataobject/intf` owns pure data-object markers (`AIiDataObject`, `AIiInputDataObject`, `AIiOutputDataObject`) and normalized contract annotations (`AIaDataObject`, `AIaDataObjectField`). These interfaces and annotations are independent of SmartDataObject and any source schema language.

`data/smartdataobject/intf` depends on the general marker; its `impl` contains the mutable SmartDataObject runtime and private graph snapshot/rendering helpers. The `text/format` modules remain general-purpose and separate.

The established naming profile record has additional **copy-returning** methods for deriving input/output rule overrides without changing existing constructors, fields or accessors. Implementations and new modules in this repository belong to the `1.1-SNAPSHOT` release line. Consumers must explicitly upgrade rather than silently reuse old `1.0-SNAPSHOT` publications.

## Artifact layout migration (1.1-SNAPSHOT)

The naming artifacts have moved from `coreintf`/`coreimpl` to `intf`/`impl`.
`documentation` has been split into `documentation/intf` (read/write-access
interfaces) and `documentation/impl` (concrete model classes). `version/core`
is replaced by `version/commonintf` and `version/commonimpl`. No Java/Python
package names or pre-existing public class names change as part of this move.

For migration details, full old/new artifact coordinates and the one-shot
`git mv` procedure, see [the 1.1 migration guide](devtools/README-migration-1.1.md).

The naming profile API gains Java `withInputConvention` / `withOutputRule`
and analogous Python `with_input_convention` / `with_output_rule` methods; they
produce derived profiles rather than modifying existing instances.

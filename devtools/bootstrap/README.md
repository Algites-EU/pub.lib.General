# Local native-product bootstrap for pub.lib.General

This bootstrap builds and optionally uploads **all native Java Maven and Python products** from the checked-out source tree without applying the normal Modustro Builder/Gradle settings plugin or running GitHub Actions. It does not regenerate Java/Python Defs/SDO sources; `src/product/java.gen` and `src/product/python.gen` are consumed **read-only**. If those generated sources are stale, regenerate them using the normal Defs/SDO Codegen process first.

## Commands

Run from the `pub.lib.General` repository root:

```bash
bash devtools/bootstrap.sh --plan-only
bash devtools/bootstrap.sh --prepare-only
bash devtools/bootstrap.sh --upload
```

The default without a switch is `--prepare-only`. The legacy safeguard `MODUSTRO_BOOTSTRAP_PREPARE_ONLY=1` prevents upload even when `--upload` is requested.

`--plan-only` requires only Python 3.11+ and PyYAML; it validates the module dependency graph and creates a disposable standalone Gradle project without downloading Gradle or other dependencies. `--prepare-only` additionally requires a JDK 17+ (compilation targets Java 17), Gradle 9.2.1 via the repository wrapper, Python `setuptools` and `wheel`, plus access to repositories needed by declared native dependencies. It compiles/packages all 31 Java Maven artifacts and produces 9 Python wheels with 9 source distributions. **No Cloudsmith credentials are required for preparation.**

`--upload` first prepares and verifies every package, then uploads them to Cloudsmith inside **one** `algites-cloudsmith --session -- bash -c ...` invocation. This resolves the authentication key once rather than querying Secret Service for each file. The upload is a sequence of API operations and is not transactional; a network or authentication error midway may require an operator to retry after inspecting what was published.

## Output

All temporary Gradle descriptions and native outputs are under:

```text
build/run/general-bootstrap/
  gradle/                  # standalone Gradle project; no source build scripts modified
  gradle-build/            # compilation and Maven build outputs
  maven-repository/        # staging Maven artifacts
  packages.tsv             # validated Maven JAR, POM, module, sources manifests
  python-projects/         # isolated Python build inputs
  python-dist/             # wheel + sdist files
  python-packages.tsv      # validated Python distribution manifest
  python-release.txt       # single immutable PEP 440 version for this invocation
```

Each run recreates the staging output directory to prevent mixing old and new snapshot versions. The Gradle bootstrap uses module project dependencies from every `modustro-artifact.yml`; Maven group IDs inherit from their nearest `modustro-artifact-set.yml`. External production dependencies use declared versions (or an explicit compatible dependency version when only a range is present). Packaging excludes develop/test dependencies.

Default version is derived from `modustro-source-repository.yml`. For special use it can be overridden, but a release should normally use the repository-defined version:

```bash
MODUSTRO_GENERAL_BOOTSTRAP_VERSION=1.1-SNAPSHOT \
MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_VERSION=1.1.dev202610240001 \
bash devtools/bootstrap.sh --prepare-only
```

Overrides: `ALGITES_PYTHON_EXECUTABLE`, `MODUSTRO_GRADLE_EXECUTABLE`, `MODUSTRO_BOOTSTRAP_REFRESH_DEPENDENCIES=1`, `MODUSTRO_GENERAL_BOOTSTRAP_JAVA_REPO` (default `algites/java-snapshots-pub`) and `MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_REPO` (default `algites/python-snapshots-pub`). Maven and Python coordinates are verified before upload.

## Limitations

- This is a **manual recovery/publication bootstrap**, not a replacement for the full Modustro publication pipeline. It publishes native products, not the docs/schema site or ancillary CI variants.
- Dependencies defined for production are used for Maven metadata and Python requirements; it does not replicate every optimization from the Builder (e.g. native develop test variants).
- It requires the generated `*.gen` trees, if present, to be up to date. Never edit these output trees by hand.
- It runs no Java/Python test suite by default; it verifies module graph, Java compilation through packaging, Maven coordinates/archives, and wheel/sdist identities. Run the repository's usual test suite separately before distributing critical releases.
- Publishing overwrites/re-uploads mutable Maven snapshots but uses a fresh Python PEP 440 `.dev` version per invocation to avoid wheel filename collisions.

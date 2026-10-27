# Local native-product bootstrap for pub.lib.General

This bootstrap builds and optionally uploads **all native Java Maven and Python products** from the checked-out source tree without applying the normal Modustro Builder/Gradle settings plugin or running GitHub Actions. It does not regenerate Java/Python Defs/SDO sources; `src/product/java.gen` and `src/product/python.gen` are consumed **read-only**. If those generated sources are stale, regenerate them using the normal Defs/SDO Codegen process first.

## Single-invocation workflow (same as the other Modustro bootstraps)

From the `pub.lib.General` repository root, **one invocation** normally
prepares, verifies and uploads all Maven and Python distributions:

```bash
bash devtools/bootstrap.sh
```

To compile and stage without any Cloudsmith upload, use the same shared
environment variable as the other standalone bootstraps:

```bash
MODUSTRO_BOOTSTRAP_PREPARE_ONLY=1 bash devtools/bootstrap.sh
```

Planning only (no Gradle build or upload) is an optional diagnostic action:

```bash
bash devtools/bootstrap.sh --plan-only
```

The optional `--prepare-only` and `--upload` switches are retained as
convenience aliases. **They are alternatives, not three steps that must be run
in sequence.** `--upload` is the default. The environment safeguard
`MODUSTRO_BOOTSTRAP_PREPARE_ONLY=1` always wins, including over `--upload`.

This matches the default-upload behavior of
`pub.tool.General/generators/code/bootstrap.sh` and the standalone
`pub.gov.Algites` bootstraps. Be aware that running without the safeguard
will publish to Cloudsmith if the build succeeds and authentication is present.

### Shared bootstrap environment variables

| Variable | Meaning |
| --- | --- |
| `MODUSTRO_BOOTSTRAP_PREPARE_ONLY=1` | Build and verify, but do not upload |
| `MODUSTRO_BOOTSTRAP_DEPENDENCY_MODE=remote|local|source` | External dependency repository precedence; default `remote` |
| `MODUSTRO_BOOTSTRAP_REFRESH_DEPENDENCIES=0|1` | Refresh resolved dependencies; default `1` for `remote`, `0` otherwise |
| `MODUSTRO_BOOTSTRAP_OUTPUT_DIR` | Optional destination for copies of the preparation manifests |
| `MODUSTRO_GRADLE_EXECUTABLE` | Override repository Gradle wrapper executable |
| `ALGITES_PYTHON_EXECUTABLE` | Override Python interpreter |

`remote` prefers published Cloudsmith artifacts, then Maven Central, with
Maven Local as fallback. `local` and `source` prefer Maven Local. Internal
`pub.lib.General` dependencies are *always* built from the current source
checkout as Gradle project dependencies; this bootstrap therefore does not
require a separate source-preparation phase even in `source` mode.

`--plan-only` requires only Python 3.11+ and PyYAML; it validates the module dependency graph and creates a disposable standalone Gradle project without downloading Gradle or other dependencies. `--prepare-only` additionally requires a JDK 17+ (compilation targets Java 17), Gradle 9.2.1 via the repository wrapper, Python `setuptools` and `wheel`, plus access to repositories needed by declared native dependencies. It compiles/packages every Java and Python product declared by native artifact descriptors; the actual numbers of verified Maven publications, wheels and matching sdists are reported at runtime. **No Cloudsmith credentials are required for preparation.**

A regular invocation (or explicit `--upload`) first prepares and verifies every package, then uploads them to Cloudsmith inside **one** `algites-cloudsmith --session -- bash -c ...` invocation. This resolves the authentication key once rather than querying Secret Service for each file. The upload is a sequence of API operations and is not transactional; a network or authentication error midway may require an operator to retry after inspecting what was published.

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

Each run recreates the staging output directory to prevent mixing old and new snapshot versions. Maven snapshot publications can have uniquely timestamped filenames within a `1.1-SNAPSHOT/` directory. The verification step now resolves their filenames through `maven-metadata.xml` (and also accepts literal `-SNAPSHOT` filenames), then records the actual staged paths in `packages.tsv` for Cloudsmith upload. The Gradle bootstrap uses module project dependencies from every `modustro-artifact.yml`; Maven group IDs inherit from their nearest `modustro-artifact-set.yml`. External production dependencies use declared versions (or an explicit compatible dependency version when only a range is present). Packaging excludes develop/test dependencies.

Default version is derived from `modustro-source-repository.yml`. For special use it can be overridden, but a release should normally use the repository-defined version:

```bash
MODUSTRO_GENERAL_BOOTSTRAP_VERSION=1.1-SNAPSHOT \
MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_VERSION=1.1.dev202610240001 \
MODUSTRO_BOOTSTRAP_PREPARE_ONLY=1 bash devtools/bootstrap.sh
```

Repository overrides: `MODUSTRO_GENERAL_BOOTSTRAP_CLOUDSMITH_REPO` (Java/Maven; default `algites/java-snapshots-pub`) and `MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_CLOUDSMITH_REPO` (Python; default `algites/python-snapshots-pub`). Older aliases `MODUSTRO_GENERAL_BOOTSTRAP_JAVA_REPO` and `MODUSTRO_GENERAL_BOOTSTRAP_PYTHON_REPO` remain supported. The new names take precedence when both forms are set. Maven and Python coordinates are verified before upload.

## Limitations

- This is a **manual recovery/publication bootstrap**, not a replacement for the full Modustro publication pipeline. It publishes native products, not the docs/schema site or ancillary CI variants.
- Dependencies defined for production are used for Maven metadata and Python requirements; it does not replicate every optimization from the Builder (e.g. native develop test variants).
- It requires the generated `*.gen` trees, if present, to be up to date. Never edit these output trees by hand.
- It runs no Java/Python test suite by default; it verifies module graph, Java compilation through packaging, Maven coordinates/archives, and wheel/sdist identities. Run the repository's usual test suite separately before distributing critical releases.
- Publishing overwrites/re-uploads mutable Maven snapshots but uses a fresh Python PEP 440 `.dev` version per invocation to avoid wheel filename collisions.

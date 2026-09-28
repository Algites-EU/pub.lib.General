# Algites Version Scheme v1

Defines version 1 of the Algites ecosystem version model.

The v1 model consists of a numeric `ReleaseLineVersion`, revision, qualifier kind, and optional qualifier sequence. It is intentionally independent from Maven, Gradle, and PEP 440. Mapping into those schemes is implemented only in explicit conversion artifacts.

Explicitly versioning the scheme allows a future incompatible `v2` to coexist with `v1` while preserving the meaning of previously published artifacts and build metadata.


`ReleaseLineVersion` contains only numeric components separated by dots (for example `1` or `1.3`). Variant/classifier information is intentionally not part of the version scheme.

# Shared version APIs and implementations

- `intf`: public contracts and foundational primitives required by the existing API.
- `impl`: higher-level concrete version helpers, parsers and normalization; depends on `intf`.

Both artifacts retain the existing Java package `eu.algites.lib.common.version`.
The directory structure determines the artifact identifiers `pub.lib.General_version.common.intf`
and `pub.lib.General_version.common.impl`.

# PEP 440 Version Scheme

Defines PEP 440 version semantics and the native `AIrPep440VersionRequirement` representation used for Python package versions and dependency specifiers.

`AIcPep440VersionRequirementRenderer` converts the portable `AIiVersionRequirement` into a sparse map keyed by `AInPythonBuildPhase`:

- `PREFERRED` exists only when a preferred exact version is declared.
- `NON_STRICT_MAXIMUMS` exists only when a maximum is declared with `MaximumStrict=false`; it retains that maximum.
- `STRICT_MAXIMUMS` is always returned and omits non-strict maximums while retaining strict maximums, minimums, and exclusions.

This allows build orchestration to perform at most three native Python resolution attempts without implementing a custom package resolver. The module contains no Algites-specific build orchestration.

# Gradle Version Scheme

Defines Gradle dependency-version semantics and `AIrGradleVersionConstraint`, including the rich-constraint fields `Require`, `Strictly`, `Prefer`, and `Reject` at the Java API level.

`AIcGradleVersionRequirementRenderer` first normalizes the portable `AIiVersionRequirement` against the Gradle version scheme and then renders one Gradle rich constraint. An exact version is rendered through `strictly`; a strict maximum is rendered through `strictly`, a non-strict maximum through `require`, the portable preferred version through `prefer`, and exclusions through `reject`. Effective requirements may therefore contain inherited soft preferences together with an exact version without changing exact-version semantics.

This module depends only on `version/common/intf` and intentionally knows nothing about Maven or Algites. Conversions from other schemes are separate artifacts.

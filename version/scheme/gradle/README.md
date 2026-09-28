# Gradle Version Scheme

Defines Gradle dependency-version semantics and `AIrGradleVersionConstraint`, including the rich-constraint fields `Require`, `Strictly`, `Prefer`, and `Reject` at the Java API level.

`AIcGradleVersionRequirementRenderer` converts the portable `AIiVersionRequirement` model into one Gradle rich constraint. A strict maximum is rendered through `strictly`, a non-strict maximum through `require`, the portable preferred version through `prefer`, and exclusions through `reject`.

This module depends only on `version/core` and intentionally knows nothing about Maven or Algites. Conversions from other schemes are separate artifacts.

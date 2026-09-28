# Version Core

Generic, technology-neutral version model used by all Algites version-scheme modules.

This artifact contains immutable version values, tokenization, intervals, comparison/format/codec SPIs, and the portable `AIiVersionRequirement` contract. The requirement model supports an exact version or a bounded range, optional strictness of the maximum, zero or more excluded versions, and one preferred version. `MaximumStrict` preserves whether strictness was explicitly specified (`true`, `false`, or unspecified). Its effective value is `false` when no maximum is present; when a maximum is present and strictness is unspecified, the effective value defaults to `true`, while an explicit `false` keeps the maximum non-strict. Compact boundary expressions may use either operator direction, for example `>=1.2.0` or `1.2.0<=` for the same inclusive minimum.

Concrete ecosystem renderers remain in the corresponding scheme artifacts. The core model intentionally does not define Maven, Gradle, PEP 440, or Algites-specific serialization.

## Usage

Depend on this artifact when implementing a new version scheme or when code needs the generic `AIcVersion`, `AIcVersionInterval`, `AIiVersionScheme`, or `AIiVersionRequirement` APIs.

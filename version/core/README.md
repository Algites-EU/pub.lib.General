# Version Core

Generic, technology-neutral version model used by all Algites version-scheme modules.

This artifact contains immutable version values, tokenization, intervals, comparison/format/codec SPIs, and reusable generic implementations. It intentionally does not define the public contract of Maven, Gradle, PEP 440, or Algites version schemes; those are separate artifacts below `version/scheme`.

## Usage

Depend on this artifact when implementing a new version scheme or when code needs only the generic `AIcVersion`, `AIcVersionInterval`, and `AIiVersionScheme` APIs.

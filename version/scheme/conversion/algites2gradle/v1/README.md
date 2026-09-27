# Algites v1 to Gradle Version Conversion

Converts Algites version scheme v1 directly into a Gradle version requirement.

The conversion is intentionally direct rather than routed through Maven so Gradle build infrastructure does not accidentally inherit Maven semantics where they are not required.

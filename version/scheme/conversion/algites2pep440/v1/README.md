# Algites v1 to PEP 440 Version Conversion

Converts Algites version scheme v1 into PEP 440 version text.

Release versions map to the base version, snapshots map to `.devN`, and release candidates map to `rcN`.

The module also converts complete portable `AIiVersionRequirement` instances whose version values use Algites v1 into the sparse Python build-phase map (`PREFERRED`, `NON_STRICT_MAXIMUMS`, `STRICT_MAXIMUMS`).

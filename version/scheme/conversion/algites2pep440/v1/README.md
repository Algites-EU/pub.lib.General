# Algites v1 to PEP 440 Version Conversion

Converts Algites version scheme v1 into PEP 440 version text.

Release versions map to the base version, snapshots map to `.devN`, and release candidates map to `rcN`.

The module also converts complete portable `AIiVersionRequirement` instances whose version values use Algites v1 into PEP 440 requirements and the sparse Python build-phase map (`PREFERRED`, `NON_STRICT_MAXIMUMS`, `STRICT_MAXIMUMS`).

An Algites exact snapshot requirement such as `1.0-SNAPSHOT` denotes the snapshot series for that release/revision, not the single synthetic publication version `1.0.dev0`. It is therefore rendered as `>=1.0.dev0,<1.0a0`, which accepts timestamped snapshot publications such as `1.0.dev20260930025108359` while excluding alpha, beta, release-candidate and final releases.

# Version common interfaces

Public `AIiVersion*` interfaces, supporting enums, records, and the minimum
concrete primitives needed for the existing Java API to remain source/binary
compatible. In particular `AIiVersionCodec` returns `AIcVersion`, and
`AIcVersion` references the built-in scheme and comparator implementations.
Consequently these foundational concrete types cannot be placed in
`common/impl` without changing the current public contract or introducing a
dependency cycle. This artifact deliberately contains those primitives.

Package remains `eu.algites.lib.common.version`.

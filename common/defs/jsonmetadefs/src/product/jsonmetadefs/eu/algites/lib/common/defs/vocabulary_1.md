# Algites Common Defs Identity Vocabulary v1

Vocabulary URI: `https://defs.dev.algites.eu/api/jsonmetadefs/eu/algites/lib/common/defs/vocabulary_1.md`

This language-independent logical identity vocabulary defines two **JSON Schema**
annotation keywords for Algites definition contracts. It is not a general-purpose
schema-metadata namespace, and it does not define XSD lexical representation.

## `x-algites-schema-id`

A non-empty string identifying the logical Algites schema contract, independently
of `$id` (the JSON Schema document resource URI) and the physical file path.
It identifies a contract across serialization kinds when they are intentionally
published as representations of the same logical contract. It is not a generated
Java/Python package name and is not inherited automatically into nested schemas.

## `x-algites-schema-version`

A positive integer representing a release of the logical schema contract.
It is NOT the JSON Schema draft version and is independent of `$schema`.
The version may accompany `x-algites-schema-id` at the root definition.

## Drafts and semantics

For drafts 2019-09 and 2020-12, consuming dialects declare this vocabulary
using `$vocabulary` and reference its draft-specific syntax meta-schema.
Draft-03, Draft-04, Draft-06 and Draft-07 have no `$vocabulary`: their
version-specific dialect metaschemas incorporate the same two keyword
schemas as proprietary annotations. Draft-05 follows Draft-04. These keywords
do not affect standard JSON Schema instance validation. A Defs processor must
interpret identity and version according to the rules above.

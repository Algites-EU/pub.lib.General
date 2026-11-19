# Algites Common Defs JSON Schema dialects

This artifact publishes the reusable Algites Common Defs Identity Vocabulary and
version-specific JSON Schema dialect meta-schemas. The dialects extend the
standard draft meta-schema with two **annotation** keywords:
`x-algites-schema-id` (non-empty string) and `x-algites-schema-version`
(positive integer).

Supported dialects (published under `https://defs.dev.algites.eu/api/jsonmetadefs/eu/algites/lib/common/defs/`):

| JSON Schema draft | Dialect meta-schema path | Mechanism |
| --- | --- | --- |
| Draft-03 | `draft-03/dialect_1.jsonmetadef.schema.json` | Legacy `extends` |
| Draft-04 | `draft-04/dialect_1.jsonmetadef.schema.json` | Legacy `allOf` |
| Draft-06 | `draft-06/dialect_1.jsonmetadef.schema.json` | Legacy `allOf` (preserving boolean schemas) |
| Draft-07 | `draft-07/dialect_1.jsonmetadef.schema.json` | Legacy `allOf` (preserving boolean schemas) |
| 2019-09 | `2019-09/dialect_1.jsonmetadef.schema.json` | `$vocabulary` + `$recursiveAnchor` |
| 2020-12 | `2020-12/dialect_1.jsonmetadef.schema.json` | `$vocabulary` + `$dynamicAnchor` |

Only drafts 2019-09 and 2020-12 support formal `$vocabulary` declarations.
For older drafts the dialect is an extended *meta-schema*, not a standards-level
vocabulary negotiation mechanism. The syntax restriction is still available,
but implementations of older drafts do not automatically understand custom
keywords: consumers must interpret their semantics explicitly.

New JSONDEFS and YAMLDEFS schemas intended to use these keywords should place
the relevant dialect URI in `$schema`. Defs-specific keywords are **not** part
of these common dialects; Defs Codegen can provide its own extended dialect.

All meta-schemas have `.meta.yml` sidecars for publishing. Schema definitions
are versioned, and adding or changing vocabulary semantics requires versioning
rather than silent replacement.

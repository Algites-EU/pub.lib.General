# General text-format profiles

`text/format/intf` supplies the public, technology-neutral contracts:

- `AInTextFormatType`: JSON, YAML, XML.
- `AIiTextFormatProfile`: immutable descriptor of text syntax, reference encoding, diagnostic intent and supported reference policies.
- `AInTextOutputObjectReferenceMode`: `NO_REFERENCES`, `ALL_OBJECTS`, `CYCLIC_REFERENCES_ONLY`, `REPEATED_OBJECTS`, `OPTIMIZED_TEXT_OUTPUT_SIZE`.
- `AIsTextFormatProfiles`: **static**, non-instantiable catalog of predefined immutable profiles.
- `AInTextReferenceEncoding` and `AIcdTextFormatProfile`: protocol encoding descriptor and immutable profile value.

`text/format/impl` supplies generic JSON / XML string escaping helpers. `data/smartdo/impl` currently provides the schema-aware object graph renderer that consumes these profiles. Other applications can reuse the profiles independently; no `text/format` module depends on SmartDataObject, AAC, or Defs Codegen.

Reference policies describe **when** to emit identities and references. The profile determines **how** to represent them. Unsupported combinations throw rather than silently rewriting the wire contract. An object's per-output `dNr` is not a persistent ID; aliases use identity, never equality. Raw/effective values are independent of the reference mode.

`JSON_DOTNET_PRESERVE` uses `$id` / `$ref` / `$values`; unescaped user fields with reserved names must not collide with protocol metadata. Canonical plain JSON and XML XSD profiles deliberately support only `NO_REFERENCES`. Diagnostic profiles can render cycles using transient references. YAML's native profile writes `&anchor` and `*alias`.

# DataObject (pub.lib.General)

`data/dataobject/intf` defines common Java/Python structured data-object contracts.
`AIaDataObjectField` exposes `fieldName`, `presence` and `allowsNull` (default `true`).
Its presentation text is declared through `AIaFieldLabel` in the common library,
not through `description`. Its effective default is supplied by a separate
`AIaFieldDefault.defaultValueResolver` category/provider factory selection.
Effective defaults do not make raw fields present and never replace explicit nulls.

XML metadata is separate, in `eu.algites.lib.data.dataobject.xmldefs` and the
Python `xmldefs` package. `AIaXmlDataObject` represents XML types or global
elements; `AIaXmlDataObjectField` marks XML QNames and ELEMENT/ATTRIBUTE fields.
Its optional `order` (default `-1`) is the zero-based declaration position
within the XML type. Generated XML contracts provide this position explicitly;
the Java SmartDataObject metadata scanner sorts annotated getters by position
within each declaring contract, because reflection order is unspecified. Python
metadata retains the same position and maintains declaration order. Strict XSD
validation of sequence ordering is separate from tolerant QName-based parsing.
The shared DataObject APIs do not carry XML namespace or attribute flags.

XML substitution-group relationships are declared through generated `_xe`
interfaces inheriting both their XML type `_x` interface and their substitution
head `_xe` interface. No OpenChoice annotations, runtime role registry or special
runtime assignment check are used. XML schema legality is decided by Defs Codegen,
not by this core data-object marker library.

## Technology-specific contract markers

The `xmldefs`, `jsondefs`, and `yamldefs` packages define
`AIiXmlDataObject`, `AIiJsonDataObject`, and `AIiYamlDataObject` respectively
in both Java and Python. Generated read-only contracts extend the matching
technology marker, which in turn extends `AIiDataObject`. These markers are
source-definition identities, not serialization, validation, or runtime adapters.
Mutable contracts additionally inherit the matching technology-specific
SmartDataObject marker described in `data/smartdataobject/README.md`.

## Schema identity and instance-document metadata

A source schema has three independent metadata values: the published **schema URL**,
its stable logical **schema ID**, and its **schema version**. The corresponding
Java `AIaDataObject` annotation / Python `AIcdDataObject` descriptor preserve
`schemaUrl` / `schema_url`, `schemaId` / `schema_id`, and
`schemaVersion` / `schema_version` separately. Do not derive the logical ID
from the URL.

Three optional target *property names* control inclusion in JSON/YAML instance
documents: `dataSourceSchemaUrlField`, `dataSourceSchemaIdentifierField`, and
`dataSourceSchemaVersionField` (snake_case in Python). An empty target suppresses
emission of the respective metadata, even if its value is known. For Algites,
only `$schemaUrl` is enabled by default; logical ID and version are retained in
contract annotations but not duplicated in the document. The JSON Schema
keyword `$schema` continues to designate the JSON Schema dialect inside the
**schema definition** and must not be substituted there.

The Defs Codegen source naming profile supplies the exact source metadata key
names (by default `$id`, `$x-algites-schema-id`, and
`$x-algites-schema-version`) and destination property names. Generated types
propagate this mapping into the second-stage SDO annotations. During JSON/YAML
serialization, an explicitly declared property with the same target metadata
name is emitted **once**; a conflicting stored value fails rather than silently
overriding the contract metadata. Missing or disabled metadata is not guessed.

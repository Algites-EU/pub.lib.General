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

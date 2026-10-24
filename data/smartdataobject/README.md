# SmartDataObject runtime

- `data/smartdataobject/intf` defines the SmartDataObject technical API; it extends the *general* `AIiDataObject` marker from `data/dataobject/intf`.
- `data/smartdataobject/impl` provides `AIcSmartDataObject`, validation, raw/presence state and diagnostic or transferable serialization.
- Runtime graph snapshot and renderer classes remain implementation-private.
- The `AIig...` read-only generated contract extends the matching technology-specific `AIi*DataObject` marker (which extends `AIiDataObject`) and carries `AIaDataObject` and `AIaDataObjectField` from `data/dataobject/intf`.
- The default mutable generated `AIigd...` interface extends the read-only contract plus the matching `AIi*SmartDataObject` technology marker (which extends `AIiSmartDataObject`). The default `AIcgd...` implementation extends the appropriate XML/JSON/YAML technology-specific `AIc*SmartDataObject` implementation, itself a subclass of `AIcSmartDataObject`. The generated class prefix is the **naming profile's rule**, not a fixed SmartDataObject requirement.
- In Python, contracts carry `__data_object_fields__` and optional `__data_object__`. An anonymous Java read-only contract object, or equivalent Python object, can serve as a structured default.

Serializer scope: XML/XSD output supports namespace-qualified nodes, XML attributes and repeated `maxOccurs` element siblings using the original QName; empty collections emit no repeated elements. Java and Python both emit XSD lexical boolean values (`true`/`false`) and canonical enum wire values (Java generated enums use `wireValue()`, Python enums use `value`). Collection-style `<Item>` wrappers are restricted to generic/diagnostic collection output, not canonical repeating XML fields. The runtime does **not** yet provide a schema-aware XML decoder accepting elements out of input order. Strict XSD validation, including `xs:sequence` ordering, is a separate operation. Normal JSON schema-compatible output refuses cycles; reference-preserving JSON uses its own encoding. See tests for supported cases.

### SmartDO effective getter policy

The SmartDO-only `@AIaSmartDataObject(allowNonPresentRead = true)` sets a
read-policy default for the annotated read-only contract. The optional
`@AIaSmartDataObjectField(allowNonPresentRead = false/true)` on an annotated
getter overrides the interface-level default, including explicit `false`.
These annotations do not change `AIaDataObjectField.presence`,
validation, explicit presence, raw reads, or default-factory precedence.
An absent required field without a default returns `null` only when the policy
permits it. Primitive Java getters cannot opt in because they cannot return
`null`. Python equivalents are `@smart_data_object(...)` and
`@smart_data_object_field(...)` in the SmartDO package.

## Canonical mapping for generated SmartDataObjects

Python `AIcSmartDataObject.from_mapping(mapping)` creates a new instance from
canonical wire-key fields, checks unknown keys and validates raw required/null
constraints. `to_mapping()` exports only present raw fields, recursively mapping
nested SmartDataObjects, lists and enum values without generating framework-
specific DTO classes. Cyclic inline object graphs are rejected because canonical
mapping has no universal reference encoding; use the configurable general SDO
text format profiles for reference-preserving serialization.

## Typed virtual SDO mappings (Java and Python)

The lossless in-process representation is **not** the legacy plain wire mapping.
The read-only `AIig` contract declares fields; the generated `AIigd` mutable
contract carries a complete schema identity, and its `AIcgd` implementation
stores raw values and explicit presence. Calling `_toMapping(RAW_VALUES)` or
`_toMapping(EFFECTIVE_VALUES)` builds a detached graph of typed field wrappers.
Every field value is an `AIidVirtualFieldValue` exposing a concrete
`getValueClass()` and `getValueRepresentation()`; SDO representations are
`Map<String, AIidVirtualFieldValue>` / Python mappings with matching keys.

The virtual graph reuses the **same in-memory mapping instances** when objects
are shared or cyclic. Identity is determined by `==`, not `equals()`; containers
are also memoized. This is intentionally not plain JSON data. The runtime
factory `AIsSmartDataObjectFactory.fromMapping(Class, Map)` in Java (and
`from_mapping(type, mapping)` in Python) restores the actual concrete type and
shared instance identity. The Java generator emits a map-accepting constructor
on each `AIcgd` that delegates to the base class; the factory centralizes
reflection. Both construction routes use scoped mapping contexts, cleaned up
on success and exception. Never expose partially populated cyclic objects
before the outermost construction has completed.

Raw mappings omit absent fields and preserve explicit nulls. Effective mappings
include calculated defaults where applicable; consequently an effective
mapping is not a substitute for a raw mapping when restoring presence state.
The simpler Python `to_mapping()` and `from_mapping()` remain compatibility
operations at existing AAC wire boundaries; they do **not** have the same
lossless graph semantics as typed virtual mappings.

The existing text-format profiles still render through the common internal
SmartDataObject graph snapshot. They do **not yet** implement a portable
round-trip decoder for the typed virtual representation. Carrying concrete
Java/Python classes in memory does not identify a portable wire type: that
requires the separate `SchemaId`/`SchemaVersion`, `SchemaUrl`, and object
reference identifiers in the eventual JSON/YAML/XML format. No inference
from multiple inherited interfaces can recreate a missing concrete type.

## Generic validation issues and field topics

`AIiSmartDataObject` implements `AIiValidatable<AIiSdoValidationIssue>`.
Its generic validity delegates to the existing deeply-effective validation;
all existing local/deep and raw/effective methods remain available.
`AIiSdoValidationIssue` extends the general issue interface with related-text
roles `AIaFieldLabel.FIELD_NAME`, `AIaFieldLabel.FIELD_TITLE`, and
`AIaFieldLabel.FIELD_DESCRIPTION` (string topic identifiers, not a closed enum). `getFieldName()`, `getFieldTitle(Locale)` and
`getFieldDescription(Locale)` are convenience methods.

A null locale means technical, untranslated field names, source descriptions
and original diagnostic text; it is **not** an instruction to use the process
locale. Unsupported non-null locales fall back to English. Missing related
text topics return `null`. Deep SDO validation retains the owning field's
metadata when reporting nested `AIiValidatable` collection issues, and keeps
technical `path` values independent of localized labels.

## Derived fields and XML substitution

Java and Python SmartDataObject implementations select the nearest declared
field descriptor when a derived contract redeclares an inherited getter.
Presence may only narrow: `ALLOWED` → `REQUIRED` or `FORBIDDEN`; existing
`REQUIRED` and `FORBIDDEN` states may not be widened. For `FORBIDDEN`, raw and
effective reads and raw assignments throw immediately. Raw validation also
reports `FORBIDDEN_PRESENT` if an invalid instance somehow contains a forbidden
value. `from_mapping` / generated map construction must not bypass setters.
XML substitution is represented by generated Java/Python element interface
inheritance; no OpenChoice authorization annotations or runtime registry exist.
A field assignment still enforces its declared value type. Other mutable
collection APIs must obey the same type and presence restrictions.

## Technology-specific SmartDataObject markers

The `xmldefs`, `jsondefs`, and `yamldefs` API packages define
`AIiXmlSmartDataObject`, `AIiJsonSmartDataObject`, and `AIiYamlSmartDataObject`
for Java and Python. Each extends **both** the shared `AIiSmartDataObject`
interface and the matching read-only `AIi*DataObject` marker. This allows a
schema-generated mutable contract to remain assignable to both generic runtime
SmartDataObject code and the appropriate schema technology. The `impl.xmldefs`, `impl.jsondefs`, and `impl.yamldefs` packages now provide
`AIcXmlSmartDataObject`, `AIcJsonSmartDataObject`, and `AIcYamlSmartDataObject`,
each extending the shared runtime base `AIcSmartDataObject` and implementing
the matching technology interface. The XML technology base now also supports
QName-aware canonical XML deserialization. The JSON/YAML technology bases
remain extension points without additional format-specific behavior.

A downstream generator should make its read-only contract extend the matching
`AIi*DataObject`, its mutable contract extend both the read-only contract and
the matching `AIi*SmartDataObject`, and its generated concrete class extend
the appropriate `AIc*SmartDataObject` technology-specific implementation class. The XML type/element inheritance
required for `substitutionGroup` is orthogonal and remains in the read-only
contract graph. Defs/SDO generators in `pub.tool.General` are independently versioned and
need to be synchronized with the technology-specific API in this library.


## Canonical QName-aware XML input

The XML technology bases support input of XML elements and attributes using their
expanded `{namespaceURI}localName` identity, not the input document's prefix or
child ordering. The operation creates **concrete typed** SmartDataObject instances,
including repeated sibling elements and nested objects. Repeating fields preserve
the relative order of their own XML element occurrences.

- **Java:** `AIcXmlSmartDataObject.fromXml(xml, GeneratedClass.class, nestedTypes)`
  accepts a map of generated concrete implementation types indexed by field paths
  such as `Address` or `Address.City`; concrete getter types are inferred when
  possible. A zero-argument constructor is required on instantiated classes.
- **Python:** `GeneratedClass.from_xml(xml, nested_types)` uses optional
  `__xml_field_types__` and `__xml_repeated_fields__` declarations. Repetition
  is also inferred when a field's expected type is `list`. Nested overrides
  can be provided by dot-qualified field paths.

Unknown QNames, ambiguous field definitions, repeated scalar elements, invalid
lexical values, unsupported scalar types and unexpected mixed content fail
explicitly instead of being discarded. DTDs and external entities are disabled.
The input parser checks ordinary SmartDataObject presence and type validation;
it does **not** replace a validating XML Schema processor. Complex particles,
full XSD facets, wildcard validation and substitution-group resolution require
independent support from the schema generator/validator. The structure of those
constructs must not be silently projected onto ordinary fields.

`pub.tool.General` must emit the nested type and repetition metadata when it
generates XML concrete SmartDataObject classes. Without that metadata, nested
interface-typed fields cannot automatically select a concrete class, and a
single occurrence of an untyped repeating field cannot always be distinguished
from an ordinary scalar field.

### Setter parameter inheritance policy (1.1)

`data/dataobject/intf` defines
`AInFieldSetterInheritancePolicy` and the same
`setterInheritancePolicy` member on the Java `AIaDataObject` and
`AIaDataObjectField` annotations. Their Python `AIcdDataObject` and
`AIcdDataObjectField` counterparts expose `setter_inheritance_policy`.
`DEFAULT` delegates to configured Defs generation defaults;
`INHERIT_AS_GENERIC_PARAMETER` preserves a writable type variable across
future subclasses, while `INHERIT_AS_FIXED_PARAMETER` retains the
inherited setter's published parameter type and forbids incompatible
read-getter narrowing in the writable hierarchy. This policy is structural
metadata of a public contract, not a mutable runtime setting.

The schema-neutral SDO runtime still consumes values through the normal raw
field access methods. Generation of generic mutable API signatures, parent
inheritance and concrete implementations belongs to `pub.tool.General`.

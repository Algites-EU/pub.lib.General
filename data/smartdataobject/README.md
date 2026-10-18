# SmartDataObject runtime

- `data/smartdataobject/intf` defines the SmartDataObject technical API; it extends the *general* `AIiDataObject` marker from `data/dataobject/intf`.
- `data/smartdataobject/impl` provides `AIcSmartDataObject`, validation, raw/presence state and diagnostic or transferable serialization.
- Runtime graph snapshot and renderer classes remain implementation-private.
- The `AIig...` read-only generated contract extends `AIiDataObject` and carries `AIaDataObject` and `AIaDataObjectField` from `data/dataobject/intf`.
- The default mutable generated `AIigd...` interface extends the read-only contract plus `AIiSmartDataObject`. The default `AIcgd...` implementation extends `AIcSmartDataObject`. The generated class prefix is the **naming profile's rule**, not a fixed SmartDataObject requirement.
- In Python, contracts carry `__data_object_fields__` and optional `__data_object__`. An anonymous Java read-only contract object, or equivalent Python object, can serve as a structured default.

Serializer scope: XML/XSD output supports simple namespace-qualified nodes but not the entire XSD content model. Normal JSON schema-compatible output refuses cycles; reference-preserving JSON uses its own encoding. See tests for supported cases.

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
stores raw values and explicit presence. Calling `toMapping_RawValues()` or
`toMapping_EffectiveValues()` builds a detached graph of typed field wrappers.
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

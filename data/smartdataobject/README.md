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
These annotations do not change `AIaDataObjectField.presenceRequired`,
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

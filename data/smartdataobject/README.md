# SmartDataObject runtime

- `data/smartdataobject/intf` defines the SmartDataObject technical API; it extends the *general* `AIiDataObject` marker from `data/dataobject/intf`.
- `data/smartdataobject/impl` provides `AIcSmartDataObject`, validation, raw/presence state and diagnostic or transferable serialization.
- Runtime graph snapshot and renderer classes remain implementation-private.
- The `AIig...` read-only generated contract extends `AIiDataObject` and carries `AIaDataObject` and `AIaDataObjectField` from `data/dataobject/intf`.
- The default mutable generated `AIigd...` interface extends the read-only contract plus `AIiSmartDataObject`. The default `AIcgd...` implementation extends `AIcSmartDataObject`. The generated class prefix is the **naming profile's rule**, not a fixed SmartDataObject requirement.
- In Python, contracts carry `__data_object_fields__` and optional `__data_object__`. An anonymous Java read-only contract object, or equivalent Python object, can serve as a structured default.

Serializer scope: XML/XSD output supports simple namespace-qualified nodes but not the entire XSD content model. Normal JSON schema-compatible output refuses cycles; reference-preserving JSON uses its own encoding. See tests for supported cases.

# SmartDataObject common metadata and binding

This artifact contains the **new**, independent metadata and implementation-binding APIs for Java and Python. The legacy `data/smartdataobject/impl` runtime remains unchanged and will be deprecated and removed after the new runtime is fully migrated.

- `AIsSmartDataObjectMetadataRegistry.forType(...)` provides common per-implementation-class metadata (`ClassValue` in Java; class-owned cache in Python), independent of the legacy cache.
- `AIcSmartDataObjectBindingRegistry.builder().register(...).build()` selects a purpose-scoped subset of concrete implementations. Multiple implementations for one read-only contract are valid.
- `AIaSmartDataObjectImplementationBinding(readContracts=...)` defines class-level contracts, including inherited declarations.
- `AIaSmartDataObjectFieldImplementationBinding` optionally chooses a concrete implementation for an SDO-valued field; an overriding getter can replace the inherited choice.
- `AIiSmartDataObjectFormatBindingProvider` is an independently discoverable service. The common descriptor does not hardcode XML/JSON/YAML annotations.

See [SDO-BINDING-REFERENCE.md](SDO-BINDING-REFERENCE.md) for the contracts and lifecycle rules.

# General DataObject contracts

`data/dataobject/intf` contains only foundational data-object markers and contract metadata.

- `AIiDataObject` is a pure marker; input/output markers extend it. Objects may implement both.
- `AIaDataObject` describes the normalized contract identity (source independent). A version of `-1` denotes an unversioned contract.
- `AIaDataObjectField` describes a normalized getter: presence, nullability, schema name, defaults and XML mapping. These annotations are owned here, **not** by SmartDataObject.
- Python provides analogous `AIcdDataObject` and `AIcdDataObjectField` immutable descriptors, held on the read-only class using `__data_object__` and `__data_object_fields__`. The base marker does not implement or inspect descriptors.

Generators producing `AIig...` contracts must resolve any external annotations first. The normalized contract is authoritative, while downstream generators can create SmartDataObjects or any other representation.

# Naming conventions public contracts

Java package namespace: `eu.algites.lib.naming.convention`. Python uses the same namespace as a PEP 420 package and keeps each public `AI*` type in its own module.

## Source-code naming schema namespace

All naming convention schemas published by this module live under
`eu/algites/lib/naming/convention/sourcecode/` in each of the `jsondefs`,
`yamldefs`, and `xmldefs` source families. Their schema IDs use the
`eu.algites.lib.naming.convention.sourcecode.*` prefix.

* `source-structure-name-conventions_1` defines generic source-code structure roles; schema metadata roles are **not** part of this contract.
* `dataobject/dataobject-schema-metadata-kind_1` defines six typed schema/instance metadata roles.
* `dataobject/dataobject-naming-conventions_1` defines a metadata role-to-field-name map under `DataObjectNameConventions`.
* `smartdataobject/smartdataobject-method-kind_1` defines five typed accessor-method roles.
* `smartdataobject/smartdataobject-naming-conventions_1` defines method role-to-composition maps under `SmartDataObjectNameConventions`.

JSONDEFS and YAMLDEFS represent maps with object properties. XMLDEFS
represents those maps using keyed `NameConvention` and `Rule` entries with
uniqueness constraints. The `StructureNaming` property has different enum
key types in the DataObject and SmartDataObject profile schemas.

These are **data contracts only**; using the newly published profiles in
Defs/SDO generators requires a separate, subsequent generator release.
Generated Java and Python sources must be recreated by the Defs/SDO codegen,
not edited by hand.

The schema move changes public `$id` and `$x-algites-schema-id` values and
may require profile configurations and consumers to update their `$schema`
references when those consumers are migrated. Do not remove previously
published schema URLs before consumers are updated.

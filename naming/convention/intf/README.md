# Naming convention common interfaces and shared definitions

`pub.lib.General_naming.convention.intf` exposes the Java/Python catalog interface, shared name fragment/composition schema `common-naming-conventions_1`, the `standard-conventions_1` schema, and the shared default `standard-conventions.yml` resource.

Both schemas publish directly beneath `eu/algites/lib/naming/convention/`, with no `sourcecode` segment. They can be used for other naming domains (tables, services, documents) without coupling to source-code/DO/SDO concepts.

Source-code-specific JSONDEFS/YAMLDEFS/XMLDEFS and their typed generated API belong to the sibling artifact `pub.lib.General_naming.convention.sourcecode`. Source-code profiles continue to live in their named `profile/*` artifacts.

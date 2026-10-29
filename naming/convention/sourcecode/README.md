# Source-code naming conventions

Artifact: `pub.lib.General_naming.convention.sourcecode`.

Contains exclusively JSONDEFS/YAMLDEFS/XMLDEFS contracts for naming source-code structures, DataObject schema metadata, and SmartDataObject accessors. The shared naming grammar (`common-naming-conventions_1`) and `standard-conventions_1` live one level above in `naming/convention/intf` and are general-purpose.

Every source file under `src/product/{jsondefs,yamldefs,xmldefs}` is a **source** for Defs Codegen. Never manually edit generated Java/Python source directories. Consumers should depend on this artifact for generated source-code naming types.

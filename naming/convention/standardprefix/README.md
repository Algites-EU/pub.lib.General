# Standard prefix naming profile

This source-only Java/Python resource artifact provides complete naming catalogs for Defs and SDO code generation. It does not activate any generator settings; those are packaged in `pub.tool.General_generators.code.profile.standardprefix`.

Prefix profiles put `I`, `C`, `IGD` etc. before the entity name, followed by `_` and technology marker `x`, `j`, or `y` where applicable. Postfix profiles use a separated role suffix such as `_GenYamlDataIntf`. The variant mappings are represented by `NameCompositions` rather than generator hardcoding.

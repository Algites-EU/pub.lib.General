# Versioning infrastructure

`common/intf` contains shared public version interfaces and their required
foundational primitives. `common/impl` contains additional concrete helpers.
The existing `scheme/*` artifacts implement external or Algites version
schemes and use the corresponding shared dependency.

This is an artifact restructuring, not a change to Java package names.

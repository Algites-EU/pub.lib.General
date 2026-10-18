# General DataObject contracts

`data/dataobject/intf` contains only foundational data-object markers and contract metadata.

- `AIiDataObject` is a pure marker; input/output markers extend it. Objects may implement both.
- `AIaDataObject` describes the normalized contract identity (source independent). A version of `-1` denotes an unversioned contract.
- `AIaDataObjectField` describes a normalized getter: presence, nullability, schema name, defaults and XML mapping. These annotations are owned here, **not** by SmartDataObject.
- Python provides analogous `AIcdDataObject` and `AIcdDataObjectField` immutable descriptors, held on the read-only class using `__data_object__` and `__data_object_fields__`. The base marker does not implement or inspect descriptors.

Generators producing `AIig...` contracts must resolve any external annotations first. The normalized contract is authoritative, while downstream generators can create SmartDataObjects or any other representation.

## Explicit OpenChoice participation and derived presence (1.1)

`AIaDataObjectField.presence()` uses `AInFieldPresenceKind.REQUIRED`, `ALLOWED`
(default), or `FORBIDDEN`. The old `presenceRequired` boolean is removed.
Presence is independent of `allowsNull`; repeated fields still need independent
cardinality constraints in the generator. A child contract can narrow an
`ALLOWED` property to either `REQUIRED` or `FORBIDDEN` by redeclaring its
getter and `AIaDataObjectField`. Widening an inherited required/forbidden
field is invalid. Data-object and field annotations expose common `namespace`
and `localName`; `xmlAttribute` remains XML-specific. Schema IDs and version
remain independent of representation-local naming.

An exported interface can declare direct, explicit participation with
`@AIaOpenChoiceTypeOf(AIiParent.class)`. The annotation is repeatable via
`AIaOpenChoiceTypesOf`; a second edge requires another annotation. The root
needs no marker. The utility `AIsOpenChoiceTypes.allowedHeads(Class<?>)`
computes transitive permitted parent heads, cached using `ClassValue`.
`permits(object, head)` checks actual contract membership; `extends` alone
is not authorization. `AIaDataObjectField(openChoice = true)` opts a particular
SDO field into this check. Generated domain interfaces should carry
`@AIaDataObject` to distinguish a newly declared derived domain contract
from an unannotated implementation class. Generated mutable `AIigd` contracts
should set `@AIaDataObject(sourceContract = AIigDomain.class)` to delegate
OpenChoice authorization to their source read-only `AIig` contract instead of
accidentally becoming a new unmarked domain alternative. Python equivalents
use `AIcdDataObject(source_contract=AIigDomain)`. XML element substitution interfaces
may additionally encode their valid substitutions in the Java type hierarchy,
without changing business packages.

Python parallels are `AInFieldPresenceKind`, `AIcdDataObjectField.presence`,
`open_choice_type_of(parent)`, `allowed_heads(contract)` and `permits(value, head)`.
The metadata and utilities do not perform class discovery or deserialization
registration. New schema authors must state each *direct* substitution edge;
this system must not infer those edges from Java/Python inheritance alone.

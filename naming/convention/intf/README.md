# Naming convention common interfaces and shared definitions

`pub.lib.General_naming.convention.intf` exposes the Java/Python catalog interface, shared name fragment/composition schema `common-naming-conventions_1`, the `standard-conventions_1` schema, and the shared default `standard-conventions.yml` resource.

Both schemas publish directly beneath `eu/algites/lib/naming/convention/`, with no `sourcecode` segment. They can be used for other naming domains (tables, services, documents) without coupling to source-code/DO/SDO concepts.

Source-code-specific JSONDEFS/YAMLDEFS/XMLDEFS and their typed generated API belong to the sibling artifact `pub.lib.General_naming.convention.sourcecode`. Source-code profiles continue to live in their named `profile/*` artifacts.

Bázové JSONDEFS/YAMLDEFS schéma `common-naming-conventions_1` je záměrně
otevřené pro specializované profily: na kořenovém objektu **vynechává**
`additionalProperties`. Standardní JSON Schema tím další vlastnosti stále
povoluje, ale Defs Codegen nevytváří netypovaný getter
`Map<String, Object>`/`dict[str, Any]`. Explicitní
`additionalProperties: true` nelze použít pro veřejné typované API bez
konkrétního schématu hodnot.

Kolekce `$defs` (`IdentifierConventions`, `VersionConventions`, `NameFragments`,
`NameCompositions`) odpovídají XSD: objekt s pojmenovaným polem
`IdentifierConvention` / `VersionConvention` / `NameFragment` /
`NameComposition`, obsahujícím seznam položek s povinným `name`.
Původní mapy pod dynamickými klíči nejsou publikovaným vstupním formátem.
Java a Python katalogy při načtení vytvoří interní index podle `name` a
odmítají duplicity; pořadí položek a segmentů zůstává zachované.

Each list's `items` references an **explicit, named** `$defs` type; the
matching XMLDEFS `xs:element` references the corresponding named
`xs:complexType`. No synthetic `ItemItem` is required for a named list entry.
The formerly inline `NameFragmentsSegmentValue` alternative is now the named
`NameFragmentsSegmentValueContent` choice under a required outer `Content`
element/property; `NameRegexp` also has its own named type. An example YAML
segment is `NameFragmentsSegmentValue: {Kind: identifier, Content:
{Convention: upper_camel_case_1}}` (shown compactly for readability).

## Temporary naming bootstrap ABI (`*.extgen`)

Until the standard builder takes over, only this library's interim bootstrap
uses a checked-in `java.extgen` / `python.extgen` compiler snapshot. The first
source-mode build of `pub.lib.General_naming.convention.intf` cannot yet run
Java SDO Codegen, since the tool depends on naming types. The initial snapshot
therefore contains:

- Java: exact copies of read-only Defs interfaces, sealed variants and enums,
  plus **minimal compiler-only** mutable interfaces (`AIigd*`) and abstract
  implementation placeholders (`AIcgd*`). They deliberately contain no
  setters, getters or instance behavior and **must never be used as runtime
  SDO implementations**. The minimal generic type parameters support compiling
  dependent code during the initial ABI cycle.
- Python: the already available genuine read-only and SDO generated source
  snapshot; no invented Python APIs. It will also be renewed from actual
  generation after a successful bootstrap.

The initial naming compiler phase uses `java.extgen` instead of `java.gen`
so Java classes are not compiled twice. The SDO CLI is then built from source;
its normal Java/Python generation runs on the read-only interfaces, and only
after the complete successful build does
`devops/bootstrap/promote-naming-extgen.py --refresh` replace **both**
`extgen` snapshots from the genuine `*.gen` outputs. The second Gradle
invocation selects `java.gen` rather than the abstract `java.extgen` stubs;
refresh happens after verification but before Maven Local promotion. The guard
`--check` verifies that the seed has not been modified or truncated. The
bootstrap fails instead of silently promoting incomplete Java SDO results.

This special handling applies to `naming.convention.intf` only and to the
interim bootstrap, not to unrelated libraries or a future standard Gradle
builder. No generated `*.gen` files are edited manually.

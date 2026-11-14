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

The interim bootstrap uses a repository-local `java.extgen` / `python.extgen`
compiler snapshot to break the initial dependency cycle: the first source-mode
build of `naming.convention.intf` cannot yet run Java SDO Codegen, because that
generator itself depends on naming types. Originally, Java used minimal
compiler-only `AIigd*` and abstract `AIcgd*` ABI placeholders. Those placeholders
are **not runtime implementations** and must not be published as final output.

After a successful full bootstrap, the snapshots must instead contain genuine
Defs and SDO generated sources for Java and Python, with a single unified
contract per type, without technology-specific interface projections.
The current repository snapshot has already been promoted from real generated
sources, so rechecking it must not depend on a removed historical manifest.

The initial naming compiler phase uses `java.extgen` instead of `java.gen`
so Java classes are not compiled twice. The SDO CLI is then built from source;
its normal Java/Python generation runs on the read-only interfaces, and only
after the complete successful build does
`devops/bootstrap/promote-naming-extgen.py --refresh` replace **both**
`extgen` snapshots from the genuine `*.gen` outputs. The second Gradle
invocation selects `java.gen` rather than the interim `java.extgen` seed;
refresh happens after verification but before Maven Local promotion. The guard `--check` validates the complete Java/Python ABI source
snapshots directly, checking required read-only and writable roles and
rejecting historical technology-specific projections. No hash manifest,
checked-in generated-file list, or other persistent build metadata is used.
`--refresh` regenerates the snapshots from complete `*.gen` output using
staged directory replacement and never writes such a manifest. Missing or
incomplete snapshots remain a fatal error; they are never silently rebuilt
from stale historical files.

This special handling applies to `naming.convention.intf` only and to the
interim bootstrap, not to unrelated libraries or a future standard Gradle
builder. No generated `*.gen` files are edited manually.

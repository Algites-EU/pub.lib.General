# YAML-driven naming compositions (1.1 transition)

The new naming catalog is defined by four schema kinds in `intf` (shared grammar and standard conventions) and `sourcecode`
(source-specific definitions), each under
`src/product/{yamldefs,jsondefs,xmldefs}/eu/algites/lib/naming/convention/`:

- `source-structure-kind_1` enumerates named source components, including `file_name_stem` and `file_name_suffix`.
- `container-structure-type_1` names supported container types (object, class, interface, enum, exception, annotation, resource_bundle).
- `standard-conventions_1` describes reusable named capture regexes.
- `common-naming-conventions_1` describes reusable `NameFragments` and `NameCompositions`; source-specific `NameConventions` are declared by `source-structure-name-conventions_1`.

The schema-derived Java/Python interfaces and enums are in `intf/src/product/java.gen` and `python.gen` (not handwritten `AIcd` DTOs).

The common `standard-conventions.yml` is owned by `intf/src/product/config/eu/algites/lib/naming/convention/`
and is published **once**, both in the naming interface JAR and its Python wheel.
Specific YAML catalogs live in `profile/{algites,standardprefix,standardpostfix}/src/product/config/`.
Their language-neutral interpreter lives exclusively in `impl` (`impl.standard.AIcNameConventionCatalog`);
Algites-specific profile construction is provided by `profile.algites.AIcAlgitesNamingProfiles`.
`loadResource`/`load_resource` resolve the common standard conventions from the `intf` dependency,
not from duplicate files bundled into each profile. External catalogs passed to `load(path)` may
provide a sibling `standard-conventions.yml`; when absent, the shared interface resource is used.
The Python product uses `importlib.resources` and its Modustro resource namespaces, never
arbitrary repository walks. In development, include the `intf/src/product/config` resource root
on `PYTHONPATH` alongside the implementation and profile roots.

The full filename is the concatenation of `file_name_stem` and `file_name_suffix`; the suffix includes its leading separator and may be `.yamldef.schema.json`. The catalog resolves `Ref` recursively, rejects cycles and unknown references, compiles complete anchored regexes, and rejects multiple matching profiles. Literal strings are escaped, while `identifier`, `version`, and custom `variable` segments contribute named regex captures. Source regexes use Java-style `(?<name>...)` captures; Python translates them into its native syntax.

Native catalog APIs:

- Java: `AIcNameConventionCatalog.load(Path)`, `names()`, `recognizeFilename(String, Set<String>)`, `matches(...)`, `parse(...)`, `format(...)`, `convert(...)`, `clean(Path, Set<String>)`.
- Python: `AIcNameConventionCatalog.load(Path)`, `load_resource(filename)`, `standard(technology)`, `names`, `recognize_filename(...)`, `matches(...)`, `parse(...)`, `format(...)`, `convert(...)`, `clean(...)`.

The `allowed` set restricts cleanup ownership to a generator's output roles. `--clean-only` does not need source schemas. Regular generation deliberately cleans old matching outputs before generating new ones. `gradle clean` remains responsible for removing all `*.gen` directories.

## Migration boundary

The 1.1 code generation pipeline uses `AIiNameConventionCatalog` and native `AIcNameConventionCatalog` directly. The old input/output naming profile DTOs and the legacy YAML adaptation layer have been removed; the Java and Python catalogs load shared YAML composition definitions.

XSD, JSONDEFS and YAMLDEFS now publish the same named-list structure for
`IdentifierConventions/IdentifierConvention`, `VersionConventions/VersionConvention`,
`NameFragments/NameFragment`, `NameCompositions/NameComposition`, and for
`NameConventions/NameConvention` under each profile family. Every item has an
explicit `name`. Structural and schema-metadata rules use `StructureNaming/Rule`
and `SchemaMetadataNaming/Rule` with `kind` and explicit child `Value`
in all three schema dialects. Duplicate `name`/`kind` entries are
rejected by catalog readers. They index the named lists internally to preserve
fast lookups without changing the published serialization.

For example:

```yaml
NameCompositions:
  NameComposition:
    - name: java-type-name_1
      Segments:
        - NameFragmentsSegmentValue:
            Kind: identifier
            Content:
              Convention: upper_camel_case_1
SourceStructureNameConventions:
  NameConventions:
    NameConvention:
      - name: java-source_1
        ContainerStructureType: interface
        StructureNaming:
          Rule:
            - kind: type_name
              Value: java-type-name_1
```

Schema metadata mappings belong to the separate typed DataObject convention
profile, not as six untyped sibling fields on `SourceStructureNameConventions`.
Changing the serialization is not a naming-policy change; IDs, regexes and
segment order remain unchanged.

For all named repeated items, XSD refers to a named `xs:complexType` and
JSONDEFS/YAMLDEFS arrays use `$ref` to the corresponding named `$defs` type;
thus the common read-only interface does not depend on synthetic `ItemItem`
item types. `NameFragmentsSegmentValue` has a required `Kind` and a required
`Content` field of named type `NameFragmentsSegmentValueContent`, containing
one explicitly tagged alternative: `Value`, `Convention`,
`CustomConventionRegexp`, or `NameRegexp` (the latter is a named object with
`Name` and `Regexp`). The outer `Content` is present in XML, JSON and YAML,
so these representations have matching serializations and generated getters.
Example variable segment:

```yaml
- NameFragmentsSegmentValue:
    Kind: variable
    Content:
      NameRegexp:
        Name: region
        Regexp: '(?<region>[a-z]+)'
```

All published YAML catalogs use this shape. The native Java/Python loaders
normalize the tagged payload in memory for regex matching and rendering.
Any `oneOf` validation restrictions that relate `Kind` to the selected
alternative remain *validation* rules and are not implicitly generated as
DataObject setters/getters; the `Content` choice provides the typed contract.



## 1.1 API migration

The old handwritten naming profile DTOs, input/output-kind enums and the
`naming.conversion`/`naming.validation` artifacts are removed. Name parsing,
formatting, conversion and validation now use the YAML-backed
`AIcNameConventionCatalog` in Java and Python, exposed through
`AIiNameConventionCatalog` to Java API consumers.

## XML schema identities and naming

The active Defs Codegen emits a single common, read-only DataObject contract
for each canonical logical type. XML namespace URIs, global element QNames,
substitution relationships, JSON `$id`, and YAML schema identity are stored
as independent format metadata; they are **not** a reason to create a second
nominal interface or any separate writable class family.

XMLDEFS, JSONDEFS and YAMLDEFS input filenames may have different source
naming conventions. Output names, however, are selected from the common
interface and SDO naming conventions. The naming catalog rejects ambiguous
filenames and collisions; these checks also apply to external read-only type
references and to case-folded paths across output roots.

The historical XML namespace and element naming conventions belong only to
the archived schema generator and must not be used by the new Defs/SDO
pipeline. An XML-only document may add XML annotations to its shared type;
it must not introduce a second nominal DataObject interface.

## Getter and setter method naming

The `source-structure-kind_1` structural roles also define `getter_method_name`
and `setter_method_name` alongside `method_name`. These are independent
`StructureNaming` entries; both JSONDEFS and YAMLDEFS map schemas and the
XMLDEFS enum expose the same two keys.

Every default Java naming profile uses `java-getter-method-name_1` and
`java-setter-method-name_1`. Every default Python naming profile uses
`python-getter-method-name_1` and `python-setter-method-name_1`.
Their compositions yield `getCustomerAddress` and `setCustomerAddress` for
`customer-address`, with `get`/`set` literal segments defined exclusively in
catalog YAML and an independently converted upper-camel identifier segment.

For a custom naming catalog, either composition may be replaced with `read`,
`write`, a different case, or any other language-valid naming syntax. Defs
Codegen resolves generated getter declarations, metadata and XML-element/wildcard
references from the selected contract profile. SDO Codegen resolves mutable
setter declarations and implementations from its selected mutable profile and
verifies compatibility with the corresponding concrete implementation profile.
The read getter in SDO is inherited from the source contract (and is therefore
not synthesized from the setter's spelling).

Source schemas are authoritative: `src/product/{java,python}.gen` is regenerated
by the build/codegen pipeline and must never be hand-edited.

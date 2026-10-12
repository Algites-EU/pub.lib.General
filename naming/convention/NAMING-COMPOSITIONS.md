# YAML-driven naming compositions (1.1 transition)

The new naming catalog is defined by four schema kinds in `intf/src/product/{yamldefs,jsondefs,xmldefs}/eu/algites/lib/naming/convention`:

- `source-structure-kind_1` enumerates named source components, including `file_name_stem` and `file_name_suffix`.
- `container-structure-type_1` names supported container types (object, class, interface, enum, exception, annotation, resource_bundle).
- `standard-conventions_1` describes reusable named capture regexes.
- `name-conventions_1` describes reusable `NameFragments`, `NameCompositions`, and `NameConventions` mapping structural roles to compositions.

The schema-derived Java/Python interfaces and enums are in `intf/src/product/java.gen` and `python.gen` (not handwritten `AIcd` DTOs).

The authoritative catalog YAML files reside in `impl/src/product/resources/eu/algites/lib/naming/convention/`. Identical package-data copies under `impl/src/product/python/eu/algites/lib/naming/convention/` support standalone Python packaging; a parity test checks byte-for-byte equality:

- `standard-conventions.yml`
- `algites-java-name-conventions.yml` (32 profiles)
- `algites-python-name-conventions.yml` (32 profiles)
- `algites-jsondef-name-conventions.yml`
- `algites-yamldef-name-conventions.yml`
- `algites-xmldef-name-conventions.yml`

The full filename is the concatenation of `file_name_stem` and `file_name_suffix`; the suffix includes its leading separator and may be `.yamldef.schema.json`. The catalog resolves `Ref` recursively, rejects cycles and unknown references, compiles complete anchored regexes, and rejects multiple matching profiles. Literal strings are escaped, while `identifier`, `version`, and custom `variable` segments contribute named regex captures. Source regexes use Java-style `(?<name>...)` captures; Python translates them into its native syntax.

Native catalog APIs:

- Java: `AIcNameConventionCatalog.load(Path)`, `names()`, `recognizeFilename(String, Set<String>)`, `matches(...)`, `parse(...)`, `format(...)`, `convert(...)`, `clean(Path, Set<String>)`.
- Python: `AIcNameConventionCatalog.load(Path)`, `names`, `recognize_filename(...)`, `matches(...)`, `parse(...)`, `format(...)`, `convert(...)`, `clean(...)`.

The `allowed` set restricts cleanup ownership to a generator's output roles. `--clean-only` does not need source schemas. Regular generation deliberately cleans old matching outputs before generating new ones. `gradle clean` remains responsible for removing all `*.gen` directories.

## Migration boundary

The 1.1 code generation pipeline uses `AIiNameConventionCatalog` and native `AIcNameConventionCatalog` directly. The old input/output naming profile DTOs and the legacy YAML adaptation layer have been removed; the Java and Python catalogs load shared YAML composition definitions.

The XSD variants for configuration-map schemas presently encode their outer named sections only; JSONDEFS/YAMLDEFS schemas provide strict inner validation. Do not claim full cross-format structural equivalence of the configuration XSD variants until the corresponding XML representation is explicitly modeled. Enum XSD representations are complete.


## 1.1 API migration

The old handwritten naming profile DTOs, input/output-kind enums and the
`naming.conversion`/`naming.validation` artifacts are removed. Name parsing,
formatting, conversion and validation now use the YAML-backed
`AIcNameConventionCatalog` in Java and Python, exposed through
`AIiNameConventionCatalog` to Java API consumers.

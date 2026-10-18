# YAML-driven naming compositions (1.1 transition)

The new naming catalog is defined by four schema kinds in `intf/src/product/{yamldefs,jsondefs,xmldefs}/eu/algites/lib/naming/convention`:

- `source-structure-kind_1` enumerates named source components, including `file_name_stem` and `file_name_suffix`.
- `container-structure-type_1` names supported container types (object, class, interface, enum, exception, annotation, resource_bundle).
- `standard-conventions_1` describes reusable named capture regexes.
- `name-conventions_1` describes reusable `NameFragments`, `NameCompositions`, and `NameConventions` mapping structural roles to compositions.

The schema-derived Java/Python interfaces and enums are in `intf/src/product/java.gen` and `python.gen` (not handwritten `AIcd` DTOs).

The authoritative catalog YAML files reside in `impl/src/product/config/eu/algites/lib/naming/convention/`. They are maintained once in the Modustro `config` resource root. Java exposes them as classpath resources. For Python, Modustro stages them under the naming implementation artifact's isolated resource-package namespace in its wheel; `AIcNameConventionCatalog_1.standard()` and `load_resource()` access them through `importlib.resources`, **not by walking from the Python module into parent directories or the source repository**. In source-checkout development the `src/product/config` directory can instead be added as an explicit Python resource root (`PYTHONPATH`/`sys.path`), alongside `src/product/python` and `src/product/python.gen`. Explicit external catalogs can also be opened with `load(path)`:

- `standard-conventions.yml`
- `algites-java-name-conventions.yml` (48 profiles)
- `algites-python-name-conventions.yml` (48 profiles)
- `algites-jsondef-name-conventions.yml`
- `algites-yamldef-name-conventions.yml`
- `algites-xmldef-name-conventions.yml`

The full filename is the concatenation of `file_name_stem` and `file_name_suffix`; the suffix includes its leading separator and may be `.yamldef.schema.json`. The catalog resolves `Ref` recursively, rejects cycles and unknown references, compiles complete anchored regexes, and rejects multiple matching profiles. Literal strings are escaped, while `identifier`, `version`, and custom `variable` segments contribute named regex captures. Source regexes use Java-style `(?<name>...)` captures; Python translates them into its native syntax.

Native catalog APIs:

- Java: `AIcNameConventionCatalog_1.load(Path)`, `names()`, `recognizeFilename(String, Set<String>)`, `matches(...)`, `parse(...)`, `format(...)`, `convert(...)`, `clean(Path, Set<String>)`.
- Python: `AIcNameConventionCatalog_1.load(Path)`, `load_resource(filename)`, `standard(technology)`, `names`, `recognize_filename(...)`, `matches(...)`, `parse(...)`, `format(...)`, `convert(...)`, `clean(...)`.

The `allowed` set restricts cleanup ownership to a generator's output roles. `--clean-only` does not need source schemas. Regular generation deliberately cleans old matching outputs before generating new ones. `gradle clean` remains responsible for removing all `*.gen` directories.

## Migration boundary

The 1.1 code generation pipeline uses `AIiNameConventionCatalog` and native `AIcNameConventionCatalog_1` directly. The old input/output naming profile DTOs and the legacy YAML adaptation layer have been removed; the Java and Python catalogs load shared YAML composition definitions.

The XSD variants for configuration-map schemas presently encode their outer named sections only; JSONDEFS/YAMLDEFS schemas provide strict inner validation. Do not claim full cross-format structural equivalence of the configuration XSD variants until the corresponding XML representation is explicitly modeled. Enum XSD representations are complete.


## 1.1 API migration

The old handwritten naming profile DTOs, input/output-kind enums and the
`naming.conversion`/`naming.validation` artifacts are removed. Name parsing,
formatting, conversion and validation now use the YAML-backed
`AIcNameConventionCatalog_1` in Java and Python, exposed through
`AIiNameConventionCatalog` to Java API consumers.

## XML element synthetic contract naming

The Java and Python catalogs add 16 XML-element-specific versions of their
existing interface/class roles (48 profiles per language). Normal generated
type conventions are unchanged and packages continue to represent business
areas. XML global element declarations can use their own synthetic interfaces,
uniquely keyed by XML QName in the canonical model. These new roles use the
normal naming compositions with an extra marker after their technical prefix:

- Java class and filename: `_xe`, e.g. `AIig_xePayment_1.java`,
  `AIigd_xePayment_1.java`, `AIcgd_xePayment_1.java`.
- Python class: `_xe`, e.g. `AIig_xePayment_1`.
- Python file: a further underscore to separate snake-case segments, e.g.
  `aiig__xe_payment_1.py`, `aiigd__xe_payment_1.py`, `aicgd__xe_payment_1.py`.

The general-role identifier starts with uppercase in a Java/Python class name,
whereas reserved role markers start with an underscore and lowercase letters.
Python filenames require `__xe_` because `lower_snake_case` itself contains
single underscores; double underscores distinguish the new role. Both the
standard identifier and version grammars must remain anchored and distinct.
Custom naming catalogs may use postfixes or other compositions, but the
catalog must reject any name matching multiple active conventions. This
requirement applies independently to container and filename recognizers.
Generator output must additionally reject duplicate full names/paths, including
case-folded filesystem collisions, before writing outputs. The marker is a
convention fragment, never a hardcoded operation in the Defs or SDO generator.

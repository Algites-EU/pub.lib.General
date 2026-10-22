# Schema-field constant naming convention fix

Created: 2026-10-21 12:46:32 CEST
Baseline: modustro-three-repositories-getter-setter-naming-final-20261021.zip

## Changes

- Removed the domain-specific `SCHEMA_FIELD_NAME__` literal from the generic `constant-name_1` composition in both Java and Python naming catalogs (`pub.lib.General`). The composition now contains only one identifier segment formatted with `upper_snake_case_1`.
- Defs Codegen now constructs the schema-field semantic designation from the `schema`, `field`, `name` words and renders it through `constant_name`, just like the actual source field identifier (`pub.tool.General`, Java and Python).
- The two rendered portions are joined with an **empty middle component**. The default output remains `SCHEMA_FIELD_NAME__OPTIONAL_TEXT`, preserving the double underscore and existing API compatibility.
- A custom `upper_camel_case_1` constant formatter produces `SchemaFieldName__OptionalText`; the generator does not force uppercase output spelling.
- Added regressions for the generic catalog and custom naming in both Java and Python test sources.
- `pub.gov.Algites` is unchanged. No source files in `java.gen` or `python.gen` were edited.

## Verification

- 23 naming catalog Python tests PASSED.
- 26 schema-parity and schema-object-bindings Python tests PASSED.
- 27 Defs, XML namespace and wildcard Python tests PASSED.
- 15 definition-format and lineage Python tests PASSED.
- **91 Python tests PASSED in total, no failures**, across the listed individual invocations.
- Java `AIcGenerationNames.java` and required Java interfaces compiled with `javac` 21 using minimal external-dependency stubs; a Java execution check verified default and custom naming outputs.
- Complete Gradle/Java TestNG build was not run in this container (Gradle 9.2.1 unavailable); do not interpret the Java smoke check as a full build.

## File delta relative to baseline

Changed (7):
- `pub.lib.General/naming/convention/algites/src/develop/python/test_name_convention_catalog_1.py`
- `pub.lib.General/naming/convention/algites/src/product/config/eu/algites/lib/naming/convention/algites-java-name-conventions.yml`
- `pub.lib.General/naming/convention/algites/src/product/config/eu/algites/lib/naming/convention/algites-python-name-conventions.yml`
- `pub.tool.General/generators/code/defscodegen/impl/src/develop/java/eu/algites/tool/codegen/defs/impl/standard/AItcSchemaParityTest.java`
- `pub.tool.General/generators/code/defscodegen/impl/src/develop/python/test_schema_parity.py`
- `pub.tool.General/generators/code/defscodegen/impl/src/product/java/eu/algites/tool/codegen/defs/impl/standard/AIcGenerationNames.java`
- `pub.tool.General/generators/code/defscodegen/impl/src/product/python/eu/algites/tool/codegen/defs/impl/standard/aic_generation_names.py`

Added (0):
- None

Removed (0):
- None


## Follow-up: Native Java naming catalog test correction

Updated: 2026-10-21 13:13:05 CEST
Baseline: pub.lib.General-schema-field-constant-naming-20261021.tgz

### Changes

- Updated four outdated Java TestNG assertions in `naming/convention/algites/src/develop/java/eu/algites/lib/naming/convention/impl/algites/AItcNameConventionCatalog_1Test.java` from 57 to 59 profiles. The two new XML namespace profiles were already present in both Java and Python resource catalogs.
- No catalog definitions, product Java/Python code, or generated sources were modified.

### Verification

- YAML catalogs each contain 59 `NameConventions`.
- Matching Python naming-catalog suite: **23 passed**.
- Java Gradle test attempted but could not run: local Gradle 9.2.1 wrapper distribution is missing and `services.gradle.org` could not be resolved (`UnknownHostException`). Native Java test execution therefore remains unverified.

### Changes relative to baseline

Changed:
- `naming/convention/algites/src/develop/java/eu/algites/lib/naming/convention/impl/algites/AItcNameConventionCatalog_1Test.java`
- `CHECKPOINT-NOTES.md` (this change log)

Added: none.
Removed: none.

## Corrected file timestamps

- Repackaged: 2026-10-21 13:54:11 CEST
- Refreshed `mtime` for the four-assertion update in `naming/convention/algites/src/develop/java/eu/algites/lib/naming/convention/impl/algites/AItcNameConventionCatalog_1Test.java`.
- Expected naming profile count was corrected from 57 to 59 in four Java assertions. The file length remained identical (9171 bytes), causing timestamp/size-only comparison to overlook it.
- Source content of all other repository files is unchanged.
- Removed files: none.

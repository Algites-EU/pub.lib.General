# SmartDataObject metadata and binding reference

## Ownership and type identity

`AIiSmartDataObjectMetadata` describes **one actual implementation class**: one effective list of fields (semantic and canonical data names, getter signatures and common constraints) plus plugin format-binding metadata. No metadata is copied from the legacy `AIcSmartDataMetadata` cache or wrapper. It is cached by actual `Class<?>` identity, not by binary class name.

`AIiSmartDataObjectBinding` describes **one registered concrete implementation** within an `AIiSmartDataObjectBindingRegistry`. Its `getDeclaredReadContracts()` returns contracts directly annotated on that implementation class; `getEffectiveReadContracts()` additionally includes contracts from annotated implementation superclasses and the read-only superinterface closure.

The registry is limited to explicitly registered implementations and does not enumerate all classes for which metadata have been cached. Multiple implementations of one contract are permitted. An unmarshaller must choose one implementation using explicit field binding, polymorphic input, or an agreed unambiguous default rule; the registry itself does not silently select a candidate.

## Field names and overrides

`getSemanticFieldName()` is the normalized getter identity (`PostalCode`). `getDataFieldName()` is the canonical data field name (`postal_code`), supplied by the new `AIaDataObjectField.dataFieldName()` attribute or inherited from the original getter declaration. XML QName and other format-specific mapping are separate provider metadata. `AIaDataObjectField.fieldName()` remains deprecated temporarily for the legacy runtime.

`AIaSmartDataObjectFieldImplementationBinding` on an overriding getter replaces the nearest inherited implementation selection. The requested concrete class must be type-compatible and present in the binding registry when used for unmarshalling.

## Provider discovery

Java discovers `AIiSmartDataObjectFormatBindingProvider` using `ServiceLoader`, including explicitly registered classloaders and named-module `ModuleLayer` scopes. Python discovers entry points in the `eu.algites.lib.data.smartdataobject.format_binding_providers` group. Each provider declares its own stable `getFormatId()` and `getProviderId()` and contributes metadata independently.

`AInTextFormatType` implements `AIiEnumItem<String>` with stable `code()` values for the built-in formats. The `formatId` interface also accepts codes supplied by future formats.

## Compatibility and limitations

The legacy runtime in `data/smartdataobject/impl` and its original metadata cache are **independent** of the new implementation. Neither runtime delegates to, adapts, or shares its cache with the other. The new `AIcSmartDataObject` resides in `impl/common`, not in the legacy artifact. Generated sources must be migrated by Defs/SDO Codegen; do not edit `src/product/java.gen` or `src/product/python.gen` manually.

The new `impl/format/{json,yaml,xml}` artifacts provide concrete Java and Python codecs and metadata resolvers. The Java implementations are declared with `META-INF/services` files, the Python implementations with distribution entry points. A package existing in a source checkout does not by itself install Python distribution entry points: installation/build metadata must make the wheels available.

Current tests cover nested XML/JSON/YAML roundtrips, discovery, raw-field presence, inherited field constraints, duplicate data names, input graph identity and cyclic serialization rejection. These checks are **not** a full Gradle/Modustro bootstrap or full-schema conformance verification. Advanced YAML tags and aliases, all XML Schema constructs and arbitrary polymorphic discriminator strategies require separate coverage before declaring the replacement complete.

## Artifact coordinates and Git migration

The independent implementation artifacts use these coordinates (group `eu.algites.lib.data`):

| Module | Artifact ID | Dependency role |
| --- | --- | --- |
| Shared SDO runtime | `pub.lib.General_data.smartdataobject.impl.common` | Public dependency of each format module |
| XML | `pub.lib.General_data.smartdataobject.impl.format.xml` | XML codec/provider |
| JSON | `pub.lib.General_data.smartdataobject.impl.format.json` | JSON codec/provider |
| YAML | `pub.lib.General_data.smartdataobject.impl.format.yaml` | YAML codec/provider |
| Legacy compatibility | `pub.lib.General_data.smartdataobject.impl` | Existing generated code only until migration |

The existing `documentation/intf` and `naming/convention/intf` artifacts temporarily depend on both the legacy and new SDO runtimes; the legacy dependency is intentional while their existing generated sources still use the former base class.

The only existing tracked file **renamed** by this source-tree layout change is `data/smartdataobject/README.md` (the former legacy runtime reference), which moves to `data/smartdataobject/impl/README.md`. The newly created root `README.md` is a different file. The common and format modules consist of new sources rather than renamed legacy sources; therefore, do not `git mv` the legacy runtime into common. Before extracting the updated `.tgz` into a Git checkout, run `bash devops/migrations/git-mv-sdo-modules.sh <checkout-root>` from the archive's migration script, then extract and inspect `git diff --cached` plus `git status`.

## Java discovery and roundtrip test

The test `AItcSmartDataObjectFormatRoundtripTest` in `impl/format/xml/src/develop/java` checks that all three provider services can be discovered from classpath resource directories and roundtrip nested data. Test execution must include each format artifact's `src/product/resources` or the corresponding built JAR on the classpath. Explicit `ModuleLayer` registration is available for providers declared in named Java modules. Providers can also be exposed via `registerClassLoader()` and subsequently removed from discovery via `unregisterClassLoader()`.

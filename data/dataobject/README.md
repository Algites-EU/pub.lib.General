# DataObject (pub.lib.General)

`data/dataobject/intf` defines common Java/Python structured data-object contracts.
`AIaDataObjectField` exposes the shared field name, presence, nullability,
order and empty-state permissions. An optional, **separate** getter annotation,
`AIaDataObjectFieldDefault`, configures default semantics via a CPDiscovery
factory. The earlier `AIaFieldDefault` annotation and nested
`AIaDataObjectField.defaultValue` are no longer part of the current API.
Presentation text belongs to `AIaFieldLabel`, not the field definition.
Raw presence is unchanged by default evaluation, and an explicit null is not
replaced with a default.

XML metadata is separate, in `eu.algites.lib.data.dataobject.xmldefs` and the
Python `xmldefs` package. `AIaDataObjectXmlSchema` represents XML types or global
elements; `AIaDataObjectFieldXmlSchema` marks XML QNames and ELEMENT/ATTRIBUTE fields.
Its optional `order` (default `-1`) is the zero-based declaration position
within the XML type. Generated XML contracts provide this position explicitly;
the Java SmartDataObject metadata scanner sorts annotated getters by position
within each declaring contract, because reflection order is unspecified. Python
metadata retains the same position and maintains declaration order. Strict XSD
validation of sequence ordering is separate from tolerant QName-based parsing.
The shared DataObject APIs do not carry XML namespace or attribute flags.

XML substitution-group relationships are declared through generated `_xe`
interfaces inheriting both their XML type `_x` interface and their substitution
head `_xe` interface. No OpenChoice annotations, runtime role registry or special
runtime assignment check are used. XML schema legality is decided by Defs Codegen,
not by this core data-object marker library.

## Typově specifická omezení a výchozí hodnoty (1.1-SNAPSHOT)

`AIaDataObjectFieldStringConstraints`, `AIaDataObjectFieldNumericConstraints`,
`AIaDataObjectFieldListConstraints` a `AIaDataObjectFieldObjectConstraints`
patří přímo na getter; nesou jen omezení příslušného typu. Obecné `order`, `isEmptyAllowed` a `presence` náleží `AIaDataObjectField`;
výchozí hodnota je **samostatná anotace getteru** `AIaDataObjectFieldDefault`.
Technologicky specifická metadata XML/JSON/YAML neovlivňují její význam.

Přítomnost anotace určuje, zda je default vůbec deklarovaný — atribut
`defined` již neexistuje. `defaultValueFactory` obsahuje výběr CPDiscovery
(`factoryInterface`, nepovinné `factoryCategory` a `factoryProviderId`).
`defaultArguments` poskytuje lexikální parametry pro factory. Metadata SDO
uchovávají tuto deklaraci na fieldu; při efektivním vyhodnocování předají
`expectedType` (typ getteru) a argumenty vybrané factory. CPDiscovery drží
registry již objevených providerů, a proto opakovaný lookup nespouští
ServiceLoader scan. SDO neukládá sdílenou instanci defaultní hodnoty.
`Optional.empty()` je explicitní default `null`, nikoliv chybějící anotace.

Standardní `AIiFieldDefaultScalarValueFactory` zpracovává jednu řetězcovou
hodnotu (String, boolean/Boolean, celočíselné a desetinné typy, BigInteger,
BigDecimal a enumy s `AIiEnumItem.code()`). Nepotřebuje explicitní kategorii;
standardní CPDiscovery kategorie je `"default"`.

`AInDataObjectFieldDefaultKind` má čtyři režimy:

- `DECLARATION_ONLY`: default pouze dokumentuje, efektivní hodnotu nenahrazuje;
- `IF_FIELD_VALUE_NOT_PRESENT`: dosadí default, jen když raw field chybí;
- `IF_FIELD_VALUE_EMPTY`: dosadí default pouze pro technický sentinel EMPTY,
  nikoliv při obyčejném `setXxx("")`;
- `CONSTRAINT_DEFAULT`: neprovádí dosazení, ale odmítne přidělení hodnoty,
  která se nerovná deklarovanému defaultu (XSD `fixed`).

XML atribut `default` odpovídá `IF_FIELD_VALUE_NOT_PRESENT`, XML element
`default` odpovídá `IF_FIELD_VALUE_EMPTY`. Raw serializace nikdy nevypisuje
efektivní default místo uložené raw hodnoty.

```java
@AIaDataObjectField(dataFieldName = "Name", isEmptyAllowed = true)
@AIaDataObjectFieldDefault(
    kind = AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_EMPTY,
    defaultValueFactory = @AIaCategoryProviderFactoryDefinition(
        factoryInterface = AIiFieldDefaultScalarValueFactory.class
    ),
    defaultArguments = {"Anonymous"}
)
String getName();
```

V Pythonu jde o analogický samostatný dekorátor `@AIaDataObjectFieldDefault`
s konfigurací `default_value_factory` a `default_arguments`.

## Canonical field identity and metadata inheritance

The semantic identity of a DataObject/SmartDataObject field is the normalized
`UpperCamelCase` identifier obtained from its **annotated getter** using its
explicit input naming convention. For a standard getter `getCustomerId`, the
identity is `CustomerId`, irrespective of the value of `fieldName`.

`fieldName` is the external data property name for JSON/YAML and related
schema-facing representations; it is never parsed as a naming convention or
used to derive generated Java/Python methods. With an omitted `fieldName`, a
new field defaults to its semantic ID. An overridden getter inherits the
parent's effective `fieldName` when the annotation value is left blank.
Explicitly changing that value on an override is forbidden.

Within the complete effective contract, each semantic ID identifies one field
and each external data name identifies at most one semantic ID. Two different
getters must not declare the same `fieldName`; use an ordinary delegating
getter if an alternative convenience accessor is needed.

In Python, `AIaDataObjectField` is a **method decorator** analogous to
`AIaFieldLabel`: place it on the getter, not in a parallel
`__data_object_fields__` tuple with a repeated `getter_name`. The older
`AIcdDataObjectField` tuple model remains a compatibility input for published
legacy contracts. For those legacy descriptors which have no getter at all,
the original key is retained rather than claiming that it came from a getter.
New generated contracts emit method decorators.

SmartDataObject runtime raw methods (`set_RawField`, `get_RawField`, presence
and unset) use the canonical semantic ID. Serializers and `from_mapping` use
external field names with an explicit mapping back to semantic IDs; XML uses
its separate QName metadata. Raw typed graph keys remain semantic IDs.

## Independent schema metadata (XMLDEFS / JSONDEFS / YAMLDEFS)

Read-only Java and Python contracts use the common `AIiDataObject` API.
XML, JSON and YAML identities are declared independently by
`AIaDataObjectXmlSchema`, `AIaDataObjectJsonSchema` and
`AIaDataObjectYamlSchema` (and corresponding Python metadata).
No technology-specific DataObject or SmartDataObject superinterfaces exist.
XML field wire names use `AIaDataObjectFieldXmlSchema`.

## Possible future extension: input-only field-name aliases (deferred)

**Status: idea only / deferred (`ad acta`); not part of the current DataObject,
SmartDataObject, Defs Codegen, SDO Codegen or schema contracts.** Do not implement
or depend on the following behavior without a separate design decision.

A future unmarshalling API might accept legacy wire property names for a single
canonical data field. For example, a field whose canonical external name is
`customerIdentifier` could also accept `customer_id` or `customerId` on input,
while marshalling would always emit only `customerIdentifier`. A potential
Java annotation member name is `unmarshallingAlternateFieldNames` (a `String[]`);
the corresponding Python metadata and schema vocabulary would need to be
defined together. These names are **wire-format aliases**, not additional
getters, independent SDO fields, or sources for naming-convention inference.

If this proposal is revisited, decide and test at least the following:

- Alias uniqueness and collision detection across the complete inherited
  contract: an alias must not shadow another field's canonical name or alias.
  Supplying two names for the same field in one input should be rejected as
  ambiguous rather than silently choosing a value.
- Canonical output only, and precise handling of inherited annotations and
  metadata precedence. Legacy-name acceptance should be explicitly scoped to
  unmarshalling, not to internal field identity or generated method names.
- Schema support (for example, an Algites-specific JSON/YAML schema extension)
  and propagation into generated contract metadata; JSON Schema validators do
  not automatically treat such annotations as aliases.
- XML needs its own design because an element/attribute is identified by its
  kind and expanded QName (namespace URI + local name), not just a bare
  string. Schema validation against XSD may reject a legacy QName before
  unmarshalling, and must be addressed separately.
- Java/Python parity, malformed-input behavior, backward compatibility and
  whether aliases should be available per format rather than globally.

This section records a possible future feature only. It does not change the
current meaning of `fieldName`, XML QNames, or any existing serializer.

## Schema identity and instance-document metadata

A source schema has three independent metadata values: the published **schema URL**,
its stable logical **schema ID**, and its **schema version**. The corresponding
Java `AIaDataObject` annotation / Python `AIcdDataObject` descriptor preserve
`schemaUrl` / `schema_url`, `schemaId` / `schema_id`, and
`schemaVersion` / `schema_version` separately. Do not derive the logical ID
from the URL.

Three optional target *property names* control inclusion in JSON/YAML instance
documents: `dataSourceSchemaUrlField`, `dataSourceSchemaIdentifierField`, and
`dataSourceSchemaVersionField` (snake_case in Python). An empty target suppresses
emission of the respective metadata, even if its value is known. For Algites,
only `$schema` is enabled by default; logical ID and version are retained in
contract annotations but not duplicated in the document. The JSON Schema
keyword `$schema` continues to designate the JSON Schema dialect inside the
**schema definition** and must not be substituted there.

The Defs Codegen source naming profile supplies the exact source metadata key
names (by default `$id`, `x-algites-schema-id`, and
`x-algites-schema-version`) and destination property names. Generated types
propagate this mapping into the second-stage SDO annotations. During JSON/YAML
serialization, an explicitly declared property with the same target metadata
name is emitted **once**; a conflicting stored value fails rather than silently
overriding the contract metadata. Missing or disabled metadata is not guessed.

## Schématové anotace (1.1-SNAPSHOT)

Pro identitu formátů se používají výhradně nezávislé anotace
`AIaDataObjectXmlSchema` a `AIaDataObjectFieldXmlSchema` v `xmldefs`,
`AIaDataObjectJsonSchema` v `jsondefs` a `AIaDataObjectYamlSchema` v
`yamldefs`. Každá identita schématu je samostatná, i když více formátů
sdílí jeden typ odvozený z `AIiDataObject`.

`AIaDataObject` a `AIaDataObjectField` nadále popisují společnou
sémantiku typu a jeho polí; Defs i SDO Codegen zapisují pouze platné
schématové anotace. Původní technologické markery a staré anotace
nejsou součástí API. Pravidla výchozích hodnot a jejich resolverů se v
této fázi nemění.

## Umístění nových schématových anotací

**Umístění nových anotací (Java/Python):**

| Anotace | Java package | Python modul |
|---|---|---|
| `AIaDataObjectJsonSchema` | `eu.algites.lib.data.dataobject.jsonschema` | `eu.algites.lib.data.dataobject.jsondefs.aia_data_object_json_schema` |
| `AIaDataObjectXmlSchema` | `eu.algites.lib.data.dataobject.xmlschema` | `eu.algites.lib.data.dataobject.xmldefs.aia_data_object_xml_schema` |
| `AIaDataObjectFieldXmlSchema` | `eu.algites.lib.data.dataobject.xmlschema` | `eu.algites.lib.data.dataobject.xmldefs.aia_data_object_field_xml_schema` |

Původní formátové anotace (`AIaDataObjectJsonSchema`, `AIaDataObjectYamlSchema`,
`AIaDataObjectXmlSchema`, `AIaDataObjectFieldXmlSchema`) zůstávají v dosavadních
`jsondefs`, `yamldefs` a `xmldefs` packages jako deprecated; jejich veřejná
signatura se nemění. Obecné `AIaDataObject` / `AIaDataObjectField` zůstávají
v `eu.algites.lib.data.dataobject` a náleží výhradně společným `AIig…`
kontraktům. Při generování se importy nových anotací vytvářejí pro nové
packages; pro kompatibilitu se nadále emitují i původní anotace.

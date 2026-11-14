package eu.algites.lib.naming.convention;

/**
 * Reusable regular expressions with named capture groups. Regex syntax canonicalized from Java named groups.
 *
 * <p>Canonical identity: {https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/standard-conventions_1.xsd}StandardConventions; version: 1.</p>
 */
@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(schemaId = "eu.algites.lib.naming.convention.standard-conventions", schemaVersion = 1, schemaUrl = "https://defs.dev.algites.eu/api/jsondefs/eu/algites/lib/naming/convention/standard-conventions_1.jsondef.schema.json")
@eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.standard-conventions", schemaVersion = 1, schemaUrl = "https://defs.dev.algites.eu/api/jsondefs/eu/algites/lib/naming/convention/standard-conventions_1.jsondef.schema.json")
@eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject(schemaId = "eu.algites.lib.naming.convention.standard-conventions", schemaVersion = 1, schemaUrl = "https://defs.dev.algites.eu/api/yamldefs/eu/algites/lib/naming/convention/standard-conventions_1.yamldef.schema.json")
@eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/standard-conventions_1.xsd", namespaceLocalName = "StandardConventions")
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/standard-conventions_1.xsd", namespaceLocalName = "StandardConventions")
@eu.algites.lib.data.dataobject.AIaDataObject(description = "Reusable regular expressions with named capture groups. Regex syntax canonicalized from Java named groups.")
public interface AIigStandardConventions_1 extends AIigCommonNamingConventions_1 {
    /** Canonical key IDs and field names for this DataObject. */
    interface CONSTStandardConventions {
        /** Source identifier IdentifierConventions. */
        String FIELD_NAME__IDENTIFIER_CONVENTIONS = "IdentifierConventions";
        /** Source identifier VersionConventions. */
        String FIELD_NAME__VERSION_CONVENTIONS = "VersionConventions";
        /** Source identifier UniqueIdentifierConventionNames. */
        String KEY_ID__UNIQUE_IDENTIFIER_CONVENTION_NAMES = "UniqueIdentifierConventionNames";
        /** Source identifier name. */
        String FIELD_NAME__NAME = "name";
        /** Source identifier UniqueVersionConventionNames. */
        String KEY_ID__UNIQUE_VERSION_CONVENTION_NAMES = "UniqueVersionConventionNames";
    }

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "IdentifierConventions", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "IdentifierConventions", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaKeyDefinition(name = CONSTStandardConventions.KEY_ID__UNIQUE_IDENTIFIER_CONVENTION_NAMES, selector = "IdentifierConvention", fields = {CONSTStandardConventions.FIELD_NAME__NAME}, required = true)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "IdentifierConventions", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.REQUIRED, allowsNull = false, order = 0, minItemCount = 1, maxItemCount = 1, isEmptyAllowed = false)
    /** Value of schema field IdentifierConventions. Field is required; cardinality 1..1.
     * @return field value in its canonical representation
     */
    AIigIdentifierConventions_1 getIdentifierConventions();

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "VersionConventions", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "VersionConventions", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaKeyDefinition(name = CONSTStandardConventions.KEY_ID__UNIQUE_VERSION_CONVENTION_NAMES, selector = "VersionConvention", fields = {CONSTStandardConventions.FIELD_NAME__NAME}, required = true)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "VersionConventions", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.REQUIRED, allowsNull = false, order = 1, minItemCount = 1, maxItemCount = 1, isEmptyAllowed = false)
    /** Value of schema field VersionConventions. Field is required; cardinality 1..1.
     * @return field value in its canonical representation
     */
    AIigVersionConventions_1 getVersionConventions();

}

package eu.algites.lib.naming.convention;

/**
 * Contract for CommonNamingConventions.
 *
 * <p>Canonical identity: {https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd}CommonNamingConventions; version: 1.</p>
 */
@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions", schemaVersion = 1, schemaUrl = "https://defs.dev.algites.eu/api/jsondefs/eu/algites/lib/naming/convention/common-naming-conventions_1.jsondef.schema.json")
@eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions", schemaVersion = 1, schemaUrl = "https://defs.dev.algites.eu/api/jsondefs/eu/algites/lib/naming/convention/common-naming-conventions_1.jsondef.schema.json")
@eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions", schemaVersion = 1, schemaUrl = "https://defs.dev.algites.eu/api/yamldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.yamldef.schema.json")
@eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "CommonNamingConventions")
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "CommonNamingConventions")
@eu.algites.lib.data.dataobject.AIaDataObject(description = "CommonNamingConventions")
public interface AIigCommonNamingConventions_1 extends eu.algites.lib.data.dataobject.AIiDataObject {
    /** Canonical key IDs and field names for this DataObject. */
    interface CONSTCommonNamingConventions {
        /** Source identifier StandardConventionsFile. */
        String FIELD_NAME__STANDARD_CONVENTIONS_FILE = "StandardConventionsFile";
        /** Source identifier IdentifierConventions. */
        String FIELD_NAME__IDENTIFIER_CONVENTIONS = "IdentifierConventions";
        /** Source identifier VersionConventions. */
        String FIELD_NAME__VERSION_CONVENTIONS = "VersionConventions";
        /** Source identifier NameFragments. */
        String FIELD_NAME__NAME_FRAGMENTS = "NameFragments";
        /** Source identifier NameCompositions. */
        String FIELD_NAME__NAME_COMPOSITIONS = "NameCompositions";
        /** Source identifier UniqueIdentifierConventionNames. */
        String KEY_ID__UNIQUE_IDENTIFIER_CONVENTION_NAMES = "UniqueIdentifierConventionNames";
        /** Source identifier name. */
        String FIELD_NAME__NAME = "name";
        /** Source identifier UniqueVersionConventionNames. */
        String KEY_ID__UNIQUE_VERSION_CONVENTION_NAMES = "UniqueVersionConventionNames";
        /** Source identifier UniqueNameFragmentNames. */
        String KEY_ID__UNIQUE_NAME_FRAGMENT_NAMES = "UniqueNameFragmentNames";
        /** Source identifier UniqueNameCompositionNames. */
        String KEY_ID__UNIQUE_NAME_COMPOSITION_NAMES = "UniqueNameCompositionNames";
    }

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "StandardConventionsFile", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "StandardConventionsFile", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "StandardConventionsFile", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.ALLOWED, allowsNull = false, order = 0, minItemCount = 0, maxItemCount = 1, isEmptyAllowed = true)
    /** Optional reference to standard identifier/version definitions. Field is optional; cardinality 0..1.
     * @return field value in its canonical representation
     */
    String getStandardConventionsFile();

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "IdentifierConventions", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "IdentifierConventions", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaKeyDefinition(name = CONSTCommonNamingConventions.KEY_ID__UNIQUE_IDENTIFIER_CONVENTION_NAMES, selector = "IdentifierConvention", fields = {CONSTCommonNamingConventions.FIELD_NAME__NAME}, required = true)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "IdentifierConventions", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.ALLOWED, allowsNull = false, order = 1, minItemCount = 0, maxItemCount = 1, isEmptyAllowed = false)
    /** Named identifier conventions. Field is optional; cardinality 0..1.
     * @return field value in its canonical representation
     */
    AIigIdentifierConventions_1 getIdentifierConventions();

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "VersionConventions", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "VersionConventions", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaKeyDefinition(name = CONSTCommonNamingConventions.KEY_ID__UNIQUE_VERSION_CONVENTION_NAMES, selector = "VersionConvention", fields = {CONSTCommonNamingConventions.FIELD_NAME__NAME}, required = true)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "VersionConventions", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.ALLOWED, allowsNull = false, order = 2, minItemCount = 0, maxItemCount = 1, isEmptyAllowed = false)
    /** Named version conventions. Field is optional; cardinality 0..1.
     * @return field value in its canonical representation
     */
    AIigVersionConventions_1 getVersionConventions();

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "NameFragments", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "NameFragments", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaKeyDefinition(name = CONSTCommonNamingConventions.KEY_ID__UNIQUE_NAME_FRAGMENT_NAMES, selector = "NameFragment", fields = {CONSTCommonNamingConventions.FIELD_NAME__NAME}, required = true)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "NameFragments", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.ALLOWED, allowsNull = false, order = 3, minItemCount = 0, maxItemCount = 1, isEmptyAllowed = false)
    /** Reusable fragment definitions. Field is optional; cardinality 0..1.
     * @return field value in its canonical representation
     */
    AIigNameFragments_1 getNameFragments();

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "NameCompositions", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "NameCompositions", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaKeyDefinition(name = CONSTCommonNamingConventions.KEY_ID__UNIQUE_NAME_COMPOSITION_NAMES, selector = "NameComposition", fields = {CONSTCommonNamingConventions.FIELD_NAME__NAME}, required = true)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "NameCompositions", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.ALLOWED, allowsNull = false, order = 4, minItemCount = 0, maxItemCount = 1, isEmptyAllowed = false)
    /** Reusable name composition definitions. Field is optional; cardinality 0..1.
     * @return field value in its canonical representation
     */
    AIigNameCompositions_1 getNameCompositions();

}

package eu.algites.lib.naming.convention;

/**
 * Contract for VersionConvention.
 *
 * <p>Canonical identity: {https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd}VersionConvention; version: 1.</p>
 */
@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/VersionConvention", schemaVersion = 1)
@eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/VersionConvention", schemaVersion = 1)
@eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/VersionConvention", schemaVersion = 1)
@eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "VersionConvention")
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "VersionConvention")
@eu.algites.lib.data.dataobject.AIaDataObject(description = "VersionConvention")
public interface AIigVersionConvention_1 extends eu.algites.lib.data.dataobject.AIiDataObject {
    /** Canonical key IDs and field names for this DataObject. */
    interface CONSTVersionConvention {
        /** Source identifier Regexp. */
        String FIELD_NAME__REGEXP = "Regexp";
        /** Source identifier name. */
        String FIELD_NAME__NAME = "name";
    }

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "Regexp", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "Regexp", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "Regexp", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.REQUIRED, allowsNull = false, order = 0, minItemCount = 1, maxItemCount = 1, isEmptyAllowed = true)
    /** Value of schema field Regexp. Field is required; cardinality 1..1.
     * @return field value in its canonical representation
     */
    String getRegexp();

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "name", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ATTRIBUTE)
    @eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "name", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ATTRIBUTE)
    @eu.algites.lib.data.dataobject.AIaDataObjectFieldStringConstraints(minLength = 1)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "name", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.REQUIRED, allowsNull = false, order = 1, minItemCount = 1, maxItemCount = 1, isEmptyAllowed = true)
    /** Value of schema field name. Field is required; cardinality 1..1.
     * @return field value in its canonical representation
     */
    String getName();

}

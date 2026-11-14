package eu.algites.lib.naming.convention;

/**
 * Contract for NameFragmentsSegmentReference.
 *
 * <p>Canonical identity: {https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd}NameFragmentsSegmentReference; version: 1.</p>
 */
@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentReference", schemaVersion = 1)
@eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentReference", schemaVersion = 1)
@eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentReference", schemaVersion = 1)
@eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameFragmentsSegmentReference")
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameFragmentsSegmentReference")
@eu.algites.lib.data.dataobject.AIaDataObject(description = "NameFragmentsSegmentReference")
public interface AIigNameFragmentsSegmentReference_1 extends eu.algites.lib.data.dataobject.AIiDataObject {
    /** Canonical key IDs and field names for this DataObject. */
    interface CONSTNameFragmentsSegmentReference {
        /** Source identifier Ref. */
        String FIELD_NAME__REF = "Ref";
    }

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "Ref", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "Ref", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "Ref", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.REQUIRED, allowsNull = false, order = 0, minItemCount = 1, maxItemCount = 1, isEmptyAllowed = true)
    /** Value of schema field Ref. Field is required; cardinality 1..1.
     * @return field value in its canonical representation
     */
    String getRef();

}

package eu.algites.lib.naming.convention;

/**
 * Contract for NameCompositions.
 *
 * <p>Canonical identity: {https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd}NameCompositions; version: 1.</p>
 */
@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions", schemaVersion = 1)
@eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions", schemaVersion = 1)
@eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions", schemaVersion = 1)
@eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameCompositions")
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameCompositions")
@eu.algites.lib.data.dataobject.AIaDataObject(description = "NameCompositions")
public interface AIigNameCompositions_1 extends eu.algites.lib.data.dataobject.AIiDataObject {
    /** Canonical key IDs and field names for this DataObject. */
    interface CONSTNameCompositions {
        /** Source identifier NameComposition. */
        String FIELD_NAME__NAME_COMPOSITION = "NameComposition";
    }

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "NameComposition", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "NameComposition", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "NameComposition", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.ALLOWED, allowsNull = false, order = 0, minItemCount = 0, maxItemCount = -1)
    /** Value of schema field NameComposition. Field is optional; cardinality 0..-1.
     * @return field value in its canonical representation
     */
    eu.algites.lib.common.list.AIiMultiMappedList<? extends AIigNameComposition_1> getNameCompositionList();

}

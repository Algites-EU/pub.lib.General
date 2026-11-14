package eu.algites.lib.naming.convention.impl.intf;

/** Generated mutable contract derived from AIigNameFragmentsSegmentValue_1. */
@eu.algites.lib.data.dataobject.AIaDataObject(title = "", description = "NameFragmentsSegmentValue", setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentValue", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentValue", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentValue", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameFragmentsSegmentValue")
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameFragmentsSegmentValue")
public interface AIigdNameFragmentsSegmentValue_1 extends eu.algites.lib.naming.convention.AIigNameFragmentsSegmentValue_1, eu.algites.lib.data.smartdataobject.AIiSmartDataObject {
    /** Typed writable access to field Content under the published setter policy. */
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "Content", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.REQUIRED, allowsNull = false, order = 1, minItemCount = 1, maxItemCount = 1, isEmptyAllowed = false, defaultValue = @eu.algites.lib.data.dataobject.AIaDataObjectFieldDefault(kind = eu.algites.lib.data.dataobject.AInDataObjectFieldDefaultKind.DECLARATION_ONLY, value = "", defined = false), setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
        @eu.algites.lib.common.object.props.labels.AIaFieldLabel(label = "Content")
    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "Content", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "Content", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
            @Override eu.algites.lib.naming.convention.AIigNameFragmentsSegmentValueContent_1 getContent();
    void setContent(eu.algites.lib.naming.convention.AIigNameFragmentsSegmentValueContent_1 aContent);
    eu.algites.lib.naming.convention.AIigNameFragmentsSegmentValueContent_1 _getRaw_Content();
    boolean _isPresent_Content();
    void _unset_Content();
    /** Typed writable access to field Kind under the published setter policy. */
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "Kind", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.REQUIRED, allowsNull = false, order = 0, minItemCount = 1, maxItemCount = 1, isEmptyAllowed = false, defaultValue = @eu.algites.lib.data.dataobject.AIaDataObjectFieldDefault(kind = eu.algites.lib.data.dataobject.AInDataObjectFieldDefaultKind.DECLARATION_ONLY, value = "", defined = false), setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
        @eu.algites.lib.common.object.props.labels.AIaFieldLabel(label = "Kind")
    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "Kind", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "Kind", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
            @Override eu.algites.lib.naming.convention.AIngNameFragmentsSegmentValueKind_1 getKind();
    void setKind(eu.algites.lib.naming.convention.AIngNameFragmentsSegmentValueKind_1 aKind);
    eu.algites.lib.naming.convention.AIngNameFragmentsSegmentValueKind_1 _getRaw_Kind();
    boolean _isPresent_Kind();
    void _unset_Kind();
}

package eu.algites.lib.naming.convention.impl.intf;

/** Generated mutable contract derived from AIigNameFragmentsSegmentReference_1. */
@eu.algites.lib.data.dataobject.AIaDataObject(title = "", description = "NameFragmentsSegmentReference", setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentReference", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentReference", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentReference", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameFragmentsSegmentReference")
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameFragmentsSegmentReference")
public interface AIigdNameFragmentsSegmentReference_1 extends eu.algites.lib.naming.convention.AIigNameFragmentsSegmentReference_1, eu.algites.lib.data.smartdataobject.AIiSmartDataObject {
    /** Typed writable access to field Ref under the published setter policy. */
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "Ref", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.REQUIRED, allowsNull = false, order = 0, minItemCount = 1, maxItemCount = 1, isEmptyAllowed = true, defaultValue = @eu.algites.lib.data.dataobject.AIaDataObjectFieldDefault(kind = eu.algites.lib.data.dataobject.AInDataObjectFieldDefaultKind.DECLARATION_ONLY, value = "", defined = false), setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
        @eu.algites.lib.common.object.props.labels.AIaFieldLabel(label = "Ref")
    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "Ref", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "Ref", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
            @Override java.lang.String getRef();
    void setRef(java.lang.String aRef);
    java.lang.String _getRaw_Ref();
    boolean _isPresent_Ref();
    void _unset_Ref();
    void _setEmpty_Ref();
    boolean _isEmpty_Ref();
}

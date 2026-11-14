package eu.algites.lib.naming.convention.impl.intf;

/** Generated mutable contract derived from AIigNameFragment_1. */
@eu.algites.lib.data.dataobject.AIaDataObject(title = "", description = "NameFragment", setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragment", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragment", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragment", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameFragment")
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameFragment")
public interface AIigdNameFragment_1<TSegmentsList extends eu.algites.lib.naming.convention.AIigNameFragmentsSegmentChoice_1> extends eu.algites.lib.naming.convention.AIigNameFragment_1, eu.algites.lib.data.smartdataobject.AIiSmartDataObject {
    /** Typed writable access to field Name under the published setter policy. */
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "name", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.REQUIRED, allowsNull = false, order = 1, minItemCount = 1, maxItemCount = 1, isEmptyAllowed = true, defaultValue = @eu.algites.lib.data.dataobject.AIaDataObjectFieldDefault(kind = eu.algites.lib.data.dataobject.AInDataObjectFieldDefaultKind.DECLARATION_ONLY, value = "", defined = false), setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    @eu.algites.lib.data.dataobject.AIaDataObjectFieldStringConstraints(minLength = 1, maxLength = -1, pattern = "")
        @eu.algites.lib.common.object.props.labels.AIaFieldLabel(label = "Name")
    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "name", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ATTRIBUTE)
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "name", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ATTRIBUTE)
            @Override java.lang.String getName();
    void setName(java.lang.String aName);
    java.lang.String _getRaw_Name();
    boolean _isPresent_Name();
    void _unset_Name();
    void _setEmpty_Name();
    boolean _isEmpty_Name();
    /** Typed writable access to field SegmentsList under the published setter policy. */
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "Segments", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.REQUIRED, allowsNull = false, order = 0, minItemCount = 1, maxItemCount = -1, isEmptyAllowed = false, defaultValue = @eu.algites.lib.data.dataobject.AIaDataObjectFieldDefault(kind = eu.algites.lib.data.dataobject.AInDataObjectFieldDefaultKind.DECLARATION_ONLY, value = "", defined = false), setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    @eu.algites.lib.data.dataobject.AIaDataObjectFieldListConstraints(minItems = 1, maxItems = -1, uniqueItems = false)
        @eu.algites.lib.common.object.props.labels.AIaFieldLabel(label = "Segments list")
    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "Segments", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "Segments", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
            @Override java.util.List<? extends TSegmentsList> getSegmentsList();
    java.util.List<? extends TSegmentsList> _getRaw_SegmentsList();
    boolean _isPresent_SegmentsList();
    void _unset_SegmentsList();
}

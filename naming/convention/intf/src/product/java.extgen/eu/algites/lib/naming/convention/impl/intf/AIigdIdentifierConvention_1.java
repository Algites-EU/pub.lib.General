package eu.algites.lib.naming.convention.impl.intf;

/** Generated mutable contract derived from AIigIdentifierConvention_1. */
@eu.algites.lib.data.dataobject.AIaDataObject(title = "", description = "IdentifierConvention", setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConvention", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConvention", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConvention", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "IdentifierConvention")
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "IdentifierConvention")
public interface AIigdIdentifierConvention_1 extends eu.algites.lib.naming.convention.AIigIdentifierConvention_1, eu.algites.lib.data.smartdataobject.AIiSmartDataObject {
    /** Typed writable access to field ConversionStyle under the published setter policy. */
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "ConversionStyle", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.ALLOWED, allowsNull = false, order = 1, minItemCount = 0, maxItemCount = 1, isEmptyAllowed = false, defaultValue = @eu.algites.lib.data.dataobject.AIaDataObjectFieldDefault(kind = eu.algites.lib.data.dataobject.AInDataObjectFieldDefaultKind.DECLARATION_ONLY, value = "", defined = false), setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
        @eu.algites.lib.common.object.props.labels.AIaFieldLabel(label = "Conversion style")
    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "ConversionStyle", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "ConversionStyle", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
            @Override eu.algites.lib.naming.convention.AIngIdentifierConversionStyle_1 getConversionStyle();
    void setConversionStyle(eu.algites.lib.naming.convention.AIngIdentifierConversionStyle_1 aConversionStyle);
    eu.algites.lib.naming.convention.AIngIdentifierConversionStyle_1 _getRaw_ConversionStyle();
    boolean _isPresent_ConversionStyle();
    void _unset_ConversionStyle();
    /** Typed writable access to field Name under the published setter policy. */
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "name", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.REQUIRED, allowsNull = false, order = 2, minItemCount = 1, maxItemCount = 1, isEmptyAllowed = true, defaultValue = @eu.algites.lib.data.dataobject.AIaDataObjectFieldDefault(kind = eu.algites.lib.data.dataobject.AInDataObjectFieldDefaultKind.DECLARATION_ONLY, value = "", defined = false), setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
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
    /** Typed writable access to field Regexp under the published setter policy. */
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "Regexp", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.REQUIRED, allowsNull = false, order = 0, minItemCount = 1, maxItemCount = 1, isEmptyAllowed = true, defaultValue = @eu.algites.lib.data.dataobject.AIaDataObjectFieldDefault(kind = eu.algites.lib.data.dataobject.AInDataObjectFieldDefaultKind.DECLARATION_ONLY, value = "", defined = false), setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
        @eu.algites.lib.common.object.props.labels.AIaFieldLabel(label = "Regexp")
    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "Regexp", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "Regexp", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
            @Override java.lang.String getRegexp();
    void setRegexp(java.lang.String aRegexp);
    java.lang.String _getRaw_Regexp();
    boolean _isPresent_Regexp();
    void _unset_Regexp();
    void _setEmpty_Regexp();
    boolean _isEmpty_Regexp();
}

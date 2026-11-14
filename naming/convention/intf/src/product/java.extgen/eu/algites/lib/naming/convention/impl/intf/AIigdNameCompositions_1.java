package eu.algites.lib.naming.convention.impl.intf;

/** Generated mutable contract derived from AIigNameCompositions_1. */
@eu.algites.lib.data.dataobject.AIaDataObject(title = "", description = "NameCompositions", setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameCompositions")
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameCompositions")
public interface AIigdNameCompositions_1<TNameCompositionList extends eu.algites.lib.naming.convention.impl.intf.AIigdNameComposition_1<?>> extends eu.algites.lib.naming.convention.AIigNameCompositions_1, eu.algites.lib.data.smartdataobject.AIiSmartDataObject {
    /** Typed writable access to field NameCompositionList under the published setter policy. */
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "NameComposition", presence = eu.algites.lib.data.dataobject.AInFieldPresenceKind.ALLOWED, allowsNull = false, order = 0, minItemCount = 0, maxItemCount = -1, isEmptyAllowed = false, defaultValue = @eu.algites.lib.data.dataobject.AIaDataObjectFieldDefault(kind = eu.algites.lib.data.dataobject.AInDataObjectFieldDefaultKind.DECLARATION_ONLY, value = "", defined = false), setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
        @eu.algites.lib.common.object.props.labels.AIaFieldLabel(label = "Name composition list")
    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "NameComposition", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
@eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "NameComposition", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
            @Override eu.algites.lib.common.list.AIiMultiMappedList<? extends TNameCompositionList> getNameCompositionList();
    eu.algites.lib.common.list.AIiMultiMappedList<? extends TNameCompositionList> _getRaw_NameCompositionList();
    boolean _isPresent_NameCompositionList();
    void _unset_NameCompositionList();
}

package eu.algites.lib.naming.convention.impl;

/** Generated mutable implementation of AIigVersionConventions_1. */
@eu.algites.lib.data.dataobject.AIaDataObject(title = "", description = "VersionConventions", setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
@eu.algites.lib.data.dataobject.jsondefs.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/VersionConventions", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.yamldefs.AIaDataObjectYamlSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/VersionConventions", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.xmldefs.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "VersionConventions")
@eu.algites.lib.data.smartdataobject.impl.common.AIaSmartDataObjectImplementationBinding(readContracts = {eu.algites.lib.naming.convention.AIigVersionConventions_1.class})
public class AIcgdVersionConventions_1 extends eu.algites.lib.data.smartdataobject.impl.common.AIcSmartDataObject implements eu.algites.lib.naming.convention.impl.intf.AIigdVersionConventions_1<eu.algites.lib.naming.convention.impl.intf.AIigdVersionConvention_1> {
    /** Create an object whose raw fields are initially absent. */
    public AIcgdVersionConventions_1() { super(); }
    /** Reconstruct an identity-preserving typed SmartDataObject graph. */
    public AIcgdVersionConventions_1(java.util.Map<String, eu.algites.lib.data.smartdataobject.AIidVirtualFieldValue> aMapping) { super(aMapping); }
    @Override public eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdVersionConvention_1> getVersionConventionList() { return (eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdVersionConvention_1>) get_OrCreateCollectionField("VersionConventionList", true); }
    @Override public eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdVersionConvention_1> _getRaw_VersionConventionList() { return (eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdVersionConvention_1>) get_RawField("VersionConventionList"); }
    @Override public boolean _isPresent_VersionConventionList() { return isPresent_Field("VersionConventionList"); }
    @Override public void _unset_VersionConventionList() { unset_Field("VersionConventionList"); }
}

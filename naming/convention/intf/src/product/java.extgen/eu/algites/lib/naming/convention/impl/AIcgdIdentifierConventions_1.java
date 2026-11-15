package eu.algites.lib.naming.convention.impl;

/** Generated mutable implementation of AIigIdentifierConventions_1. */
@eu.algites.lib.data.dataobject.AIaDataObject(title = "", description = "IdentifierConventions", setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
@eu.algites.lib.data.dataobject.jsondefs.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConventions", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.yamldefs.AIaDataObjectYamlSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConventions", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.xmldefs.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "IdentifierConventions")
@eu.algites.lib.data.smartdataobject.impl.common.AIaSmartDataObjectImplementationBinding(readContracts = {eu.algites.lib.naming.convention.AIigIdentifierConventions_1.class})
public class AIcgdIdentifierConventions_1 extends eu.algites.lib.data.smartdataobject.impl.common.AIcSmartDataObject implements eu.algites.lib.naming.convention.impl.intf.AIigdIdentifierConventions_1<eu.algites.lib.naming.convention.impl.intf.AIigdIdentifierConvention_1> {
    /** Create an object whose raw fields are initially absent. */
    public AIcgdIdentifierConventions_1() { super(); }
    /** Reconstruct an identity-preserving typed SmartDataObject graph. */
    public AIcgdIdentifierConventions_1(java.util.Map<String, eu.algites.lib.data.smartdataobject.AIidVirtualFieldValue> aMapping) { super(aMapping); }
    @Override public eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdIdentifierConvention_1> getIdentifierConventionList() { return (eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdIdentifierConvention_1>) get_OrCreateCollectionField("IdentifierConventionList", true); }
    @Override public eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdIdentifierConvention_1> _getRaw_IdentifierConventionList() { return (eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdIdentifierConvention_1>) get_RawField("IdentifierConventionList"); }
    @Override public boolean _isPresent_IdentifierConventionList() { return isPresent_Field("IdentifierConventionList"); }
    @Override public void _unset_IdentifierConventionList() { unset_Field("IdentifierConventionList"); }
}

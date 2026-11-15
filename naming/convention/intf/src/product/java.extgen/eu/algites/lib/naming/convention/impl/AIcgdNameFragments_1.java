package eu.algites.lib.naming.convention.impl;

/** Generated mutable implementation of AIigNameFragments_1. */
@eu.algites.lib.data.dataobject.AIaDataObject(title = "", description = "NameFragments", setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
@eu.algites.lib.data.dataobject.jsondefs.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragments", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.yamldefs.AIaDataObjectYamlSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragments", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.xmldefs.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameFragments")
@eu.algites.lib.data.smartdataobject.impl.common.AIaSmartDataObjectImplementationBinding(readContracts = {eu.algites.lib.naming.convention.AIigNameFragments_1.class})
public class AIcgdNameFragments_1 extends eu.algites.lib.data.smartdataobject.impl.common.AIcSmartDataObject implements eu.algites.lib.naming.convention.impl.intf.AIigdNameFragments_1<eu.algites.lib.naming.convention.impl.intf.AIigdNameFragment_1<?>> {
    /** Create an object whose raw fields are initially absent. */
    public AIcgdNameFragments_1() { super(); }
    /** Reconstruct an identity-preserving typed SmartDataObject graph. */
    public AIcgdNameFragments_1(java.util.Map<String, eu.algites.lib.data.smartdataobject.AIidVirtualFieldValue> aMapping) { super(aMapping); }
    @Override public eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdNameFragment_1<?>> getNameFragmentList() { return (eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdNameFragment_1<?>>) get_OrCreateCollectionField("NameFragmentList", true); }
    @Override public eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdNameFragment_1<?>> _getRaw_NameFragmentList() { return (eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdNameFragment_1<?>>) get_RawField("NameFragmentList"); }
    @Override public boolean _isPresent_NameFragmentList() { return isPresent_Field("NameFragmentList"); }
    @Override public void _unset_NameFragmentList() { unset_Field("NameFragmentList"); }
}

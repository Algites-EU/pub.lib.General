package eu.algites.lib.naming.convention.impl;

/** Generated mutable implementation of AIigNameCompositions_1. */
@eu.algites.lib.data.dataobject.AIaDataObject(title = "", description = "NameCompositions", setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
@eu.algites.lib.data.dataobject.jsondefs.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.yamldefs.AIaDataObjectYamlSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.xmldefs.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameCompositions")
@eu.algites.lib.data.smartdataobject.impl.common.AIaSmartDataObjectImplementationBinding(readContracts = {eu.algites.lib.naming.convention.AIigNameCompositions_1.class})
public class AIcgdNameCompositions_1 extends eu.algites.lib.data.smartdataobject.impl.common.AIcSmartDataObject implements eu.algites.lib.naming.convention.impl.intf.AIigdNameCompositions_1<eu.algites.lib.naming.convention.impl.intf.AIigdNameComposition_1<?>> {
    /** Create an object whose raw fields are initially absent. */
    public AIcgdNameCompositions_1() { super(); }
    /** Reconstruct an identity-preserving typed SmartDataObject graph. */
    public AIcgdNameCompositions_1(java.util.Map<String, eu.algites.lib.data.smartdataobject.AIidVirtualFieldValue> aMapping) { super(aMapping); }
    @Override public eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdNameComposition_1<?>> getNameCompositionList() { return (eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdNameComposition_1<?>>) get_OrCreateCollectionField("NameCompositionList", true); }
    @Override public eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdNameComposition_1<?>> _getRaw_NameCompositionList() { return (eu.algites.lib.common.list.AIiMultiMappedList<? extends eu.algites.lib.naming.convention.impl.intf.AIigdNameComposition_1<?>>) get_RawField("NameCompositionList"); }
    @Override public boolean _isPresent_NameCompositionList() { return isPresent_Field("NameCompositionList"); }
    @Override public void _unset_NameCompositionList() { unset_Field("NameCompositionList"); }
}

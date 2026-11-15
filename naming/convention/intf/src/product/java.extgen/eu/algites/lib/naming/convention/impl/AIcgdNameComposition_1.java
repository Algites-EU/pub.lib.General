package eu.algites.lib.naming.convention.impl;

/** Generated mutable implementation of AIigNameComposition_1. */
@eu.algites.lib.data.dataobject.AIaDataObject(title = "", description = "NameComposition", setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
@eu.algites.lib.data.dataobject.jsondefs.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameComposition", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.yamldefs.AIaDataObjectYamlSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameComposition", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.xmldefs.AIaDataObjectXmlSchema(definitionKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind.TYPE, namespace = "https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd", namespaceLocalName = "NameComposition")
@eu.algites.lib.data.smartdataobject.impl.common.AIaSmartDataObjectImplementationBinding(readContracts = {eu.algites.lib.naming.convention.AIigNameComposition_1.class})
public class AIcgdNameComposition_1 extends eu.algites.lib.data.smartdataobject.impl.common.AIcSmartDataObject implements eu.algites.lib.naming.convention.impl.intf.AIigdNameComposition_1<eu.algites.lib.naming.convention.AIigNameFragmentsSegmentChoice_1> {
    /** Create an object whose raw fields are initially absent. */
    public AIcgdNameComposition_1() { super(); }
    /** Reconstruct an identity-preserving typed SmartDataObject graph. */
    public AIcgdNameComposition_1(java.util.Map<String, eu.algites.lib.data.smartdataobject.AIidVirtualFieldValue> aMapping) { super(aMapping); }
    @Override public java.lang.String getName() { return (java.lang.String) get_EffectiveField("Name"); }
    @Override public void setName(java.lang.String aName) { set_RawField("Name", aName); }
    @Override public java.lang.String _getRaw_Name() { return (java.lang.String) get_RawField("Name"); }
    @Override public boolean _isPresent_Name() { return isPresent_Field("Name"); }
    @Override public void _unset_Name() { unset_Field("Name"); }
    @Override public void _setEmpty_Name() { set_EmptyField("Name"); }
    @Override public boolean _isEmpty_Name() { return isEmpty_Field("Name"); }
    @Override public eu.algites.lib.common.list.AIiBoundedList<? extends eu.algites.lib.naming.convention.AIigNameFragmentsSegmentChoice_1> getSegmentsList() { return (eu.algites.lib.common.list.AIiBoundedList<? extends eu.algites.lib.naming.convention.AIigNameFragmentsSegmentChoice_1>) get_OrCreateCollectionField("SegmentsList", false); }
    @Override public eu.algites.lib.common.list.AIiBoundedList<? extends eu.algites.lib.naming.convention.AIigNameFragmentsSegmentChoice_1> _getRaw_SegmentsList() { return (eu.algites.lib.common.list.AIiBoundedList<? extends eu.algites.lib.naming.convention.AIigNameFragmentsSegmentChoice_1>) get_RawField("SegmentsList"); }
    @Override public boolean _isPresent_SegmentsList() { return isPresent_Field("SegmentsList"); }
    @Override public void _unset_SegmentsList() { unset_Field("SegmentsList"); }
}

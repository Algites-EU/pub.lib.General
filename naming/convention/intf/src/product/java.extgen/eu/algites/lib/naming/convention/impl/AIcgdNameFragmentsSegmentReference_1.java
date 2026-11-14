package eu.algites.lib.naming.convention.impl;

/** Generated mutable implementation of AIigNameFragmentsSegmentReference_1. */
@eu.algites.lib.data.dataobject.AIaDataObject(title = "", description = "NameFragmentsSegmentReference", setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentReference", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentReference", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentReference", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.smartdataobject.impl.common.AIaSmartDataObjectImplementationBinding(readContracts = {eu.algites.lib.naming.convention.AIigNameFragmentsSegmentReference_1.class})
public class AIcgdNameFragmentsSegmentReference_1 extends eu.algites.lib.data.smartdataobject.impl.common.AIcSmartDataObject implements eu.algites.lib.naming.convention.impl.intf.AIigdNameFragmentsSegmentReference_1 {
    /** Create an object whose raw fields are initially absent. */
    public AIcgdNameFragmentsSegmentReference_1() { super(); }
    /** Reconstruct an identity-preserving typed SmartDataObject graph. */
    public AIcgdNameFragmentsSegmentReference_1(java.util.Map<String, eu.algites.lib.data.smartdataobject.AIidVirtualFieldValue> aMapping) { super(aMapping); }
    @Override public java.lang.String getRef() { return (java.lang.String) get_EffectiveField("Ref"); }
    @Override public void setRef(java.lang.String aRef) { set_RawField("Ref", aRef); }
    @Override public java.lang.String _getRaw_Ref() { return (java.lang.String) get_RawField("Ref"); }
    @Override public boolean _isPresent_Ref() { return isPresent_Field("Ref"); }
    @Override public void _unset_Ref() { unset_Field("Ref"); }
    @Override public void _setEmpty_Ref() { set_EmptyField("Ref"); }
    @Override public boolean _isEmpty_Ref() { return isEmpty_Field("Ref"); }
}

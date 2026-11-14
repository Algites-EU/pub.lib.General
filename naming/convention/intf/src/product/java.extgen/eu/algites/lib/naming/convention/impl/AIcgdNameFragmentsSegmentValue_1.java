package eu.algites.lib.naming.convention.impl;

/** Generated mutable implementation of AIigNameFragmentsSegmentValue_1. */
@eu.algites.lib.data.dataobject.AIaDataObject(title = "", description = "NameFragmentsSegmentValue", setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentValue", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentValue", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentValue", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.smartdataobject.impl.common.AIaSmartDataObjectImplementationBinding(readContracts = {eu.algites.lib.naming.convention.AIigNameFragmentsSegmentValue_1.class})
public class AIcgdNameFragmentsSegmentValue_1 extends eu.algites.lib.data.smartdataobject.impl.common.AIcSmartDataObject implements eu.algites.lib.naming.convention.impl.intf.AIigdNameFragmentsSegmentValue_1 {
    /** Create an object whose raw fields are initially absent. */
    public AIcgdNameFragmentsSegmentValue_1() { super(); }
    /** Reconstruct an identity-preserving typed SmartDataObject graph. */
    public AIcgdNameFragmentsSegmentValue_1(java.util.Map<String, eu.algites.lib.data.smartdataobject.AIidVirtualFieldValue> aMapping) { super(aMapping); }
    @Override public eu.algites.lib.naming.convention.AIigNameFragmentsSegmentValueContent_1 getContent() { return (eu.algites.lib.naming.convention.AIigNameFragmentsSegmentValueContent_1) get_EffectiveField("Content"); }
    @Override public void setContent(eu.algites.lib.naming.convention.AIigNameFragmentsSegmentValueContent_1 aContent) { set_RawField("Content", aContent); }
    @Override public eu.algites.lib.naming.convention.AIigNameFragmentsSegmentValueContent_1 _getRaw_Content() { return (eu.algites.lib.naming.convention.AIigNameFragmentsSegmentValueContent_1) get_RawField("Content"); }
    @Override public boolean _isPresent_Content() { return isPresent_Field("Content"); }
    @Override public void _unset_Content() { unset_Field("Content"); }
    @Override public eu.algites.lib.naming.convention.AIngNameFragmentsSegmentValueKind_1 getKind() { return (eu.algites.lib.naming.convention.AIngNameFragmentsSegmentValueKind_1) get_EffectiveField("Kind"); }
    @Override public void setKind(eu.algites.lib.naming.convention.AIngNameFragmentsSegmentValueKind_1 aKind) { set_RawField("Kind", aKind); }
    @Override public eu.algites.lib.naming.convention.AIngNameFragmentsSegmentValueKind_1 _getRaw_Kind() { return (eu.algites.lib.naming.convention.AIngNameFragmentsSegmentValueKind_1) get_RawField("Kind"); }
    @Override public boolean _isPresent_Kind() { return isPresent_Field("Kind"); }
    @Override public void _unset_Kind() { unset_Field("Kind"); }
}

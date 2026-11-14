package eu.algites.lib.naming.convention.impl;

/** Generated mutable implementation of AIigIdentifierConvention_1. */
@eu.algites.lib.data.dataobject.AIaDataObject(title = "", description = "IdentifierConvention", setterInheritancePolicy = eu.algites.lib.data.dataobject.AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
@eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConvention", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConvention", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema(schemaId = "eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConvention", schemaVersion = 1, schemaUrl = "", dataSourceSchemaUrlField = "", dataSourceSchemaIdentifierField = "", dataSourceSchemaVersionField = "")
@eu.algites.lib.data.smartdataobject.impl.common.AIaSmartDataObjectImplementationBinding(readContracts = {eu.algites.lib.naming.convention.AIigIdentifierConvention_1.class})
public class AIcgdIdentifierConvention_1 extends eu.algites.lib.data.smartdataobject.impl.common.AIcSmartDataObject implements eu.algites.lib.naming.convention.impl.intf.AIigdIdentifierConvention_1 {
    /** Create an object whose raw fields are initially absent. */
    public AIcgdIdentifierConvention_1() { super(); }
    /** Reconstruct an identity-preserving typed SmartDataObject graph. */
    public AIcgdIdentifierConvention_1(java.util.Map<String, eu.algites.lib.data.smartdataobject.AIidVirtualFieldValue> aMapping) { super(aMapping); }
    @Override public eu.algites.lib.naming.convention.AIngIdentifierConversionStyle_1 getConversionStyle() { return (eu.algites.lib.naming.convention.AIngIdentifierConversionStyle_1) get_EffectiveField("ConversionStyle"); }
    @Override public void setConversionStyle(eu.algites.lib.naming.convention.AIngIdentifierConversionStyle_1 aConversionStyle) { set_RawField("ConversionStyle", aConversionStyle); }
    @Override public eu.algites.lib.naming.convention.AIngIdentifierConversionStyle_1 _getRaw_ConversionStyle() { return (eu.algites.lib.naming.convention.AIngIdentifierConversionStyle_1) get_RawField("ConversionStyle"); }
    @Override public boolean _isPresent_ConversionStyle() { return isPresent_Field("ConversionStyle"); }
    @Override public void _unset_ConversionStyle() { unset_Field("ConversionStyle"); }
    @Override public java.lang.String getName() { return (java.lang.String) get_EffectiveField("Name"); }
    @Override public void setName(java.lang.String aName) { set_RawField("Name", aName); }
    @Override public java.lang.String _getRaw_Name() { return (java.lang.String) get_RawField("Name"); }
    @Override public boolean _isPresent_Name() { return isPresent_Field("Name"); }
    @Override public void _unset_Name() { unset_Field("Name"); }
    @Override public void _setEmpty_Name() { set_EmptyField("Name"); }
    @Override public boolean _isEmpty_Name() { return isEmpty_Field("Name"); }
    @Override public java.lang.String getRegexp() { return (java.lang.String) get_EffectiveField("Regexp"); }
    @Override public void setRegexp(java.lang.String aRegexp) { set_RawField("Regexp", aRegexp); }
    @Override public java.lang.String _getRaw_Regexp() { return (java.lang.String) get_RawField("Regexp"); }
    @Override public boolean _isPresent_Regexp() { return isPresent_Field("Regexp"); }
    @Override public void _unset_Regexp() { unset_Field("Regexp"); }
    @Override public void _setEmpty_Regexp() { set_EmptyField("Regexp"); }
    @Override public boolean _isEmpty_Regexp() { return isEmpty_Field("Regexp"); }
}

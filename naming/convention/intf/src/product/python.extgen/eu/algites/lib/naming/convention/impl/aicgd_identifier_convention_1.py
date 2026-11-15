"""Generated SmartDataObject implementation for AIigIdentifierConvention_1."""
from __future__ import annotations
from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.data.dataobject.ain_field_setter_inheritance_policy import AInFieldSetterInheritancePolicy
from eu.algites.lib.data.dataobject.yamldefs.aia_data_object_yaml_schema import AIaDataObjectYamlSchema
from eu.algites.lib.data.dataobject.jsondefs.aia_data_object_json_schema import AIaDataObjectJsonSchema
from eu.algites.lib.naming.convention.impl.intf.aiigd_identifier_convention_1 import AIigdIdentifierConvention_1
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.common.aia_smart_data_object_implementation_binding import AIaSmartDataObjectImplementationBinding
from eu.algites.lib.naming.convention.aiig_identifier_convention_1 import AIigIdentifierConvention_1


@AIaSmartDataObjectImplementationBinding(readContracts=(AIigIdentifierConvention_1,))
class AIcgdIdentifierConvention_1(AIcSmartDataObject, AIigdIdentifierConvention_1):
    __data_object__ = AIcdDataObject(setter_inheritance_policy=AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    """Generated implementation; inherited fields use parent behavior."""
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConvention', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __yaml_schema__ = AIaDataObjectYamlSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConvention', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')

    def getRegexp(self):
        return self.get_EffectiveField('Regexp')

    def setRegexp(self, a_value):
        self.set_RawField('Regexp', a_value)

    def _getRaw_Regexp(self):
        return self.get_RawField('Regexp')

    def _isPresent_Regexp(self):
        return self.isPresent_Field('Regexp')

    def _unset_Regexp(self):
        self.unset_Field('Regexp')

    def _setEmpty_Regexp(self):
        self.set_EmptyField('Regexp')

    def _isEmpty_Regexp(self):
        return self.isEmpty_Field('Regexp')

    def getConversionStyle(self):
        return self.get_EffectiveField('ConversionStyle')

    def setConversionStyle(self, a_value):
        self.set_RawField('ConversionStyle', a_value)

    def _getRaw_ConversionStyle(self):
        return self.get_RawField('ConversionStyle')

    def _isPresent_ConversionStyle(self):
        return self.isPresent_Field('ConversionStyle')

    def _unset_ConversionStyle(self):
        self.unset_Field('ConversionStyle')

    def getName(self):
        return self.get_EffectiveField('Name')

    def setName(self, a_value):
        self.set_RawField('Name', a_value)

    def _getRaw_Name(self):
        return self.get_RawField('Name')

    def _isPresent_Name(self):
        return self.isPresent_Field('Name')

    def _unset_Name(self):
        self.unset_Field('Name')

    def _setEmpty_Name(self):
        self.set_EmptyField('Name')

    def _isEmpty_Name(self):
        return self.isEmpty_Field('Name')

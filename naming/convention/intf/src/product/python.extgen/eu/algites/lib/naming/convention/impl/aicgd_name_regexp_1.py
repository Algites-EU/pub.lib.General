"""Generated SmartDataObject implementation for AIigNameRegexp_1."""
from __future__ import annotations
from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.data.dataobject.ain_field_setter_inheritance_policy import AInFieldSetterInheritancePolicy
from eu.algites.lib.data.dataobject.yamldefs.aia_yaml_data_object import AIaYamlDataObject
from eu.algites.lib.data.dataobject.jsondefs.aia_json_data_object import AIaJsonDataObject
from eu.algites.lib.data.dataobject.jsonschema.aia_data_object_json_schema import AIaDataObjectJsonSchema
from eu.algites.lib.naming.convention.impl.intf.aiigd_name_regexp_1 import AIigdNameRegexp_1
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.common.aia_smart_data_object_implementation_binding import AIaSmartDataObjectImplementationBinding
from eu.algites.lib.naming.convention.aiig_name_regexp_1 import AIigNameRegexp_1


@AIaSmartDataObjectImplementationBinding(readContracts=(AIigNameRegexp_1,))
class AIcgdNameRegexp_1(AIcSmartDataObject, AIigdNameRegexp_1):
    __data_object__ = AIcdDataObject(setter_inheritance_policy=AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    """Generated implementation; inherited fields use parent behavior."""
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameRegexp', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __json_data_object__ = AIaJsonDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameRegexp', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __yaml_data_object__ = AIaYamlDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameRegexp', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')

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

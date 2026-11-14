"""Generated SmartDataObject implementation for AIigNameFragmentsSegmentReference_1."""
from __future__ import annotations
from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.data.dataobject.ain_field_setter_inheritance_policy import AInFieldSetterInheritancePolicy
from eu.algites.lib.data.dataobject.yamldefs.aia_yaml_data_object import AIaYamlDataObject
from eu.algites.lib.data.dataobject.jsondefs.aia_json_data_object import AIaJsonDataObject
from eu.algites.lib.data.dataobject.jsonschema.aia_data_object_json_schema import AIaDataObjectJsonSchema
from eu.algites.lib.naming.convention.impl.intf.aiigd_name_fragments_segment_reference_1 import AIigdNameFragmentsSegmentReference_1
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.common.aia_smart_data_object_implementation_binding import AIaSmartDataObjectImplementationBinding
from eu.algites.lib.naming.convention.aiig_name_fragments_segment_reference_1 import AIigNameFragmentsSegmentReference_1


@AIaSmartDataObjectImplementationBinding(readContracts=(AIigNameFragmentsSegmentReference_1,))
class AIcgdNameFragmentsSegmentReference_1(AIcSmartDataObject, AIigdNameFragmentsSegmentReference_1):
    __data_object__ = AIcdDataObject(setter_inheritance_policy=AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    """Generated implementation; inherited fields use parent behavior."""
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentReference', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __json_data_object__ = AIaJsonDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentReference', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __yaml_data_object__ = AIaYamlDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentReference', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')

    def getRef(self):
        return self.get_EffectiveField('Ref')

    def setRef(self, a_value):
        self.set_RawField('Ref', a_value)

    def _getRaw_Ref(self):
        return self.get_RawField('Ref')

    def _isPresent_Ref(self):
        return self.isPresent_Field('Ref')

    def _unset_Ref(self):
        self.unset_Field('Ref')

    def _setEmpty_Ref(self):
        self.set_EmptyField('Ref')

    def _isEmpty_Ref(self):
        return self.isEmpty_Field('Ref')

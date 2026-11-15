"""Generated SmartDataObject implementation for AIigNameFragmentsSegmentValue_1."""
from __future__ import annotations
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from eu.algites.lib.naming.convention.aiig_name_fragments_segment_value_content_1 import AIigNameFragmentsSegmentValueContent_1
from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.data.dataobject.ain_field_setter_inheritance_policy import AInFieldSetterInheritancePolicy
from eu.algites.lib.data.dataobject.yamldefs.aia_data_object_yaml_schema import AIaDataObjectYamlSchema
from eu.algites.lib.data.dataobject.jsondefs.aia_data_object_json_schema import AIaDataObjectJsonSchema
from eu.algites.lib.naming.convention.impl.intf.aiigd_name_fragments_segment_value_1 import AIigdNameFragmentsSegmentValue_1
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.common.aia_smart_data_object_implementation_binding import AIaSmartDataObjectImplementationBinding
from eu.algites.lib.naming.convention.aiig_name_fragments_segment_value_1 import AIigNameFragmentsSegmentValue_1


@AIaSmartDataObjectImplementationBinding(readContracts=(AIigNameFragmentsSegmentValue_1,))
class AIcgdNameFragmentsSegmentValue_1(AIcSmartDataObject, AIigdNameFragmentsSegmentValue_1):
    __data_object__ = AIcdDataObject(setter_inheritance_policy=AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    """Generated implementation; inherited fields use parent behavior."""
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentValue', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __yaml_schema__ = AIaDataObjectYamlSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentValue', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')

    def getKind(self):
        return self.get_EffectiveField('Kind')

    def setKind(self, a_value):
        self.set_RawField('Kind', a_value)

    def _getRaw_Kind(self):
        return self.get_RawField('Kind')

    def _isPresent_Kind(self):
        return self.isPresent_Field('Kind')

    def _unset_Kind(self):
        self.unset_Field('Kind')

    def getContent(self) -> AIigNameFragmentsSegmentValueContent_1:
        return self.get_EffectiveField('Content')

    def setContent(self, a_value: AIigNameFragmentsSegmentValueContent_1):
        self.set_RawField('Content', a_value)

    def _getRaw_Content(self) -> AIigNameFragmentsSegmentValueContent_1:
        return self.get_RawField('Content')

    def _isPresent_Content(self):
        return self.isPresent_Field('Content')

    def _unset_Content(self):
        self.unset_Field('Content')

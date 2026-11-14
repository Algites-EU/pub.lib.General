"""Generated SmartDataObject implementation for AIigNameCompositions_1."""
from __future__ import annotations
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from eu.algites.lib.common.list.aii_multi_mapped_list import AIiMultiMappedList
    from eu.algites.lib.naming.convention.impl.intf.aiigd_name_composition_1 import AIigdNameComposition_1
from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.data.dataobject.ain_field_setter_inheritance_policy import AInFieldSetterInheritancePolicy
from eu.algites.lib.data.dataobject.yamldefs.aia_yaml_data_object import AIaYamlDataObject
from eu.algites.lib.data.dataobject.jsondefs.aia_json_data_object import AIaJsonDataObject
from eu.algites.lib.data.dataobject.jsonschema.aia_data_object_json_schema import AIaDataObjectJsonSchema
from eu.algites.lib.naming.convention.impl.intf.aiigd_name_compositions_1 import AIigdNameCompositions_1
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.common.aia_smart_data_object_implementation_binding import AIaSmartDataObjectImplementationBinding
from eu.algites.lib.naming.convention.aiig_name_compositions_1 import AIigNameCompositions_1


@AIaSmartDataObjectImplementationBinding(readContracts=(AIigNameCompositions_1,))
class AIcgdNameCompositions_1(AIcSmartDataObject, AIigdNameCompositions_1['AIigdNameComposition_1']):
    __data_object__ = AIcdDataObject(setter_inheritance_policy=AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    """Generated implementation; inherited fields use parent behavior."""
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __json_data_object__ = AIaJsonDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __yaml_data_object__ = AIaYamlDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')

    def getNameCompositionList(self) -> AIiMultiMappedList[AIigdNameComposition_1]:
        return self.get_OrCreateCollectionField('NameCompositionList', True)

    def _getRaw_NameCompositionList(self) -> AIiMultiMappedList[AIigdNameComposition_1]:
        return self.get_RawField('NameCompositionList')

    def _isPresent_NameCompositionList(self):
        return self.isPresent_Field('NameCompositionList')

    def _unset_NameCompositionList(self):
        self.unset_Field('NameCompositionList')

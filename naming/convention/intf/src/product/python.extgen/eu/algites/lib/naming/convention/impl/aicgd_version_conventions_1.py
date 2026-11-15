"""Generated SmartDataObject implementation for AIigVersionConventions_1."""
from __future__ import annotations
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from eu.algites.lib.common.list.aii_multi_mapped_list import AIiMultiMappedList
    from eu.algites.lib.naming.convention.impl.intf.aiigd_version_convention_1 import AIigdVersionConvention_1
from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.data.dataobject.ain_field_setter_inheritance_policy import AInFieldSetterInheritancePolicy
from eu.algites.lib.data.dataobject.yamldefs.aia_data_object_yaml_schema import AIaDataObjectYamlSchema
from eu.algites.lib.data.dataobject.jsondefs.aia_data_object_json_schema import AIaDataObjectJsonSchema
from eu.algites.lib.naming.convention.impl.intf.aiigd_version_conventions_1 import AIigdVersionConventions_1
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.common.aia_smart_data_object_implementation_binding import AIaSmartDataObjectImplementationBinding
from eu.algites.lib.naming.convention.aiig_version_conventions_1 import AIigVersionConventions_1


@AIaSmartDataObjectImplementationBinding(readContracts=(AIigVersionConventions_1,))
class AIcgdVersionConventions_1(AIcSmartDataObject, AIigdVersionConventions_1['AIigdVersionConvention_1']):
    __data_object__ = AIcdDataObject(setter_inheritance_policy=AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    """Generated implementation; inherited fields use parent behavior."""
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/VersionConventions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __yaml_schema__ = AIaDataObjectYamlSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/VersionConventions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')

    def getVersionConventionList(self) -> AIiMultiMappedList[AIigdVersionConvention_1]:
        return self.get_OrCreateCollectionField('VersionConventionList', True)

    def _getRaw_VersionConventionList(self) -> AIiMultiMappedList[AIigdVersionConvention_1]:
        return self.get_RawField('VersionConventionList')

    def _isPresent_VersionConventionList(self):
        return self.isPresent_Field('VersionConventionList')

    def _unset_VersionConventionList(self):
        self.unset_Field('VersionConventionList')

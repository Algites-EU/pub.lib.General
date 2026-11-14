"""Generated SmartDataObject implementation for AIigIdentifierConventions_1."""
from __future__ import annotations
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from eu.algites.lib.common.list.aii_multi_mapped_list import AIiMultiMappedList
    from eu.algites.lib.naming.convention.impl.intf.aiigd_identifier_convention_1 import AIigdIdentifierConvention_1
from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.data.dataobject.ain_field_setter_inheritance_policy import AInFieldSetterInheritancePolicy
from eu.algites.lib.data.dataobject.yamldefs.aia_yaml_data_object import AIaYamlDataObject
from eu.algites.lib.data.dataobject.jsondefs.aia_json_data_object import AIaJsonDataObject
from eu.algites.lib.data.dataobject.jsonschema.aia_data_object_json_schema import AIaDataObjectJsonSchema
from eu.algites.lib.naming.convention.impl.intf.aiigd_identifier_conventions_1 import AIigdIdentifierConventions_1
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.common.aia_smart_data_object_implementation_binding import AIaSmartDataObjectImplementationBinding
from eu.algites.lib.naming.convention.aiig_identifier_conventions_1 import AIigIdentifierConventions_1


@AIaSmartDataObjectImplementationBinding(readContracts=(AIigIdentifierConventions_1,))
class AIcgdIdentifierConventions_1(AIcSmartDataObject, AIigdIdentifierConventions_1['AIigdIdentifierConvention_1']):
    __data_object__ = AIcdDataObject(setter_inheritance_policy=AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    """Generated implementation; inherited fields use parent behavior."""
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConventions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __json_data_object__ = AIaJsonDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConventions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __yaml_data_object__ = AIaYamlDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConventions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')

    def getIdentifierConventionList(self) -> AIiMultiMappedList[AIigdIdentifierConvention_1]:
        return self.get_OrCreateCollectionField('IdentifierConventionList', True)

    def _getRaw_IdentifierConventionList(self) -> AIiMultiMappedList[AIigdIdentifierConvention_1]:
        return self.get_RawField('IdentifierConventionList')

    def _isPresent_IdentifierConventionList(self):
        return self.isPresent_Field('IdentifierConventionList')

    def _unset_IdentifierConventionList(self):
        self.unset_Field('IdentifierConventionList')

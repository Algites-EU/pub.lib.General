from __future__ import annotations
from typing import Any

from abc import ABC, abstractmethod
from typing import TYPE_CHECKING
from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.data.dataobject.aia_data_object_field import AIaDataObjectField
from eu.algites.lib.data.dataobject.ain_field_presence_kind import AInFieldPresenceKind
from eu.algites.lib.data.dataobject.aia_data_object_field_default import AIaDataObjectFieldDefault
from eu.algites.lib.data.dataobject.ain_data_object_field_default_kind import AInDataObjectFieldDefaultKind
from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject
from eu.algites.lib.data.dataobject.jsondefs.aia_json_data_object import AIaJsonDataObject
from eu.algites.lib.data.dataobject.yamldefs.aia_yaml_data_object import AIaYamlDataObject
from eu.algites.lib.data.dataobject.jsonschema.aia_data_object_json_schema import AIaDataObjectJsonSchema
from eu.algites.lib.common.list.aii_multi_mapped_list import AIiMultiMappedList
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
from eu.algites.lib.data.dataobject.xmlschema.aia_data_object_field_xml_schema import AIaDataObjectFieldXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind

if TYPE_CHECKING:
    from .aiig_identifier_convention_1 import AIigIdentifierConvention_1


from eu.algites.lib.data.dataobject.aia_data_object_field_object_constraints import AIaDataObjectFieldObjectConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_list_constraints import AIaDataObjectFieldListConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_numeric_constraints import AIaDataObjectFieldNumericConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_string_constraints import AIaDataObjectFieldStringConstraints
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object import AIaXmlDataObject
from eu.algites.lib.data.dataobject.xmlschema.aia_data_object_xml_schema import AIaDataObjectXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_object_definition_kind import AInXmlObjectDefinitionKind
class AIigIdentifierConventions_1(AIiDataObject):
    """Contract for IdentifierConventions."""

    __data_object__ = AIcdDataObject(description='IdentifierConventions')
    __json_data_object__ = AIaJsonDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConventions', schema_version=1)
    __yaml_data_object__ = AIaYamlDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConventions', schema_version=1)
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConventions', schema_version=1)

    __xml_data_object__ = AIaXmlDataObject(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='IdentifierConventions')

    __xml_schema__ = AIaDataObjectXmlSchema(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='IdentifierConventions')

    __xml_data_object_fields__ = {
        'IdentifierConvention': AIaXmlDataObjectField(namespace='', namespace_local_name='IdentifierConvention', value_kind=AInXmlValueKind.ELEMENT, ),
    }

    __xml_schema_fields__ = {
        'IdentifierConvention': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='IdentifierConvention', value_kind=AInXmlValueKind.ELEMENT),
    }

    __xml_repeated_fields__ = ('IdentifierConvention',)

    class CONSTIdentifierConventions:
        """Stable key IDs and field names for this DataObject."""
        FIELD_NAME__IDENTIFIER_CONVENTION = 'IdentifierConvention'

    @AIaDataObjectField(dataFieldName='IdentifierConvention', presence=AInFieldPresenceKind.ALLOWED, allows_null=False, expected_type=AIiMultiMappedList, order=0, min_item_count=0, max_item_count=-1, is_empty_allowed=False)
    @abstractmethod
    def getIdentifierConventionList(self) -> AIiMultiMappedList[AIigIdentifierConvention_1]:
        """Return Canonical field IdentifierConvention. (optional, cardinality 0..-1)."""
        raise NotImplementedError


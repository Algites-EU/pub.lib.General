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
from eu.algites.lib.common.list.aii_bounded_list import AIiBoundedList
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
from eu.algites.lib.data.dataobject.xmlschema.aia_data_object_field_xml_schema import AIaDataObjectFieldXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind

if TYPE_CHECKING:
    from .aiig_name_fragments_segment_choice_1 import AIigNameFragmentsSegmentChoice_1


from eu.algites.lib.data.dataobject.aia_data_object_field_object_constraints import AIaDataObjectFieldObjectConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_list_constraints import AIaDataObjectFieldListConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_numeric_constraints import AIaDataObjectFieldNumericConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_string_constraints import AIaDataObjectFieldStringConstraints
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object import AIaXmlDataObject
from eu.algites.lib.data.dataobject.xmlschema.aia_data_object_xml_schema import AIaDataObjectXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_object_definition_kind import AInXmlObjectDefinitionKind
class AIigNameFragment_1(AIiDataObject):
    """Contract for NameFragment."""

    __data_object__ = AIcdDataObject(description='NameFragment')
    __json_data_object__ = AIaJsonDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragment', schema_version=1)
    __yaml_data_object__ = AIaYamlDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragment', schema_version=1)
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragment', schema_version=1)

    __xml_data_object__ = AIaXmlDataObject(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='NameFragment')

    __xml_schema__ = AIaDataObjectXmlSchema(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='NameFragment')

    __xml_data_object_fields__ = {
        'Segments': AIaXmlDataObjectField(namespace='', namespace_local_name='Segments', value_kind=AInXmlValueKind.ELEMENT, ),
        'name': AIaXmlDataObjectField(namespace='', namespace_local_name='name', value_kind=AInXmlValueKind.ATTRIBUTE, ),
    }

    __xml_schema_fields__ = {
        'Segments': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='Segments', value_kind=AInXmlValueKind.ELEMENT),
        'name': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='name', value_kind=AInXmlValueKind.ATTRIBUTE),
    }

    __xml_repeated_fields__ = ('Segments',)

    class CONSTNameFragment:
        """Stable key IDs and field names for this DataObject."""
        FIELD_NAME__SEGMENTS = 'Segments'
        FIELD_NAME__NAME = 'name'

    @AIaDataObjectFieldListConstraints(min_items=1)
    @AIaDataObjectField(dataFieldName='Segments', presence=AInFieldPresenceKind.REQUIRED, allows_null=False, expected_type=AIiBoundedList, order=0, min_item_count=1, max_item_count=-1, is_empty_allowed=False)
    @abstractmethod
    def getSegmentsList(self) -> AIiBoundedList[AIigNameFragmentsSegmentChoice_1]:
        """Return Canonical field Segments. (required, cardinality 1..-1)."""
        raise NotImplementedError

    @AIaDataObjectFieldStringConstraints(min_length=1)
    @AIaDataObjectField(dataFieldName='name', presence=AInFieldPresenceKind.REQUIRED, allows_null=False, expected_type=str, order=1, min_item_count=1, max_item_count=1, is_empty_allowed=True)
    @abstractmethod
    def getName(self) -> str:
        """Return Canonical field name. (required, cardinality 1..1)."""
        raise NotImplementedError


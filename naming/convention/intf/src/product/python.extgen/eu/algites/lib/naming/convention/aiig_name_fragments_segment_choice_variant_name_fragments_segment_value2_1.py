from __future__ import annotations
from typing import Any

from eu.algites.lib.data.dataobject.xmldefs.aia_data_object_field_xml_schema import AIaDataObjectFieldXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind
from dataclasses import dataclass
from typing import final
from eu.algites.lib.data.dataobject.aia_data_object_field_string_constraints import AIaDataObjectFieldStringConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_numeric_constraints import AIaDataObjectFieldNumericConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_list_constraints import AIaDataObjectFieldListConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_object_constraints import AIaDataObjectFieldObjectConstraints
from .aiig_name_fragments_segment_choice_1 import AIigNameFragmentsSegmentChoice_1


@final
@dataclass(frozen=True)
class AIigNameFragmentsSegmentChoiceVariantNameFragmentsSegmentValue2_1(AIigNameFragmentsSegmentChoice_1):
    """Exclusive canonical object variant"""
    __xml_choice_component_names__ = ('NameFragmentsSegmentValue',)
    __xml_choice_first_qnames__ = ('NameFragmentsSegmentValue',)
    __xml_schema_fields__ = {
        'NameFragmentsSegmentValue': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='NameFragmentsSegmentValue', value_kind=AInXmlValueKind.ELEMENT, ),
    }

    field1: AIigNameFragmentsSegmentValue_1

    def __post_init__(self) -> None:
        """Validate required fields of this exact alternative."""
        if self.field1 is None:
            raise ValueError('NameFragmentsSegmentValue' + " is required")

    def getNameFragmentsSegmentValue(self) -> AIigNameFragmentsSegmentValue_1:
        """Return field NameFragmentsSegmentValue."""
        return self.field1

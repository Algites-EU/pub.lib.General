from __future__ import annotations
from typing import Any

from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind
from dataclasses import dataclass
from typing import final
from eu.algites.lib.data.dataobject.aia_data_object_field_string_constraints import AIaDataObjectFieldStringConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_numeric_constraints import AIaDataObjectFieldNumericConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_list_constraints import AIaDataObjectFieldListConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_object_constraints import AIaDataObjectFieldObjectConstraints
from .aiig_name_fragments_segment_value_content_1 import AIigNameFragmentsSegmentValueContent_1


@final
@dataclass(frozen=True)
class AIigNameFragmentsSegmentValueContentVariantNameRegexp4_1(AIigNameFragmentsSegmentValueContent_1):
    """Exclusive canonical object variant"""
    __xml_choice_component_names__ = ('NameRegexp',)
    __xml_choice_first_qnames__ = ('NameRegexp',)
    __xml_data_object_fields__ = {
        'NameRegexp': AIaXmlDataObjectField(namespace='', namespace_local_name='NameRegexp', value_kind=AInXmlValueKind.ELEMENT, ),
    }

    field1: AIigNameRegexp_1

    def __post_init__(self) -> None:
        """Validate required fields of this exact alternative."""
        if self.field1 is None:
            raise ValueError('NameRegexp' + " is required")

    def getNameRegexp(self) -> AIigNameRegexp_1:
        """Return field NameRegexp."""
        return self.field1

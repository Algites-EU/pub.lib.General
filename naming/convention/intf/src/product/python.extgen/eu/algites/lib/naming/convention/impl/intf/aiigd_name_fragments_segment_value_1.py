"""Generated mutable contract for AIigNameFragmentsSegmentValue_1."""
from __future__ import annotations
from abc import abstractmethod
from eu.algites.lib.naming.convention.aiig_name_fragments_segment_value_1 import AIigNameFragmentsSegmentValue_1
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from eu.algites.lib.naming.convention.aiig_name_fragments_segment_value_content_1 import AIigNameFragmentsSegmentValueContent_1
from eu.algites.lib.data.dataobject.xmlschema.aia_data_object_field_xml_schema import AIaDataObjectFieldXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind
from eu.algites.lib.data.dataobject.xmlschema.aia_data_object_xml_schema import AIaDataObjectXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object import AIaXmlDataObject
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_object_definition_kind import AInXmlObjectDefinitionKind
from eu.algites.lib.data.dataobject.yamldefs.aia_yaml_data_object import AIaYamlDataObject
from eu.algites.lib.data.dataobject.jsondefs.aia_json_data_object import AIaJsonDataObject
from eu.algites.lib.data.dataobject.jsonschema.aia_data_object_json_schema import AIaDataObjectJsonSchema
from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.common.object.props.labels.aia_field_label import AIaFieldLabel
from eu.algites.lib.data.dataobject.ain_field_setter_inheritance_policy import AInFieldSetterInheritancePolicy
from eu.algites.lib.data.smartdataobject.aii_smart_data_object import AIiSmartDataObject


class AIigdNameFragmentsSegmentValue_1(AIigNameFragmentsSegmentValue_1, AIiSmartDataObject):
    __data_object__ = AIcdDataObject(title='', description='NameFragmentsSegmentValue', setter_inheritance_policy=AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    """Presence-aware setters, raw getters and unsetters."""
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentValue', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __json_data_object__ = AIaJsonDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentValue', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __yaml_data_object__ = AIaYamlDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentValue', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __xml_data_object__ = AIaXmlDataObject(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='NameFragmentsSegmentValue')
    __xml_schema__ = AIaDataObjectXmlSchema(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='NameFragmentsSegmentValue')
    __xml_data_object_fields__ = {
        'Kind': AIaXmlDataObjectField(namespace='', namespace_local_name='Kind', value_kind=AInXmlValueKind.ELEMENT),
        'Content': AIaXmlDataObjectField(namespace='', namespace_local_name='Content', value_kind=AInXmlValueKind.ELEMENT),
    }
    __xml_schema_fields__ = {
        'Kind': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='Kind', value_kind=AInXmlValueKind.ELEMENT),
        'Content': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='Content', value_kind=AInXmlValueKind.ELEMENT),
    }
    __field_labels__ = {**getattr(AIigNameFragmentsSegmentValue_1, '__field_labels__', {}),
        'Kind': AIaFieldLabel(label='Kind', roleCode='field.name', labels=()),
        'Content': AIaFieldLabel(label='Content', roleCode='field.name', labels=()),
    }

    @abstractmethod
    def setKind(self, a_value: object) -> None: ...

    @abstractmethod
    def _getRaw_Kind(self) -> object: ...

    @abstractmethod
    def _isPresent_Kind(self) -> bool: ...

    @abstractmethod
    def _unset_Kind(self) -> None: ...

    @abstractmethod
    def getContent(self) -> AIigNameFragmentsSegmentValueContent_1: ...

    @abstractmethod
    def setContent(self, a_value: AIigNameFragmentsSegmentValueContent_1) -> None: ...

    @abstractmethod
    def _getRaw_Content(self) -> AIigNameFragmentsSegmentValueContent_1: ...

    @abstractmethod
    def _isPresent_Content(self) -> bool: ...

    @abstractmethod
    def _unset_Content(self) -> None: ...

"""Generated mutable contract for AIigNameFragments_1."""
from __future__ import annotations
from typing import TypeVar, Generic
from abc import abstractmethod
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from eu.algites.lib.common.list.aii_multi_mapped_list import AIiMultiMappedList
    from eu.algites.lib.naming.convention.impl.intf.aiigd_name_fragment_1 import AIigdNameFragment_1
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
from eu.algites.lib.naming.convention.aiig_name_fragments_1 import AIigNameFragments_1
from eu.algites.lib.data.smartdataobject.aii_smart_data_object import AIiSmartDataObject

TNameFragmentList = TypeVar('TNameFragmentList', bound='AIigdNameFragment_1')


class AIigdNameFragments_1(AIigNameFragments_1, AIiSmartDataObject, Generic[TNameFragmentList]):
    __data_object__ = AIcdDataObject(title='', description='NameFragments', setter_inheritance_policy=AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    """Presence-aware setters, raw getters and unsetters."""
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragments', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __json_data_object__ = AIaJsonDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragments', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __yaml_data_object__ = AIaYamlDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragments', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __xml_data_object__ = AIaXmlDataObject(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='NameFragments')
    __xml_schema__ = AIaDataObjectXmlSchema(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='NameFragments')
    __xml_data_object_fields__ = {
        'NameFragment': AIaXmlDataObjectField(namespace='', namespace_local_name='NameFragment', value_kind=AInXmlValueKind.ELEMENT),
    }
    __xml_schema_fields__ = {
        'NameFragment': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='NameFragment', value_kind=AInXmlValueKind.ELEMENT),
    }
    __field_labels__ = {**getattr(AIigNameFragments_1, '__field_labels__', {}),
        'NameFragmentList': AIaFieldLabel(label='Name fragment list', roleCode='field.name', labels=()),
    }

    @abstractmethod
    def getNameFragmentList(self) -> AIiMultiMappedList[TNameFragmentList]: ...

    @abstractmethod
    def _getRaw_NameFragmentList(self) -> AIiMultiMappedList[TNameFragmentList]: ...

    @abstractmethod
    def _isPresent_NameFragmentList(self) -> bool: ...

    @abstractmethod
    def _unset_NameFragmentList(self) -> None: ...

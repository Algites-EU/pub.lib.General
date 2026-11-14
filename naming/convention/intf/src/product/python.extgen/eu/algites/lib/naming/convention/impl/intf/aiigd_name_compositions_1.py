"""Generated mutable contract for AIigNameCompositions_1."""
from __future__ import annotations
from typing import TypeVar, Generic
from abc import abstractmethod
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from eu.algites.lib.common.list.aii_multi_mapped_list import AIiMultiMappedList
    from eu.algites.lib.naming.convention.impl.intf.aiigd_name_composition_1 import AIigdNameComposition_1
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
from eu.algites.lib.naming.convention.aiig_name_compositions_1 import AIigNameCompositions_1
from eu.algites.lib.data.smartdataobject.aii_smart_data_object import AIiSmartDataObject

TNameCompositionList = TypeVar('TNameCompositionList', bound='AIigdNameComposition_1')


class AIigdNameCompositions_1(AIigNameCompositions_1, AIiSmartDataObject, Generic[TNameCompositionList]):
    __data_object__ = AIcdDataObject(title='', description='NameCompositions', setter_inheritance_policy=AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    """Presence-aware setters, raw getters and unsetters."""
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __json_data_object__ = AIaJsonDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __yaml_data_object__ = AIaYamlDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameCompositions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __xml_data_object__ = AIaXmlDataObject(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='NameCompositions')
    __xml_schema__ = AIaDataObjectXmlSchema(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='NameCompositions')
    __xml_data_object_fields__ = {
        'NameComposition': AIaXmlDataObjectField(namespace='', namespace_local_name='NameComposition', value_kind=AInXmlValueKind.ELEMENT),
    }
    __xml_schema_fields__ = {
        'NameComposition': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='NameComposition', value_kind=AInXmlValueKind.ELEMENT),
    }
    __field_labels__ = {**getattr(AIigNameCompositions_1, '__field_labels__', {}),
        'NameCompositionList': AIaFieldLabel(label='Name composition list', roleCode='field.name', labels=()),
    }

    @abstractmethod
    def getNameCompositionList(self) -> AIiMultiMappedList[TNameCompositionList]: ...

    @abstractmethod
    def _getRaw_NameCompositionList(self) -> AIiMultiMappedList[TNameCompositionList]: ...

    @abstractmethod
    def _isPresent_NameCompositionList(self) -> bool: ...

    @abstractmethod
    def _unset_NameCompositionList(self) -> None: ...

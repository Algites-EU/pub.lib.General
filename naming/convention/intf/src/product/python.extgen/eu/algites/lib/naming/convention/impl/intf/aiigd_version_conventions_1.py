"""Generated mutable contract for AIigVersionConventions_1."""
from __future__ import annotations
from typing import TypeVar, Generic
from abc import abstractmethod
from typing import TYPE_CHECKING

if TYPE_CHECKING:
    from eu.algites.lib.common.list.aii_multi_mapped_list import AIiMultiMappedList
    from eu.algites.lib.naming.convention.impl.intf.aiigd_version_convention_1 import AIigdVersionConvention_1
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
from eu.algites.lib.naming.convention.aiig_version_conventions_1 import AIigVersionConventions_1
from eu.algites.lib.data.smartdataobject.aii_smart_data_object import AIiSmartDataObject

TVersionConventionList = TypeVar('TVersionConventionList', bound='AIigdVersionConvention_1')


class AIigdVersionConventions_1(AIigVersionConventions_1, AIiSmartDataObject, Generic[TVersionConventionList]):
    __data_object__ = AIcdDataObject(title='', description='VersionConventions', setter_inheritance_policy=AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    """Presence-aware setters, raw getters and unsetters."""
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/VersionConventions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __json_data_object__ = AIaJsonDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/VersionConventions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __yaml_data_object__ = AIaYamlDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/VersionConventions', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __xml_data_object__ = AIaXmlDataObject(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='VersionConventions')
    __xml_schema__ = AIaDataObjectXmlSchema(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='VersionConventions')
    __xml_data_object_fields__ = {
        'VersionConvention': AIaXmlDataObjectField(namespace='', namespace_local_name='VersionConvention', value_kind=AInXmlValueKind.ELEMENT),
    }
    __xml_schema_fields__ = {
        'VersionConvention': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='VersionConvention', value_kind=AInXmlValueKind.ELEMENT),
    }
    __field_labels__ = {**getattr(AIigVersionConventions_1, '__field_labels__', {}),
        'VersionConventionList': AIaFieldLabel(label='Version convention list', roleCode='field.name', labels=()),
    }

    @abstractmethod
    def getVersionConventionList(self) -> AIiMultiMappedList[TVersionConventionList]: ...

    @abstractmethod
    def _getRaw_VersionConventionList(self) -> AIiMultiMappedList[TVersionConventionList]: ...

    @abstractmethod
    def _isPresent_VersionConventionList(self) -> bool: ...

    @abstractmethod
    def _unset_VersionConventionList(self) -> None: ...

"""Generated mutable contract for AIigNameRegexp_1."""
from __future__ import annotations
from abc import abstractmethod
from eu.algites.lib.naming.convention.aiig_name_regexp_1 import AIigNameRegexp_1
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


class AIigdNameRegexp_1(AIigNameRegexp_1, AIiSmartDataObject):
    __data_object__ = AIcdDataObject(title='', description='NameRegexp', setter_inheritance_policy=AInFieldSetterInheritancePolicy.INHERIT_AS_GENERIC_PARAMETER)
    """Presence-aware setters, raw getters and unsetters."""
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameRegexp', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __json_data_object__ = AIaJsonDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameRegexp', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __yaml_data_object__ = AIaYamlDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameRegexp', schema_version=1, schema_url='', data_source_schema_url_field='', data_source_schema_identifier_field='', data_source_schema_version_field='')
    __xml_data_object__ = AIaXmlDataObject(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='NameRegexp')
    __xml_schema__ = AIaDataObjectXmlSchema(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='NameRegexp')
    __xml_data_object_fields__ = {
        'Name': AIaXmlDataObjectField(namespace='', namespace_local_name='Name', value_kind=AInXmlValueKind.ELEMENT),
        'Regexp': AIaXmlDataObjectField(namespace='', namespace_local_name='Regexp', value_kind=AInXmlValueKind.ELEMENT),
    }
    __xml_schema_fields__ = {
        'Name': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='Name', value_kind=AInXmlValueKind.ELEMENT),
        'Regexp': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='Regexp', value_kind=AInXmlValueKind.ELEMENT),
    }
    __field_labels__ = {**getattr(AIigNameRegexp_1, '__field_labels__', {}),
        'Name': AIaFieldLabel(label='Name', roleCode='field.name', labels=()),
        'Regexp': AIaFieldLabel(label='Regexp', roleCode='field.name', labels=()),
    }

    @abstractmethod
    def setName(self, a_value: object) -> None: ...

    @abstractmethod
    def _getRaw_Name(self) -> object: ...

    @abstractmethod
    def _isPresent_Name(self) -> bool: ...

    @abstractmethod
    def _unset_Name(self) -> None: ...

    @abstractmethod
    def _setEmpty_Name(self) -> None: ...

    @abstractmethod
    def _isEmpty_Name(self) -> bool: ...

    @abstractmethod
    def setRegexp(self, a_value: object) -> None: ...

    @abstractmethod
    def _getRaw_Regexp(self) -> object: ...

    @abstractmethod
    def _isPresent_Regexp(self) -> bool: ...

    @abstractmethod
    def _unset_Regexp(self) -> None: ...

    @abstractmethod
    def _setEmpty_Regexp(self) -> None: ...

    @abstractmethod
    def _isEmpty_Regexp(self) -> bool: ...

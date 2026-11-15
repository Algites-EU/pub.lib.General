from __future__ import annotations
from typing import Any

from abc import ABC, abstractmethod
from typing import TYPE_CHECKING
from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.data.dataobject.aia_data_object_field import AIaDataObjectField
from eu.algites.lib.data.dataobject.ain_field_presence_kind import AInFieldPresenceKind
from eu.algites.lib.data.dataobject.aia_data_object_field_default import AIaDataObjectFieldDefault
from eu.algites.lib.data.dataobject.ain_data_object_field_default_kind import AInDataObjectFieldDefaultKind
from eu.algites.lib.data.dataobject.aii_field_default_scalar_value_factory import AIiFieldDefaultScalarValueFactory
from eu.algites.lib.factory.cpdiscovery.aia_category_provider_factory_definition import AIaCategoryProviderFactoryDefinition
from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject
from eu.algites.lib.data.dataobject.jsondefs.aia_data_object_json_schema import AIaDataObjectJsonSchema
from eu.algites.lib.data.dataobject.yamldefs.aia_data_object_yaml_schema import AIaDataObjectYamlSchema
from eu.algites.lib.data.dataobject.xmldefs.aia_data_object_field_xml_schema import AIaDataObjectFieldXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind

if TYPE_CHECKING:
    from .aing_identifier_conversion_style_1 import AIngIdentifierConversionStyle_1


from eu.algites.lib.data.dataobject.aia_data_object_field_object_constraints import AIaDataObjectFieldObjectConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_list_constraints import AIaDataObjectFieldListConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_numeric_constraints import AIaDataObjectFieldNumericConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_string_constraints import AIaDataObjectFieldStringConstraints
from eu.algites.lib.data.dataobject.xmldefs.aia_data_object_xml_schema import AIaDataObjectXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_object_definition_kind import AInXmlObjectDefinitionKind
class AIigIdentifierConvention_1(AIiDataObject):
    """Contract for IdentifierConvention."""

    __data_object__ = AIcdDataObject(description='IdentifierConvention')
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConvention', schema_version=1)
    __yaml_schema__ = AIaDataObjectYamlSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConvention', schema_version=1)

    __xml_schema__ = AIaDataObjectXmlSchema(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='IdentifierConvention')

    __xml_scalar_type_imports__ = {'ConversionStyle': 'eu.algites.lib.naming.convention.aing_identifier_conversion_style_1:AIngIdentifierConversionStyle_1'}

    __xml_schema_fields__ = {
        'Regexp': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='Regexp', value_kind=AInXmlValueKind.ELEMENT),
        'ConversionStyle': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='ConversionStyle', value_kind=AInXmlValueKind.ELEMENT),
        'Name': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='name', value_kind=AInXmlValueKind.ATTRIBUTE),
    }

    class CONSTIdentifierConvention:
        """Stable key IDs and field names for this DataObject."""
        FIELD_NAME__REGEXP = 'Regexp'
        FIELD_NAME__CONVERSION_STYLE = 'ConversionStyle'
        FIELD_NAME__NAME = 'name'

    @AIaDataObjectField(dataFieldName='Regexp', presence=AInFieldPresenceKind.REQUIRED, allows_null=False, expected_type=str, order=0, min_item_count=1, max_item_count=1, is_empty_allowed=True)
    @abstractmethod
    def getRegexp(self) -> str:
        """Return Canonical field Regexp. (required, cardinality 1..1)."""
        raise NotImplementedError

    @AIaDataObjectField(dataFieldName='ConversionStyle', presence=AInFieldPresenceKind.ALLOWED, allows_null=False, order=1, min_item_count=0, max_item_count=1, is_empty_allowed=False)
    @abstractmethod
    def getConversionStyle(self) -> AIngIdentifierConversionStyle_1:
        """Return Canonical field ConversionStyle. (optional, cardinality 0..1)."""
        raise NotImplementedError

    @AIaDataObjectFieldStringConstraints(min_length=1)
    @AIaDataObjectField(dataFieldName='name', presence=AInFieldPresenceKind.REQUIRED, allows_null=False, expected_type=str, order=2, min_item_count=1, max_item_count=1, is_empty_allowed=True)
    @abstractmethod
    def getName(self) -> str:
        """Return Canonical field name. (required, cardinality 1..1)."""
        raise NotImplementedError


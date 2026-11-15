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
from eu.algites.lib.data.dataobject.jsondefs.aia_data_object_json_schema import AIaDataObjectJsonSchema
from eu.algites.lib.data.dataobject.yamldefs.aia_data_object_yaml_schema import AIaDataObjectYamlSchema
from eu.algites.lib.data.dataobject.xmldefs.aia_data_object_field_xml_schema import AIaDataObjectFieldXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind
from .aiig_common_naming_conventions_1 import AIigCommonNamingConventions_1

if TYPE_CHECKING:
    from .aiig_identifier_conventions_1 import AIigIdentifierConventions_1
    from .aiig_version_conventions_1 import AIigVersionConventions_1


from eu.algites.lib.data.dataobject.aia_data_object_field_object_constraints import AIaDataObjectFieldObjectConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_list_constraints import AIaDataObjectFieldListConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_numeric_constraints import AIaDataObjectFieldNumericConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_string_constraints import AIaDataObjectFieldStringConstraints
from eu.algites.lib.data.dataobject.xmldefs.aia_data_object_xml_schema import AIaDataObjectXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_object_definition_kind import AInXmlObjectDefinitionKind
class AIigStandardConventions_1(AIigCommonNamingConventions_1):
    """Reusable regular expressions with named capture groups. Regex syntax canonicalized from Java named groups."""

    __data_object__ = AIcdDataObject(description='Reusable regular expressions with named capture groups. Regex syntax canonicalized from Java named groups.')
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.standard-conventions', schema_version=1, schema_url='https://defs.dev.algites.eu/api/jsondefs/eu/algites/lib/naming/convention/standard-conventions_1.jsondef.schema.json')
    __yaml_schema__ = AIaDataObjectYamlSchema(schema_id='eu.algites.lib.naming.convention.standard-conventions', schema_version=1, schema_url='https://defs.dev.algites.eu/api/yamldefs/eu/algites/lib/naming/convention/standard-conventions_1.yamldef.schema.json')

    __xml_schema__ = AIaDataObjectXmlSchema(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/standard-conventions_1.xsd', namespace_local_name='StandardConventions')

    __xml_schema_fields__ = {
        'IdentifierConventions': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='IdentifierConventions', value_kind=AInXmlValueKind.ELEMENT),
        'VersionConventions': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='VersionConventions', value_kind=AInXmlValueKind.ELEMENT),
    }

    class CONSTStandardConventions:
        """Stable key IDs and field names for this DataObject."""
        FIELD_NAME__IDENTIFIER_CONVENTIONS = 'IdentifierConventions'
        FIELD_NAME__VERSION_CONVENTIONS = 'VersionConventions'
        KEY_ID__UNIQUE_IDENTIFIER_CONVENTION_NAMES = 'UniqueIdentifierConventionNames'
        FIELD_NAME__NAME = 'name'
        KEY_ID__UNIQUE_VERSION_CONVENTION_NAMES = 'UniqueVersionConventionNames'

    __scoped_key_definitions__ = {
        'IdentifierConventions': (
            ('IdentifierConvention', 'UniqueIdentifierConventionNames', ('name',), True),
        ),
        'VersionConventions': (
            ('VersionConvention', 'UniqueVersionConventionNames', ('name',), True),
        ),
    }

    @AIaDataObjectField(dataFieldName='IdentifierConventions', presence=AInFieldPresenceKind.REQUIRED, allows_null=False, order=0, min_item_count=1, max_item_count=1, is_empty_allowed=False)
    @abstractmethod
    def getIdentifierConventions(self) -> AIigIdentifierConventions_1:
        """Return Canonical field IdentifierConventions. (required, cardinality 1..1)."""
        raise NotImplementedError

    @AIaDataObjectField(dataFieldName='VersionConventions', presence=AInFieldPresenceKind.REQUIRED, allows_null=False, order=1, min_item_count=1, max_item_count=1, is_empty_allowed=False)
    @abstractmethod
    def getVersionConventions(self) -> AIigVersionConventions_1:
        """Return Canonical field VersionConventions. (required, cardinality 1..1)."""
        raise NotImplementedError


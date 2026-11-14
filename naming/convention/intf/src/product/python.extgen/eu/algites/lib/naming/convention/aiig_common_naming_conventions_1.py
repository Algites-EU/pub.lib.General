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
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
from eu.algites.lib.data.dataobject.xmlschema.aia_data_object_field_xml_schema import AIaDataObjectFieldXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind

if TYPE_CHECKING:
    from .aiig_identifier_conventions_1 import AIigIdentifierConventions_1
    from .aiig_version_conventions_1 import AIigVersionConventions_1
    from .aiig_name_fragments_1 import AIigNameFragments_1
    from .aiig_name_compositions_1 import AIigNameCompositions_1


from eu.algites.lib.data.dataobject.aia_data_object_field_object_constraints import AIaDataObjectFieldObjectConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_list_constraints import AIaDataObjectFieldListConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_numeric_constraints import AIaDataObjectFieldNumericConstraints
from eu.algites.lib.data.dataobject.aia_data_object_field_string_constraints import AIaDataObjectFieldStringConstraints
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object import AIaXmlDataObject
from eu.algites.lib.data.dataobject.xmlschema.aia_data_object_xml_schema import AIaDataObjectXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_object_definition_kind import AInXmlObjectDefinitionKind
class AIigCommonNamingConventions_1(AIiDataObject):
    """Contract for CommonNamingConventions."""

    __data_object__ = AIcdDataObject(description='CommonNamingConventions')
    __json_data_object__ = AIaJsonDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions', schema_version=1, schema_url='https://defs.dev.algites.eu/api/jsondefs/eu/algites/lib/naming/convention/common-naming-conventions_1.jsondef.schema.json')
    __yaml_data_object__ = AIaYamlDataObject(schema_id='eu.algites.lib.naming.convention.common-naming-conventions', schema_version=1, schema_url='https://defs.dev.algites.eu/api/yamldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.yamldef.schema.json')
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='eu.algites.lib.naming.convention.common-naming-conventions', schema_version=1, schema_url='https://defs.dev.algites.eu/api/jsondefs/eu/algites/lib/naming/convention/common-naming-conventions_1.jsondef.schema.json')

    __xml_data_object__ = AIaXmlDataObject(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='CommonNamingConventions')

    __xml_schema__ = AIaDataObjectXmlSchema(definition_kind=AInXmlObjectDefinitionKind.TYPE, namespace='https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd', namespace_local_name='CommonNamingConventions')

    __xml_data_object_fields__ = {
        'StandardConventionsFile': AIaXmlDataObjectField(namespace='', namespace_local_name='StandardConventionsFile', value_kind=AInXmlValueKind.ELEMENT, ),
        'IdentifierConventions': AIaXmlDataObjectField(namespace='', namespace_local_name='IdentifierConventions', value_kind=AInXmlValueKind.ELEMENT, ),
        'VersionConventions': AIaXmlDataObjectField(namespace='', namespace_local_name='VersionConventions', value_kind=AInXmlValueKind.ELEMENT, ),
        'NameFragments': AIaXmlDataObjectField(namespace='', namespace_local_name='NameFragments', value_kind=AInXmlValueKind.ELEMENT, ),
        'NameCompositions': AIaXmlDataObjectField(namespace='', namespace_local_name='NameCompositions', value_kind=AInXmlValueKind.ELEMENT, ),
    }

    __xml_schema_fields__ = {
        'StandardConventionsFile': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='StandardConventionsFile', value_kind=AInXmlValueKind.ELEMENT),
        'IdentifierConventions': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='IdentifierConventions', value_kind=AInXmlValueKind.ELEMENT),
        'VersionConventions': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='VersionConventions', value_kind=AInXmlValueKind.ELEMENT),
        'NameFragments': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='NameFragments', value_kind=AInXmlValueKind.ELEMENT),
        'NameCompositions': AIaDataObjectFieldXmlSchema(namespace='', namespace_local_name='NameCompositions', value_kind=AInXmlValueKind.ELEMENT),
    }

    class CONSTCommonNamingConventions:
        """Stable key IDs and field names for this DataObject."""
        FIELD_NAME__STANDARD_CONVENTIONS_FILE = 'StandardConventionsFile'
        FIELD_NAME__IDENTIFIER_CONVENTIONS = 'IdentifierConventions'
        FIELD_NAME__VERSION_CONVENTIONS = 'VersionConventions'
        FIELD_NAME__NAME_FRAGMENTS = 'NameFragments'
        FIELD_NAME__NAME_COMPOSITIONS = 'NameCompositions'
        KEY_ID__UNIQUE_IDENTIFIER_CONVENTION_NAMES = 'UniqueIdentifierConventionNames'
        FIELD_NAME__NAME = 'name'
        KEY_ID__UNIQUE_VERSION_CONVENTION_NAMES = 'UniqueVersionConventionNames'
        KEY_ID__UNIQUE_NAME_FRAGMENT_NAMES = 'UniqueNameFragmentNames'
        KEY_ID__UNIQUE_NAME_COMPOSITION_NAMES = 'UniqueNameCompositionNames'

    __scoped_key_definitions__ = {
        'IdentifierConventions': (
            ('IdentifierConvention', 'UniqueIdentifierConventionNames', ('name',), True),
        ),
        'VersionConventions': (
            ('VersionConvention', 'UniqueVersionConventionNames', ('name',), True),
        ),
        'NameFragments': (
            ('NameFragment', 'UniqueNameFragmentNames', ('name',), True),
        ),
        'NameCompositions': (
            ('NameComposition', 'UniqueNameCompositionNames', ('name',), True),
        ),
    }

    @AIaDataObjectField(dataFieldName='StandardConventionsFile', presence=AInFieldPresenceKind.ALLOWED, allows_null=False, expected_type=str, order=0, min_item_count=0, max_item_count=1, is_empty_allowed=True)
    @abstractmethod
    def getStandardConventionsFile(self) -> str:
        """Return Optional reference to standard identifier/version definitions. (optional, cardinality 0..1)."""
        raise NotImplementedError

    @AIaDataObjectField(dataFieldName='IdentifierConventions', presence=AInFieldPresenceKind.ALLOWED, allows_null=False, order=1, min_item_count=0, max_item_count=1, is_empty_allowed=False)
    @abstractmethod
    def getIdentifierConventions(self) -> AIigIdentifierConventions_1:
        """Return Named identifier conventions. (optional, cardinality 0..1)."""
        raise NotImplementedError

    @AIaDataObjectField(dataFieldName='VersionConventions', presence=AInFieldPresenceKind.ALLOWED, allows_null=False, order=2, min_item_count=0, max_item_count=1, is_empty_allowed=False)
    @abstractmethod
    def getVersionConventions(self) -> AIigVersionConventions_1:
        """Return Named version conventions. (optional, cardinality 0..1)."""
        raise NotImplementedError

    @AIaDataObjectField(dataFieldName='NameFragments', presence=AInFieldPresenceKind.ALLOWED, allows_null=False, order=3, min_item_count=0, max_item_count=1, is_empty_allowed=False)
    @abstractmethod
    def getNameFragments(self) -> AIigNameFragments_1:
        """Return Reusable fragment definitions. (optional, cardinality 0..1)."""
        raise NotImplementedError

    @AIaDataObjectField(dataFieldName='NameCompositions', presence=AInFieldPresenceKind.ALLOWED, allows_null=False, order=4, min_item_count=0, max_item_count=1, is_empty_allowed=False)
    @abstractmethod
    def getNameCompositions(self) -> AIigNameCompositions_1:
        """Return Reusable name composition definitions. (optional, cardinality 0..1)."""
        raise NotImplementedError


"""Independent XML, JSON and YAML schema annotations on neutral DataObjects.

Only the format-specific schema descriptor varies. Fields and their defaults
remain defined once by the common DataObject getter.
"""
from eu.algites.lib.data.dataobject.jsondefs.aia_data_object_json_schema import AIaDataObjectJsonSchema
from eu.algites.lib.data.dataobject.yamldefs.aia_data_object_yaml_schema import AIaDataObjectYamlSchema
from eu.algites.lib.data.dataobject.xmldefs.aia_data_object_xml_schema import AIaDataObjectXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.aia_data_object_field_xml_schema import AIaDataObjectFieldXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_object_definition_kind import AInXmlObjectDefinitionKind
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind
from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.data.dataobject.aia_data_object_field import AIaDataObjectField


class CommonContract:
    __data_object__ = AIcdDataObject(description='Neutral example')

    @AIaDataObjectField(field_name='code')
    def getCode(self):
        """Return the code."""
        raise NotImplementedError


class CombinedContract(CommonContract):
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='json-sample', schema_version=7)
    __yaml_schema__ = AIaDataObjectYamlSchema(schema_id='yaml-sample', schema_version=11)
    __xml_schema__ = AIaDataObjectXmlSchema(namespace='urn:example', namespace_local_name='Sample')
    __xml_schema_fields__ = {
        'code': AIaDataObjectFieldXmlSchema(namespace='urn:example',
            namespace_local_name='Code', value_kind=AInXmlValueKind.ATTRIBUTE),
    }


def test_json_and_yaml_identities_remain_independent():
    """Two schema definitions on one neutral contract are not overwritten."""
    assert CombinedContract.__json_schema__.schema_id == 'json-sample'
    assert CombinedContract.__json_schema__.schema_version == 7
    assert CombinedContract.__yaml_schema__.schema_id == 'yaml-sample'
    assert CombinedContract.__yaml_schema__.schema_version == 11
    assert CombinedContract.__json_schema__ != CombinedContract.__yaml_schema__


def test_xml_schema_and_field_metadata_are_separate():
    """XML QName metadata is independent of the JSON and YAML identities."""
    schema = CombinedContract.__xml_schema__
    assert schema.definition_kind is AInXmlObjectDefinitionKind.TYPE
    assert (schema.namespace, schema.namespace_local_name) == ('urn:example', 'Sample')
    field = CombinedContract.__xml_schema_fields__['code']
    assert field.value_kind is AInXmlValueKind.ATTRIBUTE
    assert (field.namespace, field.namespace_local_name) == ('urn:example', 'Code')


def test_schema_projections_do_not_define_new_data_objects():
    """One neutral contract owns data fields; formats contribute metadata only."""
    assert '__data_object__' in CommonContract.__dict__
    assert '__data_object__' not in CombinedContract.__dict__
    assert hasattr(CommonContract.getCode, '__data_object_field__')
    assert 'getCode' not in CombinedContract.__dict__
    assert set(CombinedContract.__dict__).isdisjoint({
        '__json_schema__', '__yaml_schema__', '__xml_schema__',
        '__xml_schema_fields__',
    })

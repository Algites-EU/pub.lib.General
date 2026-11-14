"""Python mirror of AItcDataObjectSchemaMetadataCompatibilityTest.

The same five independent scenarios must pass in Java and Python.
"""
from eu.algites.lib.data.dataobject.jsonschema.aia_data_object_json_schema import AIaDataObjectJsonSchema
from eu.algites.lib.data.dataobject.xmlschema.aia_data_object_xml_schema import AIaDataObjectXmlSchema
from eu.algites.lib.data.dataobject.xmlschema.aia_data_object_field_xml_schema import AIaDataObjectFieldXmlSchema
from eu.algites.lib.data.dataobject.jsondefs.aia_json_data_object import AIaJsonDataObject
from eu.algites.lib.data.dataobject.yamldefs.aia_yaml_data_object import AIaYamlDataObject
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object import AIaXmlDataObject
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
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


class JsonContract(CommonContract):
    __json_data_object__ = AIaJsonDataObject(schema_id='sample', schema_version=7)
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='sample', schema_version=7)


class YamlContract(CommonContract):
    __yaml_data_object__ = AIaYamlDataObject(schema_id='sample', schema_version=7)
    __json_schema__ = AIaDataObjectJsonSchema(schema_id='sample', schema_version=7)


class XmlContract(CommonContract):
    __xml_data_object__ = AIaXmlDataObject(namespace='urn:example', namespace_local_name='Sample')
    __xml_schema__ = AIaDataObjectXmlSchema(namespace='urn:example', namespace_local_name='Sample')
    __xml_data_object_fields__ = {'code': AIaXmlDataObjectField(namespace='urn:example',
        namespace_local_name='Code', value_kind=AInXmlValueKind.ATTRIBUTE)}
    __xml_schema_fields__ = {'code': AIaDataObjectFieldXmlSchema(namespace='urn:example',
        namespace_local_name='Code', value_kind=AInXmlValueKind.ATTRIBUTE)}


def test_common_json_schema_for_json_and_yaml():
    """A single schema metadata class serves both serialization technologies."""
    for cls in (JsonContract, YamlContract):
        assert cls.__json_schema__.schema_id == 'sample'
        assert cls.__json_schema__.schema_version == 7
    assert JsonContract.__json_data_object__.schema_id == 'sample'
    assert YamlContract.__yaml_data_object__.schema_id == 'sample'


def test_xml_schema_identity_is_identical():
    """Old and new XML definitions have the same QName and definition kind."""
    modern, legacy = XmlContract.__xml_schema__, XmlContract.__xml_data_object__
    assert modern.definition_kind is AInXmlObjectDefinitionKind.TYPE
    assert (modern.namespace, modern.namespace_local_name) == (legacy.namespace, legacy.namespace_local_name)


def test_xml_field_identity_is_identical():
    """Old and new field definitions have the same QName and attribute kind."""
    modern, legacy = XmlContract.__xml_schema_fields__['code'], XmlContract.__xml_data_object_fields__['code']
    assert modern.value_kind is AInXmlValueKind.ATTRIBUTE
    assert (modern.namespace, modern.namespace_local_name) == (legacy.namespace, legacy.namespace_local_name)


def test_generic_annotations_live_only_on_common_contract():
    """A format projection inherits field semantics but declares only XML wire bindings."""
    assert '__data_object__' in CommonContract.__dict__
    assert '__data_object__' not in XmlContract.__dict__
    assert hasattr(CommonContract.getCode, '__data_object_field__')
    assert 'getCode' not in XmlContract.__dict__


def test_legacy_annotations_deprecated_but_present():
    """No legacy metadata type was deleted as part of the migration."""
    for cls in (AIaJsonDataObject, AIaYamlDataObject, AIaXmlDataObject, AIaXmlDataObjectField):
        assert 'deprecated' in cls.__deprecated__.lower()

"""Cross-feature Python runtime integration checks (no external test framework required)."""
import json
import pytest

from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.smartdataobject.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.text.format.ais_text_format_profiles import AIsTextFormatProfiles as F
from eu.algites.lib.text.format.ain_text_output_object_reference_mode import AInTextOutputObjectReferenceMode as M


class AIcgdDemo(AIcSmartDataObject):
    __schema_fingerprint__ = 'abc123'
    __data_object_fields__ = (
        AIcdDataObjectField('Name', presence_required=True, expected_type=str),
        AIcdDataObjectField('Port', presence_required=True, default_factory=lambda: 8080, expected_type=int),
        AIcdDataObjectField('Child', allows_null=True),
    )

    def getName(self): return self.get_EffectiveField('Name')
    def setName(self, value): self.set_RawField('Name', value)
    def getPort(self): return self.get_EffectiveField('Port')
    def setPort(self, value): self.set_RawField('Port', value)
    def get_Port(self): return self.get_RawField('Port')
    def isPresent_Port(self): return self.isPresent_Field('Port')
    def unset_Port(self): self.unset_Field('Port')
    def setChild(self, value): self.set_RawField('Child', value)
    def get_Child(self): return self.get_RawField('Child')
    def unset_Child(self): self.unset_Field('Child')


def test_raw_effective_and_defaults():
    obj = AIcgdDemo()
    obj.setName('A')
    assert obj.getPort() == 8080
    assert obj.get_Port() is None and not obj.isPresent_Port()
    assert obj.get_RawNotPresentRequiredFields() == {'Port'}
    assert not obj.get_EffectiveNotPresentRequiredFields()
    assert not obj.is_ByRawValuesValid()
    assert obj.is_ByEffectiveValuesValid()
    assert json.loads(obj.toString_RawValues(F.JSON_STANDARD, M.NO_REFERENCES)) == {'Name': 'A'}
    assert json.loads(obj.toString_EffectiveValues(F.JSON_STANDARD, M.NO_REFERENCES)) == {'Name': 'A', 'Port': 8080}
    obj.setPort(None)
    assert obj.isPresent_Port() and not obj.is_ByEffectiveValuesValid()
    obj.unset_Port()
    assert obj.getPort() == 8080 and not obj.isPresent_Port()


def test_deep_validation_cyclic_identity_and_diagnostic():
    a = AIcgdDemo()
    a.setName('A')
    b = AIcgdDemo()
    a.setChild(b)
    b.setChild(a)
    assert a.is_ByEffectiveValuesValid()
    assert not a.is_DeeplyByEffectiveValuesValid()
    assert 'Child.Name' in a.get_DeeplyEffectiveNotPresentRequiredFields()
    from eu.algites.lib.data.smartdataobject.aix_smart_data_validation_exception import AIxSmartDataValidationException
    with pytest.raises(AIxSmartDataValidationException):
        a.toString_EffectiveValues(F.JSON_DOTNET_PRESERVE, M.REPEATED_OBJECTS)
    b.setName('B')
    rendered = json.loads(a.toString_EffectiveValues(F.JSON_DOTNET_PRESERVE, M.REPEATED_OBJECTS))
    assert rendered['$id'] == '1'
    assert rendered['Child']['Child']['$ref'] == '1'
    assert 'dNrRef' in str(a)
    with pytest.raises(ValueError, match='cyclic'):
        a.toString_RawValues(F.JSON_STANDARD, M.NO_REFERENCES)


def test_shared_identity_without_cycle():
    a = AIcgdDemo(); a.setName('A')
    same = ['a very long string repeated many times']
    a.setChild([same, same])
    rendered = json.loads(a.toString_RawValues(F.JSON_DIAGNOSTIC, M.REPEATED_OBJECTS))
    assert 'dNrRef' in json.dumps(rendered)


def test_read_only_structured_default_is_serialized_not_stringified():
    from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject

    class AIigOptions(AIiDataObject):
        __data_object_fields__ = (
            AIcdDataObjectField('Host', presence_required=True, expected_type=str),
            AIcdDataObjectField('Secure', presence_required=True, expected_type=bool),
        )
        def getHost(self): return 'localhost'
        def getSecure(self): return True

    class AIcgdWithDefault(AIcSmartDataObject):
        __data_object_fields__ = (AIcdDataObjectField('Options', default_factory=AIigOptions),)
        def getOptions(self): return self.get_EffectiveField('Options')

    dto = AIcgdWithDefault()
    assert dto.getOptions().getHost() == 'localhost'
    assert json.loads(dto.toString_RawValues(F.JSON_STANDARD, M.NO_REFERENCES)) == {}
    assert json.loads(dto.toString_EffectiveValues(F.JSON_STANDARD, M.NO_REFERENCES)) == {
        'Options': {'Host': 'localhost', 'Secure': True}}
    assert not dto.isPresent_Field('Options')


def test_yaml_native_cycles_are_parseable():
    yaml = pytest.importorskip('yaml')
    a = AIcgdDemo()
    a.setName('Alice')
    a.setChild(a)
    for style in (F.YAML_NATIVE_REFERENCES, F.YAML_DIAGNOSTIC):
        text = a.toString_RawValues(style, M.REPEATED_OBJECTS)
        data = yaml.safe_load(text)
        assert '&o1' in text and '*o1' in text
        if style is F.YAML_NATIVE_REFERENCES:
            assert data['Child'] is data
        else:
            assert data['$fields']['Child']['value'] is data


def test_xml_namespaced_fields_and_raw_document():
    import xml.etree.ElementTree as ET
    class AIcgdXml(AIcSmartDataObject):
        __xml_local_name__ = 'Settings'
        __xml_namespace__ = 'urn:settings'
        __data_object_fields__ = (
            AIcdDataObjectField('Name', presence_required=True, xml_namespace='urn:field'),)
    obj = AIcgdXml()
    obj.set_RawField('Name', 'A&B')
    xml = obj.toString_EffectiveValues(F.XML_XSD, M.NO_REFERENCES)
    root = ET.fromstring(xml)
    assert root.tag == '{urn:settings}Settings'
    assert root.find('{urn:field}Name').text == 'A&B'

"""xs:any XML extension elements preserve their complete namespace-aware subtree."""
import xml.etree.ElementTree as ET
import pytest

from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object import AIaXmlDataObject
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind
from eu.algites.lib.data.dataobject.xmldefs.aic_xml_wildcard_fragment import AIcXmlWildcardFragment
from eu.algites.lib.data.smartdataobject.impl.xmldefs.aic_xml_smart_data_object import AIcXmlSmartDataObject
from eu.algites.lib.text.format.ais_text_format_profiles import AIsTextFormatProfiles


class AIcgdWildcardExtensions(AIcXmlSmartDataObject):
    __xml_data_object__ = AIaXmlDataObject(namespace='urn:builder', namespace_local_name='Extensions')
    __data_object_fields__ = (
        AIcdDataObjectField('Key', expected_type=str),
        AIcdDataObjectField('AnyXmlElement2', expected_type=list),
    )
    __xml_data_object_fields__ = {
        'Key': AIaXmlDataObjectField(namespace='urn:builder', namespace_local_name='Key', order=0),
        'AnyXmlElement2': AIaXmlDataObjectField(namespace='urn:builder',
            namespace_local_name='AnyXmlElement2', value_kind=AInXmlValueKind.ELEMENT,
            order=1, min_occurs=0, max_occurs=-1, wildcard_namespace='##other',
            wildcard_process_contents='lax'),
    }
    __xml_repeated_fields__ = ('AnyXmlElement2',)


XML = ('<b:Extensions xmlns:b="urn:builder" xmlns:x="urn:plugin">'
       '<b:Key>example</b:Key>'
       '<x:Plugin active="true"><x:Child>value</x:Child></x:Plugin>'
       '<x:Other><x:Item code="X"/></x:Other>'
       '</b:Extensions>')


def test_xs_any_uses_element_wire_kind_with_independent_namespace_constraint():
    metadata = AIcgdWildcardExtensions.__xml_data_object_fields__['AnyXmlElement2']
    assert metadata.value_kind is AInXmlValueKind.ELEMENT
    assert metadata.wildcard_namespace == '##other'
    assert len(AInXmlValueKind) == 2


def test_wildcard_round_trip_keeps_nested_attributes_and_elements():
    result = AIcgdWildcardExtensions.from_xml(XML)
    values = result.get_RawField('AnyXmlElement2')
    assert len(values) == 2
    assert all(isinstance(value, AIcXmlWildcardFragment) for value in values)
    assert ET.fromstring(values[0].xml_fragment).find('{urn:plugin}Child').text == 'value'
    output = result.toString_RawValues(AIsTextFormatProfiles.XML_XSD)
    root = ET.fromstring(output)
    assert [node.tag for node in root] == [
        '{urn:builder}Key', '{urn:plugin}Plugin', '{urn:plugin}Other']
    assert root[1].attrib['active'] == 'true'
    assert root[1][0].text == 'value'
    assert root[2][0].attrib['code'] == 'X'
    assert AIcgdWildcardExtensions.from_xml(output).to_mapping() == result.to_mapping()


def test_wildcard_rejects_foreign_target_namespace_and_unsafe_fragment():
    with pytest.raises(ValueError, match='Unknown XML element'):
        AIcgdWildcardExtensions.from_xml(XML.replace('x:Plugin', 'b:Plugin'))
    obj = AIcgdWildcardExtensions()
    obj.set_RawField('Key', 'example')
    obj.set_RawField('AnyXmlElement2', ['<!DOCTYPE e [<!ENTITY a "b">]><e/>'])
    with pytest.raises(ValueError, match='DTD or entity'):
        obj.toString_RawValues(AIsTextFormatProfiles.XML_XSD)


@pytest.mark.parametrize('constraint,child', [
    ('##targetNamespace', '<b:Plugin/>'),
    ('urn:plugin', '<x:Plugin/>'),
])
def test_typed_xml_namespace_wildcard_returns_namespace_interface(constraint, child):
    """Runtime xs:any objects implement their generated, namespace-specific contract."""
    from eu.algites.lib.data.dataobject.xmldefs.aii_xml_data_object_namespace import AIiXmlDataObjectNamespace

    class AIig_xnXmlDataObjectTest(AIiXmlDataObjectNamespace):
        __xml_data_object__ = AIaXmlDataObject(namespace='urn:builder')

    class AIig_xnXmlDataObjectPlugin(AIiXmlDataObjectNamespace):
        __xml_data_object__ = AIaXmlDataObject(namespace='urn:plugin')

    selected = (AIig_xnXmlDataObjectTest if constraint == '##targetNamespace'
                else AIig_xnXmlDataObjectPlugin)

    class AIcgdTypedWildcardExtensions(AIcgdWildcardExtensions):
        AnyXmlElement2: tuple[selected, ...]
        __xml_data_object_fields__ = {
            **AIcgdWildcardExtensions.__xml_data_object_fields__,
            'AnyXmlElement2': AIaXmlDataObjectField(namespace='urn:builder',
                namespace_local_name='AnyXmlElement2', value_kind=AInXmlValueKind.ELEMENT,
                order=1, min_occurs=0, max_occurs=-1, wildcard_namespace=constraint,
                wildcard_process_contents='lax'),
        }

    xml = ('<b:Extensions xmlns:b="urn:builder" xmlns:x="urn:plugin">'
           '<b:Key>example</b:Key>' + child + '</b:Extensions>')
    parsed = AIcgdTypedWildcardExtensions.from_xml(xml)
    wildcard = parsed.get_RawField('AnyXmlElement2')[0]
    assert isinstance(wildcard, selected)
    assert isinstance(wildcard, AIiXmlDataObjectNamespace)
    result = ET.fromstring(parsed.toString_RawValues(AIsTextFormatProfiles.XML_XSD))
    assert result[1].tag == ('{urn:builder}Plugin' if constraint == '##targetNamespace'
                             else '{urn:plugin}Plugin')

"""XML Schema maxOccurs is encoded as repeated QName siblings, not Item wrappers."""
import xml.etree.ElementTree as ET

from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object import AIaXmlDataObject
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.text.format.ais_text_format_profiles import AIsTextFormatProfiles


class AIcgdRepeatedXml(AIcSmartDataObject):
    __xml_data_object__ = AIaXmlDataObject(namespace='urn:orders', namespace_local_name='Order')
    __data_object_fields__ = (
        AIcdDataObjectField('Code', expected_type=str),
        AIcdDataObjectField('Entry'),
        AIcdDataObjectField('Empty'),
    )
    __xml_data_object_fields__ = {
        'Code': AIaXmlDataObjectField(namespace='urn:orders', namespace_local_name='Code', order=0),
        'Entry': AIaXmlDataObjectField(namespace='urn:orders', namespace_local_name='Entry', order=1),
        'Empty': AIaXmlDataObjectField(namespace='urn:orders', namespace_local_name='Empty', order=2),
    }


def test_repeated_xml_elements_are_siblings_with_original_qnames():
    instance = AIcgdRepeatedXml()
    instance.set_RawField('Code', 'A')
    instance.set_RawField('Entry', ['first', 'second'])
    instance.set_RawField('Empty', [])
    document = ET.fromstring(instance.toString_RawValues(AIsTextFormatProfiles.XML_XSD))
    assert document.tag == '{urn:orders}Order'
    assert [(e.tag, e.text) for e in document] == [
        ('{urn:orders}Code', 'A'),
        ('{urn:orders}Entry', 'first'),
        ('{urn:orders}Entry', 'second'),
    ]
    assert all(e.tag != 'Item' for e in document.iter())


def test_xml_boolean_and_enum_lexical_values():
    from enum import Enum

    class WireToken(Enum):
        ENABLED = 'enabled'

    class AIcgdLexical(AIcSmartDataObject):
        __data_object_fields__ = (
            AIcdDataObjectField('Enabled'),
            AIcdDataObjectField('Token'),
        )
        __xml_data_object_fields__ = {
            'Enabled': AIaXmlDataObjectField(namespace_local_name='Enabled'),
            'Token': AIaXmlDataObjectField(namespace_local_name='Token'),
        }

    example = AIcgdLexical()
    example.set_RawField('Enabled', True)
    example.set_RawField('Token', WireToken.ENABLED)
    document = ET.fromstring(example.toString_RawValues(AIsTextFormatProfiles.XML_XSD))
    assert document.find('Enabled').text == 'true'
    assert document.find('Token').text == 'enabled'


def test_enum_item_code_overrides_python_enum_value():
    """Serialize AIiEnumItem.code(), not an internal Enum.name or Enum.value."""
    from enum import Enum
    from eu.algites.lib.common.enums.aii_enum_item import AIiEnumItem

    class AInWireToken(str, Enum):
        APPROVED = 'implementation-token'

        def code(self) -> str:
            """Return the separately specified document-level enum code."""
            return 'approved-wire'

    assert isinstance(AInWireToken.APPROVED, AIiEnumItem)
    class AIcgdCodeValue(AIcSmartDataObject):
        """Minimal XML contract containing an enum-coded field."""
        __data_object_fields__ = (AIcdDataObjectField('Status'),)
        __xml_data_object_fields__ = {
            'Status': AIaXmlDataObjectField(namespace_local_name='Status'),
        }

    value = AIcgdCodeValue()
    value.set_RawField('Status', AInWireToken.APPROVED)
    document = ET.fromstring(value.toString_RawValues(AIsTextFormatProfiles.XML_XSD))
    assert document.find('Status').text == 'approved-wire'
    assert value.to_mapping()['Status'] == 'approved-wire'

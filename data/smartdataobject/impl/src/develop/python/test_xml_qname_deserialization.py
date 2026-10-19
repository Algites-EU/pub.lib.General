"""QName-aware typed XML parsing regression coverage for generated SDO bases."""

from decimal import Decimal
from enum import Enum
import pytest

from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object import AIaXmlDataObject
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind
from eu.algites.lib.data.smartdataobject.impl.xmldefs.aic_xml_smart_data_object import AIcXmlSmartDataObject
from eu.algites.lib.text.format.ais_text_format_profiles import AIsTextFormatProfiles


class AInStatus(Enum):
    APPROVED = 'approved-wire'


class AIcgdAddress(AIcXmlSmartDataObject):
    __xml_data_object__ = AIaXmlDataObject(namespace='urn:internal', namespace_local_name='AddressType')
    __data_object_fields__ = (AIcdDataObjectField('City', expected_type=str),)
    __xml_data_object_fields__ = {'City': AIaXmlDataObjectField(namespace='urn:test', namespace_local_name='City')}


class AIcgdOrder(AIcXmlSmartDataObject):
    __xml_data_object__ = AIaXmlDataObject(namespace='urn:test', namespace_local_name='Order')
    __data_object_fields__ = (
        AIcdDataObjectField('Code', expected_type=str),
        AIcdDataObjectField('Enabled', expected_type=bool),
        AIcdDataObjectField('Total', expected_type=Decimal),
        AIcdDataObjectField('Status', expected_type=AInStatus),
        AIcdDataObjectField('Entry', expected_type=list),
        AIcdDataObjectField('Address', expected_type=AIcgdAddress),
    )
    __xml_data_object_fields__ = {
        'Code': AIaXmlDataObjectField(namespace_local_name='code', value_kind=AInXmlValueKind.ATTRIBUTE),
        'Enabled': AIaXmlDataObjectField(namespace='urn:test', namespace_local_name='Enabled'),
        'Total': AIaXmlDataObjectField(namespace='urn:test', namespace_local_name='Total'),
        'Status': AIaXmlDataObjectField(namespace='urn:test', namespace_local_name='Status'),
        'Entry': AIaXmlDataObjectField(namespace='urn:test', namespace_local_name='Entry'),
        'Address': AIaXmlDataObjectField(namespace='urn:test', namespace_local_name='Address'),
    }
    __xml_field_types__ = {'Address': AIcgdAddress}
    __xml_repeated_fields__ = ('Entry',)


XML = '''<e:Order xmlns:e="urn:test" code="A">
    <e:Address><e:City>Prague</e:City></e:Address>
    <e:Entry>one</e:Entry><e:Status>approved-wire</e:Status>
    <e:Enabled>1</e:Enabled><e:Entry>two</e:Entry>
    <e:Total>1.50</e:Total>
</e:Order>'''


def test_qname_parse_order_independent_typed_nested_and_repeated():
    result = AIcgdOrder.from_xml(XML)
    assert result.get_RawField('Code') == 'A'
    assert result.get_RawField('Enabled') is True
    assert result.get_RawField('Total') == Decimal('1.50')
    assert result.get_RawField('Status') == AInStatus.APPROVED
    assert result.get_RawField('Entry') == ['one', 'two']
    assert isinstance(result.get_RawField('Address'), AIcgdAddress)
    assert result.get_RawField('Address').get_RawField('City') == 'Prague'


def test_xml_round_trip_preserves_field_values():
    instance = AIcgdOrder.from_xml(XML)
    emitted = instance.toString_RawValues(AIsTextFormatProfiles.XML_XSD)
    decoded = AIcgdOrder.from_xml(emitted)
    assert decoded.to_mapping() == instance.to_mapping()


def test_xsi_nil_and_explicit_false():
    xml = ('<Order xmlns="urn:test" code="A" '
           'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'
           '<Total xsi:nil="true"/>'
           '<Enabled xsi:nil="false">false</Enabled>'
           '</Order>')
    parsed = AIcgdOrder.from_xml(xml)
    assert parsed.get_RawField('Total') is None
    assert parsed.get_RawField('Enabled') is False


def test_missing_repeated_field_is_empty_list():
    parsed = AIcgdOrder.from_xml('<Order xmlns="urn:test" code="A"/>')
    assert parsed.get_RawField('Entry') == []


@pytest.mark.parametrize('xml,reason', [
    ('<Order xmlns="urn:bad"/>', 'QName'),
    ('<Order xmlns="urn:test"><Unknown/></Order>', 'Unknown XML element'),
    ('<Order xmlns="urn:test" surprise="yes"/>', 'Unknown XML attribute'),
    ('<Order xmlns="urn:test"><Enabled>true</Enabled><Enabled>false</Enabled></Order>', 'Duplicate'),
    ('<Order xmlns="urn:test"><Enabled>maybe</Enabled></Order>', 'boolean'),
    ('<Order xmlns="urn:test"><Entry><Unexpected/></Entry></Order>', 'nested'),
    ('<!DOCTYPE Order [<!ENTITY x "bad">]><Order xmlns="urn:test"/>', 'DTD'),
])
def test_rejects_unsafe_or_ambiguous_xml(xml, reason):
    with pytest.raises((ValueError, TypeError), match=reason):
        AIcgdOrder.from_xml(xml)


class AIcgdBoundedXml(AIcXmlSmartDataObject):
    """Runtime fixture for exact XML occurrence limits independent of field presence."""
    __xml_data_object__ = AIaXmlDataObject(namespace='urn:bounded', namespace_local_name='Root')
    __data_object_fields__ = (
        AIcdDataObjectField('Required', expected_type=str),
        AIcdDataObjectField('Entry', expected_type=list),
    )
    __xml_data_object_fields__ = {
        'Required': AIaXmlDataObjectField(namespace='urn:bounded', namespace_local_name='Required',
                                          min_occurs=1, max_occurs=1),
        'Entry': AIaXmlDataObjectField(namespace='urn:bounded', namespace_local_name='Entry',
                                       min_occurs=1, max_occurs=2),
    }
    __xml_repeated_fields__ = ('Entry',)


def test_xml_parser_enforces_wire_cardinalities():
    """A required XML node and a bounded sibling list must respect their source XSD interval."""
    good = '<Root xmlns="urn:bounded"><Entry>A</Entry><Required>X</Required><Entry>B</Entry></Root>'
    result = AIcgdBoundedXml.from_xml(good)
    assert result.get_RawField('Required') == 'X'
    assert result.get_RawField('Entry') == ['A', 'B']
    with pytest.raises(ValueError, match='Required.*minimum'):
        AIcgdBoundedXml.from_xml('<Root xmlns="urn:bounded"><Entry>A</Entry></Root>')
    with pytest.raises(ValueError, match='Entry.*minimum'):
        AIcgdBoundedXml.from_xml('<Root xmlns="urn:bounded"><Required>X</Required></Root>')
    with pytest.raises(ValueError, match='Entry.*maximum'):
        AIcgdBoundedXml.from_xml('<Root xmlns="urn:bounded"><Required>X</Required>'
            '<Entry>A</Entry><Entry>B</Entry><Entry>C</Entry></Root>')


def test_xml_occurrence_metadata_rejects_invalid_intervals():
    """Generated wire intervals must not contain negative or inverted finite bounds."""
    with pytest.raises(ValueError, match='min_occurs exceeds max_occurs'):
        AIaXmlDataObjectField(min_occurs=2, max_occurs=1)
    with pytest.raises(ValueError, match='bound'):
        AIaXmlDataObjectField(min_occurs=-2)

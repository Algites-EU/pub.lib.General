"""XML roundtrip for embedded, named/reused structural groups without wrappers."""
import xml.etree.ElementTree as ET
import pytest

from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object import AIaXmlDataObject
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
from eu.algites.lib.data.smartdataobject.impl.xmldefs.aic_xml_smart_data_object import AIcXmlSmartDataObject
from eu.algites.lib.text.format.ais_text_format_profiles import AIsTextFormatProfiles


class PersonIdentity(AIcXmlSmartDataObject):
    __data_object_fields__ = (
        AIcdDataObjectField('FirstName', expected_type=str, min_item_count=1),
        AIcdDataObjectField('LastName', expected_type=str, min_item_count=1),
    )
    __xml_data_object_fields__ = {
        'FirstName': AIaXmlDataObjectField(namespace='urn:person', namespace_local_name='FirstName'),
        'LastName': AIaXmlDataObjectField(namespace='urn:person', namespace_local_name='LastName'),
    }


class Employee(AIcXmlSmartDataObject):
    __xml_data_object__ = AIaXmlDataObject(namespace='urn:person', namespace_local_name='Employee')
    __data_object_fields__ = (
        AIcdDataObjectField('', semantic_id='IdentityGroup1', embedded=True, expected_type=PersonIdentity,
                            min_item_count=1, max_item_count=1),
        AIcdDataObjectField('', semantic_id='IdentityGroup2', embedded=True, expected_type=PersonIdentity,
                            min_item_count=1, max_item_count=1),
        AIcdDataObjectField('Department', expected_type=str),
    )
    __xml_data_object_fields__ = {
        'Department': AIaXmlDataObjectField(namespace='urn:person', namespace_local_name='Department'),
    }
    __xml_field_types__ = {'IdentityGroup1': PersonIdentity, 'IdentityGroup2': PersonIdentity}


class RepeatedEmployee(AIcXmlSmartDataObject):
    __xml_data_object__ = AIaXmlDataObject(namespace='urn:person', namespace_local_name='Employee')
    __data_object_fields__ = (
        AIcdDataObjectField('', semantic_id='Identities', embedded=True, expected_type=list,
                            min_item_count=2, max_item_count=2),
        AIcdDataObjectField('Department', expected_type=str),
    )
    __xml_data_object_fields__ = {'Department': AIaXmlDataObjectField(
        namespace='urn:person', namespace_local_name='Department')}
    __xml_field_types__ = {'Identities': PersonIdentity}
    __xml_repeated_fields__ = ('Identities',)


SAMPLE = '''<Employee xmlns="urn:person">
  <FirstName>John</FirstName><LastName>Smith</LastName>
  <FirstName>Jane</FirstName><LastName>Doe</LastName>
  <Department>IT</Department>
</Employee>'''


def test_two_distinct_embedded_occurrences_roundtrip():
    employee = Employee.from_xml(SAMPLE)
    first = employee.get_RawField('IdentityGroup1')
    second = employee.get_RawField('IdentityGroup2')
    assert type(first) is type(second) is PersonIdentity
    assert first.get_RawField('FirstName') == 'John'
    assert second.get_RawField('FirstName') == 'Jane'
    assert employee.get_RawField('Department') == 'IT'
    emitted = employee.toString_RawValues(AIsTextFormatProfiles.XML_XSD)
    root = ET.fromstring(emitted)
    assert [el.tag.rsplit('}', 1)[-1] for el in root] == [
        'FirstName', 'LastName', 'FirstName', 'LastName', 'Department']
    assert Employee.from_xml(emitted).get_RawField('IdentityGroup2').get_RawField('LastName') == 'Doe'


def test_one_embedded_repeating_group_roundtrip():
    employee = RepeatedEmployee.from_xml(SAMPLE)
    assert [i.get_RawField('LastName') for i in employee.get_RawField('Identities')] == ['Smith', 'Doe']
    emitted = employee.toString_RawValues(AIsTextFormatProfiles.XML_XSD)
    assert len(ET.fromstring(emitted)) == 5
    assert len(RepeatedEmployee.from_xml(emitted).get_RawField('Identities')) == 2


def test_missing_repeated_group_rejected():
    with pytest.raises(ValueError, match='minimum'):
        RepeatedEmployee.from_xml('<Employee xmlns="urn:person"><Department>IT</Department></Employee>')

# Immutable alternatives generated for nested xs:choice are not mutable SDOs.
# Their read-only closed base provides explicit lazy import identities, while
# the enclosing SDO getter remains an EMBEDDED field without any XML QName.
from dataclasses import dataclass


class IdentitySelection:
    """Read-only closed choice for one of Value or Name+Regexp."""

    __xml_choice_variant_imports__ = (
        __name__ + ':ChoiceValue', __name__ + ':ChoicePair')


@dataclass(frozen=True)
class ChoiceValue(IdentitySelection):
    """Scalar variant without an XML wrapper around the choice."""

    __xml_choice_component_names__ = ('Value',)
    __xml_choice_first_qnames__ = ('{urn:person}Value',)
    __xml_data_object_fields__ = {
        'Value': AIaXmlDataObjectField(namespace='urn:person', namespace_local_name='Value'),
    }
    field1: str

    def getValue(self):
        """Return the selected literal."""
        return self.field1


@dataclass(frozen=True)
class ChoicePair(IdentitySelection):
    """Atomic sequence whose object boundary does not exist in XML."""

    __xml_choice_component_names__ = ('NameRegexp',)
    __xml_choice_first_qnames__ = ('{urn:person}FirstName',)
    field1: PersonIdentity

    def getNameRegexp(self):
        """Return the selected reusable group."""
        return self.field1


class ChoiceEmployee(AIcXmlSmartDataObject):
    """One transparent closed choice inside a generated-style SDO."""

    __xml_data_object__ = AIaXmlDataObject(namespace='urn:person', namespace_local_name='ChoiceEmployee')
    __xml_embedded_read_types__ = {'Choice': __name__ + ':IdentitySelection'}
    __data_object_fields__ = (AIcdDataObjectField('', semantic_id='Choice', embedded=True,
                              min_item_count=1, max_item_count=1),)


def test_closed_choice_scalar_roundtrip_without_wrapper():
    employee = ChoiceEmployee.from_xml(
        '<ChoiceEmployee xmlns="urn:person"><Value>hello</Value></ChoiceEmployee>')
    assert employee.get_RawField('Choice') == ChoiceValue('hello')
    emitted = employee.toString_RawValues(AIsTextFormatProfiles.XML_XSD)
    assert [node.tag.rsplit('}', 1)[-1] for node in ET.fromstring(emitted)] == ['Value']
    assert ChoiceEmployee.from_xml(emitted).get_RawField('Choice') == ChoiceValue('hello')


def test_closed_choice_atomic_pair_roundtrip_without_wrapper():
    employee = ChoiceEmployee.from_xml(
        '<ChoiceEmployee xmlns="urn:person">'
        '<FirstName>Alice</FirstName><LastName>Smith</LastName></ChoiceEmployee>',
        {'Choice.NameRegexp': PersonIdentity})
    choice = employee.get_RawField('Choice')
    assert isinstance(choice, ChoicePair)
    assert choice.getNameRegexp().get_RawField('FirstName') == 'Alice'
    emitted = employee.toString_RawValues(AIsTextFormatProfiles.XML_XSD)
    assert [node.tag.rsplit('}', 1)[-1] for node in ET.fromstring(emitted)] == [
        'FirstName', 'LastName']
    again = ChoiceEmployee.from_xml(emitted, {'Choice.NameRegexp': PersonIdentity})
    assert isinstance(again.get_RawField('Choice'), ChoicePair)
    assert again.get_RawField('Choice').getNameRegexp().get_RawField('LastName') == 'Smith'


def test_unselected_choice_branch_needs_no_concrete_nested_type():
    """An absent nested implementation must not invalidate another alternative."""
    employee = ChoiceEmployee.from_xml(
        '<ChoiceEmployee xmlns="urn:person"><Value>simple</Value></ChoiceEmployee>')
    assert employee.get_RawField('Choice') == ChoiceValue('simple')
    with pytest.raises(TypeError, match='concrete nested implementation'):
        ChoiceEmployee.from_xml(
            '<ChoiceEmployee xmlns="urn:person">'
            '<FirstName>Alice</FirstName><LastName>Smith</LastName>'
            '</ChoiceEmployee>')

"""Round-trip parity for schema-generated local xs:key/xs:unique SDO indexes."""

from __future__ import annotations

import pytest

from eu.algites.lib.common.list.aic_multi_mapped_list import AIcMultiMappedList
from eu.algites.lib.common.list.aii_multi_mapped_list import AIiMultiMappedList
from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object import AIaXmlDataObject
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
from eu.algites.lib.data.smartdataobject.impl.xmldefs.aic_xml_smart_data_object import AIcXmlSmartDataObject
from eu.algites.lib.text.format.ais_text_format_profiles import AIsTextFormatProfiles


class Identity(AIcXmlSmartDataObject):
    """Nested typed value used by a composite key."""

    __data_object_fields__ = (AIcdDataObjectField('Code', expected_type=int),)
    __xml_data_object_fields__ = {
        'Code': AIaXmlDataObjectField(namespace='urn:employees', namespace_local_name='code')}


class Employee(AIcXmlSmartDataObject):
    """Employee with one optional key and one required nested key component."""

    __data_object_fields__ = (
        AIcdDataObjectField('Department', expected_type=str),
        AIcdDataObjectField('Identity', expected_type=Identity),
        AIcdDataObjectField('Alias', expected_type=str),
    )
    __xml_data_object_fields__ = {
        'Department': AIaXmlDataObjectField(namespace='urn:employees', namespace_local_name='department'),
        'Identity': AIaXmlDataObjectField(namespace='urn:employees', namespace_local_name='identity'),
        'Alias': AIaXmlDataObjectField(namespace='urn:employees', namespace_local_name='alias'),
    }
    __xml_field_types__ = {'Identity': Identity}


class StaffContract:
    """Generated Defs read contract: key metadata belongs to the inherited API."""

    __xml_repeated_fields__ = ('employee',)
    __xml_key_definitions__ = {
        'employee': (
            ('ByDepartmentAndCode', ('department', 'identity.code'), True),
            ('UniqueAlias', ('alias',), False),
        )
    }


class Staff(AIcXmlSmartDataObject, StaffContract):
    """Generated-style mutable implementation with inherited key metadata."""

    __data_object_fields__ = (AIcdDataObjectField('employee', expected_type=AIiMultiMappedList),)
    __xml_data_object_fields__ = {
        'employee': AIaXmlDataObjectField(namespace='urn:employees', namespace_local_name='employee',
                                          min_occurs=0, max_occurs=-1)}
    __xml_field_types__ = {'employee': Employee}


class Organization(AIcXmlSmartDataObject):
    """Root containing two independent keyed staff collections."""

    __xml_data_object__ = AIaXmlDataObject(namespace='urn:employees', namespace_local_name='organization')
    __data_object_fields__ = (AIcdDataObjectField('staff', expected_type=list),)
    __xml_data_object_fields__ = {
        'staff': AIaXmlDataObjectField(namespace='urn:employees', namespace_local_name='staff')}
    __xml_field_types__ = {'staff': Staff}
    __xml_repeated_fields__ = ('staff',)


XML = '''<organization xmlns="urn:employees">
  <staff>
    <employee><department>north</department><identity><code>003</code></identity><alias>A</alias></employee>
    <employee><department>south</department><identity><code>3</code></identity></employee>
  </staff>
  <staff>
    <employee><department>north</department><identity><code>3</code></identity><alias>A</alias></employee>
  </staff>
</organization>'''


def test_local_xsd_indexes_are_typed_and_independent():
    """Same composite key in independent owners is valid and resolves to typed integers."""
    root = Organization.from_xml(XML)
    a, b = root.get_RawField('staff')
    employees_a = a.get_RawField('employee')
    employees_b = b.get_RawField('employee')
    assert isinstance(employees_a, AIcMultiMappedList)
    assert isinstance(employees_b, AIcMultiMappedList)
    key_a = employees_a.key_definition('ByDepartmentAndCode')
    key_b = employees_b.key_definition('ByDepartmentAndCode')
    assert key_a is not key_b
    assert employees_a.map_by_key_type(key_a)[('north', 3)][0].get_RawField('Department') == 'north'
    assert employees_b.map_by_key_type(key_b)[('north', 3)][0].get_RawField('Department') == 'north'
    optional = employees_a.key_definition('UniqueAlias')
    assert employees_a.map_by_key_type(optional)['A'][0] is employees_a[0]
    assert employees_a.map_by_key_type(optional)[None][0] is employees_a[1]


def test_local_xsd_indexes_survive_xml_round_trip():
    """XML writes repeated siblings, and parsing rebuilds the indexes per owner."""
    source = Organization.from_xml(XML)
    xml = source.toString_RawValues(AIsTextFormatProfiles.XML_XSD)
    restored = Organization.from_xml(xml)
    for owner in restored.get_RawField('staff'):
        employees = owner.get_RawField('employee')
        assert isinstance(employees, AIcMultiMappedList)
        key = employees.key_definition('ByDepartmentAndCode')
        assert len(employees.map_by_key_type(key)) == len(employees)
    assert len(restored.get_RawField('staff')[0].get_RawField('employee')) == 2


def test_duplicate_composite_xsd_key_rejected_atomically():
    """Duplicate typed composite values fail rather than replace an existing entry."""
    root = Organization.from_xml(XML)
    employees = root.get_RawField('staff')[0].get_RawField('employee')
    with pytest.raises(ValueError, match='Duplicate unique key'):
        employees.append(employees[0])
    assert len(employees) == 2
    invalid = XML.replace('<department>south</department>', '<department>north</department>')
    with pytest.raises(ValueError, match='Duplicate unique key'):
        Organization.from_xml(invalid)


def test_required_composite_key_must_have_all_components():
    """Missing nested values may not enter a mandatory xs:key index."""
    invalid = XML.replace('<code>003</code>', '<code></code>')
    with pytest.raises((ValueError, TypeError)):
        Organization.from_xml(invalid)

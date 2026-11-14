"""XML keys select a list in a shared named TYPE without cloning that TYPE."""

import pytest
from eu.algites.lib.common.list.aii_multi_mapped_list import AIiMultiMappedList
from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object import AIaXmlDataObject
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind
from eu.algites.lib.data.smartdataobject.impl.xmldefs.aic_xml_smart_data_object import AIcXmlSmartDataObject


class Entry(AIcXmlSmartDataObject):
    """One named list entry carrying a scalar key attribute."""

    __data_object_fields__ = (AIcdDataObjectField('name', expected_type=str),)
    __xml_data_object_fields__ = {'name': AIaXmlDataObjectField(
        namespace_local_name='name', value_kind=AInXmlValueKind.ATTRIBUTE)}


class SharedEntries(AIcXmlSmartDataObject):
    """One reusable object TYPE with a multi-map-capable but unkeyed list."""

    __data_object_fields__ = (AIcdDataObjectField('Entry', expected_type=AIiMultiMappedList),)
    __xml_data_object_fields__ = {'Entry': AIaXmlDataObjectField(namespace_local_name='Entry')}
    __xml_field_types__ = {'Entry': Entry}
    __xml_repeated_fields__ = ('Entry',)


class Root(AIcXmlSmartDataObject):
    """Two occurrences of SharedEntries; only the first declares an xs:key."""

    __xml_data_object__ = AIaXmlDataObject(namespace_local_name='Root')
    __data_object_fields__ = (
        AIcdDataObjectField('Keyed', expected_type=SharedEntries),
        AIcdDataObjectField('Unkeyed', expected_type=SharedEntries),
    )
    __xml_data_object_fields__ = {
        'Keyed': AIaXmlDataObjectField(namespace_local_name='Keyed'),
        'Unkeyed': AIaXmlDataObjectField(namespace_local_name='Unkeyed'),
    }
    __xml_field_types__ = {'Keyed': SharedEntries, 'Unkeyed': SharedEntries}
    __scoped_key_definitions__ = {'Keyed': (('Entry', 'ByName', ('name',), True),)}


def test_scoped_index_only_applies_to_selected_occurrence():
    """Both child values use one TYPE; the unkeyed one may contain duplicates."""
    root = Root.from_xml('''<Root>
      <Keyed><Entry name="A"/><Entry name="B"/></Keyed>
      <Unkeyed><Entry name="A"/><Entry name="A"/></Unkeyed>
    </Root>''')
    keyed = root.get_RawField('Keyed').get_RawField('Entry')
    unkeyed = root.get_RawField('Unkeyed').get_RawField('Entry')
    assert isinstance(keyed, AIiMultiMappedList)
    assert isinstance(unkeyed, AIiMultiMappedList)
    definition = keyed.key_definition('ByName')
    assert definition is not None
    assert len(keyed.map_by_key_type(definition)['A']) == 1
    assert unkeyed.key_definition('ByName') is None
    assert len(unkeyed) == 2
    assert type(root.get_RawField('Keyed')) is type(root.get_RawField('Unkeyed')) is SharedEntries


def test_duplicate_scoped_key_rejected_without_mutating_type():
    """A duplicate key within the keyed occurrence fails, unlike the unkeyed one."""
    with pytest.raises(ValueError, match='Duplicate unique key'):
        Root.from_xml('''<Root><Keyed><Entry name="A"/><Entry name="A"/></Keyed></Root>''')

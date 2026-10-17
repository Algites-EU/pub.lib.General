"""Regression tests for graph-preserving virtual SmartDataObject mappings."""
from __future__ import annotations

import pytest

from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.standard.ais_smart_data_object_factory import AIsSmartDataObjectFactory
from eu.algites.lib.data.smartdataobject.aiid_virtual_field_value import AIidVirtualFieldValue


class AIiB:
    """First view of a shared polymorphic object."""


class AIiC:
    """Second view of the same shared polymorphic object."""


class AIcgdD(AIcSmartDataObject, AIiB, AIiC):
    """A concrete type implementing both independently declared views."""
    __data_object_fields__ = (
        AIcdDataObjectField('Name', expected_type=str),
        AIcdDataObjectField('Friend', allows_null=True),
    )


class AIcgdHolder(AIcSmartDataObject):
    """A holder referencing one instance through two logical interfaces."""
    __data_object_fields__ = (
        AIcdDataObjectField('First', expected_type=AIiB),
        AIcdDataObjectField('Second', expected_type=AIiC),
        AIcdDataObjectField('Extras', allows_null=True),
    )


def test_virtual_mapping_preserves_polymorphic_identity_and_cycles():
    shared = AIcgdD()
    shared.set_RawField('Name', 'shared')
    holder = AIcgdHolder()
    holder.set_RawField('First', shared)
    holder.set_RawField('Second', shared)
    shared.set_RawField('Friend', holder)
    same_list = [shared]
    holder.set_RawField('Extras', [same_list, same_list])

    mapping = holder.toMapping_RawValues()
    assert all(isinstance(v, AIidVirtualFieldValue) for v in mapping.values())
    assert mapping['First'].getValueClass() is AIcgdD
    assert mapping['First'] is mapping['Second']
    assert mapping['First'].getValueRepresentation()['Friend'].getValueRepresentation() is mapping

    round_trip = AIsSmartDataObjectFactory.from_mapping(AIcgdHolder, mapping)
    first = round_trip.get_RawField('First')
    second = round_trip.get_RawField('Second')
    assert type(first) is AIcgdD
    assert first is second
    assert first.get_RawField('Friend') is round_trip
    assert round_trip.get_RawField('Extras')[0] is round_trip.get_RawField('Extras')[1]
    assert round_trip.get_RawField('Extras')[0][0] is first


def test_virtual_mapping_preserves_absent_vs_explicit_null():
    value = AIcgdD()
    assert 'Friend' not in value.toMapping_RawValues()
    value.set_RawField('Friend', None)
    mapping = value.toMapping_RawValues()
    assert mapping['Friend'].getValueRepresentation() is None
    loaded = AIsSmartDataObjectFactory.from_mapping(AIcgdD, mapping)
    assert loaded.isPresent_Field('Friend')
    assert loaded.get_RawField('Friend') is None


def test_factory_context_is_cleaned_after_failed_mapping():
    value = AIcgdD()
    value.set_RawField('Name', 'ok')
    mapping = value.toMapping_RawValues()
    mapping['Unknown'] = mapping['Name']
    with pytest.raises(ValueError, match='Unknown DataObject field'):
        AIsSmartDataObjectFactory.from_mapping(AIcgdD, mapping)
    mapping.pop('Unknown')
    restored = AIsSmartDataObjectFactory.from_mapping(AIcgdD, mapping)
    assert restored.get_RawField('Name') == 'ok'

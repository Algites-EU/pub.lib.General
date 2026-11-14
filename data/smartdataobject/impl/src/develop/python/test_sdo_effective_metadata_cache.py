"""Cache normalized inherited SDO field metadata without repeated getter introspection."""
import pytest

from eu.algites.lib.data.dataobject.aia_data_object_field import AIaDataObjectField
from eu.algites.lib.data.dataobject.aia_data_object_field_string_constraints import (
    AIaDataObjectFieldStringConstraints,
)
from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import (
    AIcSmartDataObject,
    _data_object_fields,
)


class Base(AIcSmartDataObject):
    """Handwritten read-only contract with schema-neutral restrictions."""

    @AIaDataObjectField(field_name='code', expected_type=str)
    @AIaDataObjectFieldStringConstraints(min_length=3)
    def getCode(self):
        return self.get_EffectiveField('Code')

    def setCode(self, value):
        self.set_RawField('Code', value)


class Child(Base):
    """A covariant projection that does not repeat string restrictions."""

    @AIaDataObjectField(expected_type=str)
    def getCode(self):
        return self.get_EffectiveField('Code')


def test_fields_are_cached_per_concrete_class_and_inherited_constraints_are_retained():
    base = Base._fields()
    child = Child._fields()
    assert base is Base._fields()
    assert child is Child._fields()
    assert base is not child
    assert child['Code'].field_name == 'code'
    assert child['Code'].string_constraints is base['Code'].string_constraints
    with pytest.raises(ValueError, match='String constraint'):
        Child().setCode('ab')
    obj = Child()
    obj.setCode('abc')
    assert obj.to_mapping() == {'code': 'abc'}


def test_custom_semantic_identity_provider_never_reuses_default_cache():
    default = _data_object_fields(Base)
    custom = _data_object_fields(Base, identity_provider=lambda _owner, _getter: 'Custom')
    assert default is Base._fields()
    assert custom is not default
    assert list(custom) == ['Custom']

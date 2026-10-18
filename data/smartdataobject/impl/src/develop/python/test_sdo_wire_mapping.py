from eu.algites.lib.data.dataobject.ain_field_presence_kind import AInFieldPresenceKind
"""Framework-neutral mapping for generated SmartDataObject values."""
import pytest
from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject


class AIcgdExample(AIcSmartDataObject):
    __data_object_fields__ = (AIcdDataObjectField('Name', presence=AInFieldPresenceKind.REQUIRED,
                                                  getter_name='getName'),)


def test_mapping_roundtrip_and_validation():
    obj=AIcgdExample.from_mapping({'Name':'A'})
    assert obj.to_mapping() == {'Name':'A'}
    assert AIcgdExample().to_mapping() == {}
    with pytest.raises(ValueError, match='Unknown DataObject'):
        AIcgdExample.from_mapping({'Unrecognized':'x'})
    with pytest.raises(Exception):
        AIcgdExample.from_mapping({})

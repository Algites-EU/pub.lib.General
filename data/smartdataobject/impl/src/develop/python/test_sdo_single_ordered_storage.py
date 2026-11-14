"""Regression checks for one ordered SDO store and integrated additional maps."""
import pytest

from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.data.dataobject.jsondefs.aia_json_data_object import AIaJsonDataObject
from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject


class _Person(AIcSmartDataObject):
    __data_object__ = AIcdDataObject()
    __json_data_object__ = AIaJsonDataObject(schema_url='https://example.org/person.schema.json',
                                    data_source_schema_url_field='$schema')
    __data_object_fields__ = (AIcdDataObjectField('firstName'),
                              AIcdDataObjectField('lastName'))


def test_declared_and_additional_values_share_one_insertion_ordered_store():
    person = _Person()
    person.set_RawField('firstName', 'Ada')
    extras = person.get_IntegratedAdditionalPropertiesMap(bool)
    assert extras is person.get_IntegratedAdditionalPropertiesMap(bool)
    extras['Enabled'] = True
    person.set_RawField('lastName', 'Lovelace')
    extras['Hidden'] = None
    assert list(person.to_mapping()) == ['$schema', 'firstName', 'Enabled', 'lastName', 'Hidden']
    assert list(extras) == ['Enabled', 'Hidden']
    assert len(person._values) == 4
    with pytest.raises(ValueError):
        extras['firstName'] = True
    with pytest.raises(ValueError):
        extras['$schema'] = True
    with pytest.raises(TypeError):
        extras['InvalidValue'] = 3
    assert person.get_RawField('firstName') == 'Ada'
    assert person.isPresent_Field('lastName')
    assert extras['Enabled'] is True
    assert extras['Hidden'] is None
    with pytest.raises(ValueError):
        person.get_IntegratedAdditionalPropertiesMap(str)


def test_bulk_update_is_atomic_and_declared_field_presence_is_not_shadowed():
    person = _Person()
    extras = person.get_IntegratedAdditionalPropertiesMap(bool)
    extras.update({'First': True, 'Second': False})
    with pytest.raises(ValueError):
        extras.update({'Third': True, 'lastName': True})
    assert list(extras) == ['First', 'Second']
    person.set_RawField('firstName', None)
    assert person.isPresent_Field('firstName')
    assert 'firstName' in person._values
    assert person.get_RawField('firstName') is None
    person.unset_Field('firstName')
    assert not person.isPresent_Field('firstName')
    assert list(extras) == ['First', 'Second']
    extras.clear()
    assert list(person._values) == []


def test_integrated_names_are_not_confused_with_semantic_names():
    class _Renamed(AIcSmartDataObject):
        __data_object_fields__ = (AIcdDataObjectField('Name', semantic_id='PersonName'),)
    result = _Renamed()
    result.set_RawField('PersonName', 'John')
    extras = result.get_IntegratedAdditionalPropertiesMap(str)
    extras['PersonName'] = 'additional key'
    assert result.get_RawField('PersonName') == 'John'
    assert extras['PersonName'] == 'additional key'
    with pytest.raises(ValueError):
        extras['Name'] = 'reserved wire name'
    assert result.to_mapping() == {'Name': 'John', 'PersonName': 'additional key'}


def test_json_and_yaml_rendering_include_integrated_map_values_in_storage_order():
    """All supported object serializers must use the same ordered SDO storage."""
    import json
    import yaml
    from eu.algites.lib.text.format.ais_text_format_profiles import AIsTextFormatProfiles
    from eu.algites.lib.text.format.ain_text_output_object_reference_mode import AInTextOutputObjectReferenceMode
    person = _Person()
    person.set_RawField('firstName', 'Ada')
    extras = person.get_IntegratedAdditionalPropertiesMap(bool)
    extras['Enabled'] = True
    person.set_RawField('lastName', 'Lovelace')
    extras['Unspecified'] = None
    expected = dict(person.to_mapping())
    for profile in (AIsTextFormatProfiles.JSON_STANDARD, AIsTextFormatProfiles.YAML_STANDARD):
        text = person.toString_RawValues(profile, AInTextOutputObjectReferenceMode.NO_REFERENCES)
        loaded = json.loads(text) if profile is AIsTextFormatProfiles.JSON_STANDARD else yaml.safe_load(text)
        assert loaded == expected
        assert list(loaded) == list(expected)

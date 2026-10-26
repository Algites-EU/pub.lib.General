"""Regression tests for schema metadata emitted into SDO instance documents."""
import pytest
from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject


class _SchemaUrlOnly(AIcSmartDataObject):
    __data_object__ = AIcdDataObject(schema_id='sample', schema_version=3,
                                    schema_url='https://example.test/sample.json',
                                    data_source_schema_url_field='$schema')
    __data_object_fields__ = (AIcdDataObjectField('Name'),)


class _ThreeFields(AIcSmartDataObject):
    __data_object__ = AIcdDataObject(schema_id='sample', schema_version=3,
                                    schema_url='https://example.test/sample.json',
                                    data_source_schema_url_field='url',
                                    data_source_schema_identifier_field='id',
                                    data_source_schema_version_field='version')
    __data_object_fields__ = (AIcdDataObjectField('Name'),)


def test_algites_default_emits_url_only_and_checks_input():
    instance = _SchemaUrlOnly.from_mapping({'Name': 'hello'})
    assert instance.to_mapping() == {'$schema': 'https://example.test/sample.json', 'Name': 'hello'}
    assert _SchemaUrlOnly.from_mapping(instance.to_mapping()).to_mapping() == instance.to_mapping()
    with pytest.raises(ValueError, match='Schema metadata does not match'):
        _SchemaUrlOnly.from_mapping({'$schema': 'https://wrong.test/', 'Name': 'hello'})


def test_custom_target_fields_emit_three_metadata_values():
    instance = _ThreeFields.from_mapping({'Name': 'hello'})
    assert instance.to_mapping() == {'url': 'https://example.test/sample.json',
                                     'id': 'sample', 'version': 3, 'Name': 'hello'}
    for invalid in ({'id': 'other'}, {'version': 4}, {'url': 'https://wrong.test'}):
        with pytest.raises(ValueError, match='Schema metadata does not match'):
            _ThreeFields.from_mapping({**instance.to_mapping(), **invalid})

class _ExplicitSchemaField(AIcSmartDataObject):
    __data_object__ = AIcdDataObject(schema_id='sample', schema_version=3,
                                    schema_url='https://example.test/sample.json',
                                    data_source_schema_url_field='$schema')
    __data_object_fields__ = (AIcdDataObjectField('$schema'), AIcdDataObjectField('Name'))


def test_schema_metadata_and_declared_field_are_merged_once_in_wire_outputs():
    import json
    from eu.algites.lib.text.format.ais_text_format_profiles import AIsTextFormatProfiles
    from eu.algites.lib.text.format.ain_text_output_object_reference_mode import AInTextOutputObjectReferenceMode
    obj = _ExplicitSchemaField.from_mapping({'Name': 'hello'})
    expected = {'$schema': 'https://example.test/sample.json', 'Name': 'hello'}
    assert obj.to_mapping() == expected
    text = obj.toString_RawValues(AIsTextFormatProfiles.JSON_STANDARD,
                                  AInTextOutputObjectReferenceMode.NO_REFERENCES)
    assert json.loads(text) == expected
    yaml_text = obj.toString_RawValues(AIsTextFormatProfiles.YAML_STANDARD,
                                       AInTextOutputObjectReferenceMode.NO_REFERENCES)
    assert yaml_text.count('"$schema"') == 1
    assert _ExplicitSchemaField.from_mapping(expected).to_mapping() == expected
    obj.set_RawField('$schema', 'https://not-valid.example/')
    with pytest.raises(ValueError, match='Schema metadata conflicts'):
        obj.to_mapping()
    with pytest.raises(ValueError, match='Schema metadata conflicts'):
        obj.toString_RawValues(AIsTextFormatProfiles.JSON_STANDARD,
                               AInTextOutputObjectReferenceMode.NO_REFERENCES)
    with pytest.raises(ValueError, match='Schema metadata conflicts'):
        obj.toString_RawValues(AIsTextFormatProfiles.YAML_STANDARD,
                               AInTextOutputObjectReferenceMode.NO_REFERENCES)

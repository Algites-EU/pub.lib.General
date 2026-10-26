"""Data-document schema metadata follow contract annotations, not guessed names."""
import json
import pytest

from eu.algites.lib.data.dataobject.aicd_data_object import AIcdDataObject
from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.text.format.ais_text_format_profiles import AIsTextFormatProfiles as F
from eu.algites.lib.text.format.ain_text_output_object_reference_mode import AInTextOutputObjectReferenceMode as M


class AIcgdWithSchema(AIcSmartDataObject):
    __data_object__ = AIcdDataObject(schema_id='logical-contract', schema_version=7,
        schema_url='https://example.invalid/contract_7.jsondef.schema.json',
        data_source_schema_url_field='$schema')
    __data_object_fields__ = (AIcdDataObjectField('Value', expected_type=str),)


class AIcgdWithAllMetadata(AIcgdWithSchema):
    __data_object__ = AIcdDataObject(schema_id='logical-contract', schema_version=7,
        schema_url='https://example.invalid/contract_7.jsondef.schema.json',
        data_source_schema_url_field='$schema',
        data_source_schema_identifier_field='$schemaId',
        data_source_schema_version_field='$schemaVersion')


class AIcgdWithCollision(AIcgdWithSchema):
    __data_object_fields__ = (AIcdDataObjectField('$schema', expected_type=str),)


def test_default_algites_schema_url_only_roundtrip():
    obj = AIcgdWithSchema.from_mapping({'Value': 'test'})
    expected = {'$schema': 'https://example.invalid/contract_7.jsondef.schema.json', 'Value': 'test'}
    assert obj.to_mapping() == expected
    assert json.loads(obj.toString_RawValues(F.JSON_STANDARD, M.NO_REFERENCES)) == expected
    assert '$schemaId' not in obj.to_mapping() and '$schemaVersion' not in obj.to_mapping()
    assert AIcgdWithSchema.from_mapping(expected).get_RawField('Value') == 'test'
    with pytest.raises(ValueError, match='does not match'):
        AIcgdWithSchema.from_mapping({'$schema': 'wrong', 'Value': 'test'})


def test_explicitly_enabled_schema_identifier_and_version():
    obj = AIcgdWithAllMetadata.from_mapping({'Value': 'test'})
    expected = {'$schema': 'https://example.invalid/contract_7.jsondef.schema.json',
                '$schemaId': 'logical-contract', '$schemaVersion': 7, 'Value': 'test'}
    assert obj.to_mapping() == expected
    assert json.loads(obj.toString_RawValues(F.JSON_STANDARD, M.NO_REFERENCES)) == expected
    assert AIcgdWithAllMetadata.from_mapping(expected).get_RawField('Value') == 'test'


def test_declared_schema_property_is_emitted_once_and_must_match():
    obj = AIcgdWithCollision()
    expected = {'$schema': 'https://example.invalid/contract_7.jsondef.schema.json'}
    assert obj.to_mapping() == expected
    assert json.loads(obj.toString_RawValues(F.JSON_STANDARD, M.NO_REFERENCES)) == expected
    obj.set_RawField('$schema', 'wrong')
    with pytest.raises(ValueError, match='conflicts'):
        obj.to_mapping()
    with pytest.raises(ValueError, match='conflicts'):
        obj.toString_RawValues(F.JSON_STANDARD, M.NO_REFERENCES)

"""Regression checks for explicitly tagged naming-profile Choice segments.

Product resources must already use the canonical tagged wire format, while
both Java and Python naming engines normalize each selected variant at input.
"""
from __future__ import annotations

from pathlib import Path
import json
import yaml
from jsonschema import Draft202012Validator
import pytest
from eu.algites.lib.naming.convention.impl.standard.aic_name_convention_catalog import AIcNameConventionCatalog

REPOSITORY = Path(__file__).resolve().parents[7]
SCHEMA = (REPOSITORY / 'naming/convention/intf/src/product/yamldefs/eu/algites/lib/naming/convention/'
          'common-naming-conventions_1.yamldef.schema.json')


def test_all_resource_segments_are_exclusively_named():
    """Every deployed General naming resource uses one declared Choice alternative."""
    checked = 0
    for path in REPOSITORY.rglob('*.yml'):
        if '/src/product/config/' not in path.as_posix() or 'Segments:' not in path.read_text():
            continue
        payload = yaml.safe_load(path.read_text())
        if not isinstance(payload, dict):
            continue
        for section in ('NameFragments', 'NameCompositions'):
            for definition in payload.get(section, {}).values():
                for segment in definition.get('Segments', []):
                    assert isinstance(segment, dict) and len(segment) == 1, path
                    variant, value = next(iter(segment.items()))
                    assert variant in {'NameFragmentsSegmentValue', 'NameFragmentsSegmentReference'}, path
                    assert isinstance(value, dict), path
                    checked += 1
        # Only independently scoped source files with naming segments are validated;
        # generated artifacts and build outputs are intentionally not inspected.
    assert checked >= 2000, checked


def test_tagged_resource_validates_as_standard_json_schema():
    """Unextended Draft 2020-12 validation recognizes each named alternative."""
    validator = Draft202012Validator(json.loads(SCHEMA.read_text()))
    directory = (REPOSITORY / 'naming/convention/profile/algites/src/product/config/'
                 'eu/algites/lib/naming/convention/sourcecode')
    for path in directory.glob('algites-*-name-conventions.yml'):
        value = yaml.safe_load(path.read_text())
        assert validator.is_valid(value), (path, list(validator.iter_errors(value))[:1])


def test_catalog_rejects_ambiguous_choice_wrapper():
    """The reader must not silently pick the first of two declared alternatives."""
    ambiguous = {'NameFragments': {'f': {'Segments': [{
        'NameFragmentsSegmentValue': {'Kind': 'constant_string', 'Value': 'a'},
        'NameFragmentsSegmentReference': {'Ref': 'other'},
    }]}}}
    with pytest.raises(ValueError, match='exactly one'):
        AIcNameConventionCatalog(ambiguous)


def test_catalog_normalizes_choice_without_mutating_input():
    """Keep naming operations agnostic of JSON/YAML serialization wrappers."""
    source = {'NameFragments': {'f': {'Segments': [
        {'NameFragmentsSegmentValue': {'Kind': 'constant_string', 'Value': 'A'}},
        {'NameFragmentsSegmentReference': {'Ref': 'g'}},
    ]}, 'g': {'Segments': [
        {'NameFragmentsSegmentValue': {'Kind': 'constant_string', 'Value': 'B'}}]}}}
    catalog = AIcNameConventionCatalog(source)
    assert catalog.composition('f') == (
        {'Kind': 'constant_string', 'Value': 'A'},
        {'Kind': 'constant_string', 'Value': 'B'})
    assert 'NameFragmentsSegmentValue' in source['NameFragments']['f']['Segments'][0]

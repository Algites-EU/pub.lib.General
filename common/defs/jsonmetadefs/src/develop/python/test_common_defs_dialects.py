"""Offline checks for the version-specific Algites Common Defs dialect meta-schemas."""

import json
from pathlib import Path

import pytest
from jsonschema import (
    Draft3Validator,
    Draft4Validator,
    Draft6Validator,
    Draft7Validator,
    Draft201909Validator,
    Draft202012Validator,
)
from jsonschema_specifications import REGISTRY
from referencing import Resource

BASE = Path(__file__).resolve().parents[2] / 'product/jsonmetadefs/eu/algites/lib/common/defs'
COMMON = 'https://defs.dev.algites.eu/api/jsonmetadefs/eu/algites/lib/common/defs'
VERSIONS = {
    'draft-03': Draft3Validator,
    'draft-04': Draft4Validator,
    'draft-06': Draft6Validator,
    'draft-07': Draft7Validator,
    '2019-09': Draft201909Validator,
    '2020-12': Draft202012Validator,
}


def load_version(draft):
    """Load this draft's syntax vocabulary and its composed dialect without network I/O."""
    dialect = json.loads((BASE / draft / 'dialect_1.jsonmetadef.schema.json').read_text())
    vocabulary = json.loads((BASE / draft / 'vocabulary_1.jsonmetadef.schema.json').read_text())
    registry = REGISTRY
    for document in (dialect, vocabulary):
        registry = registry.with_resource(
            document.get('$id', document.get('id')),
            Resource.from_contents(document),
        )
    return dialect, VERSIONS[draft](dialect, registry=registry)


@pytest.mark.parametrize('draft', VERSIONS)
def test_dialect_is_valid_and_published(draft):
    dialect, validator = load_version(draft)
    VERSIONS[draft].check_schema(dialect)
    assert dialect.get('$id', dialect.get('id')) == f'{COMMON}/{draft}/dialect_1.jsonmetadef.schema.json'
    assert dialect['x-algites-schema-id'] == f'eu.algites.lib.common.defs.{draft}.dialect'
    assert dialect['x-algites-schema-version'] == 1
    metadata = (BASE / draft / 'dialect_1.jsonmetadef.schema.json.meta.yml').read_text()
    assert f'GlobalPublicationPathId: eu/algites/lib/common/defs/{draft}/dialect_1.jsonmetadef.schema.json' in metadata
    assert validator.is_valid({
        'type': 'object', 'x-algites-schema-id': 'eu.algites.demo', 'x-algites-schema-version': 1
    })
    assert not validator.is_valid({'x-algites-schema-id': ''})
    assert not validator.is_valid({'x-algites-schema-version': 0})
    assert not validator.is_valid({'x-algites-schema-version': '1'})
    # Draft-03's official meta-schema permits arbitrary type-name strings.
    if draft != 'draft-03':
        assert not validator.is_valid({'type': 'unknown-type'})
    if draft not in ('draft-03', 'draft-04'):
        assert validator.is_valid(True)
        assert validator.is_valid(False)


@pytest.mark.parametrize('draft', VERSIONS)
def test_keyword_discovery_matches_draft(draft):
    schema, _ = load_version(draft)
    if draft in ('2019-09', '2020-12'):
        vocabulary = schema['$vocabulary']
        assert vocabulary[f'{COMMON}/vocabulary_1.md'] is False
        assert all(value is True for url, value in vocabulary.items() if url.startswith('https://json-schema.org/'))
        if draft == '2019-09':
            assert schema.get('$recursiveAnchor') is True
        else:
            assert schema.get('$dynamicAnchor') == 'meta'
    else:
        assert '$vocabulary' not in schema


def test_schema_reference_defs_use_common_dialect():
    src = Path(__file__).resolve().parents[2] / 'product'
    for kind, suffix in [('jsondefs', 'jsondef'), ('yamldefs', 'yamldef')]:
        path = src / kind / 'eu/algites/lib/common/defs/model' / f'schema-reference_1.{suffix}.schema.json'
        schema = json.loads(path.read_text())
        assert schema['$schema'] == f'{COMMON}/2020-12/dialect_1.jsonmetadef.schema.json'
        assert schema['x-algites-schema-id'] == 'eu.algites.lib.common.defs.schema-reference'
        assert schema['x-algites-schema-version'] == 1
        _, validator = load_version('2020-12')
        validator.validate(schema)

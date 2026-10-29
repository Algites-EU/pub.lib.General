"""Stand-alone regression tests for the shared, data-only Defs vocabulary."""
import json
from pathlib import Path
import pytest
from jsonschema import Draft3Validator, Draft4Validator, Draft6Validator, Draft7Validator, Draft201909Validator, Draft202012Validator

BASE = Path(__file__).resolve().parents[2] / 'product/jsonmetadefs/eu/algites/lib/common/defs'
DRAFTS = {'draft-03': Draft3Validator, 'draft-04': Draft4Validator,
          'draft-06': Draft6Validator, 'draft-07': Draft7Validator,
          '2019-09': Draft201909Validator, '2020-12': Draft202012Validator}

@pytest.mark.parametrize('draft', DRAFTS)
def test_vocabulary_has_exactly_two_schema_identity_keywords(draft):
    schema=json.loads((BASE/draft/'vocabulary_1.jsonmetadef.schema.json').read_text())
    DRAFTS[draft].check_schema(schema)
    assert set(schema['properties'])=={'x-algites-schema-id','x-algites-schema-version'}
    assert schema['properties']['x-algites-schema-id']['minLength']==1
    assert schema['properties']['x-algites-schema-version']['minimum']==1
    assert not any(key.startswith('$x-') for key in schema['properties'])
    assert (BASE/draft/'vocabulary_1.jsonmetadef.schema.json.meta.yml').is_file()


def test_published_vocabulary_semantic_uri():
    doc=(BASE/'vocabulary_1.md').read_text()
    uri='https://defs.dev.algites.eu/api/jsonmetadefs/eu/algites/lib/common/defs/vocabulary_1.md'
    assert uri in doc
    assert 'eu/algites/lib/common/defs/vocabulary_1.md' in (BASE/'vocabulary_1.md.meta.yml').read_text()

"""Regression tests for schema identifiers in shipped naming documents."""

from pathlib import Path
import json

import pytest
import yaml
from jsonschema import Draft202012Validator, FormatChecker
from referencing import Registry, Resource


_MODULE = Path(__file__).resolve().parents[3]
_CONFIG = _MODULE / 'src/product/config/eu/algites/lib/naming/convention'
_SCHEMAS = _MODULE.parent / 'intf/src/product'


@pytest.mark.parametrize('family,suffix', [
    ('yamldefs', 'yamldef'),
    ('jsondefs', 'jsondef'),
])
def test_naming_catalogs_accept_schema_metadata(family: str, suffix: str) -> None:
    """All shipped catalogs preserve the three-header convention and validate."""
    definitions = [json.loads(path.read_text(encoding='utf-8'))
                   for path in (_SCHEMAS / family).rglob(f'*.{suffix}.schema.json')]
    by_id = {item['$id']: item for item in definitions}
    registry = Registry().with_resources(
        (uri, Resource.from_contents(schema)) for uri, schema in by_id.items())
    for catalog in sorted(_CONFIG.glob('*.yml')):
        lines = catalog.read_text(encoding='utf-8').splitlines()
        if not lines[0].startswith('# yaml-language-server: $schema='):
            continue
        uri = lines[0].partition('=')[2]
        assert lines[1] == f'# $schema: {uri}'
        assert lines[2] == f'$schemaUrl: {uri}'
        selected_uri = uri.replace('/yamldefs/', f'/{family}/').replace(
            '.yamldef.schema.json', f'.{suffix}.schema.json')
        assert selected_uri in by_id
        document = yaml.safe_load(catalog.read_text(encoding='utf-8'))
        Draft202012Validator(by_id[selected_uri], registry=registry,
                             format_checker=FormatChecker()).validate(document)


def test_common_naming_schemas_explicitly_define_schema_metadata() -> None:
    """Derived source and DO/SDO documents may carry the schema URI as data."""
    for family, suffix in (('yamldefs', 'yamldef'), ('jsondefs', 'jsondef')):
        path = (_SCHEMAS / family / 'eu/algites/lib/naming/convention' /
                f'common-naming-conventions_1.{suffix}.schema.json')
        common = json.loads(path.read_text(encoding='utf-8'))
        assert common['properties']['$schemaUrl']['type'] == 'string'
        assert common['properties']['$schemaUrl']['format'] == 'uri'

@pytest.mark.parametrize('family', ['jsondef', 'yamldef', 'xmldef'])
def test_algites_schema_metadata_mapping(family: str) -> None:
    """Keep schema-dialect metadata distinct from instance schema references."""
    document = yaml.safe_load((_CONFIG / f'algites-{family}-name-conventions.yml').read_text(encoding='utf-8'))
    for profile in document['SourceStructureNameConventions']['NameConventions'].values():
        assert profile['schema_source_schema_url'] == '$id'
        assert profile['schema_source_schema_identifier'] == '$x-algites-schema-id'
        assert profile['schema_source_schema_version'] == '$x-algites-schema-version'
        assert profile['data_source_schema_url'] == '$schemaUrl'
        assert profile['data_source_schema_identifier'] == ''
        assert profile['data_source_schema_version'] == ''

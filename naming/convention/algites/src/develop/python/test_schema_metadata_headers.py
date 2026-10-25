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
        assert lines[2] == f'$schema: {uri}'
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
        assert common['properties']['$schema']['type'] == 'string'
        assert common['properties']['$schema']['format'] == 'uri'

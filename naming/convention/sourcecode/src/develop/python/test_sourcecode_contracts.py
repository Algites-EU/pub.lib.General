"""Contract/packaging regressions for the standalone source-code naming artifact."""
from pathlib import Path
import json
from urllib.parse import urljoin

import pytest
from jsonschema import Draft202012Validator
from referencing import Registry, Resource
from referencing.jsonschema import DRAFT202012

ROOT = Path(__file__).resolve().parents[3]
COMMON = ROOT.parent / 'intf' / 'src/product'
DOMAIN = ROOT / 'src/product'
BASE = 'eu/algites/lib/naming/convention'


def load_schemas(kind: str):
    extension = {'jsondefs': 'jsondef', 'yamldefs': 'yamldef'}[kind]
    schema_files = [*COMMON.joinpath(kind).rglob(f'*.{extension}.schema.json'),
                    *DOMAIN.joinpath(kind).rglob(f'*.{extension}.schema.json')]
    by_id = {schema['$id']: schema for p in schema_files if (schema := json.loads(p.read_text()))}
    registry = Registry().with_resources(
        (url, Resource.from_contents(schema, default_specification=DRAFT202012))
        for url, schema in by_id.items())
    return by_id, registry


@pytest.mark.parametrize('kind', ['jsondefs', 'yamldefs'])
def test_all_local_schema_references_resolve_across_artifacts(kind):
    by_id, registry = load_schemas(kind)
    def walk(value, current):
        if isinstance(value, dict):
            for key, item in value.items():
                if key == '$ref':
                    url = urljoin(current, item).split('#')[0]
                    assert url in by_id, (current, item)
                else:
                    walk(item, current)
        elif isinstance(value, list):
            for item in value: walk(item, current)
    for url, schema in by_id.items():
        walk(schema, url)


@pytest.mark.parametrize('kind,suffix', [('yamldefs','yamldef'),('jsondefs','jsondef')])
def test_new_do_sdo_profiles_are_separate_and_enum_typed(kind, suffix):
    by_id, registry = load_schemas(kind)
    base = f'https://defs.dev.algites.eu/api/{kind}/{BASE}/sourcecode/'
    samples = {
        'dataobject/dataobject-naming-conventions_1': {
            'DataObjectNameConventions': {'NameConventions': {'sample': {'StructureNaming': {
                'schema_source_schema_url': '$id',
                'schema_source_schema_identifier': '$x-algites-schema-id',
                'schema_source_schema_version': '$x-algites-schema-version',
                'data_source_schema_url': '$schema',
                'data_source_schema_identifier': None,
                'data_source_schema_version': None}}}}},
        'smartdataobject/smartdataobject-naming-conventions_1': {
            'SmartDataObjectNameConventions': {'NameConventions': {'sample': {'StructureNaming': {
                'standard_getter': 'java-getter-method-name_1',
                'standard_setter': 'java-setter-method-name_1',
                'raw_getter': 'algites-java-sdo-raw-getter_1'}}}}},
    }
    for name, doc in samples.items():
        url = base + name + f'.{suffix}.schema.json'
        validator = Draft202012Validator(by_id[url], registry=registry)
        assert validator.is_valid(doc), (name, list(validator.iter_errors(doc)))
        invalid = json.loads(json.dumps(doc))
        key = next(iter(invalid))
        rolemap = invalid[key]['NameConventions']['sample']['StructureNaming']
        rolemap['not_a_declared_kind'] = 'bad'
        assert not validator.is_valid(invalid)
        other_role = ('standard_getter' if name.startswith('dataobject/') else 'schema_source_schema_url')
        rolemap.pop('not_a_declared_kind')
        rolemap[other_role] = 'must-not-leak'
        assert not validator.is_valid(invalid)


@pytest.mark.parametrize('kind,suffix', [('yamldefs','yamldef'),('jsondefs','jsondef')])
def test_common_contract_is_shared_and_not_domain_specific(kind, suffix):
    by_id, _ = load_schemas(kind)
    base = f'https://defs.dev.algites.eu/api/{kind}/{BASE}/'
    common = by_id[base + f'common-naming-conventions_1.{suffix}.schema.json']
    assert 'NameConventions' not in common['properties']
    assert 'NameFragments' in common['properties']
    assert 'NameCompositions' in common['properties']
    assert not any(key.rsplit('/', 1)[-1] == 'name-conventions_1.' + suffix + '.schema.json' for key in by_id)

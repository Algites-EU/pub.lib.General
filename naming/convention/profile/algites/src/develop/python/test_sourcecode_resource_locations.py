"""Keep naming resources, schema URIs and metadata publication paths aligned."""

from pathlib import Path
import json
import yaml


def test_naming_profile_resources_use_sourcecode_namespace():
    naming = Path(__file__).resolve().parents[5]
    for profile in ('algites', 'standardprefix', 'standardpostfix'):
        root = naming / 'profile' / profile / 'src/product/config/eu/algites/lib/naming/convention'
        assert not list(root.glob('*.yml')), profile
        for source in (root / 'sourcecode').glob('*.yml'):
            config = yaml.safe_load(source.read_text(encoding='utf-8'))
            if isinstance(config, dict) and '$schema' in config:
                assert '/naming/convention/sourcecode/' in config['$schema'], source
                assert source.read_text().splitlines()[0].endswith(config['$schema'])

def test_publication_metadata_matches_sourcecode_schema_ids():
    naming = Path(__file__).resolve().parents[5]
    intf = naming / 'sourcecode/src/product'
    for family, suffix in (('jsondefs', 'jsondef.schema.json'), ('yamldefs', 'yamldef.schema.json')):
        root = intf / family / 'eu/algites/lib/naming/convention/sourcecode'
        for source in root.rglob('*.' + suffix):
            schema = json.loads(source.read_text(encoding='utf-8'))
            relative = source.relative_to(intf / family).as_posix()
            assert schema['$id'].endswith('/' + relative), source
            metadata = yaml.safe_load(Path(str(source)+'.meta.yml').read_text(encoding='utf-8'))
            assert metadata['GlobalPublicationPathId'] == relative, source

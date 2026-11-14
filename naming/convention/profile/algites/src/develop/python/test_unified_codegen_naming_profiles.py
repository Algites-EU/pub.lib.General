"""One DO/SDO type naming family per language across all published profiles."""
from pathlib import Path
import re
import yaml
import pytest

from eu.algites.lib.naming.convention.impl.standard.aic_name_convention_catalog import (
    AIcNameConventionCatalog,
)

PROFILE_ROOT = Path(__file__).resolve().parents[3].parent
PROFILES = {
    'algites': 'algites',
    'standardprefix': 'standard-prefix',
    'standardpostfix': 'standard-postfix',
}
ROLES = ('gen-intf-versioned_1', 'gen-data-intf-versioned_1',
         'gen-data-class-versioned_1')
FORMAT_SPECIFIC = re.compile(r'(?:^|[-_])(?:xml|json|yaml)(?:[-_]|$)')


@pytest.mark.parametrize('profile,prefix', PROFILES.items())
@pytest.mark.parametrize('language', ('java', 'python'))
def test_output_catalog_has_only_format_neutral_roles(profile, prefix, language):
    """No XML/JSON/YAML-specific names survive in an output naming catalog."""
    root = (PROFILE_ROOT / profile / 'src/product/config/eu/algites/lib/naming'
            / 'convention/sourcecode')
    path = root / f'{prefix}-{language}-name-conventions.yml'
    doc = yaml.safe_load(path.read_text(encoding='utf-8'))
    assert doc['StandardConventionsFile'] == 'standard-conventions.yml'
    catalog = AIcNameConventionCatalog.load(path)
    assert len(catalog.names) == 32
    for section, key in (('NameFragments', 'NameFragment'),
                         ('NameCompositions', 'NameComposition')):
        entries = doc[section][key]
        assert all(not FORMAT_SPECIFIC.search(item['name']) for item in entries)
    assert all(not FORMAT_SPECIFIC.search(name) for name in catalog.names)
    assert all(f'{prefix}-{language}-{role}' in catalog.names for role in ROLES)
    rendered = [catalog.format(f'{prefix}-{language}-{role}', 'type_name',
                               {'identifier': 'Customer', 'version': '2'},
                               'upper_camel_case_1') for role in ROLES]
    assert len(set(rendered)) == len(ROLES), (profile, language, rendered)
    for role, type_name in zip(ROLES, rendered):
        convention = f'{prefix}-{language}-{role}'
        stem = catalog.format(convention, 'file_name_stem',
                              {'identifier': 'Customer', 'version': '2'},
                              'upper_camel_case_1')
        extension = catalog.format(convention, 'file_name_suffix',
                                   {'identifier': 'Customer', 'version': '2'},
                                   'upper_camel_case_1')
        assert extension == ('.java' if language == 'java' else '.py')
        assert stem == type_name or language == 'python'
        assert catalog.recognize_filename(stem + extension).convention_name == convention


@pytest.mark.parametrize('profile,prefix', PROFILES.items())
def test_schema_input_catalogs_remain_separate_and_unchanged(profile, prefix):
    """Source schema naming conventions are *not* generated-type naming."""
    root = (PROFILE_ROOT / profile / 'src/product/config/eu/algites/lib/naming'
            / 'convention/sourcecode')
    for kind in ('xmldef', 'jsondef', 'yamldef'):
        path = root / f'{prefix}-{kind}-name-conventions.yml'
        assert path.is_file()
        catalog = AIcNameConventionCatalog.load(path)
        assert len(catalog.names) == 2

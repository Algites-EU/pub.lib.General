from __future__ import annotations

from pathlib import Path
import pytest

from eu.algites.lib.naming.convention.impl.algites.aic_name_convention_catalog import AIcNameConventionCatalog


ROOT = Path(__file__).resolve().parents[3] / 'src/product/config/eu/algites/lib/naming/convention'


def catalog(language: str) -> AIcNameConventionCatalog:
    return AIcNameConventionCatalog.load(ROOT / f'algites-{language}-name-conventions.yml')


def test_name_match_is_in_its_own_public_type_module():
    """Each publicly named Algites type has exactly one matching product module."""
    from eu.algites.lib.naming.convention.impl.algites.aicd_name_match import AIcdNameMatch

    catalog_match = catalog('java').recognize_filename('AIigCustomer_1.java')
    assert isinstance(catalog_match, AIcdNameMatch)
    assert catalog_match.convention_name == 'algites-java-gen-intf-versioned_1'
    assert catalog_match.variables['identifier'] == 'Customer'
    assert AIcdNameMatch.__module__.endswith('.aicd_name_match')


def test_all_java_combinations_and_distinct_filename_classification():
    cat = catalog('java')
    assert len(cat.names) == 59
    for name in cat.names:
        fields = {'identifier': 'Customer', 'version': '2'}
        value = cat.format(name, 'file_name_stem', fields) + cat.format(name, 'file_name_suffix', fields)
        found = cat.recognize_filename(value)
        assert found is not None and found.convention_name == name, (name, value, found)
    assert cat.recognize_filename('AIcgdCustomer_2.java').convention_name == 'algites-java-gen-data-class-versioned_1'
    assert cat.recognize_filename('AIigCustomer_2.java').convention_name == 'algites-java-gen-intf-versioned_1'
    assert cat.recognize_filename('AIigdCustomer_2.java').convention_name == 'algites-java-gen-data-intf-versioned_1'


def test_all_python_combinations_and_distinct_filename_classification():
    cat = catalog('python')
    assert len(cat.names) == 59
    for name in cat.names:
        fields = {'identifier': 'customer', 'version': '2'}
        value = cat.format(name, 'file_name_stem', fields) + cat.format(name, 'file_name_suffix', fields)
        found = cat.recognize_filename(value)
        assert found is not None and found.convention_name == name, (name, value, found)
    assert cat.recognize_filename('aiig_customer_2.py').convention_name == 'algites-python-gen-intf-versioned_1'


@pytest.mark.parametrize('kind,extension', [('yamldef','.yamldef.schema.json'),('jsondef','.jsondef.schema.json'),('xmldef','.xsd')])
def test_schema_formats(kind, extension):
    cat = catalog(kind)
    assert len(cat.names) == 2
    found = cat.recognize_filename('customer-address_2' + extension)
    assert found is not None and found.convention_name == f'algites-{kind}-schema-versioned_1'
    assert cat.recognize_filename('customer-address_2' + extension + '.bak') is None


@pytest.mark.parametrize('language', ['java', 'python'])
def test_generic_constant_naming_has_no_schema_field_specific_prefix(language):
    """A constant is an identifier, not implicitly a schema-field binding."""
    names = catalog(language)
    assert names.composition('constant-name_1') == (
        {'Kind': 'identifier', 'Convention': 'upper_snake_case_1'},)
    profile = f'algites-{language}-gen-intf-versioned_1'
    assert names.format(profile, 'constant_name',
                        {'identifier': 'customer_id'}, 'lower_snake_case_1') == 'CUSTOMER_ID'


def test_conversion():
    source = catalog('yamldef')
    target = catalog('java')
    assert source.convert('customer-address', 'algites-yamldef-schema_1', target,
        'algites-java-gen-intf_1', 'main_container_name', 'file_name_stem') == 'AIigCustomerAddress'


def test_clean_only_owned_outputs(tmp_path):
    cat = catalog('java')
    root = tmp_path / 'java.gen' / 'some' / 'package'
    root.mkdir(parents=True)
    for name in ('AIigCustomer_1.java','AIigdCustomer_1.java','AIcgdCustomer_1.java','Handwritten.java'):
        (root/name).write_text('dummy')
    deleted = cat.clean(tmp_path / 'java.gen', {'algites-java-gen-intf-versioned_1'})
    assert [p.name for p in deleted] == ['AIigCustomer_1.java']
    assert (root/'AIigdCustomer_1.java').exists() and (root/'AIcgdCustomer_1.java').exists()


def test_ambiguous_file_names_rejected():
    import yaml
    root = yaml.safe_load((ROOT/'algites-java-name-conventions.yml').read_text())
    root['IdentifierConventions'] = {'upper_camel_case_1': {'Regexp': '(?<identifier>[A-Z][A-Za-z0-9]*)'}}
    # Deliberately duplicate a profile and require a clear ambiguity error.
    root['SourceStructureNameConventions']['NameConventions']['duplicate'] = root['SourceStructureNameConventions']['NameConventions']['algites-java-gen-intf-versioned_1']
    with pytest.raises(ValueError, match='Ambiguous'):
        cat = AIcNameConventionCatalog({**root, 'IdentifierConventions': {**yaml.safe_load((ROOT/'standard-conventions.yml').read_text())['IdentifierConventions']}, 'VersionConventions':yaml.safe_load((ROOT/'standard-conventions.yml').read_text())['VersionConventions']})
        cat.recognize_filename('AIigCustomer_1.java')


def test_custom_version_named_variables_can_be_recomposed():
    """Input 1.0 can become _1_0 without a generator-specific version algorithm."""
    identifiers = {
        'lower_snake_case_1': {'Regexp': '(?<identifier>[a-z][a-z0-9]*(?:_[a-z][a-z0-9]*)*)'},
        'upper_camel_case_1': {'Regexp': '(?<identifier>[A-Z][a-z0-9]*(?:[A-Z][a-z0-9]*)*)'},
    }
    source = AIcNameConventionCatalog({
        'IdentifierConventions': identifiers,
        'VersionConventions': {'major_minor_1': {'Regexp':
            r'(?<version>(?<major>0|[1-9][0-9]*)\.(?<minor>0|[1-9][0-9]*))'}},
        'NameCompositions': {
            'source': {'Segments': [
                {'Kind': 'identifier', 'Convention': 'lower_snake_case_1'},
                {'Kind': 'constant_string', 'Value': '_'},
                {'Kind': 'version', 'Convention': 'major_minor_1'}]},
            'suffix': {'Segments': [{'Kind': 'constant_string', 'Value': '.schema'}]},
        },
        'NameConventions': {'source-format': {'ContainerStructureType': 'object', 'StructureNaming': {
            'file_name_stem': 'source', 'file_name_suffix': 'suffix', 'main_container_name': 'source'}}},
    })
    target = AIcNameConventionCatalog({
        'IdentifierConventions': identifiers,
        'NameCompositions': {
            'target': {'Segments': [
                {'Kind': 'constant_string', 'Value': 'AIig'},
                {'Kind': 'identifier', 'Convention': 'upper_camel_case_1'},
                {'Kind': 'constant_string', 'Value': '_'},
                {'Kind': 'variable', 'Name': 'major', 'Regexp': '[0-9]+'},
                {'Kind': 'constant_string', 'Value': '_'},
                {'Kind': 'variable', 'Name': 'minor', 'Regexp': '[0-9]+'}]},
            'suffix': {'Segments': [{'Kind': 'constant_string', 'Value': '.java'}]},
        },
        'NameConventions': {'target-format': {'ContainerStructureType': 'interface', 'StructureNaming': {
            'file_name_stem': 'target', 'file_name_suffix': 'suffix', 'main_container_name': 'target'}}},
    })
    assert source.convert('customer_address_1.0', 'source-format', target,
        'target-format', 'main_container_name') == 'AIigCustomerAddress_1_0'
    match = target.recognize_filename('AIigCustomerAddress_1_0.java')
    assert match is not None and match.variables['major'] == '1' and match.variables['minor'] == '0'


def test_all_naming_catalogs_exist_under_product_config():
    """Configuration files are maintained once under the Modustro config resource root."""
    expected = {'standard-conventions.yml', 'algites-java-name-conventions.yml',
                'algites-python-name-conventions.yml', 'algites-yamldef-name-conventions.yml',
                'algites-jsondef-name-conventions.yml', 'algites-xmldef-name-conventions.yml',
                'example-input.yml', 'example-output.yml'}
    assert {p.name for p in ROOT.glob('*.yml')} == expected


@pytest.mark.parametrize('language,expected_parameter,expected_local', [
    ('java', 'aCustomerAddress', 'locCustomerAddress'),
    ('python', 'a_customer_address', 'loc_customer_address'),
])
def test_common_method_and_local_naming_for_all_profiles(language, expected_parameter, expected_local):
    cat = catalog(language)
    for profile in cat.names:
        source = 'upper_camel_case_1' if language == 'java' else 'lower_snake_case_1'
        identifier = 'CustomerAddress' if language == 'java' else 'customer_address'
        values = {'identifier': identifier}
        for structure, expected in (('method_parameter_name', expected_parameter),
                                     ('local_variable_name', expected_local)):
            actual = cat.format(profile, structure, values, source)
            assert actual == expected, (profile, structure, actual)
            assert cat.matches(profile, structure, expected)
            assert cat.parse(profile, structure, expected)['identifier'] == identifier
        assert not cat.matches(profile, 'method_parameter_name', identifier)
        assert not cat.matches(profile, 'local_variable_name', identifier)


@pytest.mark.parametrize('language,extension', [('java', '.java'), ('python', '.py')])
def test_all_filename_stems_require_exactly_one_full_suffix(language, extension):
    cat = catalog(language)
    identifier = 'Customer' if language == 'java' else 'customer'
    for profile in cat.names:
        values = {'identifier': identifier, 'version': '12'}
        stem = cat.format(profile, 'file_name_stem', values)
        suffix = cat.format(profile, 'file_name_suffix', values)
        assert suffix == extension, (profile, suffix)
        assert not stem.endswith(extension), (profile, stem)
        name = stem + suffix
        assert cat.recognize_filename(name).convention_name == profile
        assert cat.recognize_filename(name + extension) is None


def test_yaml_example_reorders_user_variables_and_major_minor_versions():
    """Runnable example-input.yml -> example-output.yml in both packaged distributions."""
    source = AIcNameConventionCatalog.load(ROOT / 'example-input.yml')
    target = AIcNameConventionCatalog.load(ROOT / 'example-output.yml')
    input_filename = 'customer_address--eu_west--beta--2.7.demo.schema'
    input_match = source.recognize_filename(input_filename)
    assert input_match is not None
    assert input_match.convention_name == 'example-input_1'
    assert input_match.variables == {
        'identifier': 'customer_address',
        'region': 'eu_west',
        'channel': 'beta',
        'version': '2.7',
        'major': '2',
        'minor': '7',
    }
    output_stem = source.convert(input_filename.removesuffix('.demo.schema'),
        'example-input_1', target, 'example-output_1', 'file_name_stem')
    output_filename = output_stem + target.format('example-output_1', 'file_name_suffix', input_match.variables)
    assert output_filename == 'AIigCustomerAddress__beta__7-2__eu_west.java'
    output_match = target.recognize_filename(output_filename)
    assert output_match is not None
    assert output_match.variables == {
        'identifier': 'CustomerAddress', 'channel': 'beta',
        'minor': '7', 'major': '2', 'region': 'eu_west'
    }
    assert target.recognize_filename('AIigCustomerAddress__beta__7-2__eu_west.java.java') is None


def test_standard_catalog_uses_python_import_roots_independent_of_checkout(tmp_path):
    """Product config is an import root, never a repository-relative search path."""
    import os
    import subprocess
    import sys

    module_root = ROOT.parents[5] / 'python'
    config_root = ROOT.parents[5] / 'config'
    assert module_root.is_dir() and config_root.is_dir()
    unrelated = tmp_path / 'unrelated-directory'
    unrelated.mkdir()
    code = ("from eu.algites.lib.naming.convention.impl.algites.aic_name_convention_catalog "
            "import AIcNameConventionCatalog; "
            "assert len(AIcNameConventionCatalog.standard('java').names) == 59; "
            "assert len(AIcNameConventionCatalog.standard('python').names) == 59")
    env = dict(os.environ, PYTHONPATH=os.pathsep.join((str(module_root), str(config_root))))
    subprocess.run([sys.executable, '-c', code], cwd=unrelated, env=env, check=True)


def test_standard_catalog_uses_published_modustro_resource_package(tmp_path):
    """The installed Python wheel keeps YAML under an isolated artifact package."""
    import os
    import shutil
    import subprocess
    import sys

    module_root = ROOT.parents[5] / 'python'
    stage = tmp_path / 'wheel-packages'
    destination = (stage / 'algites/pub/lib/general/naming/convention/algites'
                   / 'eu/algites/lib/naming/convention')
    destination.mkdir(parents=True)
    for item in ROOT.glob('*.yml'):
        shutil.copy2(item, destination / item.name)
    unrelated = tmp_path / 'outside-repository'
    unrelated.mkdir()
    code = ("from eu.algites.lib.naming.convention.impl.algites.aic_name_convention_catalog "
            "import AIcNameConventionCatalog; "
            "assert len(AIcNameConventionCatalog.standard('java').names) == 59; "
            "assert len(AIcNameConventionCatalog.standard('python').names) == 59; "
            "assert len(AIcNameConventionCatalog.load_resource('algites-yamldef-name-conventions.yml').names) == 2")
    env = dict(os.environ, PYTHONPATH=os.pathsep.join((str(module_root), str(stage))))
    subprocess.run([sys.executable, '-c', code], cwd=unrelated, env=env, check=True)


def test_xml_element_marker_is_distinct_in_java_and_python():
    java, python = catalog('java'), catalog('python')
    assert java.recognize_filename('AIig_xePayment_1.java').convention_name == 'algites-java-gen-xml-element-intf-versioned_1'
    assert java.recognize_filename('AIigd_xePayment_1.java').convention_name == 'algites-java-gen-data-xml-element-intf-versioned_1'
    assert java.recognize_filename('AIcgd_xePayment_1.java').convention_name == 'algites-java-gen-data-xml-element-class-versioned_1'
    assert python.recognize_filename('aiig__xe_payment_1.py').convention_name == 'algites-python-gen-xml-element-intf-versioned_1'
    assert python.recognize_filename('aiigd__xe_payment_1.py').convention_name == 'algites-python-gen-data-xml-element-intf-versioned_1'
    assert python.recognize_filename('aicgd__xe_payment_1.py').convention_name == 'algites-python-gen-data-xml-element-class-versioned_1'
    assert java.recognize_filename('AIigXePayment_1.java').convention_name == 'algites-java-gen-intf-versioned_1'
    assert python.recognize_filename('aiig_xe_payment_1.py').convention_name == 'algites-python-gen-intf-versioned_1'
    assert java.parse('algites-java-gen-xml-element-intf-versioned_1', 'main_container_name',
                      'AIig_xePayment_1')['identifier'] == 'Payment'
    assert python.parse('algites-python-gen-xml-element-intf-versioned_1', 'file_name_stem',
                        'aiig__xe_payment_1')['identifier'] == 'payment'


@pytest.mark.parametrize('language,source_identifier,source_style', [
    ('java', 'CustomerAddress', 'upper_camel_case_1'),
    ('python', 'customer_address', 'lower_snake_case_1'),
])
def test_all_profiles_have_independently_composed_getters_and_setters(language, source_identifier, source_style):
    """The two new StructureNaming elements must work identically in every profile."""
    from copy import deepcopy

    original = catalog(language)
    for profile in original.names:
        getter = original.format(profile, 'getter_method_name',
                                 {'identifier': source_identifier}, source_style)
        setter = original.format(profile, 'setter_method_name',
                                 {'identifier': source_identifier}, source_style)
        assert getter == 'getCustomerAddress', (language, profile, getter)
        assert setter == 'setCustomerAddress', (language, profile, setter)
        assert original.matches(profile, 'getter_method_name', getter)
        assert original.matches(profile, 'setter_method_name', setter)

    # A custom profile may change both method syntaxes without touching codegen.
    modified = deepcopy(original._config)
    modified['NameCompositions'][f'{language}-getter-method-name_1']['Segments'][0]['Value'] = 'read'
    modified['NameCompositions'][f'{language}-setter-method-name_1']['Segments'][0]['Value'] = 'write'
    custom = AIcNameConventionCatalog(modified)
    for profile in custom.names:
        assert custom.format(profile, 'getter_method_name',
                             {'identifier': source_identifier}, source_style) == 'readCustomerAddress'
        assert custom.format(profile, 'setter_method_name',
                             {'identifier': source_identifier}, source_style) == 'writeCustomerAddress'

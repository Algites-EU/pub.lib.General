"""Schema metadata mappings must be paired with corresponding source fields."""
from copy import deepcopy
from pathlib import Path

import pytest
from eu.algites.lib.naming.convention.impl.standard.aic_name_convention_catalog import AIcNameConventionCatalog


def test_disabled_source_cannot_enable_data_metadata_target():
    catalog = AIcNameConventionCatalog.load(Path(__file__).resolve().parents[3] / 'src/product/config/eu/algites/lib/naming/convention/sourcecode/algites-jsondef-name-conventions.yml')
    for suffix in ('url', 'identifier', 'version'):
        configuration = deepcopy(catalog._config)
        name, profile = next(iter(configuration['SourceStructureNameConventions']['NameConventions'].items()))
        profile[f'schema_source_schema_{suffix}'] = ''
        profile[f'data_source_schema_{suffix}'] = f'$target{suffix}'
        # The generic naming engine must not enforce DataObject metadata semantics.
        assert AIcNameConventionCatalog(configuration).profile_value(
            name, '', f'data_source_schema_{suffix}') in ('', f'$target{suffix}')


def test_empty_target_is_valid_and_suppresses_instance_metadata():
    catalog = AIcNameConventionCatalog.load(Path(__file__).resolve().parents[3] / 'src/product/config/eu/algites/lib/naming/convention/sourcecode/algites-jsondef-name-conventions.yml')
    fields = catalog.schema_metadata_fields('algites-jsondef-schema-versioned_1')
    assert fields['schema_source_schema_identifier'] == '$x-algites-schema-id'
    assert fields['schema_source_schema_version'] == '$x-algites-schema-version'
    assert fields['data_source_schema_url'] == '$schema'
    assert fields['data_source_schema_identifier'] == ''
    assert fields['data_source_schema_version'] == ''




def test_generated_metadata_namespace_is_independent_of_source_profiles(tmp_path):
    """Java/Python catalogs must resolve method metadata without source-profile coupling."""
    import yaml
    data = {'GeneratedDataObjectMethodNameConventions': {'NameConventions': {
        'example_1': {
            'StructureNaming': {'standard_getter': 'getter', 'standard_setter': 'setter'},
            'SchemaMetadataNaming': {
                'schema_source_schema_url': '$id',
                'schema_source_schema_identifier': '$x-algites-schema-id',
                'data_source_schema_url': '$schema',
                'data_source_schema_identifier': ''}}}}}
    file = tmp_path / 'generated-methods.yml'
    file.write_text(yaml.safe_dump(data), encoding='utf-8')
    catalog = AIcNameConventionCatalog.load(file)
    fields = catalog.schema_metadata_fields('example_1')
    assert fields['schema_source_schema_url'] == '$id'
    assert fields['data_source_schema_url'] == '$schema'
    assert fields['data_source_schema_version'] == ''
    data['GeneratedDataObjectMethodNameConventions']['NameConventions']['example_1']['SchemaMetadataNaming']['wrong'] = '$bad'
    file.write_text(yaml.safe_dump(data), encoding='utf-8')
    # Unknown domain keys are a schema-validation responsibility, not the generic catalog's.
    assert AIcNameConventionCatalog.load(file).profile_value(
        'example_1', 'SchemaMetadataNaming', 'wrong') == '$bad'

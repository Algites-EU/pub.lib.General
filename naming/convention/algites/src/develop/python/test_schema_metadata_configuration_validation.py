"""Schema metadata mappings must be paired with corresponding source fields."""
from copy import deepcopy

import pytest
from eu.algites.lib.naming.convention.impl.algites.aic_name_convention_catalog_1 import AIcNameConventionCatalog_1


def test_disabled_source_cannot_enable_data_metadata_target():
    catalog = AIcNameConventionCatalog_1.load_resource('algites-jsondef-name-conventions.yml')
    for suffix in ('url', 'identifier', 'version'):
        configuration = deepcopy(catalog._config)
        profile = next(iter(configuration['SourceStructureNameConventions']['NameConventions'].values()))
        profile[f'schema_source_schema_{suffix}'] = ''
        profile[f'data_source_schema_{suffix}'] = f'$target{suffix}'
        with pytest.raises(ValueError, match=f'data_source_schema_{suffix} requires schema_source_schema_{suffix}'):
            AIcNameConventionCatalog_1(configuration)


def test_empty_target_is_valid_and_suppresses_instance_metadata():
    catalog = AIcNameConventionCatalog_1.load_resource('algites-jsondef-name-conventions.yml')
    fields = catalog.schema_metadata_fields('algites-jsondef-schema-versioned_1')
    assert fields['schema_source_schema_identifier'] == '$x-algites-schema-id'
    assert fields['schema_source_schema_version'] == '$x-algites-schema-version'
    assert fields['data_source_schema_url'] == '$schemaUrl'
    assert fields['data_source_schema_identifier'] == ''
    assert fields['data_source_schema_version'] == ''

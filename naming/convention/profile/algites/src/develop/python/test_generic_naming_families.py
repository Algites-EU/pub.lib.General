"""The shared catalog must work for arbitrary naming domains and style IDs."""

from eu.algites.lib.naming.convention.impl.standard.aic_name_convention_catalog import AIcNameConventionCatalog


def test_arbitrary_domain_and_role_names():
    engine = AIcNameConventionCatalog({
        'NameCompositions': {'sql-table': {'Segments': [
            {'Kind': 'constant_string', 'Value': 'tbl_'},
            {'Kind': 'identifier', 'Convention': 'custom-prefix'}]}},
        'IdentifierConventions': {'custom-prefix': {'Regexp': r'(?<identifier>[a-z]+)'}},
        'SqlObjectConventions': {'NameConventions': {'database_1': {
            'StructureNaming': {'physical_table': 'sql-table'}}}},
    })
    assert engine.profile_names == ('database_1',)
    assert engine.names == ()  # Arbitrary SQL profiles are not filename recognizers.
    assert engine.composition_reference('database_1', 'physical_table') == 'sql-table'
    assert engine.format('database_1', 'physical_table', {'identifier': 'orders'}) == 'tbl_orders'
    assert engine.matches('database_1', 'physical_table', 'tbl_orders')
    assert engine.recognize_filename('tbl_orders') is None


def test_conversion_uses_declared_style_not_identifier_name():
    engine = AIcNameConventionCatalog({
        'IdentifierConventions': {
            'one': {'Regexp': r'(?<identifier>[a-z_]+)', 'ConversionStyle': 'lower_snake'},
            'two': {'Regexp': r'(?<identifier>[A-Z][A-Za-z]+)', 'ConversionStyle': 'upper_camel'},
        },
    })
    assert engine.convert_identifier_by_declared_style('user_name', 'one', 'two') == 'UserName'

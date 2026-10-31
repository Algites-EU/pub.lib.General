"""Regression coverage for the shared exclusive naming-segment grammar."""
from __future__ import annotations

import json
from pathlib import Path
import pytest
from jsonschema import Draft202012Validator
from eu.algites.lib.naming.convention.impl.standard.aic_name_convention_catalog import AIcNameConventionCatalog


REPOSITORY = Path(__file__).resolve().parents[7]
SCHEMAS = REPOSITORY / 'naming/convention/intf/src/product'


def segment_document(value: dict) -> dict:
    return {'NameFragments': {'fragment': {'Segments': [value]}}}


GOOD = [
    {'Ref': 'fragment_1'},
    {'Kind': 'constant_string', 'Value': ''},
    {'Kind': 'identifier', 'Convention': 'upper_camel_case_1'},
    {'Kind': 'version', 'Convention': 'positive_integer_1'},
    {'Kind': 'identifier', 'CustomConventionRegexp': r'(?<identifier>[A-Z][a-z]+)'},
    {'Kind': 'version', 'CustomConventionRegexp': r'(?<version>[0-9]+)'},
    {'Kind': 'variable', 'Name': 'region', 'Regexp': r'[a-z]+'},
]
BAD = [
    {'Kind': 'constant_string'},
    {'Kind': 'constant_string', 'Value': 'x', 'Convention': 'any'},
    {'Kind': 'identifier', 'Value': 'x'},
    {'Kind': 'identifier', 'Convention': 'a', 'CustomConventionRegexp': '(?<identifier>.*)'},
    {'Kind': 'identifier', 'Convention': 'a', 'Name': 'some'},
    {'Kind': 'identifier', 'Convention': 'a', 'Value': 'x'},
    {'Kind': 'version', 'Convention': 'a', 'Regexp': 'anything'},
    {'Kind': 'variable', 'Name': 'region'},
    {'Kind': 'variable', 'Name': 'region', 'Regexp': '[a-z]+', 'Value': 'x'},
    {'Ref': 'fragment_1', 'Kind': 'identifier'},
    {'Kind': 'version'},
]


@pytest.mark.parametrize('kind', ['jsondefs', 'yamldefs'])
def test_json_schema_segment_alternatives(kind):
    suffix = 'jsondef' if kind == 'jsondefs' else 'yamldef'
    file = SCHEMAS / kind / 'eu/algites/lib/naming/convention' / f'common-naming-conventions_1.{suffix}.schema.json'
    validator = Draft202012Validator(json.loads(file.read_text()))
    for value in GOOD:
        assert validator.is_valid(segment_document(value)), value
    for value in BAD:
        assert not validator.is_valid(segment_document(value)), value


@pytest.mark.parametrize('value', GOOD)
def test_python_catalog_accepts_valid_variants(value):
    AIcNameConventionCatalog(segment_document(value))


@pytest.mark.parametrize('value', BAD)
def test_python_catalog_rejects_invalid_variants(value):
    with pytest.raises(ValueError):
        AIcNameConventionCatalog(segment_document(value))


@pytest.mark.parametrize('kind,regex', [
    ('identifier', r'(?<version>[0-9]+)'),
    ('version', r'(?<identifier>[A-Z]+)'),
    ('identifier', r'(?<identifier>[a-z]+'),
    ('version', r'(?<version>[0-9]+)(?<version>[0-9]+)'),
])
def test_custom_regexp_requires_valid_syntax_and_correct_capture(kind, regex):
    with pytest.raises(ValueError, match='regular expression|named capture'):
        AIcNameConventionCatalog(segment_document({'Kind': kind, 'CustomConventionRegexp': regex}))


def test_custom_regexp_parses_and_formats():
    c = AIcNameConventionCatalog({'NameCompositions': {
        'example': {'Segments': [{'Kind': 'identifier', 'CustomConventionRegexp': r'(?<identifier>[a-z]+)'}]}
    }, 'NameConventions': {'profile': {'StructureNaming': {'type_name': 'example'}}}})
    assert c.parse('profile', 'type_name', 'customer') == {'identifier': 'customer'}
    assert c.format('profile', 'type_name', {'identifier': 'customer'}) == 'customer'
    with pytest.raises(ValueError):
        c.format('profile', 'type_name', {'identifier': 'Customer'})


def test_xml_xsd_enforces_exclusive_structure():
    """XSD 1.0 rejects structural conflicts in both fragment and composition segments."""
    from lxml import etree
    xsd = SCHEMAS / 'xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd'
    xml = xsd.read_text().replace(
        'xmlns:xs="http://www.w3.org/2001/XMLSchema"',
        'xmlns:xs="http://www.w3.org/2001/XMLSchema" '
        'xmlns:tns="eu.algites.lib.naming.convention.common-naming-conventions"',
    )
    tree = etree.fromstring(xml.encode())
    etree.SubElement(tree, '{http://www.w3.org/2001/XMLSchema}element',
                     name='Document', type='tns:common-naming-conventions')
    validator = etree.XMLSchema(tree)

    def valid_segment(name: str, contents: str) -> bool:
        entry = 'NameFragment' if name == 'NameFragments' else 'NameComposition'
        value = (f'<Document xmlns="eu.algites.lib.naming.convention.common-naming-conventions">'
                 f'<{name} xmlns=""><{entry} name="sample"><Segments><Segment>'
                 f'{contents}</Segment></Segments></{entry}></{name}></Document>')
        return validator.validate(etree.fromstring(value.encode()))

    for name in ('NameFragments', 'NameCompositions'):
        assert valid_segment(name, '<Ref>other</Ref>')
        assert valid_segment(name, '<Kind>constant_string</Kind><Value>X</Value>')
        assert valid_segment(name, '<Kind>identifier</Kind><Convention>lower_1</Convention>')
        assert valid_segment(name, '<Kind>variable</Kind><Name>region</Name><Regexp>[a-z]+</Regexp>')
        assert not valid_segment(name, '<Kind>identifier</Kind><Value>x</Value><Convention>a</Convention>')
        assert not valid_segment(name, '<Kind>variable</Kind><Name>region</Name>')
        assert not valid_segment(name, '<Ref>other</Ref><Kind>version</Kind>')

"""Regressions for raw EMPTY_VALUE, schema defaults and XML fixed restrictions.

Each test exercises the public annotation-backed API rather than private storage.
"""
from __future__ import annotations

import pytest

from eu.algites.lib.data.dataobject.aia_data_object_field import AIaDataObjectField
from eu.algites.lib.data.dataobject.aia_data_object_field_default import AIaDataObjectFieldDefault
from eu.algites.lib.data.dataobject.ain_data_object_field_default_kind import AInDataObjectFieldDefaultKind as Kind
from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject


class AnnotatedDefaults(AIcSmartDataObject):
    """Small read-only contract driving actual SmartDataObject runtime behavior."""

    @AIaDataObjectField(expected_type=str, is_empty_allowed=True,
                        default_value=AIaDataObjectFieldDefault(
                            Kind.IF_FIELD_VALUE_EMPTY, 'fallback', True))
    def getEmptyFallback(self):
        return self.get_EffectiveField('EmptyFallback')

    @AIaDataObjectField(expected_type=int, default_value=AIaDataObjectFieldDefault(
        Kind.IF_FIELD_VALUE_NOT_PRESENT, '42', True))
    def getAbsentFallback(self):
        return self.get_EffectiveField('AbsentFallback')

    @AIaDataObjectField(expected_type=str, default_value=AIaDataObjectFieldDefault(
        Kind.DECLARATION_ONLY, 'documentation', True))
    def getDocumentationOnly(self):
        return self.get_EffectiveField('DocumentationOnly')

    @AIaDataObjectField(expected_type=str, is_empty_allowed=True,
                        default_value=AIaDataObjectFieldDefault(
                            Kind.CONSTRAINT_DEFAULT, 'ABC', True))
    def getFixed(self):
        return self.get_EffectiveField('Fixed')

    @AIaDataObjectField(expected_type=int, default_value=AIaDataObjectFieldDefault(
        Kind.CONSTRAINT_DEFAULT, '1', True))
    def getFixedNumber(self):
        return self.get_EffectiveField('FixedNumber')


def test_empty_sentinel_differs_from_plain_empty_string():
    obj = AnnotatedDefaults()
    assert obj.getEmptyFallback() is None
    obj.set_RawField('EmptyFallback', '')
    assert obj.getEmptyFallback() == ''
    assert not obj.isEmpty_Field('EmptyFallback')
    assert obj.to_mapping() == {'EmptyFallback': ''}
    obj.set_EmptyField('EmptyFallback')
    assert obj.isEmpty_Field('EmptyFallback')
    assert obj.getEmptyFallback() == 'fallback'
    assert obj.get_RawField('EmptyFallback') == ''
    assert obj.to_mapping() == {'EmptyFallback': ''}
    obj.unset_Field('EmptyFallback')
    assert obj.getEmptyFallback() is None
    assert not obj.isEmpty_Field('EmptyFallback')
    assert obj.to_mapping() == {}


def test_default_on_absence_is_effective_only_and_typed():
    obj = AnnotatedDefaults()
    assert obj.getAbsentFallback() == 42
    assert obj.get_RawField('AbsentFallback') is None
    assert obj.to_mapping() == {}
    obj.set_RawField('AbsentFallback', 7)
    assert obj.getAbsentFallback() == 7
    assert obj.to_mapping() == {'AbsentFallback': 7}
    obj.unset_Field('AbsentFallback')
    assert obj.getAbsentFallback() == 42


def test_declaration_only_default_does_not_materialize():
    obj = AnnotatedDefaults()
    assert obj.getDocumentationOnly() is None
    assert obj.to_mapping() == {}


def test_fixed_rejects_incompatible_assignments_but_does_not_autofill():
    obj = AnnotatedDefaults()
    assert obj.getFixed() is None
    assert obj.to_mapping() == {}
    with pytest.raises(ValueError, match='Fixed'):
        obj.set_RawField('Fixed', 'WRONG')
    with pytest.raises(ValueError, match='empty'):
        obj.set_EmptyField('Fixed')
    assert obj.to_mapping() == {}
    obj.set_RawField('Fixed', 'ABC')
    assert obj.getFixed() == 'ABC'
    assert obj.to_mapping() == {'Fixed': 'ABC'}
    obj.unset_Field('Fixed')
    assert obj.getFixed() is None


def test_numeric_fixed_is_typed_and_cannot_be_satisfied_by_boolean():
    """The lexically numeric fixed value 1 cannot be bypassed with True."""
    obj = AnnotatedDefaults()
    with pytest.raises(ValueError, match='type'):
        obj.set_RawField('FixedNumber', True)
    with pytest.raises(ValueError, match='Fixed'):
        obj.set_RawField('FixedNumber', 2)
    assert obj.to_mapping() == {}
    obj.set_RawField('FixedNumber', 1)
    assert obj.getFixedNumber() == 1
    assert obj.to_mapping() == {'FixedNumber': 1}

from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object import AIaXmlDataObject
from eu.algites.lib.data.dataobject.xmldefs.aia_xml_data_object_field import AIaXmlDataObjectField
from eu.algites.lib.data.smartdataobject.impl.xmldefs.aic_xml_smart_data_object import AIcXmlSmartDataObject
from eu.algites.lib.text.format.ais_text_format_profiles import AIsTextFormatProfiles


class XmlEmptyDefault(AIcXmlSmartDataObject):
    """XML regression fixture with a schema-permitted explicit empty string."""
    __xml_data_object__ = AIaXmlDataObject(namespace='urn:default', namespace_local_name='Root')
    __xml_data_object_fields__ = {
        'Name': AIaXmlDataObjectField(namespace='urn:default', namespace_local_name='Name'),
        'Fixed': AIaXmlDataObjectField(namespace='urn:default', namespace_local_name='Fixed'),
    }

    @AIaDataObjectField(expected_type=str, is_empty_allowed=True,
                        default_value=AIaDataObjectFieldDefault(
                            Kind.IF_FIELD_VALUE_EMPTY, 'from-xsd', True))
    def getName(self):
        return self.get_EffectiveField('Name')

    @AIaDataObjectField(expected_type=str, default_value=AIaDataObjectFieldDefault(
        Kind.CONSTRAINT_DEFAULT, 'constant', True))
    def getFixed(self):
        return self.get_EffectiveField('Fixed')


def test_xml_empty_element_activates_default_without_changing_raw_xml():
    original = '<Root xmlns="urn:default"><Name/><Fixed>constant</Fixed></Root>'
    instance = XmlEmptyDefault.from_xml(original)
    assert instance.isEmpty_Field('Name')
    assert instance.getName() == 'from-xsd'
    assert instance.get_RawField('Name') == ''
    assert instance.getFixed() == 'constant'
    rendered = instance.toString_RawValues(AIsTextFormatProfiles.XML_XSD)
    assert __import__('xml.etree.ElementTree', fromlist=['fromstring']).fromstring(rendered).find('{urn:default}Name') is not None
    assert 'from-xsd' not in rendered
    assert XmlEmptyDefault.from_xml(rendered).isEmpty_Field('Name')


def test_xml_set_empty_string_not_automatically_the_sentinel():
    instance = XmlEmptyDefault()
    instance.set_RawField('Name', '')
    assert instance.getName() == ''
    assert not instance.isEmpty_Field('Name')
    xml = instance.toString_RawValues(AIsTextFormatProfiles.XML_XSD)
    assert __import__('xml.etree.ElementTree', fromlist=['fromstring']).fromstring(xml).find('{urn:default}Name') is not None
    assert XmlEmptyDefault.from_xml(xml).getName() == 'from-xsd'
    with pytest.raises(ValueError, match='Fixed'):
        XmlEmptyDefault.from_xml('<Root xmlns="urn:default"><Fixed>bad</Fixed></Root>')

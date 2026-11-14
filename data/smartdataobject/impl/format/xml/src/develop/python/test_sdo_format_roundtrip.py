"""Cross-format roundtrip checks for independent SDO binding implementations."""
from __future__ import annotations

import pytest

from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject
from eu.algites.lib.data.dataobject.aia_data_object_field import AIaDataObjectField
from eu.algites.lib.data.dataobject.xmlschema.aia_data_object_xml_schema import AIaDataObjectXmlSchema
from eu.algites.lib.data.dataobject.xmlschema.aia_data_object_field_xml_schema import AIaDataObjectFieldXmlSchema
from eu.algites.lib.data.dataobject.xmldefs.ain_xml_value_kind import AInXmlValueKind
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object_binding_registry import AIcSmartDataObjectBindingRegistry
from eu.algites.lib.data.smartdataobject.impl.common.aia_smart_data_object_implementation_binding import AIaSmartDataObjectImplementationBinding
from eu.algites.lib.data.smartdataobject.impl.common.aia_smart_data_object_field_implementation_binding import AIaSmartDataObjectFieldImplementationBinding
from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_metadata_registry import AIsSmartDataObjectMetadataRegistry
from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_format_binding_provider_registry import AIsSmartDataObjectFormatBindingProviderRegistry
from eu.algites.lib.data.smartdataobject.impl.format.xml.aic_xml_format_binding_provider import AIcXmlFormatBindingProvider
from eu.algites.lib.data.smartdataobject.impl.format.json.aic_json_format_binding_provider import AIcJsonFormatBindingProvider
from eu.algites.lib.data.smartdataobject.impl.format.yaml.aic_yaml_format_binding_provider import AIcYamlFormatBindingProvider


class Address(AIiDataObject):
    """Read-only contract for a namespaced XML nested object."""

    @AIaDataObjectField(dataFieldName='city')
    def getCity(self) -> str: ...


@AIaSmartDataObjectImplementationBinding(readContracts=(Address,))
class AddressSDO(AIcSmartDataObject, Address):
    """Generated-style address implementation."""

    __xml_schema__ = AIaDataObjectXmlSchema(namespace='urn:addr', namespace_local_name='Address')
    __xml_schema_fields__ = {'City': AIaDataObjectFieldXmlSchema(namespace='urn:addr', namespace_local_name='City')}
    __json_schema__ = object()

    def getCity(self) -> str:
        """Read the current city value."""
        return self.get_EffectiveField('City')


class Person(AIiDataObject):
    """Read-only contract for scalars and nested SDO objects."""

    @AIaDataObjectField(dataFieldName='name')
    def getName(self) -> str: ...

    @AIaDataObjectField(dataFieldName='age')
    def getAge(self) -> int: ...

    @AIaDataObjectField(dataFieldName='address')
    def getAddress(self) -> Address: ...


@AIaSmartDataObjectImplementationBinding(readContracts=(Person,))
class PersonSDO(AIcSmartDataObject, Person):
    """Generated-style person implementation with explicit address binding."""

    __xml_schema__ = AIaDataObjectXmlSchema(namespace='urn:people', namespace_local_name='Person')
    __xml_schema_fields__ = {
        'Name': AIaDataObjectFieldXmlSchema(namespace='urn:people', namespace_local_name='Name'),
        'Age': AIaDataObjectFieldXmlSchema(namespace_local_name='age', value_kind=AInXmlValueKind.ATTRIBUTE),
        'Address': AIaDataObjectFieldXmlSchema(namespace='urn:people', namespace_local_name='Address'),
    }
    __json_schema__ = object()

    def getName(self) -> str:
        """Read the person's name."""
        return self.get_EffectiveField('Name')

    def getAge(self) -> int:
        """Read the person's age."""
        return self.get_EffectiveField('Age')

    @AIaSmartDataObjectFieldImplementationBinding(implementationClass=AddressSDO)
    def getAddress(self) -> Address:
        """Read the nested address."""
        return self.get_EffectiveField('Address')


def _registry():
    for provider_class in (AIcXmlFormatBindingProvider, AIcJsonFormatBindingProvider,
                           AIcYamlFormatBindingProvider):
        AIsSmartDataObjectFormatBindingProviderRegistry.registerProvider(provider_class())
    return (AIcSmartDataObjectBindingRegistry.builder()
            .register(PersonSDO).register(AddressSDO).build())


def _person() -> PersonSDO:
    address = AddressSDO()
    address.set_RawField('City', 'Brno')
    person = PersonSDO()
    person.set_RawField('Name', 'Příliš žluťoučký')
    person.set_RawField('Age', 42)
    person.set_RawField('Address', address)
    return person


@pytest.mark.parametrize('provider_class',
                         (AIcJsonFormatBindingProvider, AIcYamlFormatBindingProvider,
                          AIcXmlFormatBindingProvider))
def test_nested_roundtrip(provider_class) -> None:
    """Preserve nested SDO, accented text and numeric type in all three codecs."""
    registry = _registry()
    provider = provider_class()
    encoded = provider.createMarshaller().marshal(_person(), registry)
    restored = provider.createUnmarshaller().unmarshal(encoded, Person, registry)
    assert type(restored) is PersonSDO
    assert restored.getName() == 'Příliš žluťoučký'
    assert restored.getAge() == 42
    assert type(restored.getAddress()) is AddressSDO
    assert restored.getAddress().getCity() == 'Brno'
    assert 'Příliš žluťoučký' in encoded
    if provider.getFormatId() == 'XML':
        assert 'urn:people' in encoded
        assert 'age="42"' in encoded
        assert 'urn:addr' in encoded


def test_metadata_provider_contributions_are_available() -> None:
    """Attach XML/JSON/YAML metadata without legacy cache or markers."""
    _registry()
    meta = AIsSmartDataObjectMetadataRegistry.forType(PersonSDO)
    assert {item.getFormatId() for item in meta.getFormatBindings()} == {'XML', 'JSON', 'YAML'}
    assert meta.getFormatBinding('XML', 'algites-xml').root_name == 'Person'


def test_xml_rejects_external_entities_and_repeated_scalar() -> None:
    """Reject DTDs and ambiguous repeated scalar elements."""
    registry = _registry()
    unmarshaller = AIcXmlFormatBindingProvider().createUnmarshaller()
    with pytest.raises(ValueError, match='DTD'):
        unmarshaller.unmarshal('<!DOCTYPE foo [<!ENTITY ex SYSTEM "file:///etc/passwd">]>'
                                 '<Person xmlns="urn:people"/>', Person, registry)
    with pytest.raises(ValueError, match='Repeated XML element'):
        unmarshaller.unmarshal('<Person xmlns="urn:people"><Name>A</Name><Name>B</Name></Person>',
                               Person, registry)


def test_xml_explicit_null_is_preserved() -> None:
    """Ensure explicitly present null differs from absence in XML."""
    registry = _registry()
    person = _person()
    person.set_RawField('Address', None)
    text = AIcXmlFormatBindingProvider().createMarshaller().marshal(person, registry)
    assert 'nil="true"' in text
    restored = AIcXmlFormatBindingProvider().createUnmarshaller().unmarshal(text, Person, registry)
    assert restored.isRawFieldPresent('Address')
    assert restored.getAddress() is None


def test_inherited_python_field_constraints() -> None:
    """A descendant's getter retains declared constraints and can refine them."""
    from eu.algites.lib.data.dataobject.aia_data_object_field_string_constraints import AIaDataObjectFieldStringConstraints
    from eu.algites.lib.data.dataobject.aia_data_object_field_numeric_constraints import AIaDataObjectFieldNumericConstraints
    from eu.algites.lib.data.dataobject.aia_data_object_field_list_constraints import AIaDataObjectFieldListConstraints
    from eu.algites.lib.data.dataobject.aia_data_object_field_object_constraints import AIaDataObjectFieldObjectConstraints

    class Base(AIiDataObject):
        @AIaDataObjectFieldStringConstraints(min_length=3)
        @AIaDataObjectField(dataFieldName='code')
        def getCode(self) -> str: ...

        @AIaDataObjectFieldNumericConstraints(minimum='10', maximum='20')
        @AIaDataObjectField(dataFieldName='quantity')
        def getQuantity(self) -> int: ...

        @AIaDataObjectFieldListConstraints(min_items=1, unique_items=True)
        @AIaDataObjectField(dataFieldName='labels')
        def getLabels(self) -> list[str]: ...

        @AIaDataObjectFieldObjectConstraints(min_properties=1)
        @AIaDataObjectField(dataFieldName='properties')
        def getProperties(self) -> dict[str, str]: ...

    @AIaSmartDataObjectImplementationBinding(readContracts=(Base,))
    class Implementation(AIcSmartDataObject, Base):
        def getCode(self) -> str: return self.get_EffectiveField('Code')
        def getQuantity(self) -> int: return self.get_EffectiveField('Quantity')
        def getLabels(self) -> list[str]: return self.get_EffectiveField('Labels')
        def getProperties(self) -> dict[str, str]: return self.get_EffectiveField('Properties')

    md = AIsSmartDataObjectMetadataRegistry.forType(Implementation)
    assert md.getField('Code').getStringConstraints().min_length == 3
    assert md.getField('Quantity').getNumericConstraints().minimum == '10'
    assert md.getField('Labels').getListConstraints().unique_items
    assert md.getField('Properties').getObjectConstraints().min_properties == 1
    instance = Implementation()
    with pytest.raises(ValueError, match='String length'):
        instance.set_RawField('Code', 'ab')
    instance.set_RawField('Code', 'abc')
    with pytest.raises(ValueError, match='minimum'):
        instance.set_RawField('Quantity', 9)
    with pytest.raises(ValueError, match='maximum'):
        instance.set_RawField('Quantity', 21)
    instance.set_RawField('Quantity', 12)
    with pytest.raises(ValueError, match='List cardinality'):
        instance.set_RawField('Labels', [])
    with pytest.raises(ValueError, match='List uniqueness'):
        instance.set_RawField('Labels', ['a', 'a'])
    instance.set_RawField('Labels', ['a', 'b'])
    with pytest.raises(ValueError, match='Object cardinality'):
        instance.set_RawField('Properties', {})
    instance.set_RawField('Properties', {'x': 'y'})


def test_json_and_yaml_reject_duplicate_keys() -> None:
    """Both data-mapping providers reject duplicate canonical field names."""
    registry = _registry()
    with pytest.raises(ValueError, match='Duplicate'):
        AIcJsonFormatBindingProvider().createUnmarshaller().unmarshal(
            '{"name":"A","name":"B"}', Person, registry)
    with pytest.raises(ValueError, match='Duplicate'):
        AIcYamlFormatBindingProvider().createUnmarshaller().unmarshal(
            '"name": "A"\n"name": "B"\n', Person, registry)

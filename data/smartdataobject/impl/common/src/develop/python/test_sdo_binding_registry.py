"""Regressions for separate, inherited Python SDO implementation bindings."""
from __future__ import annotations

from eu.algites.lib.data.smartdataobject.impl.common.aia_smart_data_object_implementation_binding import AIaSmartDataObjectImplementationBinding
from eu.algites.lib.data.smartdataobject.impl.common.aia_smart_data_object_field_implementation_binding import AIaSmartDataObjectFieldImplementationBinding
from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_metadata_registry import AIsSmartDataObjectMetadataRegistry
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object_binding_registry import AIcSmartDataObjectBindingRegistry
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object import AIcSmartDataObject
from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject
from eu.algites.lib.data.dataobject.aia_data_object_field import AIaDataObjectField


class Address(AIiDataObject):
    """Read-only address contract."""
    @AIaDataObjectField(dataFieldName='postal_code')
    def getPostalCode(self) -> str: ...


class Contact(AIiDataObject):
    """Second, unrelated read-only contract."""


@AIaSmartDataObjectImplementationBinding(readContracts=(Address,))
class AddressSDO(AIcSmartDataObject, Address):
    """Generated-style concrete SDO implementation."""
    def getPostalCode(self) -> str:
        return '100'


@AIaSmartDataObjectImplementationBinding(readContracts=(Contact,))
class CustomAddressSDO(AddressSDO, Contact):
    """Custom subclass contributing a second contract."""
    def getPostalCode(self) -> str:
        return '200'


class Person(AIiDataObject):
    """A contract with an SDO-valued field."""
    @AIaDataObjectField(dataFieldName='address')
    def getAddress(self) -> Address: ...


@AIaSmartDataObjectImplementationBinding(readContracts=(Person,))
class PersonSDO(AIcSmartDataObject, Person):
    """Annotated concrete nested-implementation selection."""
    @AIaSmartDataObjectFieldImplementationBinding(implementationClass=CustomAddressSDO)
    def getAddress(self) -> Address:
        return CustomAddressSDO()


def test_metadata_and_inherited_contracts() -> None:
    """Verify a subclass keeps the effective contract and getter metadata."""
    metadata = AIsSmartDataObjectMetadataRegistry.forType(CustomAddressSDO)
    assert len(metadata.getFields()) == 1
    assert metadata.getField('PostalCode').getDataFieldName() == 'postal_code'
    assert metadata.getField('PostalCode').getEffectiveGetter() is CustomAddressSDO.getPostalCode
    registry = (AIcSmartDataObjectBindingRegistry.builder()
                .register(AddressSDO).register(CustomAddressSDO).register(PersonSDO).build())
    assert len(registry.getBindingsForReadContract(Address)) == 2
    assert registry.getBinding(CustomAddressSDO).getDeclaredReadContracts() == frozenset({Contact})
    assert Address in registry.getBinding(CustomAddressSDO).getEffectiveReadContracts()
    assert registry.getBinding(PersonSDO).getFieldBinding('Address').getImplementationClassOverride() is CustomAddressSDO


def test_independent_registry_scope() -> None:
    """Ensure a separate registry does not inherit unrelated registrations."""
    registry = AIcSmartDataObjectBindingRegistry.builder().register(AddressSDO).build()
    assert len(registry.getBindingsForReadContract(Address)) == 1
    assert not registry.getBindingsForReadContract(Contact)


def test_empty_dataobject_implementation_is_valid():
    """Both metadata and bindings support schema types with no declared fields."""
    from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object import AIcSmartDataObject
    from eu.algites.lib.data.smartdataobject.impl.common.aia_smart_data_object_implementation_binding import AIaSmartDataObjectImplementationBinding
    from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_metadata_registry import AIsSmartDataObjectMetadataRegistry
    from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object_binding_registry import AIcSmartDataObjectBindingRegistry
    from eu.algites.lib.data.dataobject.aii_data_object import AIiDataObject

    class AIigEmpty(AIiDataObject):
        """Schema-valid empty contract."""

    @AIaSmartDataObjectImplementationBinding(readContracts=(AIigEmpty,))
    class AIcEmpty(AIcSmartDataObject, AIigEmpty):
        """No properties to store."""

    assert AIsSmartDataObjectMetadataRegistry.forType(AIcEmpty).getFields() == ()
    registry = AIcSmartDataObjectBindingRegistry.builder().register(AIcEmpty).build()
    assert len(registry.getBindingsForReadContract(AIigEmpty)) == 1

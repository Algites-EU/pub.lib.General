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


def test_eager_format_metadata_with_single_process_discovery(monkeypatch) -> None:
    """Format metadata are eager, while service discovery is once per process."""
    import eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_format_binding_provider_registry as discovery
    from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_format_binding_provider_registry import (
        AIsSmartDataObjectFormatBindingProviderRegistry as Registry,
    )
    calls = {'entry_points': 0, 'load': 0, 'instantiate': 0, 'resolve': 0}

    class FormatMetadata:
        def getFormatId(self): return 'TEST'
        def getProviderId(self): return 'unit-test'

    class Provider:
        def __init__(self): calls['instantiate'] += 1
        def getFormatId(self): return 'TEST'
        def getProviderId(self): return 'unit-test'
        def resolveMetadata(self, metadata):
            calls['resolve'] += 1
            assert metadata.getFields()  # Common metadata must be fully available.
            return FormatMetadata()

    class EntryPoint:
        def load(self):
            calls['load'] += 1
            return Provider

    def entry_points(**kwargs):
        assert kwargs['group'] == Registry.ENTRY_POINT_GROUP
        calls['entry_points'] += 1
        # Duplicate service declarations must not instantiate a provider twice.
        return (EntryPoint(), EntryPoint())

    # Isolate this test from the runtime's process-scoped provider snapshot.
    monkeypatch.setattr(discovery, 'entry_points', entry_points)
    monkeypatch.setattr(Registry, '_discovered', None)
    monkeypatch.setattr(Registry, '_explicit', {})

    class First(AIcSmartDataObject):
        @AIaDataObjectField(dataFieldName='first')
        def getFirst(self) -> str: return self.get_RawField('First')

    class Second(AIcSmartDataObject):
        @AIaDataObjectField(dataFieldName='second')
        def getSecond(self) -> str: return self.get_RawField('Second')

    first = AIsSmartDataObjectMetadataRegistry.forType(First)
    assert first.getFormatBinding('TEST', 'unit-test') is not None
    assert calls == {'entry_points': 1, 'load': 2, 'instantiate': 1, 'resolve': 1}

    for _ in range(100):
        assert AIsSmartDataObjectMetadataRegistry.forType(First) is first
        assert First().get_RawField('First') is None
        assert Registry.getProviders('TEST')[0] is Registry.getProviders()[0]

    assert calls == {'entry_points': 1, 'load': 2, 'instantiate': 1, 'resolve': 1}
    second = AIsSmartDataObjectMetadataRegistry.forType(Second)
    assert second.getFormatBinding('TEST', 'unit-test') is not None
    assert calls == {'entry_points': 1, 'load': 2, 'instantiate': 1, 'resolve': 2}


def test_empty_discovery_result_is_cached(monkeypatch) -> None:
    """No entry points is a stable result, not a reason for another scan."""
    import eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_format_binding_provider_registry as discovery
    from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_format_binding_provider_registry import (
        AIsSmartDataObjectFormatBindingProviderRegistry as Registry,
    )
    scans = []

    def no_entry_points(**kwargs):
        scans.append(kwargs['group'])
        return ()

    monkeypatch.setattr(discovery, 'entry_points', no_entry_points)
    monkeypatch.setattr(Registry, '_discovered', None)
    monkeypatch.setattr(Registry, '_explicit', {})
    assert Registry.getProviders('XML') == ()
    assert Registry.getProviders('JSON') == ()
    assert Registry.getProviders('YAML') == ()
    assert len(scans) == 1


def test_concurrent_metadata_requests_share_one_discovery(monkeypatch) -> None:
    """Multiple parallel first callers cannot instantiate the same service twice."""
    from concurrent.futures import ThreadPoolExecutor
    import eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_format_binding_provider_registry as discovery
    from eu.algites.lib.data.smartdataobject.impl.common.ais_smart_data_object_format_binding_provider_registry import (
        AIsSmartDataObjectFormatBindingProviderRegistry as Registry,
    )
    scans = []

    def no_entry_points(**kwargs):
        scans.append(kwargs['group'])
        return ()

    monkeypatch.setattr(discovery, 'entry_points', no_entry_points)
    monkeypatch.setattr(Registry, '_discovered', None)
    monkeypatch.setattr(Registry, '_explicit', {})
    classes = [type('ConcurrentSDO' + str(i), (AIcSmartDataObject,), {}) for i in range(20)]
    with ThreadPoolExecutor(max_workers=8) as executor:
        results = list(executor.map(AIsSmartDataObjectMetadataRegistry.forType, classes))
    assert len(results) == 20
    assert len(scans) == 1

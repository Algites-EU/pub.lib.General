"""Generated-contract restrictions and ordinary XML element type inheritance."""
import pytest

from eu.algites.lib.data.dataobject.aicd_data_object_field import AIcdDataObjectField
from eu.algites.lib.data.dataobject.ain_field_presence_kind import AInFieldPresenceKind as Presence
from eu.algites.lib.data.smartdataobject.impl.standard.aic_smart_data_object import AIcSmartDataObject


class AIigBase:
    __data_object_fields__ = (AIcdDataObjectField('Reference'),)


class AIigRestricted(AIigBase):
    __data_object_fields__ = (AIcdDataObjectField('Reference', presence=Presence.FORBIDDEN),)


class AIcgdRestricted(AIcSmartDataObject, AIigRestricted):
    def getReference(self):
        return self.get_EffectiveField('Reference')

    def setReference(self, value):
        self.set_RawField('Reference', value)

    def get_Reference(self):
        return self.get_RawField('Reference')


def test_derived_field_restriction_is_effective_and_absence_is_valid():
    obj = AIcgdRestricted()
    assert obj.is_ByRawValuesValid()
    assert not obj.get_RawNotPresentRequiredFields()
    with pytest.raises(ValueError, match='forbidden'):
        obj.setReference('X')
    with pytest.raises(ValueError, match='forbidden'):
        obj.getReference()
    with pytest.raises(ValueError, match='forbidden'):
        obj.get_Reference()
    with pytest.raises(ValueError, match='forbidden'):
        AIcgdRestricted.from_mapping({'Reference': 'X'})


def test_invalid_presence_widening_rejected():
    class AIigRequired:
        __data_object_fields__ = (AIcdDataObjectField('Reference', presence=Presence.REQUIRED),)

    class AIigInvalid(AIigRequired):
        __data_object_fields__ = (AIcdDataObjectField('Reference', presence=Presence.FORBIDDEN),)

    with pytest.raises(ValueError, match='presence restriction'):
        class AIcgdInvalid(AIcSmartDataObject, AIigInvalid):
            pass
        AIcgdInvalid._fields()


class AIigPayment:
    pass


class AIigCryptoPayment(AIigPayment):
    pass


class AIigTokenPayment(AIigCryptoPayment):
    pass


class AIigPrivatePayment(AIigPayment):
    pass


def test_transitive_xml_substitution_type_inheritance():
    assert issubclass(AIigTokenPayment, AIigPayment)
    assert issubclass(AIigTokenPayment, AIigCryptoPayment)
    assert issubclass(AIigPrivatePayment, AIigPayment)
    assert not issubclass(AIigPayment, AIigCryptoPayment)


class AIcgdPaymentOwner(AIcSmartDataObject):
    __data_object_fields__ = (AIcdDataObjectField('Payment', expected_type=AIigPayment),)

    def setPayment(self, value):
        self.set_RawField('Payment', value)


def test_substitutable_sdo_assignment_uses_type_inheritance():
    owner = AIcgdPaymentOwner()
    owner.setPayment(AIigTokenPayment())
    owner.setPayment(AIigPrivatePayment())
    with pytest.raises(ValueError, match='type'):
        owner.setPayment('wrong kind')


def test_factory_backed_default_without_reflective_constructor():
    from eu.algites.lib.data.dataobject.aia_field_default import AIaFieldDefault
    from eu.algites.lib.data.dataobject.aii_field_default_value_resolver_factory import AIiFieldDefaultValueResolverFactory
    from eu.algites.lib.factory.cpdiscovery.aia_category_provider_factory_definition import AIaCategoryProviderFactoryDefinition
    from eu.algites.lib.factory.cpdiscovery.category_provider_factory import category_provider_factory
    from eu.algites.lib.factory.cpdiscovery.ais_category_provider_discovery import AIsCategoryProviderDiscovery
    from eu.algites.lib.factory.cpdiscovery.impl.standard.aic_category_provider_registry import AIcCategoryProviderRegistry

    class DefaultResolver:
        def resolve_default(self, owner_class, getter_name):
            assert getter_name == 'getDefault'
            return 'resolved-value'

    @category_provider_factory(provider_id='test-defaults', categories=('default',))
    class TestDefaultFactory(AIiFieldDefaultValueResolverFactory):
        def get_implementation_interface_type(self): return DefaultResolver
        def get_context_interface_type(self): return object
        def create_instance(self, category, context): return DefaultResolver()

    registry = AIcCategoryProviderRegistry()
    registry.register_factories('test-defaults', (TestDefaultFactory(),))
    AIsCategoryProviderDiscovery.set_registry(registry)
    try:
        class Contract(AIcSmartDataObject):
            __data_object_fields__ = (AIcdDataObjectField('Default', presence=Presence.REQUIRED),)
            @AIaFieldDefault(AIaCategoryProviderFactoryDefinition(
                AIiFieldDefaultValueResolverFactory, factory_provider_id='test-defaults'))
            def getDefault(self): return self.get_EffectiveField('Default')

        instance = Contract()
        assert instance.getDefault() == 'resolved-value'
        assert not instance.isPresent_Field('Default')
        assert instance.get_RawNotPresentRequiredFields() == {'Default'}
        assert instance.get_EffectiveNotPresentRequiredFields() == set()
    finally:
        AIsCategoryProviderDiscovery.set_registry(None)

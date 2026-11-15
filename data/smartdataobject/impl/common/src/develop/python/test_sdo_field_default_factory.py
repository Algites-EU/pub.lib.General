"""Regression checks for standalone factory-backed defaults."""
from decimal import Decimal
from eu.algites.lib.factory.cpdiscovery.ais_category_provider_discovery import AIsCategoryProviderDiscovery
from eu.algites.lib.factory.cpdiscovery.impl.standard.aic_category_provider_registry import AIcCategoryProviderRegistry
from eu.algites.lib.factory.cpdiscovery.aia_category_provider_factory_definition import AIaCategoryProviderFactoryDefinition
from eu.algites.lib.data.dataobject.aia_data_object_field import AIaDataObjectField
from eu.algites.lib.data.dataobject.aia_data_object_field_default import AIaDataObjectFieldDefault
from eu.algites.lib.data.dataobject.aii_field_default_scalar_value_factory import AIiFieldDefaultScalarValueFactory
from eu.algites.lib.data.dataobject.ain_data_object_field_default_kind import AInDataObjectFieldDefaultKind
from eu.algites.lib.data.smartdataobject.impl.common.aic_field_default_scalar_value_factory import AIcFieldDefaultScalarValueFactory
from eu.algites.lib.data.smartdataobject.impl.common.aic_smart_data_object import AIcSmartDataObject

def test_field_defaults():
    registry = AIcCategoryProviderRegistry()
    registry.register_factories('test', (AIcFieldDefaultScalarValueFactory(),))
    AIsCategoryProviderDiscovery.set_registry(registry)
    select = AIaCategoryProviderFactoryDefinition(AIiFieldDefaultScalarValueFactory)
    class SDO(AIcSmartDataObject):
        @AIaDataObjectField(presence=__import__('eu.algites.lib.data.dataobject.ain_field_presence_kind', fromlist=['AInFieldPresenceKind']).AInFieldPresenceKind.REQUIRED, allows_null=False)
        @AIaDataObjectFieldDefault(default_value_factory=select, kind=AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_NOT_PRESENT, default_arguments=('12345',))
        def getNumber(self) -> int: return self.get_EffectiveField('Number')

        @AIaDataObjectField(is_empty_allowed=True)
        @AIaDataObjectFieldDefault(default_value_factory=select, kind=AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_EMPTY, default_arguments=('a',))
        def getName(self) -> str: return self.get_EffectiveField('Name')

        @AIaDataObjectField()
        @AIaDataObjectFieldDefault(default_value_factory=select, kind=AInDataObjectFieldDefaultKind.CONSTRAINT_DEFAULT, default_arguments=('1.0',))
        def getFixed(self) -> Decimal: return self.get_EffectiveField('Fixed')

    v=SDO()
    assert v.getNumber() == 12345
    assert v.getRawValue('Number') is None and not v.isRawFieldPresent('Number')
    assert v.getName() is None
    v.set_EmptyField('Name')
    assert v.getName() == 'a'
    v.set_RawField('Name', '')
    assert v.getName() == ''
    v.set_RawField('Fixed', Decimal('1.00'))
    for fn in (lambda: v.set_RawField('Fixed', Decimal('2')), lambda: v.set_RawField('Fixed', None)):
        try: fn(); raise AssertionError('expected fixed violation')
        except ValueError: pass
    assert v.get_EffectiveNotPresentRequiredFields() == set()
    assert v.get_RawNotPresentRequiredFields() == {'Number'}
    AIsCategoryProviderDiscovery.set_registry(None)

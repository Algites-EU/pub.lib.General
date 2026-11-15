"""Dedicated category-provider SPI for DataObject default values."""
from abc import ABC
from eu.algites.lib.factory.cpdiscovery.aii_category_provider_factory import AIiCategoryProviderFactory
from .aicd_field_default_value import AIcdFieldDefaultValue
from .aii_field_default_value_factory_context import AIiFieldDefaultValueFactoryContext

class AIiFieldDefaultValueFactory(AIiCategoryProviderFactory[AIcdFieldDefaultValue, AIiFieldDefaultValueFactoryContext], ABC):
    pass

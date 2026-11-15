"""Independent, method-bound default declaration for a DataObject getter."""
from dataclasses import dataclass
from .ain_data_object_field_default_kind import AInDataObjectFieldDefaultKind
from .aii_field_default_value_factory import AIiFieldDefaultValueFactory
from eu.algites.lib.factory.cpdiscovery.aia_category_provider_factory_definition import AIaCategoryProviderFactoryDefinition

@dataclass(frozen=True, slots=True)
class AIaDataObjectFieldDefault:
    """Presence defines a default; a factory result may explicitly represent null."""
    default_value_factory: AIaCategoryProviderFactoryDefinition
    kind: AInDataObjectFieldDefaultKind = AInDataObjectFieldDefaultKind.DECLARATION_ONLY
    default_arguments: tuple[str, ...] = ()

    def __post_init__(self):
        if not issubclass(self.default_value_factory.factory_class, AIiFieldDefaultValueFactory):
            raise TypeError('Default factory interface must specialize AIiFieldDefaultValueFactory')

    def __call__(self, getter):
        if not callable(getter):
            raise TypeError('Default annotation requires a getter method')
        setattr(getter, '__sdo_field_default_definition__', self)
        return getter

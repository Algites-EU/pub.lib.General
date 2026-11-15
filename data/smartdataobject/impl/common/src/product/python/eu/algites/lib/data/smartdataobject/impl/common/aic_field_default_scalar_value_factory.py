"""Standard scalar default factory registered with CPDiscovery."""
from decimal import Decimal
from enum import Enum
from eu.algites.lib.data.dataobject.aii_field_default_scalar_value_factory import AIiFieldDefaultScalarValueFactory
from eu.algites.lib.data.dataobject.aii_field_default_value_factory_context import AIiFieldDefaultValueFactoryContext
from eu.algites.lib.data.dataobject.aicd_field_default_value import AIcdFieldDefaultValue
from eu.algites.lib.factory.cpdiscovery.category_provider_factory import category_provider_factory

@category_provider_factory(provider_id='algites-standard', categories=('default',))
class AIcFieldDefaultScalarValueFactory(AIiFieldDefaultScalarValueFactory):
    def get_implementation_interface_type(self): return AIcdFieldDefaultValue
    def get_context_interface_type(self): return AIiFieldDefaultValueFactoryContext

    def create_instance(self, category: str, context: AIiFieldDefaultValueFactoryContext) -> AIcdFieldDefaultValue:
        if category != 'default':
            raise ValueError('Unsupported scalar default category: ' + category)
        if context is None or len(context.default_arguments) != 1:
            raise ValueError('Scalar default requires exactly one lexical argument')
        text = context.default_arguments[0]
        typ = context.expected_type
        if typ is str: result = text
        elif typ is bool:
            if text not in ('true', 'false'): raise ValueError('Invalid Boolean default: ' + text)
            result = text == 'true'
        elif typ is int: result = int(text)
        elif typ is float: result = float(text)
        elif typ is Decimal:
            result = Decimal(text)
            if not result.is_finite(): raise ValueError('Non-finite Decimal default: ' + text)
        elif isinstance(typ, type) and issubclass(typ, Enum):
            matches = []
            for item in typ:
                code = getattr(item, 'code', None)
                code = code() if callable(code) else code
                if str(code) == text: matches.append(item)
            if len(matches) != 1: raise ValueError(f'Unknown enum code {text!r} for {typ}')
            result = matches[0]
        else: raise ValueError(f'Unsupported scalar default type: {typ}')
        return AIcdFieldDefaultValue(result)

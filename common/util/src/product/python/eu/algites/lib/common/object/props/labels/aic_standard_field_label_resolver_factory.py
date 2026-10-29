"""Standard service provider for the FieldLabelResolver factory SPI."""
from eu.algites.lib.factory.cpdiscovery.category_provider_factory import category_provider_factory
from .aii_field_label_resolver_factory import AIiFieldLabelResolverFactory

class _DefaultLabelResolver:
    def resolveLabel(self, owner, field_name, purpose, fmt, locale):
        return None


@category_provider_factory(provider_id='algites-standard', categories=('default',))
class AIcStandardFieldLabelResolverFactory(AIiFieldLabelResolverFactory):
    """Stateless standard factory; pluggable through CategoryProviderDiscovery."""
    def get_implementation_interface_type(self):
        return object

    def get_context_interface_type(self):
        return type

    def create_instance(self, category, context):
        if category != 'default':
            raise ValueError('Unsupported resolver category: ' + str(category))
        return _DefaultLabelResolver()

"""Standard service provider for the RenderingOutputLocaleResolver factory SPI."""
from eu.algites.lib.factory.cpdiscovery.category_provider_factory import category_provider_factory
from .aii_rendering_output_locale_resolver_factory import AIiRenderingOutputLocaleResolverFactory
from .ais_rendering_output_utils import AIsRenderingOutputUtils


@category_provider_factory(provider_id='algites-standard', categories=('default',))
class AIcStandardRenderingOutputLocaleResolverFactory(AIiRenderingOutputLocaleResolverFactory):
    """Stateless standard factory; pluggable through CategoryProviderDiscovery."""
    def get_implementation_interface_type(self):
        return object

    def get_context_interface_type(self):
        return type

    def create_instance(self, category, context):
        if category != 'default':
            raise ValueError('Unsupported resolver category: ' + str(category))
        class Resolver:
            def resolveLocale(self):
                return AIsRenderingOutputUtils.usedRenderingOutputLocale
        return Resolver()

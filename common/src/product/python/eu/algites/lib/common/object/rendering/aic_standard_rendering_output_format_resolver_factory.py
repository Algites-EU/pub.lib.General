"""Standard service provider for the RenderingOutputFormatResolver factory SPI."""
from eu.algites.lib.factory.cpdiscovery.category_provider_factory import category_provider_factory
from .aii_rendering_output_format_resolver_factory import AIiRenderingOutputFormatResolverFactory
from .ais_rendering_output_utils import AIsRenderingOutputUtils


@category_provider_factory(provider_id='algites-standard', categories=('default',))
class AIcStandardRenderingOutputFormatResolverFactory(AIiRenderingOutputFormatResolverFactory):
    """Stateless standard factory; pluggable through CategoryProviderDiscovery."""
    def get_implementation_interface_type(self):
        return object

    def get_context_interface_type(self):
        return type

    def create_instance(self, category, context):
        if category != 'default':
            raise ValueError('Unsupported resolver category: ' + str(category))
        class Resolver:
            def resolveFormat(self):
                return AIsRenderingOutputUtils.usedRenderingOutputFormat
        return Resolver()

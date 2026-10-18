"""Standard service provider for the RenderingOutputPurposeResolver factory SPI."""
from eu.algites.lib.factory.cpdiscovery.category_provider_factory import category_provider_factory
from .aii_rendering_output_purpose_resolver_factory import AIiRenderingOutputPurposeResolverFactory
from .ais_rendering_output_utils import AIsRenderingOutputUtils


@category_provider_factory(provider_id='algites-standard', categories=('default',))
class AIcStandardRenderingOutputPurposeResolverFactory(AIiRenderingOutputPurposeResolverFactory):
    """Stateless standard factory; pluggable through CategoryProviderDiscovery."""
    def get_implementation_interface_type(self):
        return object

    def get_context_interface_type(self):
        return type

    def create_instance(self, category, context):
        if category != 'default':
            raise ValueError('Unsupported resolver category: ' + str(category))
        class Resolver:
            def resolvePurpose(self):
                return AIsRenderingOutputUtils.usedRenderingOutputPurpose
        return Resolver()

from abc import ABC
import pytest
from eu.algites.lib.factory.cpdiscovery.aii_category_provider_factory import AIiCategoryProviderFactory
from eu.algites.lib.factory.cpdiscovery.category_provider_factory import category_provider_factory
from eu.algites.lib.factory.cpdiscovery.aix_category_provider_conflict_exception import AIxCategoryProviderConflictException
from eu.algites.lib.factory.cpdiscovery.aix_category_provider_unavailable_exception import AIxCategoryProviderUnavailableException
from eu.algites.lib.factory.cpdiscovery.impl.standard.aic_category_provider_registry import AIcCategoryProviderRegistry
from eu.algites.lib.factory.cpdiscovery.aii_category_provider_factory_metadata import AIiCategoryProviderFactoryMetadata
from eu.algites.lib.factory.cpdiscovery.impl.standard.aicd_category_provider_factory_metadata import AIcdCategoryProviderFactoryMetadata

class AIiResult(ABC):
    pass
class AIiContext(ABC):
    pass
class AIcContext(AIiContext):
    pass
class AIcResult(AIiResult):
    def __init__(self, provider): self.provider=provider
class AIiExampleFactory(AIiCategoryProviderFactory[AIiResult,AIiContext]):
    pass
@category_provider_factory(provider_id='a',categories=('filesystem','durable'))
class AIcFactoryA(AIiExampleFactory):
    def get_implementation_interface_type(self): return AIiResult
    def get_context_interface_type(self): return AIiContext
    def create_instance(self,category,context): return AIcResult('a')
@category_provider_factory(provider_id='b',categories=('filesystem',))
class AIcFactoryB(AIcFactoryA):
    def create_instance(self,category,context): return AIcResult('b')

def test_unique_provider_and_multiple_categories():
    r=AIcCategoryProviderRegistry();r.register_factories('a',(AIcFactoryA(),))
    assert r.get_instance(AIiExampleFactory,'filesystem',AIcContext()).provider=='a'
    assert r.get_instance(AIiExampleFactory,'durable',AIcContext()).provider=='a'

def test_ambiguity_and_explicit_provider():
    r=AIcCategoryProviderRegistry();r.register_factories('a',(AIcFactoryA(),));r.register_factories('b',(AIcFactoryB(),))
    with pytest.raises(AIxCategoryProviderUnavailableException):r.get_instance(AIiExampleFactory,'filesystem',AIcContext())
    assert r.get_instance(AIiExampleFactory,'filesystem',AIcContext(),'b').provider=='b'

def test_default_and_unregistration():
    key=f'{AIiExampleFactory.__module__}.{AIiExampleFactory.__qualname__}.filesystem'
    r=AIcCategoryProviderRegistry();r.register_factories('config',(),{key:'b'})
    r.register_factories('a',(AIcFactoryA(),));r.register_factories('b',(AIcFactoryB(),))
    assert r.get_instance(AIiExampleFactory,'filesystem',AIcContext()).provider=='b'
    r.unregister_factories('b')
    with pytest.raises(AIxCategoryProviderUnavailableException):r.get_instance(AIiExampleFactory,'filesystem',AIcContext())
    r.unregister_factories('config')
    assert r.get_instance(AIiExampleFactory,'filesystem',AIcContext()).provider=='a'

def test_duplicate_provider_rolls_back():
    @category_provider_factory(provider_id='a',categories=('filesystem',))
    class AIcConflictingFactory(AIiExampleFactory):
        def get_implementation_interface_type(self): return AIiResult
        def get_context_interface_type(self): return AIiContext
        def create_instance(self,category,context): return AIcResult('conflict')
    r=AIcCategoryProviderRegistry();r.register_factories('a',(AIcFactoryA(),))
    with pytest.raises(AIxCategoryProviderConflictException):r.register_factories('b',(AIcConflictingFactory(),))
    assert r.get_instance(AIiExampleFactory,'filesystem',AIcContext()).provider=='a'

def test_duplicate_defaults_roll_back():
    key=f'{AIiExampleFactory.__module__}.{AIiExampleFactory.__qualname__}.filesystem'
    r=AIcCategoryProviderRegistry();r.register_factories('a',(AIcFactoryA(),),{key:'a'})
    with pytest.raises(AIxCategoryProviderConflictException):r.register_factories('b',(),{key:'b'})
    assert r.get_instance(AIiExampleFactory,'filesystem',AIcContext()).provider=='a'

def test_factory_selection_and_metadata_without_instantiation():
    r = AIcCategoryProviderRegistry()
    r.register_factories('a', (AIcFactoryA(),))
    r.register_factories('b', (AIcFactoryB(),))
    assert r.find_factory(AIiExampleFactory, 'filesystem', ('missing', 'b', 'a')).__class__ is AIcFactoryB
    assert r.find_factory(AIiExampleFactory, 'filesystem', ('missing',)) is None
    assert r.find_factory(AIiExampleFactory, 'missing', ('b',)) is None
    assert r.has_category(AIiExampleFactory, 'filesystem')
    assert not r.has_category(AIiExampleFactory, 'missing')
    assert r.has_provider(AIiExampleFactory, 'filesystem', 'b')
    assert not r.has_provider(AIiExampleFactory, 'durable', 'b')
    assert r.get_available_categories(AIiExampleFactory) == frozenset(('filesystem', 'durable'))
    assert r.get_available_provider_ids(AIiExampleFactory, 'filesystem') == frozenset(('a', 'b'))
    metadata = r.get_available_factory_metadata(AIiExampleFactory)
    assert [(m.category, m.provider_id) for m in metadata] == [
        ('durable', 'a'), ('filesystem', 'a'), ('filesystem', 'b')]
    assert all(not m.default_provider for m in metadata)
    assert all(isinstance(m, AIcdCategoryProviderFactoryMetadata) for m in metadata)
    assert all(isinstance(m, AIiCategoryProviderFactoryMetadata) for m in metadata)
    assert metadata[0].context_interface_type is AIiContext
    assert metadata[0].implementation_interface_type is AIiResult
    r.unregister_factories('b')
    assert r.find_factory(AIiExampleFactory, 'filesystem') is not None
    assert r.get_available_provider_ids(AIiExampleFactory, 'filesystem') == frozenset(('a',))


def test_ordered_fallback_and_configured_default_are_separate():
    key = f'{AIiExampleFactory.__module__}.{AIiExampleFactory.__qualname__}.filesystem'
    r = AIcCategoryProviderRegistry()
    r.register_factories('a', (AIcFactoryA(),))
    r.register_factories('b', (AIcFactoryB(),), {key: 'b'})
    assert r.find_factory(AIiExampleFactory, 'filesystem').create_instance('filesystem', None).provider == 'b'
    assert r.find_factory(AIiExampleFactory, 'filesystem', ['a', 'b']).create_instance('filesystem', None).provider == 'a'
    assert [m.provider_id for m in r.get_available_factory_metadata(AIiExampleFactory)
            if m.default_provider] == ['b']
    r.unregister_factories('b')
    assert r.find_factory(AIiExampleFactory, 'filesystem') is not None

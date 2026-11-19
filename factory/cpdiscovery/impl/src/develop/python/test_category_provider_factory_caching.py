"""Regression for the opt-in CPDiscovery instance cache."""
from concurrent.futures import ThreadPoolExecutor
from dataclasses import dataclass

from eu.algites.lib.factory.cpdiscovery.aic_caching_category_provider_factory import (
    AIcCachingCategoryProviderFactory,
)


@dataclass(frozen=True)
class Context:
    name: str


class Factory(AIcCachingCategoryProviderFactory[object, Context | None]):
    def __init__(self):
        super().__init__()
        self.count = 0

    def get_implementation_interface_type(self):
        return object

    def get_context_interface_type(self):
        return Context

    def create_uncached_instance(self, category, context):
        self.count += 1
        return object()


def test_caching_by_category_and_context():
    factory = Factory()
    a = factory.create_instance("a", None)
    assert factory.create_instance("a", None) is a
    b = factory.create_instance("a", Context("one"))
    assert factory.create_instance("a", Context("one")) is b
    assert factory.create_instance("a", Context("two")) is not b
    assert factory.create_instance("b", None) is not a
    assert factory.count == 4


def test_concurrent_creation():
    factory = Factory()
    with ThreadPoolExecutor(max_workers=12) as pool:
        results = list(pool.map(lambda _: factory.create_instance("shared", Context("x")), range(80)))
    assert all(result is results[0] for result in results)
    assert factory.count == 1

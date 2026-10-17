from __future__ import annotations


def category_provider_factory(*, provider_id: str, categories: tuple[str, ...]):
    """Attach the same provider/category metadata expressed by the Java annotation."""
    def decorate(factory_type: type):
        factory_type.__catprov_provider_id__ = provider_id
        factory_type.__catprov_categories__ = tuple(categories)
        return factory_type
    return decorate

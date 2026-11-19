from __future__ import annotations

from dataclasses import dataclass
from eu.algites.lib.factory.cpdiscovery.aii_category_provider_factory_metadata import AIiCategoryProviderFactoryMetadata
from eu.algites.lib.factory.cpdiscovery.impl.standard.aicd_category_provider_factory_metadata import AIcdCategoryProviderFactoryMetadata
from inspect import isabstract
from importlib import metadata
from threading import RLock
from types import MappingProxyType
from typing import Any

from eu.algites.lib.factory.cpdiscovery.aii_category_provider_registry import AIiCategoryProviderRegistry
from eu.algites.lib.factory.cpdiscovery.aii_category_provider_factory import AIiCategoryProviderFactory
from eu.algites.lib.factory.cpdiscovery.aix_category_provider_conflict_exception import AIxCategoryProviderConflictException
from eu.algites.lib.factory.cpdiscovery.aix_category_provider_discovery_exception import AIxCategoryProviderDiscoveryException
from eu.algites.lib.factory.cpdiscovery.aix_category_provider_unavailable_exception import AIxCategoryProviderUnavailableException


@dataclass(frozen=True)
class _Snapshot:
    factories: dict[tuple[type, str, str], AIiCategoryProviderFactory]
    defaults: dict[tuple[type, str], str]


class AIcCategoryProviderRegistry(AIiCategoryProviderRegistry):
    """Atomic category/provider factory catalog over Python distribution entry points."""

    ENTRY_POINT_GROUP = 'eu.algites.lib.factory.cpdiscovery.factories'
    DEFAULTS_METADATA_FILENAME = 'catprov-defaults.properties'

    def __init__(self):
        self._write_lock = RLock()
        self._sources: dict[str, tuple[tuple[AIiCategoryProviderFactory, ...], dict[str, str]]] = {}
        self._snapshot = _Snapshot({}, {})

    def register_factories(self, source_id: str, factories: tuple[AIiCategoryProviderFactory, ...],
                           defaults: dict[str, str] | None = None) -> None:
        """Explicit registration also supports uninstalled development environments."""
        with self._write_lock:
            if source_id in self._sources:
                raise AIxCategoryProviderConflictException(f'Source already registered: {source_id}')
            candidate = dict(self._sources)
            candidate[source_id] = (tuple(factories), dict(defaults or {}))
            snapshot = self._aggregate(candidate)
            self._sources = candidate
            self._snapshot = snapshot

    def unregister_factories(self, source_id: str) -> None:
        with self._write_lock:
            if source_id not in self._sources:
                raise AIxCategoryProviderUnavailableException(f'Source not registered: {source_id}')
            candidate = dict(self._sources)
            del candidate[source_id]
            snapshot = self._aggregate(candidate)
            self._sources = candidate
            self._snapshot = snapshot

    def register_entry_point_factories(self) -> None:
        factories = []
        for entry in metadata.entry_points().select(group=self.ENTRY_POINT_GROUP):
            klass = entry.load()
            factories.append(klass())
        defaults = {}
        for dist in metadata.distributions():
            text = dist.read_text(self.DEFAULTS_METADATA_FILENAME)
            if text:
                for line in text.splitlines():
                    line = line.strip()
                    if not line or line.startswith(('#', '!')):
                        continue
                    if '=' not in line:
                        raise AIxCategoryProviderDiscoveryException(f'Malformed default line: {line}')
                    key, value = (p.strip() for p in line.split('=', 1))
                    if key in defaults and defaults[key] != value:
                        raise AIxCategoryProviderConflictException(f'Conflicting default: {key}')
                    defaults[key] = value
        self.register_factories('installed-entry-points', tuple(factories), defaults)

    def unregister_entry_point_factories(self) -> None:
        self.unregister_factories('installed-entry-points')

    def get_instance(self, factory_type: type, category: str, context: Any,
                     provider_id: str | None = None) -> Any:
        factory = self.find_factory(factory_type, category,
                                    [provider_id] if provider_id is not None else None)
        if factory is None:
            raise AIxCategoryProviderUnavailableException(
                f'Unavailable provider {provider_id} for {factory_type}/{category}')
        context_type = factory.get_context_interface_type()
        if context is not None and not isinstance(context, context_type):
            raise AIxCategoryProviderDiscoveryException(f'Unexpected context for provider {provider_id}')
        instance = factory.create_instance(category, context)
        if not isinstance(instance, factory.get_implementation_interface_type()):
            raise AIxCategoryProviderDiscoveryException(f'Unexpected result for provider {provider_id}')
        return instance

    def find_factory(self, factory_type: type, category: str,
                     provider_ids: tuple[str, ...] | list[str] | None = None) -> AIiCategoryProviderFactory | None:
        """Select a factory by ordered IDs, or a default/unique provider if omitted.

        Missing providers return None. Ambiguity without preferences is an error.
        The returned factory belongs to one immutable published snapshot.
        """
        if not category or category.strip() != category:
            raise ValueError(f'Invalid category: {category!r}')
        snapshot = self._snapshot
        if provider_ids:
            for provider_id in provider_ids:
                if not provider_id or provider_id.strip() != provider_id:
                    raise ValueError(f'Invalid provider ID: {provider_id!r}')
                factory = snapshot.factories.get((factory_type, category, provider_id))
                if factory is not None:
                    return factory
            return None
        default_provider = snapshot.defaults.get((factory_type, category))
        if default_provider is not None:
            return snapshot.factories.get((factory_type, category, default_provider))
        matches = [factory for (kind, name, _), factory in snapshot.factories.items()
                   if kind is factory_type and name == category]
        if len(matches) > 1:
            raise AIxCategoryProviderUnavailableException(
                f'Ambiguous provider for {factory_type}/{category}; supply preferred provider IDs')
        return matches[0] if matches else None

    def get_available_categories(self, factory_type: type) -> frozenset[str]:
        """Inspect category presence without instantiating any implementation."""
        return frozenset(category for kind, category, _ in self._snapshot.factories
                         if kind is factory_type)

    def get_available_provider_ids(self, factory_type: type, category: str) -> frozenset[str]:
        """Inspect registered providers for an interface and category."""
        return frozenset(provider_id for kind, name, provider_id in self._snapshot.factories
                         if kind is factory_type and name == category)

    def get_available_factory_metadata(self, factory_type: type) -> tuple[AIiCategoryProviderFactoryMetadata, ...]:
        """Return immutable, sorted metadata for one specialized factory contract."""
        snapshot = self._snapshot
        result = [AIcdCategoryProviderFactoryMetadata(category, provider_id,
                   factory.get_implementation_interface_type(), factory.get_context_interface_type(),
                   snapshot.defaults.get((kind, category)) == provider_id,
                   f'{type(factory).__module__}.{type(factory).__qualname__}')
                  for (kind, category, provider_id), factory in snapshot.factories.items()
                  if kind is factory_type]
        return tuple(sorted(result, key=lambda item: (item.category, item.provider_id)))

    @staticmethod
    def _aggregate(sources):
        factories = {}
        defaults = {}
        seen_classes = set()
        for source_id, (items, properties) in sources.items():
            for key, value in properties.items():
                if key in defaults and defaults[key] != value:
                    raise AIxCategoryProviderConflictException(f'Conflicting default for {key}')
                defaults[key] = value
            for factory in items:
                cls = type(factory)
                if cls in seen_classes:
                    continue
                seen_classes.add(cls)
                provider_id = cls.__dict__.get('__catprov_provider_id__')
                categories = cls.__dict__.get('__catprov_categories__', ())
                if not provider_id or not categories:
                    raise AIxCategoryProviderDiscoveryException(f'Missing provider metadata: {cls}')
                if not isinstance(factory.get_context_interface_type(), type):
                    raise AIxCategoryProviderDiscoveryException(f'Missing factory context contract: {cls}')
                specialized = [t for t in cls.__mro__[1:]
                               if issubclass(t, AIiCategoryProviderFactory)
                               and t is not AIiCategoryProviderFactory and isabstract(t)]
                specialized = [interface for interface in specialized
                               if not any(interface is not other and issubclass(other, interface)
                                          for other in specialized)]
                if len(specialized) != 1:
                    raise AIxCategoryProviderDiscoveryException(f'Factory needs one specialized SPI: {cls}')
                for category in categories:
                    key = (specialized[0], category, provider_id)
                    if key in factories:
                        raise AIxCategoryProviderConflictException(f'Duplicate provider {key}')
                    factories[key] = factory
        mapping = {}
        for key, value in defaults.items():
            for t, category, _ in factories:
                if key == f'{t.__module__}.{t.__qualname__}.{category}':
                    mapping[(t, category)] = value
        return _Snapshot(factories, mapping)

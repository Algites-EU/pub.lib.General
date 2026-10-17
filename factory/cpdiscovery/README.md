# Category Provider Discovery (CPDiscovery)

The `factory/cpdiscovery` domain separates provider factories (`intf`) from the
standard immutable-snapshot registry (`impl`). Public contracts are in
`eu.algites.lib.factory.cpdiscovery`; implementation code resides in
`eu.algites.lib.factory.cpdiscovery.impl.standard` in both technologies.

A provider factory implements exactly one specialized factory SPI derived from
`AIiCategoryProviderFactory<I,C>` and declares `providerId` and supported
`categories` using `@AIaCategoryProviderFactory` (Java) or the
`category_provider_factory` decorator (Python). The common runtime identity is
`(factoryInterface, category, providerId)`. Duplicate identities fail registration.

In Java register implementation classes via `ServiceLoader` as providers of
`eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactory`, either using
`META-INF/services` or JPMS `provides`. The registry accepts ClassLoader or
ModuleLayer discovery scopes, aggregates resources under
`META-INF/algites/category-provider-defaults.properties` and publishes atomic
immutable snapshots. Unregistering a scope only changes future lookups, not
previously created instances or Java classloaders/module layers.

In Python register factory types with distribution entry points under the group
`eu.algites.lib.factory.cpdiscovery.factories`; default selections may be stored
in each installed distribution's `catprov-defaults.properties` metadata file.
Uninstalled development environments may register factories explicitly.

An optional default property uses the key
`<fully-qualified specialized factory interface>.<category>` and the value
`<providerId>`. Conflicting values for the same key are errors. Without a
default, unique-provider selection is automatic; multiple candidates are an error.

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

## Presence checks and ordered provider fallback (17 November 2026)

The registry supports **inspection without implementation creation** in Java and Python.
`findFactory(factoryInterface, category, providerIds)` / `find_factory(factory_type,
category, provider_ids)` accepts an **ordered** list of candidate provider IDs and
returns the first available registered factory. No matching provider returns `null`
(Java) / `None` (Python), including an unknown category or factory interface.
An explicit preference list is authoritative: a registry default is not appended
implicitly. A null/empty preference list uses the configured default, otherwise
a unique registered provider; multiple providers without a default are ambiguous
and raise `AIxCategoryProviderUnavailableException`.

The API also exposes `getAvailableCategories`, `getAvailableProviderIds`,
`getAvailableFactoryMetadata`, `hasCategory` and `hasProvider` (or Python's
snake_case equivalents). The metadata includes category, provider ID,
implementation/context interface types, source and default-selection flag.
A registry can be inspected while registering/unregistering discovery scopes;
each individual read uses an immutable snapshot. Discovery constructs and retains
factory objects as before, but metadata and presence checks do not invoke
`createInstance`.

Java example (using the service-discovered registry):

```java
var provider = registry.findFactory(AIiMyResolverFactory.class,
        "execute:source_generation", List.of("team-provider", "algites-standard"));
if (provider != null) {
    var implementation = provider.createInstance("execute:source_generation", context);
}
var providerIds = registry.getAvailableProviderIds(
        AIiMyResolverFactory.class, "execute:source_generation");
```

A Java `.properties` default key containing category separators must escape `:`
according to standard `java.util.Properties` syntax, for example
`my.package.AIiMyFactory.execute\:source_generation=algites-standard`.
The `algites-standard` fallback is only an example of **caller-supplied**
preferences; CPDiscovery does not hardcode a fallback or silently resolve one.

## Metadata interface and representation (17 November 2026)

Public metadata is returned as `AIiCategoryProviderFactoryMetadata` (Java interface,
Python read-only `Protocol`). The registry implementation constructs immutable
`AIcdCategoryProviderFactoryMetadata` data records in the `impl` artifact.
Consumers of `intf` do not depend on the implementation class. The public
accessors `category()`, `providerId()`, `implementationInterfaceType()`,
`contextInterfaceType()`, `defaultProvider()` and `source()` preserve the
previous Java record accessor spelling; Python retains its attribute names.

The Java `AIsCategoryProviderDiscovery` entry point owns its service-discovered
registry and no longer exposes `setRegistry()`. Applications register/unregister
ClassLoader and ModuleLayer scopes through the returned registry instead.
Python's existing explicit `set_registry()` remains for its current application
bootstrap and test injection workflow pending a separate lifecycle redesign.

Migration: before overlaying the new repository archive, run
`devops/migrations/git-mv-cpdiscovery-metadata-20261117.sh` from this delivery
(in the original Git worktree) to preserve `git mv` history for both Java and
Python concrete metadata classes.

## Optional factory instance caching

Java `AIcCachingCategoryProviderFactory<I,C>` (in the public `intf` artifact) and its
Python equivalent `AIcCachingCategoryProviderFactory` provide an opt-in base for
factories whose implementations are safe to share. They cache successful non-null
instances per **(category, context)** for the lifetime of the factory object and
create them once under concurrent access. Subclasses supply
`createUncachedInstance` / `create_uncached_instance`; the general
`AIiCategoryProviderFactory` contract remains unchanged.

The context must have stable hash/equality semantics; mutable resolution contexts,
request-scoped instances, and factories with per-invocation state should **not**
use this cache. Java supports null contexts. Python supports None and requires
hashable contexts. A protected invalidation method is available when factory
inputs are deliberately changed, but it does not revoke previously handed-out
instances. Failed creations are not cached. Registry registration and provider
selection are unchanged and stay independent of instance caching.

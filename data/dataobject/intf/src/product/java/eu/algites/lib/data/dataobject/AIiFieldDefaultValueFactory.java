package eu.algites.lib.data.dataobject;

import eu.algites.lib.factory.cpdiscovery.AIiCategoryProviderFactory;
import java.util.Optional;

/** CPDiscovery SPI for constructing field defaults (including explicit null). */
public interface AIiFieldDefaultValueFactory
        extends AIiCategoryProviderFactory<Optional<?>, AIiFieldDefaultValueFactoryContext> {
}

package eu.algites.lib.data.smartdataobject.impl.common;

import java.util.Optional;

/** Pluggable format semantics, metadata, and codec creation for SDO objects.
 * Providers are found through Java ServiceLoader and may be discovered from
 * explicitly supplied classloaders or module layers.
 * @author linhart1
 */
public interface AIiSmartDataObjectFormatBindingProvider {
    /** @return external format identifier */ String getFormatId();
    /** @return stable provider identifier */ String getProviderId();
    /** @param aMetadata neutral descriptor for an SDO class
     * @return immutable format-specific information, or empty if not applicable */
    Optional<? extends AIiSmartDataObjectFormatBindingMetadata> resolveMetadata(
            AIiSmartDataObjectMetadata aMetadata);
    /** @return marshaller supplied by this format implementation */
    AIiSmartDataObjectMarshaller createMarshaller();
    /** @return unmarshaller supplied by this format implementation */
    AIiSmartDataObjectUnmarshaller createUnmarshaller();
}

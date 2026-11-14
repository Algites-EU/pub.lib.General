package eu.algites.lib.data.smartdataobject.impl.common;

/** A format provider's immutable, per-SDO metadata contribution.
 * @author linhart1
 */
public interface AIiSmartDataObjectFormatBindingMetadata {
    /** @return external format ID */ String getFormatId();
    /** @return stable format-binding provider ID */ String getProviderId();
}

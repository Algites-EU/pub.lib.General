package eu.algites.lib.data.smartdataobject.impl.common;


import java.util.Collection;
import java.util.Optional;

/** Effective common and extensible format metadata for one concrete SDO type.
 * The resolved class is not bound to a particular implementation registry.
 * @author linhart1
 */
public interface AIiSmartDataObjectMetadata {
    /** @return actual concrete SDO implementation class */
    Class<? extends AIcSmartDataObject> getImplementationClass();
    /** @return one metadata item per effective semantic field */
    Collection<? extends AIiSmartDataObjectFieldMetadata> getFields();
    /** @param aSemanticFieldName semantic getter-derived name
     * @return matching field or null if absent */
    AIiSmartDataObjectFieldMetadata getField(String aSemanticFieldName);
    /** @return discovered format metadata contributions; immutable snapshot */
    Collection<? extends AIiSmartDataObjectFormatBindingMetadata> getFormatBindings();
    /** @param aFormatId stable external format ID
     * @param aProviderId unique provider ID within the discovered provider scope
     * @return matching metadata contribution, if any */
    Optional<? extends AIiSmartDataObjectFormatBindingMetadata> getFormatBinding(
            String aFormatId, String aProviderId);
}

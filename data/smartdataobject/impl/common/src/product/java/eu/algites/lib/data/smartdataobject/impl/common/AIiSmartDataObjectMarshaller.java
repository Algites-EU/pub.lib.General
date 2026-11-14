package eu.algites.lib.data.smartdataobject.impl.common;

import eu.algites.lib.data.smartdataobject.AIiSmartDataObject;

/** Serialize a typed SDO object under a selected binding registry.
 * @author linhart1
 */
public interface AIiSmartDataObjectMarshaller {
    /** @param aObject object to marshal
     * @param aRegistry registry with the application's allowed SDO implementations
     * @return encoded representation
     * @throws IllegalArgumentException if the input cannot be represented */
    String marshal(AIiSmartDataObject aObject, AIiSmartDataObjectBindingRegistry aRegistry);
}

package eu.algites.lib.data.smartdataobject.impl.common;

import eu.algites.lib.data.smartdataobject.AIiSmartDataObject;

/** Reconstruct a typed SDO graph from a supported external format.
 * @author linhart1
 */
public interface AIiSmartDataObjectUnmarshaller {
    /** @param aText external representation
     * @param aReadContract effective requested read-only data contract
     * @param aRegistry implementation bindings available for reconstruction
     * @return materialized SDO
     * @throws IllegalArgumentException on invalid or ambiguous data */
    AIiSmartDataObject unmarshal(String aText,
            Class<? extends eu.algites.lib.data.dataobject.AIiDataObject> aReadContract,
            AIiSmartDataObjectBindingRegistry aRegistry);
}

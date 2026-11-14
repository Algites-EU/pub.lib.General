package eu.algites.lib.data.smartdataobject.impl.common;


import eu.algites.lib.data.dataobject.AIiDataObject;
import java.util.Collection;
import java.util.Set;

/** One implementation and its effective contract/field bindings in a registry.
 * @author linhart1
 */
public interface AIiSmartDataObjectBinding {
    /** @return implementation class registered for this binding */
    Class<? extends AIcSmartDataObject> getImplementationClass();
    /** @return neutral class metadata shared across binding registries */
    AIiSmartDataObjectMetadata getMetadata();
    /** @return read contracts directly annotated on this concrete class */
    Set<Class<? extends AIiDataObject>> getDeclaredReadContracts();
    /** @return closure of annotated contracts, inherited superclass declarations and interfaces */
    Set<Class<? extends AIiDataObject>> getEffectiveReadContracts();
    /** @return binding entries for nested SDO fields, may be empty */
    Collection<? extends AIiSmartDataObjectFieldBinding> getFieldBindings();
    /** @param aSemanticFieldName semantic field name
     * @return nested implementation field binding, or null */
    AIiSmartDataObjectFieldBinding getFieldBinding(String aSemanticFieldName);
}

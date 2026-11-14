package eu.algites.lib.data.smartdataobject.impl.common;


import eu.algites.lib.data.dataobject.AIiDataObject;
import java.util.Collection;

/** Purpose-scoped registry of concrete SDO implementations, not a global catalogue.
 * @author linhart1
 */
public interface AIiSmartDataObjectBindingRegistry {
    /** @return all registered implementation bindings */
    Collection<? extends AIiSmartDataObjectBinding> getBindings();
    /** @param aImplementationClass exact registered class
     * @return associated binding or null */
    AIiSmartDataObjectBinding getBinding(Class<? extends AIcSmartDataObject> aImplementationClass);
    /** @param aReadContract required read-only contract
     * @return all compatible registered bindings (possibly empty) */
    Collection<? extends AIiSmartDataObjectBinding> getBindingsForReadContract(
            Class<? extends AIiDataObject> aReadContract);

    /**
     * Resolves one concrete implementation for an effective read-only contract,
     * optionally in the scope of a parent's field. An explicit field binding
     * wins; otherwise exactly one primary or compatible binding is required.
     *
     * @param aReadContract declared effective data contract
     * @param aOwningBinding binding of the parent, or null for the document root
     * @param aSemanticFieldName parent field identity, or null for the root
     * @return one registered concrete SDO binding
     * @throws IllegalArgumentException on missing, unregistered or ambiguous bindings
     */
    AIiSmartDataObjectBinding resolveBinding(
            Class<? extends AIiDataObject> aReadContract,
            AIiSmartDataObjectBinding aOwningBinding,
            String aSemanticFieldName);
}

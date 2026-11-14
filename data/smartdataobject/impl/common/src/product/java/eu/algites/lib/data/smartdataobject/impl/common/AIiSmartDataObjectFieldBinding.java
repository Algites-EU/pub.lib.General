package eu.algites.lib.data.smartdataobject.impl.common;


import java.util.Optional;

/** Purpose-scoped implementation selection for an SDO-valued field.
 * @author linhart1
 */
public interface AIiSmartDataObjectFieldBinding {
    /** @return schema-neutral effective metadata for this field */
    AIiSmartDataObjectFieldMetadata getFieldMetadata();
    /** @return explicit override, if declared and accessible in this registry */
    Optional<Class<? extends AIcSmartDataObject>> getImplementationClassOverride();
}

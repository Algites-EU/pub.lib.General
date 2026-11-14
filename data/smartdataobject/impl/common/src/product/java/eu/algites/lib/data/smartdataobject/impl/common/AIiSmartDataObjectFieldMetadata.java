package eu.algites.lib.data.smartdataobject.impl.common;

import eu.algites.lib.data.dataobject.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/** Schema-neutral metadata for one effective field of a concrete SDO class.
 * @author linhart1
 */
public interface AIiSmartDataObjectFieldMetadata {
    /** @return normalized semantic identity, e.g. PostalCode */
    String getSemanticFieldName();
    /** @return canonical external data name, e.g. postal_code */
    String getDataFieldName();
    /** @return effective getter after interface and class override resolution */
    Method getEffectiveGetter();
    /** @return generic type of the effective getter, before choosing a concrete implementation */
    Type getEffectiveReturnType();
    /** @return effective common field definition */
    AIaDataObjectField getDefinition();
    /** @return optional effective string constraints or null */
    AIaDataObjectFieldStringConstraints getStringConstraints();
    /** @return optional effective numeric constraints or null */
    AIaDataObjectFieldNumericConstraints getNumericConstraints();
    /** @return optional effective list constraints or null */
    AIaDataObjectFieldListConstraints getListConstraints();
    /** @return optional effective object constraints or null */
    AIaDataObjectFieldObjectConstraints getObjectConstraints();
    /** @return effective key definitions, never null */
    List<AIaKeyDefinition> getKeyDefinitions();
}

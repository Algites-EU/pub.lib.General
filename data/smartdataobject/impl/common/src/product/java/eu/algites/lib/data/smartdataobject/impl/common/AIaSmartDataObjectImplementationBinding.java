package eu.algites.lib.data.smartdataobject.impl.common;

import eu.algites.lib.data.dataobject.AIiDataObject;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Declares the read-only contracts represented by a concrete SDO implementation.
 * Additional contracts on subclasses are accumulated rather than replacing
 * the annotation on the superclass.
 * @author linhart1
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIaSmartDataObjectImplementationBinding {
    /** @return explicit read-only interfaces supported by this implementation */
    Class<? extends AIiDataObject>[] readContracts();
}

package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Normalized data contract identity, independent of its source or generated implementation. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AIaDataObject {
    String id();
    int version() default -1;
    String title() default "";
    String description() default "";
    String xmlLocalName() default "";
    String xmlNamespace() default "";
    String fingerprint() default "";
}

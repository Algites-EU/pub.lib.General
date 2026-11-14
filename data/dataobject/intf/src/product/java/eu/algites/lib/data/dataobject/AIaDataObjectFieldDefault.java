package eu.algites.lib.data.dataobject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Nested default declaration on {@link AIaDataObjectField}.
 * The literal is in schema lexical form and is converted to the getter return type
 * before validation or effective-value resolution. The {@code defined} flag allows
 * a genuine empty-string default to differ from an unspecified default.
 *
 * @author linhart1
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AIaDataObjectFieldDefault {
    /** @return the substitution or constraint policy; declaration-only by default */
    AInDataObjectFieldDefaultKind kind() default AInDataObjectFieldDefaultKind.DECLARATION_ONLY;
    /** @return schema lexical value, including an allowed empty string */
    String value() default "";
    /** @return true if {@link #value()} is explicitly declared, otherwise false */
    boolean defined() default false;
}

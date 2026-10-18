package eu.algites.lib.data.dataobject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Describes an individual property in a read-only structured data-object contract.
 * Schema-specific metadata such as XML QName and XML attribute/element kind belongs
 * to schema-specific annotations, and human-readable text belongs to {@code AIaFieldLabel}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AIaDataObjectField {
    /**
     * Stable unlocalized field identifier independent of its presentation and wire QName.
     * An empty string derives the name from the Java getter.
     *
     * @return schema field identifier or empty for getter-derived naming
     */
    String fieldName() default "";

    /**
     * Presence restriction enforced by data-object implementations. REQUIRED requires an
     * explicit or effective value, ALLOWED permits absence, and FORBIDDEN rejects assignment.
     *
     * @return field presence policy; ALLOWED by default
     */
    AInFieldPresenceKind presence() default AInFieldPresenceKind.ALLOWED;

    /**
     * Whether explicitly assigned {@code null} is permitted, independently of absence.
     * A derived contract may narrow this permission but cannot widen a prohibition.
     *
     * @return true unless nullability is explicitly forbidden
     */
    boolean allowsNull() default true;
}

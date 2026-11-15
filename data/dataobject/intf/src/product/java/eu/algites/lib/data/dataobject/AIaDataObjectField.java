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
     * Canonical external data-property name (for example a JSON/YAML property), not
     * the semantic identity of this field. The normalized getter identifier is the
     * internal SmartDataObject key. An empty string inherits the name of an
     * overridden getter, otherwise the canonical internal identifier is used.
     * Two different semantic fields must not share the same effective data name.
     *
     * @return schema field identifier or empty for getter-derived naming
     */
    @Deprecated(since = "1.1")
    String fieldName() default "";

    /**
     * Canonical data-field name shared by technologies; independent of XML QName
     * and of the semantic getter identity.
     *
     * @return external data field name, or empty to inherit or derive it
     */
    String dataFieldName() default "";

    /**
     * A structurally embedded member has a logical getter but no serialized
     * field/element wrapper. Its children are serialized in the parent context.
     * It must not declare a {@link #dataFieldName()} and is independent of the
     * referenced type's declared/synthetic origin.
     *
     * @return whether the member has no wire-level wrapper
     */
    boolean embedded() default false;

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

    /**
     * Schema declaration order shared by XML, JSON and YAML. An unspecified
     * order is -1; legacy XML-specific order is consulted during migration.
     *
     * @return zero-based declaration order, or -1 if not provided
     */
    int order() default -1;

    /**
     * Minimum item count for a collection-valued field. XML Schema
     * Source-schema collection lower bounds map to this schema-neutral
     * constraint. A value of {@code -1} means unspecified.
     * Field presence is controlled separately by {@link #presence()}.
     *
     * @return minimum collection item count, or -1 when unspecified
     */
    int minItemCount() default -1;

    /**
     * Maximum item count for a collection-valued field. XML Schema
     * Source-schema collection upper bounds map to this schema-neutral
     * constraint. A value of {@code -1} means unbounded
     * or unspecified; it is not a prohibition on collection items.
     *
     * @return maximum collection item count, or -1 when unspecified
     */
    int maxItemCount() default -1;

    /**
     * Whether the explicit EMPTY_VALUE sentinel may be assigned to this field.
     * It differs from assigning an ordinary empty String through setXxx("").
     *
     * @return true if the technical empty-state accessors are permitted
     */
    boolean isEmptyAllowed() default false;

    /**
     * Declares the inherited setter parameter-type strategy for this field.
     *
     * <p>{@code DEFAULT} delegates to the containing data object or configured generator default. A resolved
     * generated contract records an effective strategy independent of the
     * generating build's environment. A fixed strategy does not prevent Java
     * method inheritance; it prevents unsafe type narrowing in descendants.
     *
     * @return the setter parameter inheritance strategy, or {@code DEFAULT}
     *         when this declaration leaves the selection to its context
     */
    AInFieldSetterInheritancePolicy setterInheritancePolicy()
            default AInFieldSetterInheritancePolicy.DEFAULT;

}

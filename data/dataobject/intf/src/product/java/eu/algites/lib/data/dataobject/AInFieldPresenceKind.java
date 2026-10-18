package eu.algites.lib.data.dataobject;

/** Permitted states for an object field, independently of nullability and cardinality. */
public enum AInFieldPresenceKind {
    /** The field must occur in the effective object. */
    REQUIRED,
    /** The field may be present or absent. */
    ALLOWED,
    /** The field must not occur; useful for valid schema restrictions. */
    FORBIDDEN
}

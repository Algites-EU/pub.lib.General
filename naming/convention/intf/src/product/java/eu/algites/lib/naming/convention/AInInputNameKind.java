package eu.algites.lib.naming.convention;

/** Semantic categories of names accepted from source definitions. */
public enum AInInputNameKind {
    /** Represents the definition value. */
    DEFINITION,
    /** Represents the property value. */
    PROPERTY,
    /** Represents the enum value value. */
    ENUM_VALUE,
    /** Represents the symbolic map key value. */
    SYMBOLIC_MAP_KEY,
    /** Represents the package segment value. */
    PACKAGE_SEGMENT,
    /** Represents the file stem value. */
    FILE_STEM
}

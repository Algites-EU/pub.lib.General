package eu.algites.lib.naming.convention;

/** Ordered strategy for obtaining a canonical definition version. */
public enum AInVersionSource {
    /** Represents the explicit metadata value. */
    EXPLICIT_METADATA,
    /** Represents the file name suffix value. */
    FILE_NAME_SUFFIX,
    /** Represents the explicit then file name value. */
    EXPLICIT_THEN_FILE_NAME
}

package eu.algites.lib.text.format;

/** Decides when graph identity is rendered as a reference rather than a value. */
public enum AInTextOutputObjectReferenceMode {
    NO_REFERENCES, ALL_OBJECTS, CYCLIC_REFERENCES_ONLY, REPEATED_OBJECTS, OPTIMIZED_TEXT_OUTPUT_SIZE
}

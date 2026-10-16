package eu.algites.lib.text.format;

/**
 * Controls identity preservation when a SmartDataObject graph is written as text.
 *
 * <p>This controls object-instance references, not SchemaId, SchemaUrl, or a
 * JSON Schema {@code $id}. Each format profile accepts a subset of these modes.
 * In-memory virtual mappings preserve identity independently of this setting.
 */
public enum AInTextOutputObjectReferenceMode {
    /** Inline every occurrence; shared identity is lost and cycles cannot be represented. */
    NO_REFERENCES,
    /** Assign a reference identity to every eligible graph object, including unique objects. */
    ALL_OBJECTS,
    /** Encode references only where required to terminate cycles, not every shared value. */
    CYCLIC_REFERENCES_ONLY,
    /** Preserve repeated-object identity as well as cycles through references. */
    REPEATED_OBJECTS,
    /** Choose references to minimize text size while still representing all cycles. */
    OPTIMIZED_TEXT_OUTPUT_SIZE
}

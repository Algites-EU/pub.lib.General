package eu.algites.lib.common.documentation.model;

/**
 * Provides write access to a code block.
 * <p>
 * The read-only contract is defined by {@link AIiDocumentationCodeBlock}. This capability
 * interface is intended for construction, aggregation, and model-processing
 * phases. Validators, renderers, and other read-only consumers should depend on
 * the read-only interface instead.
 * <p>
 * This interface currently adds no type-specific write operations. It mirrors
 * the documentation element hierarchy and inherits the write capabilities of
 * its parent write-access interface.
 */
public interface AIiDocumentationCodeBlockWriteAccess
        extends AIiDocumentationCodeBlock,
        AIiDocumentationBlockElementWriteAccess {
}

package eu.algites.lib.text.format;

/** Immutable serialization protocol descriptor, independent of model implementation. */
public interface AIiTextFormatProfile {
    /**
     * Returns the id.
     * @return the id
     */
    String getId();
    /**
     * Returns the format type.
     * @return the format type
     */
    AInTextFormatType getFormatType();
    /**
     * Returns the reference encoding.
     * @return the reference encoding
     */
    AInTextReferenceEncoding getReferenceEncoding();
    /**
     * Checks whether diagnostic is enabled or present.
     * @return the diagnostic
     */
    boolean isDiagnostic();
    /**
     * Checks whether reference mode supported is enabled or present.
     * @param aMode mode supplied to the operation
     * @return the reference mode supported
     */
    boolean isReferenceModeSupported(AInTextOutputObjectReferenceMode aMode);
}

package eu.algites.lib.text.format;

/** Immutable serialization protocol descriptor, independent of model implementation. */
public interface AIiTextFormatProfile {
    String getId();
    AInTextFormatType getFormatType();
    AInTextReferenceEncoding getReferenceEncoding();
    boolean isDiagnostic();
    boolean isReferenceModeSupported(AInTextOutputObjectReferenceMode aMode);
}

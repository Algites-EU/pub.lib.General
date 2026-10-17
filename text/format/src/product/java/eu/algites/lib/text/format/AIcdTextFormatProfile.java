package eu.algites.lib.text.format;

import java.util.Objects;
import java.util.Set;

/** Concrete immutable profile descriptor; does not perform serialization. */
public record AIcdTextFormatProfile(
        String id,
        AInTextFormatType formatType,
        AInTextReferenceEncoding referenceEncoding,
        boolean diagnostic,
        Set<AInTextOutputObjectReferenceMode> supportedModes) implements AIiTextFormatProfile {
    public AIcdTextFormatProfile {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(formatType, "formatType");
        Objects.requireNonNull(referenceEncoding, "referenceEncoding");
        supportedModes = Set.copyOf(Objects.requireNonNull(supportedModes, "supportedModes"));
        if (supportedModes.isEmpty()) throw new IllegalArgumentException("At least one mode is required");
    }
    @Override public String getId() { return id; }
    @Override public AInTextFormatType getFormatType() { return formatType; }
    @Override public AInTextReferenceEncoding getReferenceEncoding() { return referenceEncoding; }
    @Override public boolean isDiagnostic() { return diagnostic; }
    @Override public boolean isReferenceModeSupported(AInTextOutputObjectReferenceMode aMode) {
        return supportedModes.contains(aMode);
    }
}

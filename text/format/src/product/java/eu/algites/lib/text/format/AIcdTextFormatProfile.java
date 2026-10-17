package eu.algites.lib.text.format;

import java.util.Objects;
import java.util.Set;

/**
 * Concrete immutable profile descriptor; does not perform serialization.
 * @param id stable identifier of the profile
 * @param formatType serialization syntax used by the profile
 * @param referenceEncoding encoding strategy used for shared references
 * @param diagnostic whether this profile is intended for diagnostic output
 * @param supportedModes reference modes supported by the profile
 */
public record AIcdTextFormatProfile(
        String id,
        AInTextFormatType formatType,
        AInTextReferenceEncoding referenceEncoding,
        boolean diagnostic,
        Set<AInTextOutputObjectReferenceMode> supportedModes) implements AIiTextFormatProfile {
    /**
     * Creates an immutable text formatting profile and its supported reference modes.
     * @param id id supplied to the operation
     * @param formatType format type supplied to the operation
     * @param referenceEncoding reference encoding supplied to the operation
     * @param diagnostic diagnostic supplied to the operation
     * @param supportedModes supported modes supplied to the operation
     */
    public AIcdTextFormatProfile {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(formatType, "formatType");
        Objects.requireNonNull(referenceEncoding, "referenceEncoding");
        supportedModes = Set.copyOf(Objects.requireNonNull(supportedModes, "supportedModes"));
        if (supportedModes.isEmpty()) throw new IllegalArgumentException("At least one mode is required");
    }
    /** {@inheritDoc} */
    @Override public String getId() { return id; }
    /** {@inheritDoc} */
    @Override public AInTextFormatType getFormatType() { return formatType; }
    /** {@inheritDoc} */
    @Override public AInTextReferenceEncoding getReferenceEncoding() { return referenceEncoding; }
    /** {@inheritDoc} */
    @Override public boolean isDiagnostic() { return diagnostic; }
    /** {@inheritDoc} */
    @Override public boolean isReferenceModeSupported(AInTextOutputObjectReferenceMode aMode) {
        return supportedModes.contains(aMode);
    }
}

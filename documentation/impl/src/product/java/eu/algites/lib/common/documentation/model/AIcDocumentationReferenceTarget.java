package eu.algites.lib.common.documentation.model;

import java.util.Objects;

/**
 * Default implementation of {@link AIiDocumentationReferenceTargetWriteAccess}.
 * <p>
 * See {@link AIiDocumentationReferenceTarget} for the read-only reference target contract.
 */
public class AIcDocumentationReferenceTarget
        extends AIcDocumentationElement
        implements AIiDocumentationReferenceTargetWriteAccess {

    private final String sourceSystemId;

    private final String targetKind;

    private final String targetId;

    private final String qualifiedName;

    private final String displayName;

    /**
     * Creates a renderer-neutral semantic reference target.
     *
     * @param aElementId stable element identifier
     * @param aSourceSystemId source-system identifier, or an empty string if unspecified
     * @param aTargetKind stable renderer-neutral target kind
     * @param aTargetId stable source-specific target identifier
     * @param aQualifiedName qualified target name, or an empty string if unavailable
     * @param aDisplayName human-readable target name, or an empty string if unavailable
     */
    public AIcDocumentationReferenceTarget(
            String aElementId,
            String aSourceSystemId,
            String aTargetKind,
            String aTargetId,
            String aQualifiedName,
            String aDisplayName) {
        super(aElementId);
        sourceSystemId = Objects.requireNonNull(aSourceSystemId, "Parameter aSourceSystemId must not be null.");
        targetKind = Objects.requireNonNull(aTargetKind, "Parameter aTargetKind must not be null.");
        targetId = Objects.requireNonNull(aTargetId, "Parameter aTargetId must not be null.");
        qualifiedName = Objects.requireNonNull(aQualifiedName, "Parameter aQualifiedName must not be null.");
        displayName = Objects.requireNonNull(aDisplayName, "Parameter aDisplayName must not be null.");
    }

    @Override
    public String getSourceSystemId() {
        return sourceSystemId;
    }

    @Override
    public String getTargetKind() {
        return targetKind;
    }

    @Override
    public String getTargetId() {
        return targetId;
    }

    @Override
    public String getQualifiedName() {
        return qualifiedName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}

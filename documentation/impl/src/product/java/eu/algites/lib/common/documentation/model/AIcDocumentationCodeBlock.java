package eu.algites.lib.common.documentation.model;

import java.util.Objects;

/**
 * Default implementation of {@link AIiDocumentationCodeBlockWriteAccess}.
 * <p>
 * See {@link AIiDocumentationCodeBlock} for the read-only code block contract.
 */
public class AIcDocumentationCodeBlock
        extends AIcDocumentationBlockElement
        implements AIiDocumentationCodeBlockWriteAccess {

    private final String languageId;

    private final String code;

    /**
     * Creates a code block.
     *
     * @param aElementId stable element identifier
     * @param aLanguageId language identifier, or an empty string if unspecified
     * @param aCode code block content
     */
    public AIcDocumentationCodeBlock(
            String aElementId,
            String aLanguageId,
            String aCode) {
        super(aElementId);
        languageId = Objects.requireNonNull(aLanguageId, "Parameter aLanguageId must not be null.");
        code = Objects.requireNonNull(aCode, "Parameter aCode must not be null.");
    }

    @Override
    public String getLanguageId() {
        return languageId;
    }

    @Override
    public String getCode() {
        return code;
    }
}

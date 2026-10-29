package eu.algites.lib.common.validation;

import java.util.Locale;

/** Structured validation diagnostic with optional related, locally resolved text topics. */
public interface AIiValidationIssue {
    /** Stable diagnostic code, independent of locale. */
    String getCode();

    /** Technical path, independent of locale; may be empty for a standalone value. */
    String getPath();

    /**
     * Diagnostic message. Null locale requests stable low-level technical diagnostic text.
     * A non-null unsupported locale falls back to English, never to the host locale.
     */
    String getMessage(Locale aLocale);

    /**
     * Related text for a semantic topic. Null locale requests a technical source value;
     * a non-null unsupported locale uses English. Unknown topics return null.
     */
    String getRelatedTopicText(String aTopicId, Locale aLocale);
}

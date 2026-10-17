package eu.algites.lib.common.validation;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/** Value that may be temporarily invalid while being assembled. */
public interface AIiValidatable<T extends AIiValidationIssue> {
    /** Whether the current state satisfies all validity constraints. */
    boolean isValid();

    /** Detailed technical diagnostic issues, independent of a requested locale. */
    List<? extends T> getValidationIssues();

    /**
     * Aggregate of current diagnostics. Null locale requests technical information,
     * while any unsupported non-null locale uses English as localization fallback.
     * A valid object returns an empty string.
     */
    default String getInvalidStateMessage(Locale aLocale) {
        return getValidationIssues().stream().map(aIssue -> {
            String locMessage = aIssue.getMessage(aLocale);
            String locPath = aIssue.getPath();
            return locPath == null || locPath.isBlank() ? locMessage : locPath + ": " + locMessage;
        }).collect(Collectors.joining(System.lineSeparator()));
    }
}

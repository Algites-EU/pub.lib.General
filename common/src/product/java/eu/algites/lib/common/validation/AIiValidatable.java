package eu.algites.lib.common.validation;

import java.util.Locale;

/** Describes a value whose current state may be incomplete or invalid. */
public interface AIiValidatable {
    /** Returns whether the current state satisfies its constraints. */
    boolean isValid();

    /** Returns a localized diagnostic or an empty string for a valid state. */
    String getInvalidStateMessage(Locale aLocale);
}

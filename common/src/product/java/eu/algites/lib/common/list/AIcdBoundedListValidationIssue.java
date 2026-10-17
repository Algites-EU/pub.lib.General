package eu.algites.lib.common.list;

import eu.algites.lib.common.validation.AIiValidationIssue;
import java.util.Locale;

/**
 * One violated cardinality constraint on an independently validatable list.
 * @param code stable validation code
 * @param actualSize actual collection size when validation failed
 * @param bound minimum or maximum allowed item count
 */
public record AIcdBoundedListValidationIssue(String code, int actualSize, int bound) implements AIiValidationIssue {
    /** {@inheritDoc} */
    @Override public String getCode() { return code; }
    /** {@inheritDoc} */
    @Override public String getPath() { return ""; }
    /** {@inheritDoc} */
    @Override public String getRelatedTopicText(String aTopicId, Locale aLocale) { return null; }
    /** {@inheritDoc} */
    @Override public String getMessage(Locale aLocale) {
        if (aLocale == null) return code + "(actualSize=" + actualSize + ", bound=" + bound + ")";
        boolean locCzech = "cs".equals(aLocale.getLanguage());
        return switch (code) {
            case "MIN_ITEMS" -> locCzech
                    ? "Seznam musí obsahovat alespoň " + bound + " položek; aktuálně obsahuje " + actualSize + "."
                    : "List must contain at least " + bound + " items; current size is " + actualSize + ".";
            case "MAX_ITEMS" -> locCzech
                    ? "Seznam smí obsahovat nejvýše " + bound + " položek; aktuálně obsahuje " + actualSize + "."
                    : "List must contain at most " + bound + " items; current size is " + actualSize + ".";
            default -> throw new IllegalStateException("Unknown cardinality issue code: " + code);
        };
    }
}

package eu.algites.lib.data.smartdataobject;

import eu.algites.lib.common.validation.AIiValidationIssue;
import java.util.Locale;

/** Validation issue whose optional related topics describe an SDO field. */
public interface AIiSdoValidationIssue extends AIiValidationIssue {
    /** Stable field schema/property name, never localized. */
    String FIELD_SYSTEM_NAME = "field.systemName";
    /** Human-readable field title, potentially localized. */
    String FIELD_LOCALIZED_NAME = "field.name";
    /** Explanatory field description, potentially localized. */
    String FIELD_LOCALIZED_DESCRIPTION = "field.description";

    /**
     * Returns the field name.
     * @return the field name
     */
    default String getFieldName() { return getRelatedTopicText(FIELD_SYSTEM_NAME, null); }
    /**
     * Returns the field title.
     * @param aLocale locale for presenting the message; null selects technical diagnostics
     * @return the field title
     */
    default String getFieldTitle(Locale aLocale) { return getRelatedTopicText(FIELD_LOCALIZED_NAME, aLocale); }
    /**
     * Returns the field description.
     * @param aLocale locale for presenting the message; null selects technical diagnostics
     * @return the field description
     */
    default String getFieldDescription(Locale aLocale) { return getRelatedTopicText(FIELD_LOCALIZED_DESCRIPTION, aLocale); }
}

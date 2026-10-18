package eu.algites.lib.data.smartdataobject;

import eu.algites.lib.common.validation.AIiValidationIssue;
import eu.algites.lib.common.object.props.labels.AIaFieldLabel;
import java.util.Locale;

/** Validation issue whose optional related topics describe an SDO field. */
public interface AIiSdoValidationIssue extends AIiValidationIssue {
    /**
     * Returns the field name.
     * @return the field name
     */
    default String getFieldName() { return getRelatedTopicText(AIaFieldLabel.FIELD_NAME, null); }
    /**
     * Returns the field title.
     * @param aLocale locale for presenting the message; null selects technical diagnostics
     * @return the field title
     */
    default String getFieldTitle(Locale aLocale) { return getRelatedTopicText(AIaFieldLabel.FIELD_TITLE, aLocale); }
    /**
     * Returns the field description.
     * @param aLocale locale for presenting the message; null selects technical diagnostics
     * @return the field description
     */
    default String getFieldDescription(Locale aLocale) { return getRelatedTopicText(AIaFieldLabel.FIELD_DESCRIPTION, aLocale); }
}

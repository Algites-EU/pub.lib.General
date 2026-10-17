package eu.algites.lib.data.smartdataobject;

import eu.algites.lib.data.dataobject.AIiDataObject;
import eu.algites.lib.common.validation.AIiValidatable;
import eu.algites.lib.text.format.AIiTextFormatProfile;
import eu.algites.lib.text.format.AInTextOutputObjectReferenceMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Public mutable data-object state, presence, validation and textual output contract. */
public interface AIiSmartDataObject extends AIiDataObject, AIiValidatable<AIiSdoValidationIssue> {
    /**
     * Role identifying the stable, unlocalized system name of an SDO field.
     */
    String FIELD_SYSTEM_NAME = AIiSdoValidationIssue.FIELD_SYSTEM_NAME;
    /**
     * Role identifying the localized display name of an SDO field.
     */
    String FIELD_LOCALIZED_NAME = AIiSdoValidationIssue.FIELD_LOCALIZED_NAME;
    /**
     * Role identifying the localized explanatory description of an SDO field.
     */
    String FIELD_LOCALIZED_DESCRIPTION = AIiSdoValidationIssue.FIELD_LOCALIZED_DESCRIPTION;

    /** {@inheritDoc} */
    @Override
    default List<? extends AIiSdoValidationIssue> getValidationIssues() {
        return get_DeeplyEffectiveValuesValidationIssues();
    }

    /** The generic validity view uses complete effective values, including nested values. */
    @Override
    default boolean isValid() { return is_DeeplyByEffectiveValuesValid(); }

    /** Localized aggregate, using field labels resolved by the common label framework. */
    @Override
    default String getInvalidStateMessage(Locale aLocale) {
        return getValidationIssues().stream().map(aIssue -> {
            String locTitle = aLocale == null ? null : aIssue.getFieldTitle(aLocale);
            String locLocation = locTitle == null || locTitle.isBlank() ? aIssue.getPath() : locTitle;
            return (locLocation == null || locLocation.isBlank() ? "" : locLocation + ": ")
                    + aIssue.getMessage(aLocale);
        }).collect(java.util.stream.Collectors.joining(System.lineSeparator()));
    }

    /**
     * Checks whether  by raw values valid is enabled or present.
     * @return the  by raw values valid
     */
    boolean is_ByRawValuesValid();
    /**
     * Checks whether  by effective values valid is enabled or present.
     * @return the  by effective values valid
     */
    boolean is_ByEffectiveValuesValid();
    /**
     * Checks whether  deeply by raw values valid is enabled or present.
     * @return the  deeply by raw values valid
     */
    boolean is_DeeplyByRawValuesValid();
    /**
     * Checks whether  deeply by effective values valid is enabled or present.
     * @return the  deeply by effective values valid
     */
    boolean is_DeeplyByEffectiveValuesValid();
    /**
     * Returns the  raw not present required fields.
     * @return the  raw not present required fields
     */
    Set<String> get_RawNotPresentRequiredFields();
    /**
     * Returns the  effective not present required fields.
     * @return the  effective not present required fields
     */
    Set<String> get_EffectiveNotPresentRequiredFields();
    /**
     * Returns the  deeply raw not present required fields.
     * @return the  deeply raw not present required fields
     */
    Set<String> get_DeeplyRawNotPresentRequiredFields();
    /**
     * Returns the  deeply effective not present required fields.
     * @return the  deeply effective not present required fields
     */
    Set<String> get_DeeplyEffectiveNotPresentRequiredFields();
    /**
     * Returns the  raw values validation issues.
     * @return the  raw values validation issues
     */
    List<AIcdSmartDataValidationIssue> get_RawValuesValidationIssues();
    /**
     * Returns the  effective values validation issues.
     * @return the  effective values validation issues
     */
    List<AIcdSmartDataValidationIssue> get_EffectiveValuesValidationIssues();
    /**
     * Returns the  deeply raw values validation issues.
     * @return the  deeply raw values validation issues
     */
    List<AIcdSmartDataValidationIssue> get_DeeplyRawValuesValidationIssues();
    /**
     * Returns the  deeply effective values validation issues.
     * @return the  deeply effective values validation issues
     */
    List<AIcdSmartDataValidationIssue> get_DeeplyEffectiveValuesValidationIssues();
    /** Capture the present raw field values as an identity-preserving typed mapping. */
    Map<String, AIidVirtualFieldValue> toMapping_RawValues();
    /** Capture effective values, including defaults, as an identity-preserving typed mapping. */
    Map<String, AIidVirtualFieldValue> toMapping_EffectiveValues();
    /**
     * Renders directly stored field values without applying defaults.
     * @param aProfile profile supplied to the operation
     * @param aMode mode supplied to the operation
     * @return the result of the operation
     */
    String toString_RawValues(AIiTextFormatProfile aProfile, AInTextOutputObjectReferenceMode aMode);
    /**
     * Renders the effective field values, including applicable defaults, for diagnostics.
     * @param aProfile profile supplied to the operation
     * @param aMode mode supplied to the operation
     * @return the result of the operation
     */
    String toString_EffectiveValues(AIiTextFormatProfile aProfile, AInTextOutputObjectReferenceMode aMode);
}

package eu.algites.lib.data.smartdataobject;

import eu.algites.lib.data.dataobject.AIiDataObject;
import java.util.Objects;
import eu.algites.lib.common.object.props.labels.AIaFieldLabel;
import eu.algites.lib.common.validation.AIiValidatable;
import eu.algites.lib.text.format.AIiTextFormatProfile;
import eu.algites.lib.text.format.AInTextOutputObjectReferenceMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Public mutable data-object state, presence, validation and textual output contract. */
public interface AIiSmartDataObject extends AIiDataObject, AIiValidatable<AIiSdoValidationIssue> {

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
     * Validates locally stored fields using the explicitly chosen view of raw or effective values.
     * Does not change field presence or materialize defaults in the underlying object.
     * @param aMode mandatory selection of the field value loading semantics
     * @return local structural and field validity issues
     * @throws NullPointerException if aMode is null
     */
    List<AIcdSmartDataValidationIssue> _getValidationIssues(AInSdoFieldValueLoadingMode aMode);
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
    /**
     * Snapshot typed field values according to an explicit loading mode.
     * @param aMode mandatory raw/effective value selection
     * @return identity-preserving typed virtual mapping
     * @throws NullPointerException if aMode is null
     */
    Map<String, AIidVirtualFieldValue> _toMapping(AInSdoFieldValueLoadingMode aMode);
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

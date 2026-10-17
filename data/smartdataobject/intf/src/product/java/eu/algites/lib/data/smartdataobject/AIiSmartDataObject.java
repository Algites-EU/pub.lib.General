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
public interface AIiSmartDataObject extends AIiDataObject, AIiValidatable {
    /** The generic validity view uses complete effective values, including nested values. */
    @Override
    default boolean isValid() { return is_DeeplyByEffectiveValuesValid(); }

    /** Returns all current validation failures in a localized, readable form. */
    @Override
    default String getInvalidStateMessage(Locale aLocale) {
        if (isValid()) return "";
        boolean locCzech = aLocale != null && "cs".equals(aLocale.getLanguage());
        return get_DeeplyEffectiveValuesValidationIssues().stream().map(aIssue -> {
            String locMessage = switch (aIssue.code()) {
                case "REQUIRED_ABSENT" -> locCzech ? "Povinná vlastnost není nastavena." : "Required property is not present.";
                case "NULL_FORBIDDEN" -> locCzech ? "Hodnota null není povolena." : "Null value is not permitted.";
                case "WRONG_TYPE" -> locCzech ? "Nesprávný datový typ." : "Property has an incompatible type.";
                case "COLLECTION_INVALID" -> locCzech ? "Neplatná kardinalita kolekce." : aIssue.message();
                default -> aIssue.message();
            };
            return (aIssue.path().isEmpty() ? "" : aIssue.path() + ": ") + locMessage;
        }).collect(java.util.stream.Collectors.joining(System.lineSeparator()));
    }

    boolean is_ByRawValuesValid();
    boolean is_ByEffectiveValuesValid();
    boolean is_DeeplyByRawValuesValid();
    boolean is_DeeplyByEffectiveValuesValid();
    Set<String> get_RawNotPresentRequiredFields();
    Set<String> get_EffectiveNotPresentRequiredFields();
    Set<String> get_DeeplyRawNotPresentRequiredFields();
    Set<String> get_DeeplyEffectiveNotPresentRequiredFields();
    List<AIcdSmartDataValidationIssue> get_RawValuesValidationIssues();
    List<AIcdSmartDataValidationIssue> get_EffectiveValuesValidationIssues();
    List<AIcdSmartDataValidationIssue> get_DeeplyRawValuesValidationIssues();
    List<AIcdSmartDataValidationIssue> get_DeeplyEffectiveValuesValidationIssues();
    /** Capture the present raw field values as an identity-preserving typed mapping. */
    Map<String, AIidVirtualFieldValue> toMapping_RawValues();
    /** Capture effective values, including defaults, as an identity-preserving typed mapping. */
    Map<String, AIidVirtualFieldValue> toMapping_EffectiveValues();
    String toString_RawValues(AIiTextFormatProfile aProfile, AInTextOutputObjectReferenceMode aMode);
    String toString_EffectiveValues(AIiTextFormatProfile aProfile, AInTextOutputObjectReferenceMode aMode);
}

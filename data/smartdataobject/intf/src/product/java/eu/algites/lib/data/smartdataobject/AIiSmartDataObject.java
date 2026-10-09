package eu.algites.lib.data.smartdataobject;

import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIiDataObject;

import eu.algites.lib.data.dataobject.AIiDataObject;
import eu.algites.lib.text.format.AIiTextFormatProfile;
import eu.algites.lib.text.format.AInTextOutputObjectReferenceMode;
import java.util.List;
import java.util.Set;

/** Public mutable data-object state, presence, validation and textual output contract. */
public interface AIiSmartDataObject extends AIiDataObject {
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
    String toString_RawValues(AIiTextFormatProfile aProfile, AInTextOutputObjectReferenceMode aMode);
    String toString_EffectiveValues(AIiTextFormatProfile aProfile, AInTextOutputObjectReferenceMode aMode);
}

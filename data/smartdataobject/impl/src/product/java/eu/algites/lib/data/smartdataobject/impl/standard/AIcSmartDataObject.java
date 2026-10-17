package eu.algites.lib.data.smartdataobject.impl.standard;


import eu.algites.lib.data.smartdataobject.AIcdSmartDataValidationIssue;
import eu.algites.lib.data.smartdataobject.AIiSmartDataObject;
import eu.algites.lib.data.smartdataobject.AIidVirtualFieldValue;
import eu.algites.lib.data.smartdataobject.AIxSmartDataValidationException;
import eu.algites.lib.text.format.AIiTextFormatProfile;
import eu.algites.lib.text.format.AInTextOutputObjectReferenceMode;
import eu.algites.lib.text.format.AIsTextFormatProfiles;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Mutable generated DTO foundation; snapshot and graph implementation remain package-private. */
public abstract class AIcSmartDataObject implements AIiSmartDataObject {
    private final Map<String, Object> values = new LinkedHashMap<>();
    private final Set<String> present = new LinkedHashSet<>();

    /** Create an initially empty, mutable data object. */
    protected AIcSmartDataObject() { }

    /**
     * Populate this object from a typed virtual field graph. The factory registers
     * the already allocated {@code this} before following any child reference.
     */
    protected AIcSmartDataObject(Map<String, AIidVirtualFieldValue> aMapping) {
        AIsSmartDataObjectFactory.populate(this, aMapping);
    }


    /**
 * Immutable pair of global diagnostic defaults, changed atomically at application startup.
 * @param profile selected text rendering profile
 * @param referenceMode selected reference rendering mode
 */
    public record AIcdDiagnosticSettings(AIiTextFormatProfile profile,
            AInTextOutputObjectReferenceMode referenceMode) {
        /**
         * Creates a diagnostic settings instance with the supplied configuration.
         * @param profile profile supplied to the operation
         * @param referenceMode reference mode supplied to the operation
         */
        public AIcdDiagnosticSettings {
            if (profile == null || !profile.isDiagnostic() || referenceMode == null ||
                !profile.isReferenceModeSupported(referenceMode))
                throw new IllegalArgumentException("Unsupported diagnostic profile or reference mode");
        }
    }
    private static volatile AIcdDiagnosticSettings defaultDiagnosticSettings = new AIcdDiagnosticSettings(
            AIsTextFormatProfiles.XML_DIAGNOSTIC,
            AInTextOutputObjectReferenceMode.REPEATED_OBJECTS);

    /**
     * Updates the  default diagnostic settings.
     * @param aSettings settings supplied to the operation
     */
    public static void set_DefaultDiagnosticSettings(AIcdDiagnosticSettings aSettings) {
        if (aSettings == null) throw new IllegalArgumentException("Settings cannot be null");
        defaultDiagnosticSettings = aSettings;
    }
    /**
     * Returns the  default diagnostic settings.
     * @return the  default diagnostic settings
     */
    public static AIcdDiagnosticSettings get_DefaultDiagnosticSettings() { return defaultDiagnosticSettings; }

    /** Generated setters invoke this method with their schema property name. */
    protected final void set_RawField(String aName, Object aValue) {
        requireField(aName);
        values.put(aName, aValue);
        present.add(aName);
    }
    /** Generated raw getters invoke this method and cast to the declared object type. */
    protected final Object get_RawField(String aName) {
        requireField(aName);
        return values.get(aName);
    }
    /** Generated effective getters invoke this method and cast to the declared object type. */
    protected final Object get_EffectiveField(String aName) {
        var locField = requireField(aName);
        if (present.contains(aName)) return values.get(aName);
        if (locField.hasDefault()) return locField.createDefault();
        if (locField.annotation().presenceRequired() && !locField.allowNonPresentRead())
            throw new IllegalStateException("Required field is absent: " + aName);
        return null;
    }
    /**
     * Checks whether present field is enabled or present.
     * @param aName name supplied to the operation
     * @return the present field
     */
    protected final boolean isPresent_Field(String aName) {
        requireField(aName);
        return present.contains(aName);
    }
    /**
     * Removes an explicitly assigned field value, allowing its default to apply again.
     * @param aName name supplied to the operation
     */
    protected final void unset_Field(String aName) {
        requireField(aName);
        values.remove(aName);
        present.remove(aName);
    }
    private AIcSmartDataMetadata.AIcdField requireField(String aName) {
        var locField = metadata().names().get(aName);
        if (locField == null) throw new IllegalArgumentException("Unknown SmartData field: " + aName);
        return locField;
    }
    final AIcSmartDataMetadata.AIcdDescriptor metadata() {
        return AIcSmartDataMetadata.forType(getClass());
    }
    final boolean hasRaw(String aName) { return present.contains(aName); }
    final Object raw(String aName) { return values.get(aName); }

    /** {@inheritDoc} */
    @Override public final Set<String> get_RawNotPresentRequiredFields() { return missing(false); }
    /** {@inheritDoc} */
    @Override public final Set<String> get_EffectiveNotPresentRequiredFields() { return missing(true); }
    private Set<String> missing(boolean aEffective) {
        Set<String> locMissing = new LinkedHashSet<>();
        for (var locField : metadata().fields()) {
            if (locField.annotation().presenceRequired() && !hasRaw(locField.name()) &&
                    !(aEffective && locField.hasDefault())) locMissing.add(locField.name());
        }
        return Collections.unmodifiableSet(locMissing);
    }
    /** {@inheritDoc} */
    @Override public final List<AIcdSmartDataValidationIssue> get_RawValuesValidationIssues() {
        return AIcSmartDataGraphSnapshot.capture(this).localIssues(false);
    }
    /** {@inheritDoc} */
    @Override public final List<AIcdSmartDataValidationIssue> get_EffectiveValuesValidationIssues() {
        return AIcSmartDataGraphSnapshot.capture(this).localIssues(true);
    }
    /** {@inheritDoc} */
    @Override public final List<AIcdSmartDataValidationIssue> get_DeeplyRawValuesValidationIssues() {
        return AIcSmartDataGraphSnapshot.capture(this).deepIssues(false);
    }
    /** {@inheritDoc} */
    @Override public final List<AIcdSmartDataValidationIssue> get_DeeplyEffectiveValuesValidationIssues() {
        return AIcSmartDataGraphSnapshot.capture(this).deepIssues(true);
    }
    /** {@inheritDoc} */
    @Override public final boolean is_ByRawValuesValid() { return get_RawValuesValidationIssues().isEmpty(); }
    /** {@inheritDoc} */
    @Override public final boolean is_ByEffectiveValuesValid() { return get_EffectiveValuesValidationIssues().isEmpty(); }
    /** {@inheritDoc} */
    @Override public final boolean is_DeeplyByRawValuesValid() { return get_DeeplyRawValuesValidationIssues().isEmpty(); }
    /** {@inheritDoc} */
    @Override public final boolean is_DeeplyByEffectiveValuesValid() { return get_DeeplyEffectiveValuesValidationIssues().isEmpty(); }
    /** {@inheritDoc} */
    @Override public final Set<String> get_DeeplyRawNotPresentRequiredFields() { return deepMissing(false); }
    /** {@inheritDoc} */
    @Override public final Set<String> get_DeeplyEffectiveNotPresentRequiredFields() { return deepMissing(true); }
    private Set<String> deepMissing(boolean aEffective) {
        Set<String> locResult = new LinkedHashSet<>();
        for (var locIssue : aEffective ? get_DeeplyEffectiveValuesValidationIssues() : get_DeeplyRawValuesValidationIssues()) {
            if (locIssue.code().equals("REQUIRED_ABSENT")) locResult.add(locIssue.path());
        }
        return Collections.unmodifiableSet(locResult);
    }
    /** {@inheritDoc} */
    @Override public final Map<String, AIidVirtualFieldValue> toMapping_RawValues() {
        return AIcSmartDataVirtualMapper.capture(this, false);
    }
    /** {@inheritDoc} */
    @Override public final Map<String, AIidVirtualFieldValue> toMapping_EffectiveValues() {
        return AIcSmartDataVirtualMapper.capture(this, true);
    }
    /** {@inheritDoc} */
    @Override public final String toString_RawValues(AIiTextFormatProfile aProfile, AInTextOutputObjectReferenceMode aMode) {
        return AIcSmartDataGraphRenderer.render(AIcSmartDataGraphSnapshot.capture(this), aProfile, aMode, false);
    }
    /** {@inheritDoc} */
    @Override public final String toString_EffectiveValues(AIiTextFormatProfile aProfile, AInTextOutputObjectReferenceMode aMode) {
        var locSnapshot = AIcSmartDataGraphSnapshot.capture(this);
        if (aProfile != null && !aProfile.isDiagnostic()) {
            var locIssues = locSnapshot.deepIssues(true);
            if (!locIssues.isEmpty()) throw new AIxSmartDataValidationException(locIssues);
        }
        return AIcSmartDataGraphRenderer.render(locSnapshot, aProfile, aMode, true);
    }
    /** Diagnostic output is intentionally separate from canonical wire serialization. */
    @Override public final String toString() {
        var locSettings = defaultDiagnosticSettings;
        return AIcSmartDataGraphRenderer.render(AIcSmartDataGraphSnapshot.capture(this),
                locSettings.profile(), locSettings.referenceMode(), false);
    }
}

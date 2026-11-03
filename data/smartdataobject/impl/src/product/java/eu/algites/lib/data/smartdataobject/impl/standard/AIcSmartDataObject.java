package eu.algites.lib.data.smartdataobject.impl.standard;


import eu.algites.lib.data.smartdataobject.AIcdSmartDataValidationIssue;
import eu.algites.lib.data.dataobject.AInFieldPresenceKind;
import eu.algites.lib.data.dataobject.AIiDataObjectIntegratedAdditionalPropertiesMap;
import eu.algites.lib.data.smartdataobject.AIiSmartDataObject;
import eu.algites.lib.data.smartdataobject.AInSdoFieldValueLoadingMode;
import eu.algites.lib.data.smartdataobject.AIidVirtualFieldValue;
import eu.algites.lib.data.smartdataobject.AIxSmartDataValidationException;
import eu.algites.lib.text.format.AIiTextFormatProfile;
import eu.algites.lib.text.format.AInTextOutputObjectReferenceMode;
import eu.algites.lib.text.format.AIsTextFormatProfiles;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Mutable generated DTO foundation; snapshot and graph implementation remain package-private. */
public abstract class AIcSmartDataObject implements AIiSmartDataObject {
    private final Map<String, Object> values = new LinkedHashMap<>();
    private final Set<String> present = new LinkedHashSet<>();
    private final Map<String, Object> additionalPropertyValues = new LinkedHashMap<>();
    private AIcIntegratedAdditionalPropertiesMap<?> additionalPropertiesView;

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

    /**
     * Returns the stable, live additional-property map of this data object.
     * Generated additional-property getters may delegate here rather than
     * creating an independent map or storing a pseudo-field in the SDO.
     *
     * <p>The first invocation fixes the map's value class for this SDO instance.
     * Later invocations must request the same class. Only one additional-property
     * namespace is supported for an SDO; multiple typed views would allow the
     * same value to be read using incompatible type assumptions.
     *
     * @param aValueClass non-null runtime value type (a super-interface is allowed)
     * @param <V> additional-property value type
     * @return stable additional-property view bound to this SDO instance
     * @throws NullPointerException if {@code aValueClass} is null
     * @throws IllegalStateException if a view with a different value type exists
     */
    @SuppressWarnings("unchecked")
    protected final <V> AIiDataObjectIntegratedAdditionalPropertiesMap<V>
            get_IntegratedAdditionalPropertiesMap(Class<V> aValueClass) {
        Objects.requireNonNull(aValueClass, "additional-property value class");
        if (additionalPropertiesView == null) {
            additionalPropertiesView = new AIcIntegratedAdditionalPropertiesMap<>(aValueClass);
        } else if (!additionalPropertiesView.valueClass.equals(aValueClass)) {
            throw new IllegalStateException("Additional-properties map already has value type "
                    + additionalPropertiesView.valueClass.getName() + ", requested " + aValueClass.getName());
        }
        return (AIiDataObjectIntegratedAdditionalPropertiesMap<V>) additionalPropertiesView;
    }

    /**
     * SDO-owned additional-property view; never copies or shadows ordinary SDO fields.
     * The backing store is distinct from the declared-field storage, while the
     * name checks use the very same resolved annotation metadata as field access.
     *
     * @param <V> accepted value type
     */
    private final class AIcIntegratedAdditionalPropertiesMap<V>
            extends AbstractMap<String, V> implements AIiDataObjectIntegratedAdditionalPropertiesMap<V> {
        private final Class<V> valueClass;

        /**
         * Constructs a typed view over this object's additional properties.
         * @param aValueClass value class for runtime assignment checking
         */
        private AIcIntegratedAdditionalPropertiesMap(Class<V> aValueClass) {
            valueClass = aValueClass;
        }

        /**
         * Rejects keys belonging to declared fields or configured wire metadata.
         * @param aName key to be inserted
         * @throws NullPointerException if the key is null
         * @throws IllegalArgumentException if the key is reserved by the SDO contract
         */
        private void requireAdditionalName(String aName) {
            Objects.requireNonNull(aName, "additional-property name");
            for (var locField : metadata().fields()) {
                if (locField.wireName().equals(aName))
                    throw new IllegalArgumentException("Reserved SDO property name: " + aName);
            }
            var locSchema = metadata().schema();
            if (locSchema != null && ((!locSchema.dataSourceSchemaUrlField().isEmpty()
                            && aName.equals(locSchema.dataSourceSchemaUrlField()))
                    || (!locSchema.dataSourceSchemaIdentifierField().isEmpty()
                            && aName.equals(locSchema.dataSourceSchemaIdentifierField()))
                    || (!locSchema.dataSourceSchemaVersionField().isEmpty()
                            && aName.equals(locSchema.dataSourceSchemaVersionField()))))
                throw new IllegalArgumentException("Reserved SDO schema metadata property: " + aName);
        }

        /**
         * Checks a dynamically assigned value against the first getter's value class.
         * @param aValue value to verify, which may be null
         * @throws ClassCastException if the value is not assignable
         */
        private void requireValue(Object aValue) {
            if (aValue != null && !valueClass.isInstance(aValue))
                throw new ClassCastException("Expected additional-property value of type "
                        + valueClass.getName() + ", got " + aValue.getClass().getName());
        }

        /** {@inheritDoc} */
        @Override public boolean containsKey(Object aKey) { return additionalPropertyValues.containsKey(aKey); }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override public V get(Object aKey) { return (V) additionalPropertyValues.get(aKey); }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override public V put(String aKey, V aValue) {
            requireAdditionalName(aKey);
            requireValue(aValue);
            return (V) additionalPropertyValues.put(aKey, aValue);
        }

        /** {@inheritDoc} */
        @Override public void putAll(Map<? extends String, ? extends V> aValues) {
            Objects.requireNonNull(aValues, "additional properties");
            // Preflight every entry so an invalid bulk update cannot partially commit.
            for (var locEntry : aValues.entrySet()) {
                requireAdditionalName(locEntry.getKey());
                requireValue(locEntry.getValue());
            }
            additionalPropertyValues.putAll(aValues);
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override public V remove(Object aKey) { return (V) additionalPropertyValues.remove(aKey); }

        /** {@inheritDoc} */
        @Override public void clear() { additionalPropertyValues.clear(); }

        /** {@inheritDoc} */
        @Override public int size() { return additionalPropertyValues.size(); }

        /**
         * Returns entries backed by the live SDO state. Iterator removals and
         * entry value replacements are applied to the underlying map.
         * @return mutable collection view of additional entries only
         */
        @Override public Set<Entry<String, V>> entrySet() {
            return new AbstractSet<>() {
                /** {@inheritDoc} */
                @Override public int size() { return additionalPropertyValues.size(); }

                /** {@inheritDoc} */
                @Override public void clear() { additionalPropertyValues.clear(); }

                /** {@inheritDoc} */
                @Override public Iterator<Entry<String, V>> iterator() {
                    Iterator<Entry<String, Object>> locIterator = additionalPropertyValues.entrySet().iterator();
                    return new Iterator<>() {
                        /** {@inheritDoc} */
                        @Override public boolean hasNext() { return locIterator.hasNext(); }

                        /** {@inheritDoc} */
                        @Override public Entry<String, V> next() {
                            Entry<String, Object> locEntry = locIterator.next();
                            return new Entry<>() {
                                /** {@inheritDoc} */
                                @Override public String getKey() { return locEntry.getKey(); }

                                /** {@inheritDoc} */
                                @SuppressWarnings("unchecked")
                                @Override public V getValue() { return (V) locEntry.getValue(); }

                                /** {@inheritDoc} */
                                @SuppressWarnings("unchecked")
                                @Override public V setValue(V aValue) {
                                    requireValue(aValue);
                                    return (V) locEntry.setValue(aValue);
                                }

                                /** {@inheritDoc} */
                                @Override public boolean equals(Object aOther) {
                                    return aOther instanceof Entry<?, ?> locOther
                                            && Objects.equals(getKey(), locOther.getKey())
                                            && Objects.equals(getValue(), locOther.getValue());
                                }

                                /** {@inheritDoc} */
                                @Override public int hashCode() {
                                    return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
                                }
                            };
                        }

                        /** {@inheritDoc} */
                        @Override public void remove() { locIterator.remove(); }
                    };
                }
            };
        }
    }

    /** Generated setters invoke this method with their schema property name. */
    protected final void set_RawField(String aName, Object aValue) {
        var locField = requireField(aName);
        requireAllowed(locField);
        if (aValue != null && !locField.getter().getReturnType().isInstance(aValue))
            throw new IllegalArgumentException("Incompatible field value for " + aName + ": " + aValue.getClass());
        values.put(aName, aValue);
        present.add(aName);
    }
    /** Generated raw getters invoke this method and cast to the declared object type. */
    protected final Object get_RawField(String aName) {
        requireAllowed(requireField(aName));
        return values.get(aName);
    }
    /** Generated effective getters invoke this method and cast to the declared object type. */
    protected final Object get_EffectiveField(String aName) {
        var locField = requireField(aName);
        requireAllowed(locField);
        if (present.contains(aName)) return values.get(aName);
        if (locField.hasDefault()) return locField.createDefault();
        if (locField.annotation().presence() == AInFieldPresenceKind.REQUIRED && !locField.allowNonPresentRead())
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
    private static void requireAllowed(AIcSmartDataMetadata.AIcdField aField) {
        if (aField.annotation().presence() == AInFieldPresenceKind.FORBIDDEN)
            throw new IllegalStateException("Field is forbidden by derived schema: " + aField.name());
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
            if (locField.annotation().presence() == AInFieldPresenceKind.REQUIRED && !hasRaw(locField.name()) &&
                    !(aEffective && locField.hasDefault())) locMissing.add(locField.name());
        }
        return Collections.unmodifiableSet(locMissing);
    }
    /** {@inheritDoc} */
    @Override public final List<AIcdSmartDataValidationIssue> _getValidationIssues(AInSdoFieldValueLoadingMode aMode) {
        return AIcSmartDataGraphSnapshot.capture(this).localIssues(
                java.util.Objects.requireNonNull(aMode, "field value loading mode") == AInSdoFieldValueLoadingMode.EFFECTIVE_VALUES);
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
    @Override public final boolean is_ByRawValuesValid() { return _getValidationIssues(AInSdoFieldValueLoadingMode.RAW_VALUES).isEmpty(); }
    /** {@inheritDoc} */
    @Override public final boolean is_ByEffectiveValuesValid() { return _getValidationIssues(AInSdoFieldValueLoadingMode.EFFECTIVE_VALUES).isEmpty(); }
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
    @Override public final Map<String, AIidVirtualFieldValue> _toMapping(AInSdoFieldValueLoadingMode aMode) {
        return AIcSmartDataVirtualMapper.capture(this,
                java.util.Objects.requireNonNull(aMode, "field value loading mode") == AInSdoFieldValueLoadingMode.EFFECTIVE_VALUES);
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

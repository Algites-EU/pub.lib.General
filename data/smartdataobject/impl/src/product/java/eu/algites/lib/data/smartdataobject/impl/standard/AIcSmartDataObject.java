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
import java.util.ArrayList;
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
    /**
     * One insertion-ordered store for declared fields and integrated map entries.
     * Namespaced keys avoid collisions when a semantic field name differs from its
     * serialized wire name. Presence is the existence of a declared key, even if
     * its explicit raw value is null; there is no independent presence collection.
     */
    private final Map<AIcdStorageKey, Object> values = new LinkedHashMap<>();

    /** Sole internal marker for a deliberately empty field; never exposed as a value. */
    static final Object EMPTY_VALUE = new Object();

    /** Returns the XML/JSON wire scalar for the internal empty marker. */
    static Object serializeRaw(Object value) { return value == EMPTY_VALUE ? "" : value; }

    /** Stable namespace distinction for entries sharing the one ordered storage. */
    private record AIcdStorageKey(boolean additional, String name) { }

    private static AIcdStorageKey declaredKey(String name) { return new AIcdStorageKey(false, name); }
    private static AIcdStorageKey additionalKey(String name) { return new AIcdStorageKey(true, name); }

    /** One snapshot entry retaining the backing-store insertion order and the key namespace. */
    record AIcdStoredEntry(boolean additional, String name, Object value) { }

    /** Provides the graph snapshot with ordered fields and extra properties. */
    final List<AIcdStoredEntry> storedEntries() {
        List<AIcdStoredEntry> result = new ArrayList<>();
        for (var entry : values.entrySet()) {
            result.add(new AIcdStoredEntry(entry.getKey().additional(),
                    entry.getKey().name(), entry.getValue()));
        }
        return List.copyOf(result);
    }

    private int additionalSize() {
        int count = 0;
        for (var key : values.keySet()) if (key.additional()) count++;
        return count;
    }

    private void clearAdditional() { values.keySet().removeIf(AIcdStorageKey::additional); }
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
     * Declared and additional entries occupy one insertion-ordered backing store,
     * while their typed key namespaces and annotation metadata prevent collisions.
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
            var locSchema = metadata().formatSchema();
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
        @Override public boolean containsKey(Object aKey) {
            return aKey instanceof String locKey && values.containsKey(additionalKey(locKey));
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override public V get(Object aKey) {
            return aKey instanceof String locKey ? (V) values.get(additionalKey(locKey)) : null;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override public V put(String aKey, V aValue) {
            requireAdditionalName(aKey);
            requireValue(aValue);
            return (V) values.put(additionalKey(aKey), aValue);
        }

        /** {@inheritDoc} */
        @Override public void putAll(Map<? extends String, ? extends V> aValues) {
            Objects.requireNonNull(aValues, "additional properties");
            /* Preflight every entry so an invalid bulk update cannot partially commit. */
            for (var locEntry : aValues.entrySet()) {
                requireAdditionalName(locEntry.getKey());
                requireValue(locEntry.getValue());
            }
            for (var locEntry : aValues.entrySet()) {
                values.put(additionalKey(locEntry.getKey()), locEntry.getValue());
            }
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override public V remove(Object aKey) {
            return aKey instanceof String locKey ? (V) values.remove(additionalKey(locKey)) : null;
        }

        /** {@inheritDoc} */
        @Override public void clear() { clearAdditional(); }

        /** {@inheritDoc} */
        @Override public int size() { return additionalSize(); }

        /**
         * Returns entries backed by the live SDO state. Iterator removals and
         * entry value replacements are applied to the underlying map.
         * @return mutable collection view of additional entries only
         */
        @Override public Set<Entry<String, V>> entrySet() {
            return new AbstractSet<>() {
                /** {@inheritDoc} */
                @Override public int size() { return additionalSize(); }

                /** {@inheritDoc} */
                @Override public void clear() { clearAdditional(); }

                /** {@inheritDoc} */
                @Override public Iterator<Entry<String, V>> iterator() {
                    /* A key snapshot makes a filtered iterator safe when removing entries
                     * from the shared store: changing map entries does not invalidate the
                     * iterator, while the backing entry values remain live. */
                    List<AIcdStorageKey> locKeys = new ArrayList<>();
                    for (var locKey : values.keySet()) if (locKey.additional()) locKeys.add(locKey);
                    Iterator<AIcdStorageKey> locIterator = locKeys.iterator();
                    return new Iterator<>() {
                        private AIcdStorageKey last;
                        private boolean canRemove;
                        /** {@inheritDoc} */
                        @Override public boolean hasNext() { return locIterator.hasNext(); }

                        /** {@inheritDoc} */
                        @Override public Entry<String, V> next() {
                            AIcdStorageKey locKey = locIterator.next();
                            last = locKey;
                            canRemove = true;
                            return new Entry<>() {
                                /** {@inheritDoc} */
                                @Override public String getKey() { return locKey.name(); }

                                /** {@inheritDoc} */
                                @SuppressWarnings("unchecked")
                                @Override public V getValue() { return (V) values.get(locKey); }

                                /** {@inheritDoc} */
                                @SuppressWarnings("unchecked")
                                @Override public V setValue(V aValue) {
                                    requireValue(aValue);
                                    if (!values.containsKey(locKey)) throw new IllegalStateException("Map entry was removed");
                                    return (V) values.put(locKey, aValue);
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
                        @Override public void remove() {
                            if (!canRemove) throw new IllegalStateException("next must precede remove");
                            values.remove(last);
                            canRemove = false;
                        }
                    };
                }
            };
        }
    }

    /**
     * Returns a stable mutable collection, allocating it on first getter call.
     * A subsequent unset detaches the old instance; clients retaining that old
     * instance cannot mutate the newly attached collection.
     *
     * <p>Index definitions are obtained solely from DataObject getter metadata,
     * never from the source XML/JSON/YAML schema or a second SDO pipeline.
     *
     * @param aName semantic name of the generated collection field
     * @param aMapped true for an AIiMultiMappedList getter; false for a plain list
     * @return stable attached collection, possibly already populated
     * @throws IllegalArgumentException for invalid key paths or incompatible getter types
     */
    protected final Object get_OrCreateCollectionField(String aName, boolean aMapped) {
        var field = requireField(aName);
        requireAllowed(field);
        var key = declaredKey(aName);
        if (values.containsKey(key)) return serializeRaw(values.get(key));
        Object created;
        if (aMapped) {
            var definitions = collectionKeyDefinitions(aName, field);
            created = definitions.isEmpty()
                    ? new eu.algites.lib.common.list.AIcMultiMappedList<>()
                    : new eu.algites.lib.common.list.AIcMultiMappedList<>(false, definitions);
        } else {
            created = new ArrayList<>();
        }
        set_RawField(aName, created);
        return created;
    }

    /**
     * Attach a concrete named indexed collection when an annotated read-only
     * contract itself extends {@code AIiMultiMappedList}.  Unlike a plain
     * {@code AIcMultiMappedList}, this factory retains the generated nominal
     * DataObject type expected by the getter.
     *
     * @param aName semantic field name
     * @param aFactory factory for the generated implementation and its index definitions
     * @param <V> indexed entry type
     * @param <T> generated concrete collection type
     * @return existing attached value or newly constructed named collection
     * @throws IllegalArgumentException if the field is unknown, its keys are invalid,
     *         or the generated value is incompatible with the declared getter
     */
    protected final <V, T extends eu.algites.lib.common.list.AIiMultiMappedList<V>> T get_OrCreateNamedMappedCollectionField(String aName,
            java.util.function.Function<java.util.Collection<? extends eu.algites.lib.common.list.AIiKeyDefinition<V, ?>>, T> aFactory) {
        java.util.Objects.requireNonNull(aFactory, "collection factory");
        var field = requireField(aName);
        requireAllowed(field);
        var key = declaredKey(aName);
        if (values.containsKey(key)) {
            @SuppressWarnings("unchecked") T existing = (T) serializeRaw(values.get(key));
            return existing;
        }
        T created = aFactory.apply(collectionKeyDefinitions(aName, field));
        set_RawField(aName, created);
        return created;
    }

    /**
     * Read indexed-list declarations from the DataObject getter.  Both generic
     * and named collections must use these same definitions to retain key views.
     *
     * @param aName semantic field name, for diagnostics
     * @param aField resolved annotated metadata
     * @param <V> element type of the indexed collection
     * @return ordered definitions with their distinct names and path extractors
     * @throws IllegalArgumentException for empty or unresolved index paths
     */
    private static <V> java.util.ArrayList<eu.algites.lib.common.list.AIiKeyDefinition<V, ?>>
            collectionKeyDefinitions(String aName, AIcSmartDataMetadata.AIcdField aField) {
        var definitions = new java.util.ArrayList<eu.algites.lib.common.list.AIiKeyDefinition<V, ?>>();
        for (var keyDefinition : aField.keyDefinitions()) {
            var paths = keyDefinition.fields().clone();
            if (paths.length == 0) throw new IllegalArgumentException("Key fields must not be empty: " + aName);
            definitions.add(new eu.algites.lib.common.list.AIcKeyDefinition<V, Object>(
                    keyDefinition.name(), value -> {
                        var keyValues = new java.util.ArrayList<Object>();
                        for (String path : paths) {
                            Object component = value;
                            for (String fragment : path.split("\\.")) {
                                if (component == null) break;
                                var part = AIcSmartDataMetadata.forReadContract(component.getClass())
                                        .names().get(fragment);
                                if (part == null) throw new IllegalArgumentException(
                                        "Unresolved key field " + path + " for " + aName);
                                try { component = part.getter().invoke(component); }
                                catch (ReflectiveOperationException error) {
                                    throw new IllegalArgumentException("Cannot read key field: " + path, error);
                                }
                            }
                            keyValues.add(component);
                        }
                        return keyValues.contains(null) ? null
                                : keyValues.size() == 1 ? keyValues.get(0) : List.copyOf(keyValues);
                    }, true, keyDefinition.required()));
        }
        return definitions;
    }

    /** Return the reference type accepted for a primitive getter's boxed setter argument. */
    private static Class<?> boxedGetterType(Class<?> aType) {
        if (!aType.isPrimitive()) return aType;
        if (aType == boolean.class) return Boolean.class;
        if (aType == byte.class) return Byte.class;
        if (aType == short.class) return Short.class;
        if (aType == int.class) return Integer.class;
        if (aType == long.class) return Long.class;
        if (aType == float.class) return Float.class;
        if (aType == double.class) return Double.class;
        if (aType == char.class) return Character.class;
        throw new IllegalArgumentException("Not a DataObject getter value type: " + aType);
    }

    /** Generated setters invoke this method with their schema property name. */
    protected final void set_RawField(String aName, Object aValue) {
        var locField = requireField(aName);
        requireAllowed(locField);
        // Reflection reports `int.class` (etc.) for primitive getters, but
        // generated setters pass boxed values into the Object-backed store.
        // Class.isInstance() is always false on a primitive Class literal.
        if (aValue != null && !boxedGetterType(locField.getter().getReturnType()).isInstance(aValue))
            throw new IllegalArgumentException("Incompatible field value for " + aName + ": " + aValue.getClass());
        locField.validateValue(aValue);
        values.put(declaredKey(aName), aValue);
    }
    /** Generated raw getters invoke this method and cast to the declared object type. */
    protected final Object get_RawField(String aName) {
        requireAllowed(requireField(aName));
        return serializeRaw(values.get(declaredKey(aName)));
    }
    /** Generated effective getters invoke this method and cast to the declared object type. */
    protected final Object get_EffectiveField(String aName) {
        var locField = requireField(aName);
        requireAllowed(locField);
        if (values.containsKey(declaredKey(aName))) {
            Object value = values.get(declaredKey(aName));
            return value == EMPTY_VALUE
                    ? locField.hasEmptyDefault() ? locField.createDefault() : null : value;
        }
        if (locField.hasDefault()) return locField.createDefault();
        if (locField.annotation().presence() == AInFieldPresenceKind.REQUIRED && !locField.allowNonPresentRead())
            throw new IllegalStateException("Required field is absent: " + aName);
        return null;
    }
    /**
     * Assigns the distinct explicit empty state, only where field metadata allows it.
     * Unlike setXxx(""), this changes the semantic empty state without storing a String.
     *
     * @param aName semantic field name
     * @throws IllegalArgumentException if the field is not empty-capable or fixed
     */
    protected final void set_EmptyField(String aName) {
        var field = requireField(aName);
        requireAllowed(field);
        if (!field.annotation().isEmptyAllowed())
            throw new IllegalArgumentException("Explicit empty not allowed for " + aName);
        if (field.hasFixed())
            throw new IllegalArgumentException("Fixed field cannot be explicitly empty: " + aName);
        values.put(declaredKey(aName), EMPTY_VALUE);
    }

    /**
     * Tests explicit empty-state presence without materializing any default.
     *
     * @param aName semantic field name
     * @return true only when the internal empty marker is present
     */
    protected final boolean isEmpty_Field(String aName) {
        requireField(aName);
        return values.get(declaredKey(aName)) == EMPTY_VALUE;
    }

    /**
     * Checks whether present field is enabled or present.
     * @param aName name supplied to the operation
     * @return the present field
     */
    protected final boolean isPresent_Field(String aName) {
        requireField(aName);
        return values.containsKey(declaredKey(aName));
    }
    /**
     * Removes an explicitly assigned field value, allowing its default to apply again.
     * @param aName name supplied to the operation
     */
    protected final void unset_Field(String aName) {
        requireField(aName);
        values.remove(declaredKey(aName));
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
    final boolean hasRaw(String aName) { return values.containsKey(declaredKey(aName)); }
    final Object raw(String aName) { return values.get(declaredKey(aName)); }
    final boolean isEmptyRaw(String aName) { return values.get(declaredKey(aName)) == EMPTY_VALUE; }

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

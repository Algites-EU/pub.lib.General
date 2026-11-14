package eu.algites.lib.data.smartdataobject.impl.common;

import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIaDataObjectFieldListConstraints;
import eu.algites.lib.data.dataobject.AIaDataObjectFieldNumericConstraints;
import eu.algites.lib.data.dataobject.AIaDataObjectFieldObjectConstraints;
import eu.algites.lib.data.dataobject.AIaDataObjectFieldStringConstraints;
import eu.algites.lib.data.dataobject.AIaKeyDefinition;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Independent schema-neutral metadata registry for SDO implementation classes.
 * The legacy {@code AIcSmartDataMetadata} cache is deliberately not consulted.
 *
 * <p>Metadata are lazily indexed by the Java {@link Class} identity. A binary
 * name shared by different classloaders does not merge their metadata.</p>
 *
 * @author linhart1
 */
public final class AIsSmartDataObjectMetadataRegistry {
    private static final ClassValue<ResolvedMetadata> METADATA = new ClassValue<>() {
        @Override
        protected ResolvedMetadata computeValue(Class<?> aType) {
            if (!AIcSmartDataObject.class.isAssignableFrom(aType)) {
                throw new IllegalArgumentException("Not a SmartDataObject implementation: " + aType);
            }
            @SuppressWarnings("unchecked")
            Class<? extends AIcSmartDataObject> locType =
                    (Class<? extends AIcSmartDataObject>) aType;
            return scan(locType);
        }
    };

    private AIsSmartDataObjectMetadataRegistry() { }

    /**
     * Retrieves or constructs metadata for a concrete SDO type, resolving all
     * currently discovered format providers. The class itself is not instantiated.
     *
     * @param aImplementationClass requested concrete SmartDataObject class
     * @return cached common descriptor with all discovered format contributions
     * @throws NullPointerException if the class is null
     * @throws IllegalArgumentException if metadata contain conflicting fields
     */
    public static AIiSmartDataObjectMetadata forType(
            Class<? extends AIcSmartDataObject> aImplementationClass) {
        Objects.requireNonNull(aImplementationClass, "aImplementationClass");
        ResolvedMetadata locMetadata = METADATA.get(aImplementationClass);
        locMetadata.refreshFormatBindings();
        return locMetadata;
    }

    private static ResolvedMetadata scan(Class<? extends AIcSmartDataObject> aType) {
        Map<String, FieldBuilder> locFields = new LinkedHashMap<>();
        for (Class<?> locType : hierarchy(aType)) {
            Method[] locMethods = locType.getDeclaredMethods();
            Arrays.sort(locMethods, Comparator.comparing(Method::getName)
                    .thenComparing(aMethod -> aMethod.getReturnType().getName()));
            for (Method locMethod : locMethods) {
                if (locMethod.isSynthetic() || locMethod.isBridge() || Modifier.isStatic(locMethod.getModifiers())) {
                    continue;
                }
                String locSemantic = semanticName(locMethod);
                if (locSemantic == null) continue;
                AIaDataObjectField locDefinition = locMethod.getDeclaredAnnotation(AIaDataObjectField.class);
                FieldBuilder locBefore = locFields.get(locSemantic);
                if (locDefinition == null && locBefore == null) continue;
                if (locBefore != null && !locBefore.getter.getReturnType().isAssignableFrom(
                        locMethod.getReturnType())) {
                    // Ignore a less specific declaration from an unrelated path.
                    if (locMethod.getReturnType().isAssignableFrom(locBefore.getter.getReturnType())) continue;
                    throw new IllegalArgumentException("Incompatible getter override for " + locSemantic
                            + " on " + aType.getName());
                }
                FieldBuilder locBuilder = locBefore == null
                        ? new FieldBuilder(locSemantic) : new FieldBuilder(locBefore);
                locBuilder.getter = locMethod;
                if (locDefinition != null) {
                    if (locBefore != null && locBefore.definition.embedded() != locDefinition.embedded()) {
                        throw new IllegalArgumentException("Incompatible embedded field override: " + locSemantic);
                    }
                    locBuilder.definition = locDefinition;
                    String locDataName = locDefinition.dataFieldName();
                    if (!locDataName.isBlank()) {
                        if (locBefore != null && !locDataName.equals(locBefore.dataFieldName)) {
                            throw new IllegalArgumentException("Conflicting dataFieldName: " + locSemantic);
                        }
                        locBuilder.dataFieldName = locDataName;
                    }
                    if (locBefore == null) locBuilder.dataFieldName = locDataName.isBlank()
                            ? locSemantic : locDataName;
                    if (locDefinition.embedded() && !locDataName.isBlank()) {
                        throw new IllegalArgumentException("Embedded field cannot supply dataFieldName: " + locSemantic);
                    }
                }
                locBuilder.string = first(locMethod.getDeclaredAnnotation(AIaDataObjectFieldStringConstraints.class), locBuilder.string);
                locBuilder.numeric = first(locMethod.getDeclaredAnnotation(AIaDataObjectFieldNumericConstraints.class), locBuilder.numeric);
                locBuilder.list = first(locMethod.getDeclaredAnnotation(AIaDataObjectFieldListConstraints.class), locBuilder.list);
                locBuilder.object = first(locMethod.getDeclaredAnnotation(AIaDataObjectFieldObjectConstraints.class), locBuilder.object);
                AIaKeyDefinition[] locKeys = locMethod.getDeclaredAnnotationsByType(AIaKeyDefinition.class);
                if (locKeys.length > 0) locBuilder.keys = List.of(locKeys);
                locFields.put(locSemantic, locBuilder);
            }
        }
        // A valid DataObject may have no fields (e.g. an empty schema type).
        Map<String, ResolvedField> locBySemantic = new LinkedHashMap<>();
        Set<String> locByData = new LinkedHashSet<>();
        for (FieldBuilder locBuilder : locFields.values()) {
            ResolvedField locField = locBuilder.build();
            if (!locField.getDefinition().embedded() && !locByData.add(locField.getDataFieldName())) {
                throw new IllegalArgumentException("Duplicate dataFieldName: " + locField.getDataFieldName());
            }
            locBySemantic.put(locField.getSemanticFieldName(), locField);
        }
        return new ResolvedMetadata(aType, locBySemantic);
    }

    private static <T> T first(T aNew, T aInherited) {
        return aNew == null ? aInherited : aNew;
    }

    static List<Class<?>> hierarchy(Class<?> aType) {
        List<Class<?>> locTypes = new ArrayList<>();
        Set<Class<?>> locSeen = Collections.newSetFromMap(new IdentityHashMap<>());
        collect(aType, locTypes, locSeen);
        return locTypes;
    }

    private static void collect(Class<?> aType, List<Class<?>> aTypes, Set<Class<?>> aSeen) {
        if (aType == null || aType == Object.class || !aSeen.add(aType)) return;
        collect(aType.getSuperclass(), aTypes, aSeen);
        for (Class<?> locInterface : aType.getInterfaces()) collect(locInterface, aTypes, aSeen);
        aTypes.add(aType);
    }

    static String semanticName(Method aMethod) {
        String locName = aMethod.getName();
        if (aMethod.getParameterCount() != 0 || aMethod.getReturnType() == void.class ||
                !locName.startsWith("get") || locName.length() < 4 ||
                !Character.isUpperCase(locName.charAt(3))) return null;
        return locName.substring(3);
    }

    private static final class FieldBuilder {
        final String semanticFieldName;
        String dataFieldName;
        AIaDataObjectField definition;
        Method getter;
        AIaDataObjectFieldStringConstraints string;
        AIaDataObjectFieldNumericConstraints numeric;
        AIaDataObjectFieldListConstraints list;
        AIaDataObjectFieldObjectConstraints object;
        List<AIaKeyDefinition> keys = List.of();
        FieldBuilder(String aSemantic) { semanticFieldName = aSemantic; }
        FieldBuilder(FieldBuilder aOriginal) {
            semanticFieldName = aOriginal.semanticFieldName;
            dataFieldName = aOriginal.dataFieldName;
            definition = aOriginal.definition;
            getter = aOriginal.getter;
            string = aOriginal.string;
            numeric = aOriginal.numeric;
            list = aOriginal.list;
            object = aOriginal.object;
            keys = aOriginal.keys;
        }
        ResolvedField build() {
            return new ResolvedField(semanticFieldName, dataFieldName, getter,
                    definition, string, numeric, list, object, List.copyOf(keys));
        }
    }

    private record ResolvedField(String semanticFieldName, String dataFieldName,
            Method getter, AIaDataObjectField definition,
            AIaDataObjectFieldStringConstraints string,
            AIaDataObjectFieldNumericConstraints numeric,
            AIaDataObjectFieldListConstraints list,
            AIaDataObjectFieldObjectConstraints object,
            List<AIaKeyDefinition> keys) implements AIiSmartDataObjectFieldMetadata {
        @Override public String getSemanticFieldName() { return semanticFieldName; }
        @Override public String getDataFieldName() { return dataFieldName; }
        @Override public Method getEffectiveGetter() { return getter; }
        @Override public Type getEffectiveReturnType() { return getter.getGenericReturnType(); }
        @Override public AIaDataObjectField getDefinition() { return definition; }
        @Override public AIaDataObjectFieldStringConstraints getStringConstraints() { return string; }
        @Override public AIaDataObjectFieldNumericConstraints getNumericConstraints() { return numeric; }
        @Override public AIaDataObjectFieldListConstraints getListConstraints() { return list; }
        @Override public AIaDataObjectFieldObjectConstraints getObjectConstraints() { return object; }
        @Override public List<AIaKeyDefinition> getKeyDefinitions() { return keys; }
    }

    private static final class ResolvedMetadata implements AIiSmartDataObjectMetadata {
        private final Class<? extends AIcSmartDataObject> implementationClass;
        private final Map<String, ResolvedField> fields;
        private final ConcurrentHashMap<Class<?>, Optional<AIiSmartDataObjectFormatBindingMetadata>>
                formatContributions = new ConcurrentHashMap<>();
        ResolvedMetadata(Class<? extends AIcSmartDataObject> aType, Map<String, ResolvedField> aFields) {
            implementationClass = aType;
            fields = Collections.unmodifiableMap(new LinkedHashMap<>(aFields));
        }
        @Override public Class<? extends AIcSmartDataObject> getImplementationClass() {
            return implementationClass;
        }
        @Override public Collection<? extends AIiSmartDataObjectFieldMetadata> getFields() {
            return List.copyOf(fields.values());
        }
        @Override public AIiSmartDataObjectFieldMetadata getField(String aSemanticFieldName) {
            return fields.get(aSemanticFieldName);
        }
        void refreshFormatBindings() {
            for (AIiSmartDataObjectFormatBindingProvider locProvider :
                    AIsSmartDataObjectFormatBindingProviderRegistry.getProviders(implementationClass.getClassLoader())) {
                formatContributions.computeIfAbsent(locProvider.getClass(), aIgnored -> {
                    Optional<? extends AIiSmartDataObjectFormatBindingMetadata> locResolved =
                            Objects.requireNonNull(locProvider.resolveMetadata(this), "provider metadata result");
                    locResolved.ifPresent(aValue -> {
                        if (!locProvider.getFormatId().equals(aValue.getFormatId()) ||
                                !locProvider.getProviderId().equals(aValue.getProviderId())) {
                            throw new IllegalArgumentException("Inconsistent format provider metadata IDs: "
                                    + locProvider.getClass().getName());
                        }
                    });
                    return locResolved.map(aValue -> (AIiSmartDataObjectFormatBindingMetadata)aValue);
                });
            }
        }
        @Override public Collection<? extends AIiSmartDataObjectFormatBindingMetadata> getFormatBindings() {
            return formatContributions.values().stream().flatMap(Optional::stream).toList();
        }
        @Override public Optional<? extends AIiSmartDataObjectFormatBindingMetadata> getFormatBinding(
                String aFormatId, String aProviderId) {
            Objects.requireNonNull(aFormatId, "aFormatId");
            Objects.requireNonNull(aProviderId, "aProviderId");
            List<? extends AIiSmartDataObjectFormatBindingMetadata> locMatching =
                getFormatBindings().stream().filter(aBinding ->
                        aFormatId.equals(aBinding.getFormatId()) && aProviderId.equals(aBinding.getProviderId())).toList();
            if (locMatching.size() > 1) {
                throw new IllegalStateException("Ambiguous format binding provider: " + aFormatId + "/" + aProviderId);
            }
            return locMatching.stream().findFirst();
        }
    }
}

package eu.algites.lib.data.smartdataobject.impl.common;

import eu.algites.lib.data.dataobject.AIiDataObject;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable purpose-specific registry of SDO implementations and contract bindings.
 * Registration never rejects a second implementation solely because it
 * implements an already registered read-only contract.
 *
 * @author linhart1
 */
public final class AIcSmartDataObjectBindingRegistry implements AIiSmartDataObjectBindingRegistry {
    private final Map<Class<? extends AIcSmartDataObject>, AIiSmartDataObjectBinding> bindings;
    private final Map<Class<? extends AIiDataObject>, List<AIiSmartDataObjectBinding>> byReadContract;

    private AIcSmartDataObjectBindingRegistry(
            Map<Class<? extends AIcSmartDataObject>, AIiSmartDataObjectBinding> aBindings,
            Map<Class<? extends AIiDataObject>, List<AIiSmartDataObjectBinding>> aByContract) {
        bindings = Collections.unmodifiableMap(new LinkedHashMap<>(aBindings));
        Map<Class<? extends AIiDataObject>, List<AIiSmartDataObjectBinding>> locCopy = new LinkedHashMap<>();
        aByContract.forEach((aKey, aValue) -> locCopy.put(aKey, List.copyOf(aValue)));
        byReadContract = Collections.unmodifiableMap(locCopy);
    }

    /**
     * Creates a builder that registers only explicitly supplied implementation types.
     *
     * @return new mutable builder
     */
    public static Builder builder() { return new Builder(); }

    /** {@inheritDoc} */
    @Override public Collection<? extends AIiSmartDataObjectBinding> getBindings() {
        return List.copyOf(bindings.values());
    }

    /** {@inheritDoc} */
    @Override public AIiSmartDataObjectBinding getBinding(
            Class<? extends AIcSmartDataObject> aImplementationClass) {
        return bindings.get(aImplementationClass);
    }

    /** {@inheritDoc} */
    @Override public Collection<? extends AIiSmartDataObjectBinding> getBindingsForReadContract(
            Class<? extends AIiDataObject> aReadContract) {
        return byReadContract.getOrDefault(aReadContract, List.of());
    }

    /** {@inheritDoc} */
    @Override public AIiSmartDataObjectBinding resolveBinding(
            Class<? extends AIiDataObject> aReadContract,
            AIiSmartDataObjectBinding aOwningBinding, String aSemanticFieldName) {
        Objects.requireNonNull(aReadContract, "aReadContract");
        if (aOwningBinding != null && aSemanticFieldName != null) {
            AIiSmartDataObjectFieldBinding locField = aOwningBinding.getFieldBinding(aSemanticFieldName);
            if (locField != null && locField.getImplementationClassOverride().isPresent()) {
                Class<? extends AIcSmartDataObject> locType = locField.getImplementationClassOverride().orElseThrow();
                AIiSmartDataObjectBinding locSelected = bindings.get(locType);
                if (locSelected == null)
                    throw new IllegalArgumentException("Explicit field implementation is not registered: " + locType.getName());
                if (!aReadContract.isAssignableFrom(locType))
                    throw new IllegalArgumentException("Incompatible explicit field implementation: " + locType.getName());
                return locSelected;
            }
        }
        List<AIiSmartDataObjectBinding> locCandidates = byReadContract.getOrDefault(aReadContract, List.of());
        if (locCandidates.isEmpty())
            throw new IllegalArgumentException("No registered implementation for " + aReadContract.getName());
        List<AIiSmartDataObjectBinding> locPrimary = locCandidates.stream()
                .filter(binding -> binding.getDeclaredReadContracts().contains(aReadContract)).toList();
        if (locPrimary.size() == 1) return locPrimary.get(0);
        if (locPrimary.isEmpty() && locCandidates.size() == 1) return locCandidates.get(0);
        throw new IllegalArgumentException("Ambiguous SDO implementations for " + aReadContract.getName()
                + ": " + locCandidates.stream().map(b -> b.getImplementationClass().getName()).toList());
    }

    /**
     * Builder for an isolated and immutable binding registry.
     * Only classes supplied to this builder enter the resulting registry.
     *
     * @author linhart1
     */
    public static final class Builder {
        private final Map<Class<? extends AIcSmartDataObject>, AIiSmartDataObjectBinding> bindings =
                new LinkedHashMap<>();

        private Builder() { }

        /**
         * Registers a concrete SDO implementation and its inherited read contracts.
         * Repeated registration of an identical class has no effect. Different classes
         * may provide the same contract without error.
         *
         * @param aImplementationClass concrete implementation class
         * @return this builder
         * @throws NullPointerException if the argument is null
         * @throws IllegalArgumentException if the class is abstract, lacks valid
         *         binding declarations or does not implement a declared contract
         */
        public Builder register(Class<? extends AIcSmartDataObject> aImplementationClass) {
            Objects.requireNonNull(aImplementationClass, "aImplementationClass");
            if (aImplementationClass.isInterface() || Modifier.isAbstract(aImplementationClass.getModifiers())) {
                throw new IllegalArgumentException("Cannot register non-concrete SDO: " + aImplementationClass);
            }
            bindings.computeIfAbsent(aImplementationClass, Builder::createBinding);
            return this;
        }

        /**
         * Creates the final immutable registry with indexes in registration order.
         *
         * @return purpose-scoped binding registry
         */
        public AIcSmartDataObjectBindingRegistry build() {
            Map<Class<? extends AIiDataObject>, List<AIiSmartDataObjectBinding>> locByContract =
                    new LinkedHashMap<>();
            for (AIiSmartDataObjectBinding locBinding : bindings.values()) {
                for (Class<? extends AIiDataObject> locContract : locBinding.getEffectiveReadContracts()) {
                    locByContract.computeIfAbsent(locContract, aIgnored -> new ArrayList<>()).add(locBinding);
                }
            }
            return new AIcSmartDataObjectBindingRegistry(bindings, locByContract);
        }

        private static AIiSmartDataObjectBinding createBinding(
                Class<? extends AIcSmartDataObject> aType) {
            AIiSmartDataObjectMetadata locMetadata = AIsSmartDataObjectMetadataRegistry.forType(aType);
            Set<Class<? extends AIiDataObject>> locDeclared = new LinkedHashSet<>();
            Set<Class<? extends AIiDataObject>> locEffective = new LinkedHashSet<>();
            for (Class<?> locType : AIsSmartDataObjectMetadataRegistry.hierarchy(aType)) {
                if (!AIcSmartDataObject.class.isAssignableFrom(locType)) continue;
                AIaSmartDataObjectImplementationBinding locAnnotation =
                        locType.getDeclaredAnnotation(AIaSmartDataObjectImplementationBinding.class);
                if (locAnnotation == null) continue;
                for (Class<? extends AIiDataObject> locContract : locAnnotation.readContracts()) {
                    if (!locContract.isInterface() || !locContract.isAssignableFrom(aType)) {
                        throw new IllegalArgumentException("Read contract " + locContract.getName()
                                + " is not implemented by " + aType.getName());
                    }
                    if (locType == aType) locDeclared.add(locContract);
                    collectContract(locContract, locEffective);
                }
            }
            if (locEffective.isEmpty()) {
                throw new IllegalArgumentException("Missing @AIaSmartDataObjectImplementationBinding on "
                        + aType.getName() + " or its SDO superclass");
            }
            Map<String, AIiSmartDataObjectFieldBinding> locFields = new LinkedHashMap<>();
            Map<String, Class<? extends AIcSmartDataObject>> locOverrides = fieldOverrides(aType);
            for (AIiSmartDataObjectFieldMetadata locField : locMetadata.getFields()) {
                if (!isSdoValued(locField.getEffectiveReturnType())) continue;
                Class<? extends AIcSmartDataObject> locOverride =
                        locOverrides.get(locField.getSemanticFieldName());
                if (locOverride != null && !compatibleWithField(locField.getEffectiveReturnType(), locOverride)) {
                    throw new IllegalArgumentException("Incompatible implementation override: " + aType.getName()
                            + "." + locField.getSemanticFieldName() + " -> " + locOverride.getName());
                }
                locFields.put(locField.getSemanticFieldName(), new ResolvedFieldBinding(locField, locOverride));
            }
            return new ResolvedBinding(aType, locMetadata, locDeclared, locEffective, locFields);
        }

        private static void collectContract(Class<? extends AIiDataObject> aContract,
                Set<Class<? extends AIiDataObject>> aEffective) {
            if (!aEffective.add(aContract)) return;
            for (Class<?> locParent : aContract.getInterfaces()) {
                if (AIiDataObject.class.isAssignableFrom(locParent)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends AIiDataObject> locTyped = (Class<? extends AIiDataObject>) locParent;
                    collectContract(locTyped, aEffective);
                }
            }
        }

        private static Map<String, Class<? extends AIcSmartDataObject>> fieldOverrides(Class<?> aType) {
            Map<String, Class<? extends AIcSmartDataObject>> locOverrides = new LinkedHashMap<>();
            // A method annotation is not @Inherited. Walk the complete type hierarchy
            // from base to derived and retain the nearest explicit declaration.
            for (Class<?> locType : AIsSmartDataObjectMetadataRegistry.hierarchy(aType)) {
                for (Method locMethod : locType.getDeclaredMethods()) {
                    if (locMethod.isBridge() || locMethod.isSynthetic()) continue;
                    String locSemantic = AIsSmartDataObjectMetadataRegistry.semanticName(locMethod);
                    if (locSemantic == null) continue;
                    AIaSmartDataObjectFieldImplementationBinding locAnnotation =
                            locMethod.getDeclaredAnnotation(AIaSmartDataObjectFieldImplementationBinding.class);
                    if (locAnnotation != null) {
                        locOverrides.put(locSemantic, locAnnotation.implementationClass());
                    }
                }
            }
            return locOverrides;
        }

        private static boolean isSdoValued(Type aType) {
            if (aType instanceof Class<?> locClass) return AIiDataObject.class.isAssignableFrom(locClass);
            if (aType instanceof ParameterizedType locParameterization) {
                for (Type locArg : locParameterization.getActualTypeArguments()) {
                    if (isSdoValued(locArg)) return true;
                }
            }
            if (aType instanceof java.lang.reflect.WildcardType locWildcard) {
                for (Type locBound : locWildcard.getUpperBounds()) {
                    if (isSdoValued(locBound)) return true;
                }
            }
            return false;
        }

        private static boolean compatibleWithField(Type aFieldType,
                Class<? extends AIcSmartDataObject> aImplementation) {
            if (aFieldType instanceof Class<?> locClass) {
                return AIiDataObject.class.isAssignableFrom(locClass) &&
                        locClass.isAssignableFrom(aImplementation);
            }
            if (aFieldType instanceof ParameterizedType locGeneric) {
                for (Type locArg : locGeneric.getActualTypeArguments()) {
                    if (compatibleWithField(locArg, aImplementation)) return true;
                }
            }
            if (aFieldType instanceof java.lang.reflect.WildcardType locWildcard) {
                for (Type locBound : locWildcard.getUpperBounds()) {
                    if (compatibleWithField(locBound, aImplementation)) return true;
                }
            }
            return false;
        }
    }

    private record ResolvedBinding(
            Class<? extends AIcSmartDataObject> implementationClass,
            AIiSmartDataObjectMetadata metadata,
            Set<Class<? extends AIiDataObject>> declaredContracts,
            Set<Class<? extends AIiDataObject>> effectiveContracts,
            Map<String, AIiSmartDataObjectFieldBinding> fields) implements AIiSmartDataObjectBinding {
        private ResolvedBinding {
            declaredContracts = Collections.unmodifiableSet(new LinkedHashSet<>(declaredContracts));
            effectiveContracts = Collections.unmodifiableSet(new LinkedHashSet<>(effectiveContracts));
            fields = Collections.unmodifiableMap(new LinkedHashMap<>(fields));
        }
        @Override public Class<? extends AIcSmartDataObject> getImplementationClass() { return implementationClass; }
        @Override public AIiSmartDataObjectMetadata getMetadata() { return metadata; }
        @Override public Set<Class<? extends AIiDataObject>> getDeclaredReadContracts() { return declaredContracts; }
        @Override public Set<Class<? extends AIiDataObject>> getEffectiveReadContracts() { return effectiveContracts; }
        @Override public Collection<? extends AIiSmartDataObjectFieldBinding> getFieldBindings() {
            return List.copyOf(fields.values());
        }
        @Override public AIiSmartDataObjectFieldBinding getFieldBinding(String aSemanticFieldName) {
            return fields.get(aSemanticFieldName);
        }
    }

    private record ResolvedFieldBinding(
            AIiSmartDataObjectFieldMetadata fieldMetadata,
            Class<? extends AIcSmartDataObject> implementationOverride) implements AIiSmartDataObjectFieldBinding {
        @Override public AIiSmartDataObjectFieldMetadata getFieldMetadata() { return fieldMetadata; }
        @Override public Optional<Class<? extends AIcSmartDataObject>> getImplementationClassOverride() {
            return Optional.ofNullable(implementationOverride);
        }
    }
}

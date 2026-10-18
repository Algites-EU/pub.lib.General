package eu.algites.lib.data.dataobject;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Lazy, classloader-friendly registry of explicitly permitted OpenChoice substitutions.
 * Only direct annotation edges are followed; Java interface inheritance alone does not
 * authorize a newly declared domain contract to join another choice.
 * All scans concern public type metadata and require no JPMS package opening.
 */
public final class AIsOpenChoiceTypes {
    private static final ClassValue<Set<Class<?>>> ALLOWED_HEADS = new ClassValue<>() {
        /** {@inheritDoc} */
        @Override protected Set<Class<?>> computeValue(Class<?> aType) {
            LinkedHashSet<Class<?>> locResult = new LinkedHashSet<>();
            collectHeads(aType, locResult, Collections.newSetFromMap(new IdentityHashMap<>()));
            return Set.copyOf(locResult);
        }
    };

    private AIsOpenChoiceTypes() { }

    /**
     * Lists heads reachable through explicitly annotated substitutions from this contract.
     * @param aContract the annotated contract, not an arbitrary implementation class
     * @return immutable set of reachable parent contracts
     */
    public static Set<Class<?>> allowedHeads(Class<?> aContract) {
        return ALLOWED_HEADS.get(aContract);
    }

    /**
     * Determines whether the actual value has a permitted direct or transitive contract.
     * A generated domain contract is identified by {@link AIaDataObject}; an unmarked
     * derived domain contract does not inherit an ancestor's participation permission.
     * @param aValue candidate value
     * @param aHead required open-choice root
     * @return whether the candidate is permitted
     */
    public static boolean permits(Object aValue, Class<?> aHead) {
        if (aValue == null || aHead == null || !aHead.isInstance(aValue)) return false;
        Set<Class<?>> locContracts = new LinkedHashSet<>();
        collectDomainContracts(aValue.getClass(), locContracts,
                Collections.newSetFromMap(new IdentityHashMap<>()));
        if (locContracts.isEmpty()) {
            collectImplementedInterfaces(aValue.getClass(), locContracts,
                    Collections.newSetFromMap(new IdentityHashMap<>()));
        }
        for (Class<?> locContract : locContracts) {
            boolean locMoreSpecific = false;
            for (Class<?> locOther : locContracts) {
                if (locContract != locOther && locContract.isAssignableFrom(locOther)) {
                    locMoreSpecific = true;
                    break;
                }
            }
            if (!locMoreSpecific) {
                AIaDataObject locMetadata = locContract.getDeclaredAnnotation(AIaDataObject.class);
                Class<?> locSource = locMetadata == null ? void.class : locMetadata.sourceContract();
                Class<?> locEffective = locSource == void.class ? locContract : locSource;
                if (locSource != void.class && !locSource.isAssignableFrom(locContract))
                    throw new IllegalArgumentException("Invalid source contract: " + locContract);
                if (locEffective == aHead || allowedHeads(locEffective).contains(aHead)) return true;
            }
        }
        return false;
    }

    private static void collectHeads(Class<?> aType, Set<Class<?>> aResult, Set<Class<?>> aVisiting) {
        if (!aVisiting.add(aType)) throw new IllegalArgumentException("OpenChoice cycle at " + aType);
        for (AIaOpenChoiceTypeOf locEdge : aType.getDeclaredAnnotationsByType(AIaOpenChoiceTypeOf.class)) {
            Class<?> locParent = locEdge.value();
            if (locParent == aType || !locParent.isAssignableFrom(aType))
                throw new IllegalArgumentException("Invalid OpenChoice edge: " + aType + " -> " + locParent);
            aResult.add(locParent);
            collectHeads(locParent, aResult, aVisiting);
        }
        aVisiting.remove(aType);
    }

    private static void collectDomainContracts(Class<?> aType, Set<Class<?>> aResult, Set<Class<?>> aVisited) {
        if (aType == null || !aVisited.add(aType)) return;
        if (aType.isInterface() && aType.isAnnotationPresent(AIaDataObject.class)) aResult.add(aType);
        for (Class<?> locParent : aType.getInterfaces()) collectDomainContracts(locParent, aResult, aVisited);
        collectDomainContracts(aType.getSuperclass(), aResult, aVisited);
    }

    private static void collectImplementedInterfaces(Class<?> aType, Set<Class<?>> aResult, Set<Class<?>> aVisited) {
        if (aType == null || !aVisited.add(aType)) return;
        for (Class<?> locParent : aType.getInterfaces()) {
            aResult.add(locParent);
            collectImplementedInterfaces(locParent, aResult, aVisited);
        }
        collectImplementedInterfaces(aType.getSuperclass(), aResult, aVisited);
    }
}

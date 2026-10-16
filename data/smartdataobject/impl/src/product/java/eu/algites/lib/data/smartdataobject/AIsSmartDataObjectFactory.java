package eu.algites.lib.data.smartdataobject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Universal graph-construction factory for generated mutable SmartDataObjects.
 *
 * <p>Generated concrete types expose a constructor accepting the typed field
 * mapping. Only this class performs constructor lookup and caches it by class.
 * A scoped identity map preserves cycles and shared objects. Intermediate,
 * partially populated objects must never escape the construction thread.
 * The ThreadLocal context is always removed by the outermost call, on success
 * and on exception. Generated constructors delegate to the same populate method.
 */
public final class AIsSmartDataObjectFactory {
    private static final class AIcContext {
        final IdentityHashMap<Object, Object> objects = new IdentityHashMap<>();
    }
    private static final ThreadLocal<AIcContext> CURRENT = new ThreadLocal<>();
    private static final ClassValue<Constructor<?>> CONSTRUCTORS = new ClassValue<>() {
        @Override protected Constructor<?> computeValue(Class<?> aType) {
            try {
                Constructor<?> locConstructor = aType.getConstructor(Map.class);
                if (!AIcSmartDataObject.class.isAssignableFrom(aType))
                    throw new IllegalArgumentException("Not a concrete SmartDataObject: " + aType);
                return locConstructor;
            } catch (NoSuchMethodException aException) {
                throw new IllegalArgumentException("Missing public generated Map constructor: " + aType, aException);
            }
        }
    };

    /** Construct a complete, shared-reference-preserving SDO graph from typed mapping nodes. */
    public static <T extends AIcSmartDataObject> T fromMapping(
            Class<? extends T> aClass, Map<String, AIidVirtualFieldValue> aMapping) {
        Objects.requireNonNull(aClass, "concrete class");
        Objects.requireNonNull(aMapping, "mapping");
        var locContext = CURRENT.get();
        boolean locRoot = locContext == null;
        if (locRoot) { locContext = new AIcContext(); CURRENT.set(locContext); }
        try {
            Object locExisting = locContext.objects.get(aMapping);
            if (locExisting != null) return aClass.cast(locExisting);
            return aClass.cast(instantiate(aClass, aMapping));
        } finally {
            if (locRoot) CURRENT.remove();
        }
    }

    private static Object instantiate(Class<?> aClass, Map<String, AIidVirtualFieldValue> aMapping) {
        try {
            return CONSTRUCTORS.get(aClass).newInstance(aMapping);
        } catch (InstantiationException | IllegalAccessException aException) {
            throw new IllegalStateException("Cannot instantiate SDO " + aClass, aException);
        } catch (InvocationTargetException aException) {
            Throwable locCause = aException.getCause();
            if (locCause instanceof RuntimeException locRuntime) throw locRuntime;
            throw new IllegalStateException("SDO constructor failed: " + aClass, locCause);
        }
    }

    /** Called by the base SDO Map constructor while {@code this} already exists. */
    static void populate(AIcSmartDataObject aObject, Map<String, AIidVirtualFieldValue> aMapping) {
        Objects.requireNonNull(aMapping, "mapping");
        AIcContext locContext = CURRENT.get();
        boolean locRoot = locContext == null;
        if (locRoot) { locContext = new AIcContext(); CURRENT.set(locContext); }
        try {
            Object locPrior = locContext.objects.putIfAbsent(aMapping, aObject);
            if (locPrior != null && locPrior != aObject)
                throw new IllegalArgumentException("Mapping already represents a different SDO instance");
            for (var locField : aMapping.entrySet()) {
                var locDescriptor = aObject.metadata().names().get(locField.getKey());
                if (locDescriptor == null)
                    throw new IllegalArgumentException("Unknown SDO property: " + locField.getKey());
                Object locValue = resolve(locField.getValue(), locContext);
                Class<?> locDeclared = locDescriptor.getter().getReturnType();
                if (locValue != null && !wrap(locDeclared).isInstance(locValue))
                    throw new IllegalArgumentException("Wrong mapping type for " + locField.getKey()
                            + ": expected " + locDeclared + ", got " + locValue.getClass());
                aObject.set_RawField(locField.getKey(), locValue);
            }
        } finally {
            if (locRoot) CURRENT.remove();
        }
    }

    private static Class<?> wrap(Class<?> aType) {
        if (!aType.isPrimitive()) return aType;
        return Map.of(boolean.class, Boolean.class, byte.class, Byte.class, short.class, Short.class,
                int.class, Integer.class, long.class, Long.class, float.class, Float.class,
                double.class, Double.class, char.class, Character.class).get(aType);
    }

    @SuppressWarnings("unchecked")
    private static Object resolve(AIidVirtualFieldValue aField, AIcContext aContext) {
        Objects.requireNonNull(aField, "virtual field wrapper");
        Object locSource = aField.getValueRepresentation();
        if (locSource == null) return null;
        Object locExisting = aContext.objects.get(locSource);
        if (locExisting != null) return locExisting;
        if (AIcSmartDataObject.class.isAssignableFrom(aField.getValueClass())) {
            if (!(locSource instanceof Map<?, ?>))
                throw new IllegalArgumentException("SDO node must contain a typed mapping");
            return fromMapping((Class<? extends AIcSmartDataObject>) aField.getValueClass(),
                    (Map<String, AIidVirtualFieldValue>) locSource);
        }
        if (locSource instanceof List<?> locList) {
            List<Object> locOutput = new ArrayList<>();
            aContext.objects.put(locSource, locOutput);
            for (Object locElement : locList) locOutput.add(resolve((AIidVirtualFieldValue) locElement, aContext));
            return locOutput;
        }
        if (locSource instanceof Map<?, ?> locMap) {
            Map<Object, Object> locOutput = new LinkedHashMap<>();
            aContext.objects.put(locSource, locOutput);
            for (var locEntry : locMap.entrySet()) locOutput.put(
                    resolve((AIidVirtualFieldValue) locEntry.getKey(), aContext),
                    resolve((AIidVirtualFieldValue) locEntry.getValue(), aContext));
            return locOutput;
        }
        return locSource;
    }
    private AIsSmartDataObjectFactory() { }
}

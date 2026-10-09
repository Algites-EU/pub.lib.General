package eu.algites.lib.data.smartdataobject;

import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIiDataObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Package-private schema metadata resolver, cached by implementation class. */
final class AIcSmartDataMetadata {
    record AIcdField(String name, AIaDataObjectField annotation, Method getter, Method defaultFactory) {
        Object createDefault() {
            if (defaultFactory == null) return null;
            try { return defaultFactory.invoke(null); }
            catch (IllegalAccessException | InvocationTargetException aException) {
                throw new IllegalStateException("Default factory failed for " + name, aException);
            }
        }
        boolean hasDefault() { return defaultFactory != null; }
    }
    record AIcdDescriptor(AIaDataObject schema, List<AIcdField> fields, Map<String, AIcdField> names) { }
    private static final ClassValue<AIcdDescriptor> CACHE = new ClassValue<>() {
        @Override protected AIcdDescriptor computeValue(Class<?> aType) { return scan(aType); }
    };
    static AIcdDescriptor forType(Class<?> aType) { return CACHE.get(aType); }
    static AIcdDescriptor forReadContract(Class<?> aType) { return CACHE.get(aType); }
    private static AIcdDescriptor scan(Class<?> aType) {
        Map<String, AIcdField> locFields = new LinkedHashMap<>();
        List<Class<?>> locTypes = new ArrayList<>();
        collect(aType, locTypes);
        AIaDataObject locSchema = null;
        for (Class<?> locType : locTypes) {
            AIaDataObject locFoundSchema = locType.getDeclaredAnnotation(AIaDataObject.class);
            if (locFoundSchema != null && locSchema == null) locSchema = locFoundSchema;
            for (Method locGetter : locType.getDeclaredMethods()) {
                AIaDataObjectField locAnnotation = locGetter.getDeclaredAnnotation(AIaDataObjectField.class);
                if (locAnnotation == null) continue;
                if (locGetter.getParameterCount() != 0 || locGetter.getReturnType() == void.class)
                    throw new IllegalArgumentException("SmartData field must annotate a zero-argument getter: " + locGetter);
                Method locFactory = null;
                if (!locAnnotation.defaultFactoryMethod().isBlank()) {
                    try { locFactory = locType.getMethod(locAnnotation.defaultFactoryMethod()); }
                    catch (NoSuchMethodException aException) {
                        throw new IllegalArgumentException("Missing static default factory: " + locGetter, aException);
                    }
                    if (!Modifier.isStatic(locFactory.getModifiers()) || !locGetter.getReturnType().isAssignableFrom(locFactory.getReturnType()))
                        throw new IllegalArgumentException("Invalid default factory: " + locFactory);
                }
                var locField = new AIcdField(locAnnotation.name(), locAnnotation, locGetter, locFactory);
                AIcdField locBefore = locFields.putIfAbsent(locAnnotation.name(), locField);
                if (locBefore != null && (!locBefore.getter().getReturnType().equals(locGetter.getReturnType()) ||
                    !locBefore.annotation().equals(locAnnotation)))
                    throw new IllegalArgumentException("Conflicting inherited SmartData field: " + locAnnotation.name());
            }
        }
        if (locFields.isEmpty()) throw new IllegalArgumentException("No annotated read-only SmartData contract: " + aType);
        return new AIcdDescriptor(locSchema, List.copyOf(locFields.values()),
                Collections.unmodifiableMap(new LinkedHashMap<>(locFields)));
    }
    private static void collect(Class<?> aType, List<Class<?>> aTypes) {
        if (aType == null || aType == Object.class || aTypes.contains(aType)) return;
        aTypes.add(aType);
        for (Class<?> locInterface : aType.getInterfaces()) collect(locInterface, aTypes);
        collect(aType.getSuperclass(), aTypes);
    }
    private AIcSmartDataMetadata() { }
}

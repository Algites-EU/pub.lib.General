package eu.algites.lib.data.smartdataobject.impl.standard;


import eu.algites.lib.data.smartdataobject.AIaSmartDataObject;
import eu.algites.lib.data.smartdataobject.AIaSmartDataObjectField;
import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AIaFieldDefault;
import eu.algites.lib.data.dataobject.AIiFieldDefaultValueResolverFactory;
import eu.algites.lib.data.dataobject.AIiFieldDefaultValueResolver;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObject;
import eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField;
import eu.algites.lib.factory.cpdiscovery.AIsCategoryProviderDiscovery;
import eu.algites.lib.data.dataobject.AIiDataObject;
import eu.algites.lib.data.dataobject.AInFieldPresenceKind;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Package-private schema metadata resolver, cached by implementation class. */
final class AIcSmartDataMetadata {
    record AIcdField(String name, AIaDataObjectField annotation, AIaXmlDataObjectField xml,
            Method getter, AIiFieldDefaultValueResolver defaultResolver, boolean allowNonPresentRead) {
        Object createDefault() {
            if (defaultResolver == null) return null;
            Object locValue = defaultResolver.resolveDefault(getter.getDeclaringClass(), getter);
            if (locValue != null && !getter.getReturnType().isInstance(locValue))
                throw new IllegalStateException("Default resolver returned incompatible type for " + name);
            return locValue;
        }
        boolean hasDefault() { return defaultResolver != null; }
        boolean isXmlAttribute() {
            return xml != null && xml.valueKind() == eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ATTRIBUTE;
        }
        String xmlLocalName() { return xml == null || xml.namespaceLocalName().isBlank() ? name : xml.namespaceLocalName(); }
        String xmlNamespace() { return xml == null ? "" : xml.namespace(); }
    }
    record AIcdDescriptor(AIaDataObject schema, AIaXmlDataObject xmlSchema,
            List<AIcdField> fields, Map<String, AIcdField> names) { }
    private static final ClassValue<AIcdDescriptor> CACHE = new ClassValue<>() {
        /** {@inheritDoc} */
        @Override protected AIcdDescriptor computeValue(Class<?> aType) { return scan(aType); }
    };
    static AIcdDescriptor forType(Class<?> aType) { return CACHE.get(aType); }
    static AIcdDescriptor forReadContract(Class<?> aType) { return CACHE.get(aType); }
    private static AIcdDescriptor scan(Class<?> aType) {
        Map<String, AIcdField> locFields = new LinkedHashMap<>();
        List<Class<?>> locTypes = new ArrayList<>();
        collect(aType, locTypes);
        AIaDataObject locSchema = null;
        AIaXmlDataObject locXmlSchema = null;
        for (Class<?> locType : locTypes) {
            AIaDataObject locFoundSchema = locType.getDeclaredAnnotation(AIaDataObject.class);
            if (locFoundSchema != null && locSchema == null) locSchema = locFoundSchema;
            AIaXmlDataObject locFoundXmlSchema = locType.getDeclaredAnnotation(AIaXmlDataObject.class);
            if (locFoundXmlSchema != null && locXmlSchema == null) locXmlSchema = locFoundXmlSchema;
            Method[] locMethods = locType.getDeclaredMethods();
            /* Java reflection does not guarantee declaration order. Generated XML
             * contracts carry an explicit order on each declaring interface;
             * sorting per interface also keeps inherited fields before extensions. */
            if (java.util.Arrays.stream(locMethods).anyMatch(aMethod -> {
                AIaXmlDataObjectField locXml = aMethod.getDeclaredAnnotation(AIaXmlDataObjectField.class);
                return locXml != null && locXml.order() >= 0;
            })) {
                java.util.Arrays.sort(locMethods, java.util.Comparator
                        .comparingInt((Method aMethod) -> {
                            AIaXmlDataObjectField locXml = aMethod.getDeclaredAnnotation(AIaXmlDataObjectField.class);
                            return locXml == null || locXml.order() < 0 ? Integer.MAX_VALUE : locXml.order();
                        }).thenComparing(Method::getName));
            }
            for (Method locGetter : locMethods) {
                AIaDataObjectField locAnnotation = locGetter.getDeclaredAnnotation(AIaDataObjectField.class);
                if (locAnnotation == null) continue;
                if (locGetter.getParameterCount() != 0 || locGetter.getReturnType() == void.class)
                    throw new IllegalArgumentException("SmartData field must annotate a zero-argument getter: " + locGetter);
                AIaFieldDefault locDefault = locGetter.getDeclaredAnnotation(AIaFieldDefault.class);
                AIiFieldDefaultValueResolver locResolver = locDefault == null ? null :
                        AIsCategoryProviderDiscovery.getInstance(locDefault.defaultValueResolver(),
                            AIiFieldDefaultValueResolverFactory.class, "default", null);
                String locName = locAnnotation.fieldName().isBlank() ? derivedFieldName(locGetter)
                        : locAnnotation.fieldName();
                AIaXmlDataObjectField locXmlField = locGetter.getDeclaredAnnotation(AIaXmlDataObjectField.class);
                AIaSmartDataObjectField locFieldPolicy = locGetter.getDeclaredAnnotation(AIaSmartDataObjectField.class);
                AIaSmartDataObject locTypePolicy = locType.getDeclaredAnnotation(AIaSmartDataObject.class);
                boolean locAllowNonPresentRead = locFieldPolicy != null
                        ? locFieldPolicy.allowNonPresentRead()
                        : locTypePolicy != null && locTypePolicy.allowNonPresentRead();
                if (locAllowNonPresentRead && locGetter.getReturnType().isPrimitive())
                    throw new IllegalArgumentException("A primitive SmartData getter cannot return null: " + locGetter);
                var locField = new AIcdField(locName, locAnnotation, locXmlField, locGetter, locResolver, locAllowNonPresentRead);
                AIcdField locBefore = locFields.get(locName);
                if (locBefore == null) {
                    locFields.put(locName, locField);
                } else if (locGetter.getDeclaringClass().isAssignableFrom(locBefore.getter().getDeclaringClass())) {
                    validateRestriction(locField, locBefore);
                } else if (locBefore.getter().getDeclaringClass().isAssignableFrom(locGetter.getDeclaringClass())) {
                    validateRestriction(locBefore, locField);
                    locFields.put(locName, locField);
                } else if (!locBefore.getter().getReturnType().equals(locGetter.getReturnType()) ||
                        !locBefore.annotation().equals(locAnnotation) ||
                        locBefore.allowNonPresentRead() != locAllowNonPresentRead) {
                    throw new IllegalArgumentException("Conflicting unrelated inherited SmartData field: " + locName);
                }
            }
        }
        if (locFields.isEmpty()) throw new IllegalArgumentException("No annotated read-only SmartData contract: " + aType);
        return new AIcdDescriptor(locSchema, locXmlSchema, List.copyOf(locFields.values()),
                Collections.unmodifiableMap(new LinkedHashMap<>(locFields)));
    }
    private static void validateRestriction(AIcdField aBase, AIcdField aDerived) {
        var locParent = aBase.annotation();
        var locChild = aDerived.annotation();
        if (locParent.presence() == AInFieldPresenceKind.REQUIRED &&
                locChild.presence() != AInFieldPresenceKind.REQUIRED ||
            locParent.presence() == AInFieldPresenceKind.FORBIDDEN &&
                locChild.presence() != AInFieldPresenceKind.FORBIDDEN)
            throw new IllegalArgumentException("Invalid presence restriction for " + aDerived.name());
        if (!locParent.allowsNull() && locChild.allowsNull())
            throw new IllegalArgumentException("Restriction cannot introduce null for " + aDerived.name());
        if (!aBase.getter().getReturnType().isAssignableFrom(aDerived.getter().getReturnType()))
            throw new IllegalArgumentException("Incompatible derived field return type: " + aDerived.name());
    }

    private static String derivedFieldName(Method aGetter) {
        String locMethod = aGetter.getName();
        if (!locMethod.startsWith("get") || locMethod.length() <= 3 || !Character.isUpperCase(locMethod.charAt(3)))
            throw new IllegalArgumentException("Cannot derive field name from getter: " + aGetter);
        return locMethod.substring(3);
    }
    private static void collect(Class<?> aType, List<Class<?>> aTypes) {
        if (aType == null || aType == Object.class || aTypes.contains(aType)) return;
        aTypes.add(aType);
        for (Class<?> locInterface : aType.getInterfaces()) collect(locInterface, aTypes);
        collect(aType.getSuperclass(), aTypes);
    }
    private AIcSmartDataMetadata() { }
}

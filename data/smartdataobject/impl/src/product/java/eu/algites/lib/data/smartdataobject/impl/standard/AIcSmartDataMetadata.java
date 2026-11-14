package eu.algites.lib.data.smartdataobject.impl.standard;


import eu.algites.lib.data.smartdataobject.AIaSmartDataObject;
import eu.algites.lib.data.smartdataobject.AIaSmartDataObjectField;
import eu.algites.lib.data.dataobject.AIaDataObject;
import eu.algites.lib.data.dataobject.AIaDataObjectField;
import eu.algites.lib.data.dataobject.AInDataObjectFieldDefaultKind;
import eu.algites.lib.data.dataobject.AIaDataObjectFieldDefault;
import eu.algites.lib.data.dataobject.AIaDataObjectFieldStringConstraints;
import eu.algites.lib.data.dataobject.AIaDataObjectFieldNumericConstraints;
import eu.algites.lib.data.dataobject.AIaDataObjectFieldListConstraints;
import eu.algites.lib.data.dataobject.AIaDataObjectFieldObjectConstraints;
import eu.algites.lib.data.dataobject.AIaFieldDefault;
import eu.algites.lib.data.dataobject.AIaKeyDefinition;
import eu.algites.lib.data.dataobject.AIiFieldDefaultValueResolverFactory;
import eu.algites.lib.data.dataobject.AIiFieldDefaultValueResolver;
import eu.algites.lib.data.dataobject.jsondefs.AIaJsonDataObject;
import eu.algites.lib.data.dataobject.yamldefs.AIaYamlDataObject;
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
    /** Normalized XML type declaration, independent of annotation API generation. */
    record AIcdXmlSchema(eu.algites.lib.data.dataobject.xmldefs.AInXmlObjectDefinitionKind definitionKind,
            String namespace, String namespaceLocalName) { }

    /** Normalized XML getter metadata, preserving old and new annotation semantics. */
    record AIcdXmlField(String namespace, String namespaceLocalName,
            eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind valueKind,
            int order, String wildcardNamespace, String wildcardProcessContents) { }

    /**
     * Resolved, class-cached field metadata. Annotation references are resolved
     * once during {@link #scan(Class)}; validation and indexed collections do
     * not need to rediscover them from getters at runtime.
     *
     * @param name semantic field identity
     * @param wireName external field name
     * @param annotation effective neutral DataObject field annotation
     * @param xml resolved format-specific XML wire mapping, if applicable
     * @param getter effective read-only getter
     * @param defaultResolver optional discovered default provider
     * @param allowNonPresentRead read policy after inheritance resolution
     * @param stringConstraints effective string constraints, if declared
     * @param numericConstraints effective numeric constraints, if declared
     * @param listConstraints effective list constraints, if declared
     * @param objectConstraints effective map/object constraints, if declared
     * @param keyDefinitions effective indexed-list definitions
     */
    record AIcdField(String name, String wireName, AIaDataObjectField annotation, AIcdXmlField xml,
            Method getter, AIiFieldDefaultValueResolver defaultResolver, boolean allowNonPresentRead,
            AIaDataObjectFieldStringConstraints stringConstraints,
            AIaDataObjectFieldNumericConstraints numericConstraints,
            AIaDataObjectFieldListConstraints listConstraints,
            AIaDataObjectFieldObjectConstraints objectConstraints,
            List<AIaKeyDefinition> keyDefinitions) {
        Object createDefault() {
            AIaDataObjectFieldDefault declaration = annotation.defaultValue();
            if (declaration.defined()) return lexicalDefault(declaration.value(), getter.getReturnType());
            if (defaultResolver == null) return null;
            Object resolved = defaultResolver.resolveDefault(getter.getDeclaringClass(), getter);
            if (resolved != null && !getter.getReturnType().isInstance(resolved))
                throw new IllegalStateException("Default resolver returned incompatible type for " + name);
            return resolved;
        }
        boolean hasDefault() {
            return (annotation.defaultValue().defined()
                    && annotation.defaultValue().kind() == AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_NOT_PRESENT)
                    || defaultResolver != null;
        }
        boolean hasEmptyDefault() {
            return annotation.defaultValue().defined()
                && annotation.defaultValue().kind() == AInDataObjectFieldDefaultKind.IF_FIELD_VALUE_EMPTY;
        }
        boolean hasFixed() {
            return annotation.defaultValue().defined()
                && annotation.defaultValue().kind() == AInDataObjectFieldDefaultKind.CONSTRAINT_DEFAULT;
        }
        Object fixedValue() { return lexicalDefault(annotation.defaultValue().value(), getter.getReturnType()); }
        void validateValue(Object value) {
            if (hasFixed() && !Objects.equals(value, fixedValue()))
                throw new IllegalArgumentException("Field " + name + " must equal its fixed value");
            AIaDataObjectFieldStringConstraints string = stringConstraints;
            if (string != null && value instanceof String text) {
                int len = text.codePointCount(0, text.length());
                if (string.minLength() >= 0 && len < string.minLength()
                        || string.maxLength() >= 0 && len > string.maxLength()
                        || !string.pattern().isEmpty() && !java.util.regex.Pattern.compile(string.pattern()).matcher(text).matches())
                    throw new IllegalArgumentException("String constraint violated for " + name);
            }
            AIaDataObjectFieldNumericConstraints numeric = numericConstraints;
            if (numeric != null && value instanceof Number number) {
                var decimal = new java.math.BigDecimal(number.toString());
                if (!numeric.minimum().isEmpty()) {
                    int compare = decimal.compareTo(new java.math.BigDecimal(numeric.minimum()));
                    if (compare < 0 || numeric.exclusiveMinimum() && compare == 0)
                        throw new IllegalArgumentException("Numeric minimum violated for " + name);
                }
                if (!numeric.maximum().isEmpty()) {
                    int compare = decimal.compareTo(new java.math.BigDecimal(numeric.maximum()));
                    if (compare > 0 || numeric.exclusiveMaximum() && compare == 0)
                        throw new IllegalArgumentException("Numeric maximum violated for " + name);
                }
                if (!numeric.multipleOf().isEmpty()
                        && decimal.remainder(new java.math.BigDecimal(numeric.multipleOf())).signum() != 0)
                    throw new IllegalArgumentException("Numeric multipleOf violated for " + name);
            }
            AIaDataObjectFieldListConstraints list = listConstraints;
            if (list != null && value instanceof java.util.Collection<?> collection) {
                if (list.minItems() >= 0 && collection.size() < list.minItems()
                        || list.maxItems() >= 0 && collection.size() > list.maxItems()
                        || list.uniqueItems() && new java.util.HashSet<>(collection).size() != collection.size())
                    throw new IllegalArgumentException("List constraint violated for " + name);
            }
            AIaDataObjectFieldObjectConstraints object = objectConstraints;
            if (object != null && value instanceof java.util.Map<?, ?> map) {
                if (object.minProperties() >= 0 && map.size() < object.minProperties()
                        || object.maxProperties() >= 0 && map.size() > object.maxProperties())
                    throw new IllegalArgumentException("Object constraint violated for " + name);
            }
        }
        boolean isXmlAttribute() {
            return xml != null && xml.valueKind() == eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ATTRIBUTE;
        }
        String xmlLocalName() { return xml == null || xml.namespaceLocalName().isBlank() ? wireName : xml.namespaceLocalName(); }
        String xmlNamespace() { return xml == null ? "" : xml.namespace(); }
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object lexicalDefault(String value, Class<?> type) {
        if (type == String.class || type == Object.class || CharSequence.class == type) return value;
        if (type == int.class || type == Integer.class) return Integer.valueOf(value);
        if (type == long.class || type == Long.class) return Long.valueOf(value);
        if (type == double.class || type == Double.class) return Double.valueOf(value);
        if (type == float.class || type == Float.class) return Float.valueOf(value);
        if (type == short.class || type == Short.class) return Short.valueOf(value);
        if (type == byte.class || type == Byte.class) return Byte.valueOf(value);
        if (type == boolean.class || type == Boolean.class) {
            if (!value.equals("true") && !value.equals("false") && !value.equals("1") && !value.equals("0"))
                throw new IllegalArgumentException("Invalid XML boolean default: " + value);
            return value.equals("true") || value.equals("1");
        }
        if (type == java.math.BigDecimal.class) return new java.math.BigDecimal(value);
        if (type == java.math.BigInteger.class) return new java.math.BigInteger(value);
        if (type.isEnum()) {
            for (Object constant : type.getEnumConstants()) {
                if (((Enum<?>) constant).name().equals(value) || String.valueOf(constant).equals(value))
                    return constant;
                try {
                    var code = type.getMethod("code").invoke(constant);
                    if (String.valueOf(code).equals(value)) return constant;
                } catch (ReflectiveOperationException ignored) { /* Ordinary enums have no code(). */ }
            }
            throw new IllegalArgumentException("No enum constant for default: " + value);
        }
        throw new IllegalArgumentException("Unsupported default value type " + type.getName());
    }

    /** Exact format-bound schema metadata, if the contract declares it. */
    record AIcdSchemaWire(String schemaId, int schemaVersion, String schemaUrl,
            String dataSourceSchemaUrlField, String dataSourceSchemaIdentifierField,
            String dataSourceSchemaVersionField) { }
    record AIcdDescriptor(AIaDataObject schema, AIcdXmlSchema xmlSchema,
            AIcdSchemaWire formatSchema, AIcdSchemaWire yamlFormatSchema,
            List<AIcdField> fields, Map<String, AIcdField> names) {
        /** Select the format-specific schema metadata; fallback only for single-format contracts. */
        AIcdSchemaWire formatSchema(boolean yaml) {
            return yaml && yamlFormatSchema != null ? yamlFormatSchema : formatSchema;
        }
    }
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
        AIcdXmlSchema locXmlSchema = null;
        AIcdSchemaWire locFormatSchema = null;
        AIcdSchemaWire locYamlFormatSchema = null;
        for (Class<?> locType : locTypes) {
            AIaDataObject locFoundSchema = locType.getDeclaredAnnotation(AIaDataObject.class);
            if (locFoundSchema != null && locSchema == null) locSchema = locFoundSchema;
            if (locFormatSchema == null) {
                AIaJsonDataObject json = locType.getDeclaredAnnotation(AIaJsonDataObject.class);
                AIaYamlDataObject yaml = locType.getDeclaredAnnotation(AIaYamlDataObject.class);
                if (json != null) locFormatSchema = new AIcdSchemaWire(json.schemaId(),
                        json.schemaVersion(), json.schemaUrl(), json.dataSourceSchemaUrlField(),
                        json.dataSourceSchemaIdentifierField(), json.dataSourceSchemaVersionField());
                if (yaml != null) locYamlFormatSchema = new AIcdSchemaWire(yaml.schemaId(),
                        yaml.schemaVersion(), yaml.schemaUrl(), yaml.dataSourceSchemaUrlField(),
                        yaml.dataSourceSchemaIdentifierField(), yaml.dataSourceSchemaVersionField());
                var modern = locType.getDeclaredAnnotation(
                        eu.algites.lib.data.dataobject.jsonschema.AIaDataObjectJsonSchema.class);
                if (modern != null) {
                    var normalized = new AIcdSchemaWire(modern.schemaId(), modern.schemaVersion(),
                            modern.schemaUrl(), modern.dataSourceSchemaUrlField(),
                            modern.dataSourceSchemaIdentifierField(), modern.dataSourceSchemaVersionField());
                    if (eu.algites.lib.data.dataobject.yamldefs.AIiYamlDataObject.class.isAssignableFrom(locType))
                        locYamlFormatSchema = normalized;
                    else locFormatSchema = normalized;
                }
            }
            var locNewXmlSchema = locType.getDeclaredAnnotation(
                    eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectXmlSchema.class);
            AIaXmlDataObject locFoundXmlSchema = locType.getDeclaredAnnotation(AIaXmlDataObject.class);
            if (locXmlSchema == null) {
                if (locNewXmlSchema != null) locXmlSchema = new AIcdXmlSchema(
                        locNewXmlSchema.definitionKind(), locNewXmlSchema.namespace(),
                        locNewXmlSchema.namespaceLocalName());
                else if (locFoundXmlSchema != null) locXmlSchema = new AIcdXmlSchema(
                        locFoundXmlSchema.definitionKind(), locFoundXmlSchema.namespace(),
                        locFoundXmlSchema.namespaceLocalName());
            }
        }
        if (locFormatSchema == null) locFormatSchema = locYamlFormatSchema;
        Collections.reverse(locTypes);
        for (Class<?> locType : locTypes) {
            Method[] locMethods = locType.getDeclaredMethods();
            /* Java reflection does not guarantee declaration order. Generated XML
             * contracts carry an explicit order on each declaring interface;
             * sorting per interface also keeps inherited fields before extensions. */
            if (java.util.Arrays.stream(locMethods).anyMatch(aMethod -> {
                AIcdXmlField locXml = xmlField(aMethod);
                return locXml != null && locXml.order() >= 0 || aMethod.getDeclaredAnnotation(AIaDataObjectField.class) != null
                    && aMethod.getDeclaredAnnotation(AIaDataObjectField.class).order() >= 0;
            })) {
                java.util.Arrays.sort(locMethods, java.util.Comparator
                        .comparingInt((Method aMethod) -> {
                            AIcdXmlField locXml = xmlField(aMethod);
                            AIaDataObjectField field = aMethod.getDeclaredAnnotation(AIaDataObjectField.class);
                            return field != null && field.order() >= 0 ? field.order()
                                : locXml == null || locXml.order() < 0 ? Integer.MAX_VALUE : locXml.order();
                        }).thenComparing(Method::getName));
            }
            for (Method locGetter : locMethods) {
                /* Covariant overrides produce synthetic bridge getters. Javac copies
                 * runtime method annotations onto these bridge declarations, whose
                 * erased return types are not independent field restrictions.
                 * Only the real getter may contribute contract metadata. */
                if (locGetter.isBridge() || locGetter.isSynthetic()) continue;
                AIaDataObjectField locAnnotation = locGetter.getDeclaredAnnotation(AIaDataObjectField.class);
                if (locAnnotation == null) continue;
                if (locGetter.getParameterCount() != 0 || locGetter.getReturnType() == void.class)
                    throw new IllegalArgumentException("SmartData field must annotate a zero-argument getter: " + locGetter);
                AIaFieldDefault locDefault = locGetter.getDeclaredAnnotation(AIaFieldDefault.class);
                AIiFieldDefaultValueResolver locResolver = locDefault == null ? null :
                        AIsCategoryProviderDiscovery.getInstance(locDefault.defaultValueResolver(),
                            AIiFieldDefaultValueResolverFactory.class, "default", null);
                String locName = derivedFieldName(locGetter);
                AIcdField locBefore = locFields.get(locName);
                String locExplicitDataName = locAnnotation.fieldName();
                if (locAnnotation.embedded() && !locExplicitDataName.isEmpty())
                    throw new IllegalArgumentException("Embedded DataObject field cannot have fieldName: " + locName);
                if (locBefore != null && locBefore.annotation().embedded() != locAnnotation.embedded())
                    throw new IllegalArgumentException("Overridden field changes embedded semantics: " + locName);
                if (locBefore != null && !locExplicitDataName.isBlank()
                        && !locBefore.wireName().equals(locExplicitDataName))
                    throw new IllegalArgumentException("An overridden field cannot change fieldName: " + locName);
                String locWireName = locAnnotation.embedded() ? "" : locExplicitDataName.isBlank()
                        ? locBefore == null ? locName : locBefore.wireName() : locExplicitDataName;
                AIcdXmlField locXmlField = xmlField(locGetter);
                AIaSmartDataObjectField locFieldPolicy = locGetter.getDeclaredAnnotation(AIaSmartDataObjectField.class);
                AIaSmartDataObject locTypePolicy = locType.getDeclaredAnnotation(AIaSmartDataObject.class);
                boolean locAllowNonPresentRead = locFieldPolicy != null
                        ? locFieldPolicy.allowNonPresentRead()
                        : locTypePolicy != null && locTypePolicy.allowNonPresentRead();
                if (locAllowNonPresentRead && locGetter.getReturnType().isPrimitive())
                    throw new IllegalArgumentException("A primitive SmartData getter cannot return null: " + locGetter);
                // Materialize all method annotations only during the ClassValue
                // scan. A covariant override may omit a constraint or key
                // declaration: in that case retain the inherited declaration.
                var locString = locGetter.getDeclaredAnnotation(AIaDataObjectFieldStringConstraints.class);
                var locNumeric = locGetter.getDeclaredAnnotation(AIaDataObjectFieldNumericConstraints.class);
                var locList = locGetter.getDeclaredAnnotation(AIaDataObjectFieldListConstraints.class);
                var locObject = locGetter.getDeclaredAnnotation(AIaDataObjectFieldObjectConstraints.class);
                List<AIaKeyDefinition> locKeys = List.of(locGetter.getAnnotationsByType(AIaKeyDefinition.class));
                var locField = new AIcdField(locName, locWireName, locAnnotation, locXmlField, locGetter,
                        locResolver, locAllowNonPresentRead,
                        locString != null || locBefore == null ? locString : locBefore.stringConstraints(),
                        locNumeric != null || locBefore == null ? locNumeric : locBefore.numericConstraints(),
                        locList != null || locBefore == null ? locList : locBefore.listConstraints(),
                        locObject != null || locBefore == null ? locObject : locBefore.objectConstraints(),
                        !locKeys.isEmpty() || locBefore == null ? locKeys : locBefore.keyDefinitions());
                if (locBefore == null) {
                    locFields.put(locName, locField);
                } else if (locBefore.getter().getDeclaringClass().isAssignableFrom(locGetter.getDeclaringClass())) {
                    validateRestriction(locBefore, locField);
                    locFields.put(locName, locField);
                } else if (locGetter.getDeclaringClass().isAssignableFrom(locBefore.getter().getDeclaringClass())) {
                    validateRestriction(locField, locBefore);
                } else if (!locBefore.getter().getReturnType().equals(locGetter.getReturnType())
                        || !locBefore.wireName().equals(locField.wireName())
                        || !locBefore.annotation().equals(locField.annotation())
                        || locBefore.allowNonPresentRead() != locAllowNonPresentRead) {
                    throw new IllegalArgumentException("Conflicting unrelated inherited SmartData field: " + locName);
                }
            }
        }
        // A format-specific override must not redeclare AIaDataObjectField.
        // It contributes only XML QName metadata to the annotated getter in
        // the common AIig contract. Join these two declarations by method name.
        for (Class<?> locType : locTypes) {
            for (Method locMethod : locType.getDeclaredMethods()) {
                if (locMethod.isBridge() || locMethod.isSynthetic()) continue;
                AIcdXmlField locWire = xmlField(locMethod);
                if (locWire == null) continue;
                for (var entry : locFields.entrySet()) {
                    AIcdField original = entry.getValue();
                    if (!original.getter().getName().equals(locMethod.getName())) continue;
                    if (original.getter().getParameterCount() != locMethod.getParameterCount()) continue;
                    entry.setValue(new AIcdField(original.name(), original.wireName(),
                            original.annotation(), locWire, original.getter(),
                            original.defaultResolver(), original.allowNonPresentRead(),
                            original.stringConstraints(), original.numericConstraints(),
                            original.listConstraints(), original.objectConstraints(),
                            original.keyDefinitions()));
                }
            }
        }
        Map<String, String> locByWireName = new LinkedHashMap<>();
        locFields.forEach((aSemantic, aField) -> {
            if (aField.annotation().embedded()) return;
            String locPrevious = locByWireName.putIfAbsent(aField.wireName(), aSemantic);
            if (locPrevious != null && !locPrevious.equals(aSemantic))
                throw new IllegalArgumentException("Duplicate fieldName '" + aField.wireName()
                        + "' for semantic fields " + locPrevious + " and " + aSemantic);
        });
        if (locFields.isEmpty()) throw new IllegalArgumentException("No annotated read-only SmartData contract: " + aType);
        return new AIcdDescriptor(locSchema, locXmlSchema, locFormatSchema, locYamlFormatSchema,
                List.copyOf(locFields.values()),
                Collections.unmodifiableMap(new LinkedHashMap<>(locFields)));
    }
    /** Resolve XML wire metadata from the new annotation, with legacy fallback. */
    private static AIcdXmlField xmlField(Method getter) {
        var modern = getter.getDeclaredAnnotation(
                eu.algites.lib.data.dataobject.xmlschema.AIaDataObjectFieldXmlSchema.class);
        if (modern != null) return new AIcdXmlField(modern.namespace(), modern.namespaceLocalName(),
                modern.valueKind(), modern.order(), modern.wildcardNamespace(),
                modern.wildcardProcessContents());
        var legacy = getter.getDeclaredAnnotation(AIaXmlDataObjectField.class);
        return legacy == null ? null : new AIcdXmlField(legacy.namespace(),
                legacy.namespaceLocalName(), legacy.valueKind(), legacy.order(),
                legacy.wildcardNamespace(), legacy.wildcardProcessContents());
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
        if (locParent.embedded() != locChild.embedded())
            throw new IllegalArgumentException("Restriction cannot change embedded semantics for " + aDerived.name());
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

package eu.algites.lib.data.smartdataobject.impl.common;

import eu.algites.lib.common.enums.AIiEnumItem;
import eu.algites.lib.data.dataobject.AIiDataObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Format-independent SDO factory and graph materializer. All concrete type
 * choices are delegated to the explicit binding registry, never inferred
 * from generated class names or from the classpath.
 *
 * @author linhart1
 */
public final class AIsSmartDataObjectFactory {
    private AIsSmartDataObjectFactory() { }

    /**
     * Creates a root SDO of the requested read-only contract.
     * @param aReadContract requested read-only interface
     * @param aProperties semantic or canonical data field names and decoded values
     * @param aRegistry selected available implementation bindings
     * @return initialized SDO object
     * @param <T> data contract
     * @throws IllegalArgumentException for ambiguous, incompatible or missing mappings
     */
    public static <T extends AIiDataObject> T fromData(
            Class<T> aReadContract, Map<String, ?> aProperties,
            AIiSmartDataObjectBindingRegistry aRegistry) {
        Objects.requireNonNull(aReadContract, "aReadContract");
        Objects.requireNonNull(aProperties, "aProperties");
        Objects.requireNonNull(aRegistry, "aRegistry");
        var binding = aRegistry.resolveBinding(aReadContract, null, null);
        Object result = materialize(binding, aProperties, aRegistry, new IdentityHashMap<>());
        return aReadContract.cast(result);
    }

    /**
     * Instantiates the exact registered class and populates values.
     * @param aImplementationClass implementation registered in the supplied registry
     * @param aProperties decoded properties
     * @param aRegistry binding registry
     * @return concrete typed instance
     * @param <T> implementation type
     */
    public static <T extends AIcSmartDataObject> T fromImplementationData(
            Class<T> aImplementationClass, Map<String, ?> aProperties,
            AIiSmartDataObjectBindingRegistry aRegistry) {
        Objects.requireNonNull(aImplementationClass, "aImplementationClass");
        AIiSmartDataObjectBinding binding = Objects.requireNonNull(
                aRegistry.getBinding(aImplementationClass), "Implementation class not registered: " + aImplementationClass);
        return aImplementationClass.cast(materialize(binding, aProperties, aRegistry, new IdentityHashMap<>()));
    }

    private static AIcSmartDataObject materialize(AIiSmartDataObjectBinding binding,
            Map<String, ?> data, AIiSmartDataObjectBindingRegistry registry,
            IdentityHashMap<Object, Object> visited) {
        Object existing = visited.get(data);
        if (existing != null) return (AIcSmartDataObject) existing;
        AIcSmartDataObject result = create(binding.getImplementationClass());
        visited.put(data, result);
        Map<String, AIiSmartDataObjectFieldMetadata> byData = new LinkedHashMap<>();
        for (AIiSmartDataObjectFieldMetadata field : binding.getMetadata().getFields()) {
            byData.put(field.getDataFieldName(), field);
            byData.putIfAbsent(field.getSemanticFieldName(), field);
        }
        java.util.Set<String> assignedSemanticFields = new java.util.HashSet<>();
        for (Map.Entry<String, ?> entry : data.entrySet()) {
            AIiSmartDataObjectFieldMetadata field = byData.get(entry.getKey());
            if (field == null) throw new IllegalArgumentException("Unknown data field " + entry.getKey()
                    + " for " + binding.getImplementationClass().getName());
            if (!assignedSemanticFields.add(field.getSemanticFieldName()))
                throw new IllegalArgumentException("Duplicate aliases for semantic field "
                        + field.getSemanticFieldName() + " on " + binding.getImplementationClass().getName());
            Object decoded = convert(entry.getValue(), field.getEffectiveReturnType(),
                    binding, field.getSemanticFieldName(), registry, visited);
            result.populateField(field.getSemanticFieldName(), decoded);
        }
        return result;
    }

    private static AIcSmartDataObject create(Class<? extends AIcSmartDataObject> type) {
        try {
            Constructor<? extends AIcSmartDataObject> constructor = type.getDeclaredConstructor();
            if (!constructor.canAccess(null) && !constructor.trySetAccessible())
                throw new IllegalArgumentException("SDO no-arg constructor inaccessible: " + type.getName());
            return constructor.newInstance();
        } catch (ReflectiveOperationException error) {
            throw new IllegalArgumentException("Cannot instantiate SDO class " + type.getName()
                    + ": a zero-argument constructor is required", error);
        }
    }

    private static Object convert(Object value, Type required, AIiSmartDataObjectBinding owner,
            String field, AIiSmartDataObjectBindingRegistry registry,
            IdentityHashMap<Object, Object> visited) {
        if (value == null) return null;
        if (required instanceof WildcardType wildcard) {
            Type[] upper = wildcard.getUpperBounds();
            return upper.length == 0 ? value : convert(value, upper[0], owner, field, registry, visited);
        }
        if (required instanceof TypeVariable<?> variable) {
            Type[] upper = variable.getBounds();
            return upper.length == 0 ? value : convert(value, upper[0], owner, field, registry, visited);
        }
        if (required instanceof ParameterizedType parameterized) {
            Type raw = parameterized.getRawType();
            if (raw instanceof Class<?> cls && Collection.class.isAssignableFrom(cls) && value instanceof List<?> values) {
                Object existing = visited.get(value);
                if (existing != null) return existing;
                List<Object> result = new ArrayList<>();
                visited.put(value, result);
                Type item = parameterized.getActualTypeArguments()[0];
                for (Object element : values) result.add(convert(element, item, owner, field, registry, visited));
                return result;
            }
            if (raw instanceof Class<?> cls && Map.class.isAssignableFrom(cls) && value instanceof Map<?, ?> values) {
                Object existing = visited.get(value);
                if (existing != null) return existing;
                Map<Object, Object> result = new LinkedHashMap<>();
                visited.put(value, result);
                Type[] args = parameterized.getActualTypeArguments();
                for (Map.Entry<?, ?> entry : values.entrySet())
                    result.put(convert(entry.getKey(), args[0], owner, field, registry, visited),
                            convert(entry.getValue(), args[1], owner, field, registry, visited));
                return result;
            }
            return convert(value, raw, owner, field, registry, visited);
        }
        if (!(required instanceof Class<?> cls)) return value;
        if (AIiDataObject.class.isAssignableFrom(cls) && value instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked") Class<? extends AIiDataObject> contract = (Class<? extends AIiDataObject>) cls;
            var binding = registry.resolveBinding(contract, owner, field);
            // Preserve the identity of the original input map. Copying it loses
            // shared references and breaks cyclic in-memory graphs.
            for (Object key : map.keySet())
                if (!(key instanceof String))
                    throw new IllegalArgumentException("SDO object field names must be strings: " + key);
            @SuppressWarnings("unchecked") Map<String, ?> locValues = (Map<String, ?>) map;
            return materialize(binding, locValues, registry, visited);
        }
        if (cls.isArray() && value instanceof List<?> values) {
            Object result = Array.newInstance(cls.getComponentType(), values.size());
            for (int i = 0; i < values.size(); i++)
                Array.set(result, i, convert(values.get(i), cls.getComponentType(), owner, field, registry, visited));
            return result;
        }
        if (cls.isInstance(value)) return value;
        // XML text nodes and attributes are lexical strings; JSON/YAML numeric
        // nodes already arrive as Number. Apply the same strict conversion for
        // either representation without lossy implicit rounding.
        if (value instanceof String text) {
            if (cls == Boolean.class || cls == boolean.class) {
                if (text.equals("true") || text.equals("1")) return true;
                if (text.equals("false") || text.equals("0")) return false;
                throw new IllegalArgumentException("Invalid boolean for " + field + ": " + text);
            }
            if (cls == Character.class || cls == char.class) {
                if (text.length() == 1) return text.charAt(0);
                throw new IllegalArgumentException("Invalid character for " + field);
            }
            if (cls == Integer.class || cls == int.class || cls == Long.class || cls == long.class ||
                    cls == Short.class || cls == short.class || cls == Byte.class || cls == byte.class ||
                    cls == BigDecimal.class || cls == BigInteger.class ||
                    cls == Double.class || cls == double.class || cls == Float.class || cls == float.class) {
                try {
                    return convert(new BigDecimal(text), required, owner, field, registry, visited);
                } catch (NumberFormatException error) {
                    throw new IllegalArgumentException("Invalid numeric value for " + field + ": " + text, error);
                }
            }
        }
        if (cls.isEnum() && value instanceof String text) {
            if (!AIiEnumItem.class.isAssignableFrom(cls))
                throw new IllegalArgumentException("Externally serialized enum must implement AIiEnumItem: " + cls.getName());
            for (Object constant : cls.getEnumConstants()) {
                AIiEnumItem<?> item = (AIiEnumItem<?>) constant;
                if (text.equals(String.valueOf(item.code()))) return constant;
            }
            throw new IllegalArgumentException("Unrecognized enum code " + text + " for " + cls.getName());
        }
        if (value instanceof Number number) {
            BigDecimal decimal = new BigDecimal(number.toString());
            if (cls == Integer.class || cls == int.class) return decimal.intValueExact();
            if (cls == Long.class || cls == long.class) return decimal.longValueExact();
            if (cls == Short.class || cls == short.class) return decimal.shortValueExact();
            if (cls == Byte.class || cls == byte.class) return decimal.byteValueExact();
            if (cls == BigDecimal.class) return decimal;
            if (cls == BigInteger.class) return decimal.toBigIntegerExact();
            if (cls == Double.class || cls == double.class) return decimal.doubleValue();
            if (cls == Float.class || cls == float.class) return decimal.floatValue();
        }
        throw new IllegalArgumentException("Value of type " + value.getClass().getName()
                + " cannot be converted to " + cls.getName() + " at " + field);
    }
}

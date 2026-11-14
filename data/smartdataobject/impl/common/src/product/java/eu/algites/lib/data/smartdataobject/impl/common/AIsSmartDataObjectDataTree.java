package eu.algites.lib.data.smartdataobject.impl.common;

import eu.algites.lib.common.enums.AIiEnumItem;
import eu.algites.lib.data.smartdataobject.AIiSmartDataObject;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Schema-neutral conversion between typed SDO values and an ordered tree of
 * scalar, list and map values. Encoding and parsing are left to format codecs.
 *
 * @author linhart1
 */
public final class AIsSmartDataObjectDataTree {
    private AIsSmartDataObjectDataTree() { }

    /**
     * Constructs a canonical field-name tree for a registered SDO instance.
     * @param aRoot root object
     * @param aRegistry allowed implementation bindings
     * @return ordered tree with canonical data-field names
     * @throws IllegalArgumentException on unregistered implementations or cyclic data
     */
    public static Map<String, Object> toData(
            AIiSmartDataObject aRoot, AIiSmartDataObjectBindingRegistry aRegistry) {
        Objects.requireNonNull(aRoot, "aRoot");
        Objects.requireNonNull(aRegistry, "aRegistry");
        return convert(aRoot, aRegistry, new IdentityHashMap<>());
    }

    private static Map<String, Object> convert(AIiSmartDataObject aObject,
            AIiSmartDataObjectBindingRegistry registry, IdentityHashMap<Object, Boolean> visiting) {
        if (visiting.put(aObject, Boolean.TRUE) != null)
            throw new IllegalArgumentException("Cyclic SDO graphs require an explicit reference-aware format profile");
        try {
            if (!(aObject instanceof AIcSmartDataObject sdo))
                throw new IllegalArgumentException("Object does not use the new common SDO runtime: " + aObject.getClass());
            var binding = registry.getBinding(sdo.getClass());
            if (binding == null)
                throw new IllegalArgumentException("Unregistered SDO implementation: " + sdo.getClass().getName());
            Map<String, Object> result = new LinkedHashMap<>();
            Map<String, Object> raw = sdo.getRawValues();
            for (var field : binding.getMetadata().getFields()) {
                String name = field.getSemanticFieldName();
                if (!raw.containsKey(name)) continue;
                result.put(field.getDataFieldName(), scalarOrComposite(raw.get(name), registry, visiting));
            }
            return result;
        } finally {
            visiting.remove(aObject);
        }
    }

    private static Object scalarOrComposite(Object input,
            AIiSmartDataObjectBindingRegistry registry, IdentityHashMap<Object, Boolean> visiting) {
        if (input == null || input instanceof String || input instanceof Number || input instanceof Boolean) return input;
        if (input instanceof AIiSmartDataObject sdo) return convert(sdo, registry, visiting);
        if (input instanceof AIiEnumItem<?> enumItem) return enumItem.code();
        if (input instanceof Enum<?> enumValue)
            throw new IllegalArgumentException("Externally serialized enum must implement AIiEnumItem: "
                    + enumValue.getDeclaringClass().getName());
        if (input instanceof Character character) return character.toString();
        if (visiting.put(input, Boolean.TRUE) != null)
            throw new IllegalArgumentException("Cyclic collections require reference-aware format profile");
        try {
            if (input instanceof Map<?, ?> map) {
                Map<String, Object> result = new LinkedHashMap<>();
                for (var entry : map.entrySet()) {
                    if (!(entry.getKey() instanceof String key))
                        throw new IllegalArgumentException("Non-string map key cannot be represented by a text object format");
                    result.put(key, scalarOrComposite(entry.getValue(), registry, visiting));
                }
                return result;
            }
            if (input instanceof Iterable<?> iterable) {
                List<Object> result = new ArrayList<>();
                for (Object element : iterable) result.add(scalarOrComposite(element, registry, visiting));
                return result;
            }
            if (input.getClass().isArray()) {
                List<Object> result = new ArrayList<>();
                for (int i = 0; i < Array.getLength(input); i++)
                    result.add(scalarOrComposite(Array.get(input, i), registry, visiting));
                return result;
            }
            throw new IllegalArgumentException("Unsupported scalar type: " + input.getClass());
        } finally {
            visiting.remove(input);
        }
    }
}

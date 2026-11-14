package eu.algites.lib.data.smartdataobject.impl.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** Utility for provider-specific annotation readers, independent of format semantics.
 * @author linhart1
 */
public final class AIsSmartDataObjectFormatMetadataUtils {
    private AIsSmartDataObjectFormatMetadataUtils() { }

    /**
     * Returns a deterministic base-to-derived traversal including superinterfaces.
     * @param aType concrete SDO class
     * @return immutable traversal list
     */
    public static List<Class<?>> hierarchy(Class<?> aType) {
        List<Class<?>> types = new ArrayList<>();
        Set<Class<?>> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        collect(aType, types, visited);
        return List.copyOf(types);
    }
    private static void collect(Class<?> type, List<Class<?>> output, Set<Class<?>> visited) {
        if (type == null || type == Object.class || !visited.add(type)) return;
        collect(type.getSuperclass(), output, visited);
        for (Class<?> next : type.getInterfaces()) collect(next, output, visited);
        output.add(type);
    }
}

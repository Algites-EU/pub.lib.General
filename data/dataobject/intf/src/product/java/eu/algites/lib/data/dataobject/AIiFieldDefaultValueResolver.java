package eu.algites.lib.data.dataobject;

import java.lang.reflect.Method;

/** Computes the effective default for a field without changing its explicit presence. */
public interface AIiFieldDefaultValueResolver {
    /**
     * Resolves a default for an absent getter. Implementations may return null as a default.
     * Repeated evaluation must obey the resolver's documented ownership of mutable values.
     *
     * @param aOwnerClass runtime data-object contract or concrete owning class
     * @param aGetter the annotated zero-argument field getter
     * @return effective default, including null when explicitly intended
     */
    Object resolveDefault(Class<?> aOwnerClass, Method aGetter);
}

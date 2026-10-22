package eu.algites.lib.data.dataobject;

/**
 * Controls whether a generated mutable setter's value type remains open to
 * compile-time specialization in independently generated descendant contracts.
 *
 * <p>The policy concerns the setter's parameter <em>type</em>, not whether Java
 * inherits the setter method itself. {@link #INHERIT_AS_GENERIC_PARAMETER}
 * requires an interface-level type variable shared by descendant writable
 * interfaces. {@link #INHERIT_AS_FIXED_PARAMETER} retains the parent's fixed
 * parameter type and prohibits incompatible narrowing of the corresponding
 * getter when writable inheritance is preserved.
 *
 * <p>Configuration sources may use {@link #DEFAULT} to delegate to a higher
 * level. A published generated read contract must carry a resolved policy so
 * that downstream artifacts can reproduce its writable inheritance semantics
 * without access to the producer's build configuration.
 */
public enum AInFieldSetterInheritancePolicy {
    /** Defer to the containing contract or the configured generation defaults. */
    DEFAULT,

    /** Preserve an interface-level type variable so descendants can narrow writable values. */
    INHERIT_AS_GENERIC_PARAMETER,

    /** Inherit the setter's fixed value type without permitting incompatible narrowing. */
    INHERIT_AS_FIXED_PARAMETER
}

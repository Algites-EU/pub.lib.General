package eu.algites.lib.common.list;

/** Defines how a constrained list handles changes to its current size. */
public enum AInBoundsMutationPolicy {
    /** Mutations may temporarily move the list to any representable size. */
    ALLOW_ANY,
    /** Mutations must not introduce or increase either bound violation. */
    PREVENT_WORSENING
}

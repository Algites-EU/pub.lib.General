package eu.algites.lib.action.interaction;

/**
 * Immutable presentation/diagnostic event emitted by an action.
 * Events are not authoritative transaction-state transitions; the executor or coordinator owns those.
 * Progress identifiers create a hierarchy without requiring a hierarchy of controller objects.
 */
public interface AIiActionInteractionEvent {

    /** Stable event kind, such as STATUS, PROGRESS, DETAIL or DIAGNOSTIC. */
    String getKind();

    /** Human-readable message suitable for display; it is not a machine event identifier. */
    String getMessage();

    /** Identity of a progress region, or null for events not bound to a region. */
    String getProgressId();

    /** Parent progress region identifier, or null for a top-level region. */
    String getParentProgressId();

    /** Current progress units, or null when indeterminate. */
    Long getCurrent();

    /** Total progress units, or null when indeterminate. */
    Long getTotal();

    /** Unit name such as bytes or objects; null where no unit is available. */
    String getUnit();
}

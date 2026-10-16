package eu.algites.lib.data.smartdataobject;

/**
 * A typed node in the in-memory virtual representation of a SmartDataObject graph.
 *
 * <p>Every SDO field is a virtual field value, including null and scalar fields.
 * Implementations retain the original runtime value class independently of the
 * declared field interface. For an SDO value the representation is a
 * {@code Map<String, AIidVirtualFieldValue>}. Lists and maps contain nested
 * virtual values. A single node may be referenced repeatedly, including in cycles.
 * This is an in-process representation, not a JSON/YAML/XML wire format.
 */
public interface AIidVirtualFieldValue {
    /** Return the virtual value graph, never confusing the wrapper with its value. */
    Object getValueRepresentation();
    /** Return the concrete original runtime class, or Void.class for explicit null. */
    Class<?> getValueClass();
}

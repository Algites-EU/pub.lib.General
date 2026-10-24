package eu.algites.lib.data.smartdataobject;

/**
 * Selects how a SmartDataObject exposes field values for snapshots, validation, and marshalling.
 * RAW_VALUES preserves only fields explicitly assigned by the caller, including explicit nulls.
 * EFFECTIVE_VALUES additionally reads applicable defaults without changing raw field presence.
 */
public enum AInSdoFieldValueLoadingMode {
    /** Include only explicitly present raw fields and never materialize defaults. */
    RAW_VALUES,
    /** Read field values after applying defaults where raw values are absent. */
    EFFECTIVE_VALUES
}

package eu.algites.lib.data.dataobject;

/**
 * Determines when a declared default applies to a SmartDataObject property.
 * Raw presence and raw serialization never depend on default evaluation.
 *
 * @author linhart1
 */
public enum AInDataObjectFieldDefaultKind {
    /** Metadata-only declaration; no runtime substitution or assignment restriction. */
    DECLARATION_ONLY,
    /** Supply the declared value only when the raw property is absent. */
    IF_FIELD_VALUE_NOT_PRESENT,
    /** Supply the declared value for an explicit empty sentinel; absence stays absent. */
    IF_FIELD_VALUE_EMPTY,
    /** XSD fixed: reject assignments not equal to the declared value; absent raw remains absent. */
    CONSTRAINT_DEFAULT
}

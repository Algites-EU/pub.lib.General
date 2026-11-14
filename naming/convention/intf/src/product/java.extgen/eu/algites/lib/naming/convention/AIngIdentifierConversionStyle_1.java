package eu.algites.lib.naming.convention;

/**
 * Optional lexical splitting and formatting policy. Custom regex conventions without a conversion style are validation-only. The style, not the naming convention ID, controls conversion.
 *
 * <p>Canonical identity: eu.algites.lib.naming.convention.common-naming-conventions#/$defs/IdentifierConversionStyle; version: 1.</p>
 * <p>Equivalent schema identity: {https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd}IdentifierConversionStyle; version: 1.</p>
 */
public enum AIngIdentifierConversionStyle_1 {
    /** Exact wire value lower_flat. */
    LOWER_FLAT("lower_flat"),
    /** Exact wire value upper_flat. */
    UPPER_FLAT("upper_flat"),
    /** Exact wire value lower_snake. */
    LOWER_SNAKE("lower_snake"),
    /** Exact wire value upper_snake. */
    UPPER_SNAKE("upper_snake"),
    /** Exact wire value lower_camel. */
    LOWER_CAMEL("lower_camel"),
    /** Exact wire value upper_camel. */
    UPPER_CAMEL("upper_camel"),
    /** Exact wire value lower_kebab. */
    LOWER_KEBAB("lower_kebab"),
    /** Exact wire value upper_kebab. */
    UPPER_KEBAB("upper_kebab");

    private final String wireCode;

    /** @param aWireCode original typed schema code */
    AIngIdentifierConversionStyle_1(String aWireCode) { wireCode = aWireCode; }

    /** @return the exact typed schema wire value */
    public String code() { return wireCode; }
}

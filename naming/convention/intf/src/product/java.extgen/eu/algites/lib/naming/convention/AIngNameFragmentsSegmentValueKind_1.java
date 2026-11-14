package eu.algites.lib.naming.convention;

/**
 * Determines how the segment value is interpreted during name construction.
 *
 * <p>Canonical identity: eu.algites.lib.naming.convention.common-naming-conventions#/$defs/NameFragmentsSegmentValueKind; version: 1.</p>
 * <p>Equivalent schema identity: {https://defs.dev.algites.eu/api/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd}NameFragmentsSegmentValueKind; version: 1.</p>
 */
public enum AIngNameFragmentsSegmentValueKind_1 {
    /** Exact wire value constant_string. */
    CONSTANT_STRING("constant_string"),
    /** Exact wire value identifier. */
    IDENTIFIER("identifier"),
    /** Exact wire value version. */
    VERSION("version"),
    /** Exact wire value variable. */
    VARIABLE("variable");

    private final String wireCode;

    /** @param aWireCode original typed schema code */
    AIngNameFragmentsSegmentValueKind_1(String aWireCode) { wireCode = aWireCode; }

    /** @return the exact typed schema wire value */
    public String code() { return wireCode; }
}

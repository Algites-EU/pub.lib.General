package eu.algites.lib.naming.convention;

/**
 * Contract for NameFragmentsSegmentValueContentVariantCustomConventionRegexp3.
 *
 * <p>Canonical identity: naming/convention/intf/src/product/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd/schema/NameFragmentsSegmentValueContent/choice/choice/2#synthetic:synthetic_choice_variant_type; version: 1.</p>
 * @param field1 Schema field CustomConventionRegexp
 */
public record AIigNameFragmentsSegmentValueContentVariantCustomConventionRegexp3_1(String field1) implements AIigNameFragmentsSegmentValueContent_1 {
    /**
     * Return the possible first XML QNames of this variant.
     * @return immutable set of expanded names without synthetic wrappers
     */
    public static java.util.Set<String> xmlFirstQNames() {
        return java.util.Set.of("CustomConventionRegexp");
    }

    /** Validate required fields on construction. */
    public AIigNameFragmentsSegmentValueContentVariantCustomConventionRegexp3_1 {
        java.util.Objects.requireNonNull(field1, "CustomConventionRegexp");
    }

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "CustomConventionRegexp", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "CustomConventionRegexp", order = 0)
    /** @return Value of CustomConventionRegexp (required) */
    public String getCustomConventionRegexp() { return field1; }
}

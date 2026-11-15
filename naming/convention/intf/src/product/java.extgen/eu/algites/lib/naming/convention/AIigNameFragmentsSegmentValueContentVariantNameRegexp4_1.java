package eu.algites.lib.naming.convention;

/**
 * Contract for NameFragmentsSegmentValueContentVariantNameRegexp4.
 *
 * <p>Canonical identity: naming/convention/intf/src/product/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd/schema/NameFragmentsSegmentValueContent/choice/choice/3#synthetic:synthetic_choice_variant_type; version: 1.</p>
 * @param field1 Schema field NameRegexp
 */
public record AIigNameFragmentsSegmentValueContentVariantNameRegexp4_1(AIigNameRegexp_1 field1) implements AIigNameFragmentsSegmentValueContent_1 {
    /**
     * Return the possible first XML QNames of this variant.
     * @return immutable set of expanded names without synthetic wrappers
     */
    public static java.util.Set<String> xmlFirstQNames() {
        return java.util.Set.of("NameRegexp");
    }

    /** Validate required fields on construction. */
    public AIigNameFragmentsSegmentValueContentVariantNameRegexp4_1 {
        java.util.Objects.requireNonNull(field1, "NameRegexp");
    }

    @eu.algites.lib.data.dataobject.xmldefs.AIaDataObjectFieldXmlSchema(namespace = "", namespaceLocalName = "NameRegexp", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "NameRegexp", order = 0)
    /** @return Value of NameRegexp (required) */
    public AIigNameRegexp_1 getNameRegexp() { return field1; }
}

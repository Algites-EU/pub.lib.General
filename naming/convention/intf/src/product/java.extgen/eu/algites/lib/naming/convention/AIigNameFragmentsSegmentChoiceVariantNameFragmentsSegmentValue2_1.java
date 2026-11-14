package eu.algites.lib.naming.convention;

/**
 * Contract for NameFragmentsSegmentChoiceVariantNameFragmentsSegmentValue2.
 *
 * <p>Canonical identity: naming/convention/intf/src/product/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd/schema/NameFragmentsSegmentChoice/choice/choice/1#synthetic:synthetic_choice_variant_type; version: 1.</p>
 * @param field1 Schema field NameFragmentsSegmentValue
 */
public record AIigNameFragmentsSegmentChoiceVariantNameFragmentsSegmentValue2_1(AIigNameFragmentsSegmentValue_1 field1) implements AIigNameFragmentsSegmentChoice_1 {
    /**
     * Return the possible first XML QNames of this variant.
     * @return immutable set of expanded names without synthetic wrappers
     */
    public static java.util.Set<String> xmlFirstQNames() {
        return java.util.Set.of("NameFragmentsSegmentValue");
    }

    /** Validate required fields on construction. */
    public AIigNameFragmentsSegmentChoiceVariantNameFragmentsSegmentValue2_1 {
        java.util.Objects.requireNonNull(field1, "NameFragmentsSegmentValue");
    }

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "NameFragmentsSegmentValue", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "NameFragmentsSegmentValue", order = 0)
    /** @return Value of NameFragmentsSegmentValue (required) */
    public AIigNameFragmentsSegmentValue_1 getNameFragmentsSegmentValue() { return field1; }
}

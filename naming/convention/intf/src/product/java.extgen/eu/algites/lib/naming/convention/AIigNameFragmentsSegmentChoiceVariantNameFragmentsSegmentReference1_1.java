package eu.algites.lib.naming.convention;

/**
 * Contract for NameFragmentsSegmentChoiceVariantNameFragmentsSegmentReference1.
 *
 * <p>Canonical identity: naming/convention/intf/src/product/xmldefs/eu/algites/lib/naming/convention/common-naming-conventions_1.xsd/schema/NameFragmentsSegmentChoice/choice/choice/0#synthetic:synthetic_choice_variant_type; version: 1.</p>
 * @param field1 Schema field NameFragmentsSegmentReference
 */
public record AIigNameFragmentsSegmentChoiceVariantNameFragmentsSegmentReference1_1(AIigNameFragmentsSegmentReference_1 field1) implements AIigNameFragmentsSegmentChoice_1 {
    /**
     * Return the possible first XML QNames of this variant.
     * @return immutable set of expanded names without synthetic wrappers
     */
    public static java.util.Set<String> xmlFirstQNames() {
        return java.util.Set.of("NameFragmentsSegmentReference");
    }

    /** Validate required fields on construction. */
    public AIigNameFragmentsSegmentChoiceVariantNameFragmentsSegmentReference1_1 {
        java.util.Objects.requireNonNull(field1, "NameFragmentsSegmentReference");
    }

    @eu.algites.lib.data.dataobject.xmldefs.AIaXmlDataObjectField(namespace = "", namespaceLocalName = "NameFragmentsSegmentReference", valueKind = eu.algites.lib.data.dataobject.xmldefs.AInXmlValueKind.ELEMENT)
    @eu.algites.lib.data.dataobject.AIaDataObjectField(dataFieldName = "NameFragmentsSegmentReference", order = 0)
    /** @return Value of NameFragmentsSegmentReference (required) */
    public AIigNameFragmentsSegmentReference_1 getNameFragmentsSegmentReference() { return field1; }
}
